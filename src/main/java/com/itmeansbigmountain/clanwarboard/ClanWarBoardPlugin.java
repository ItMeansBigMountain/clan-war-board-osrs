package com.itmeansbigmountain.clanwarboard;

import com.google.inject.Provides;
import java.awt.image.BufferedImage;
import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.swing.SwingUtilities;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ActorDeath;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.api.clan.ClanChannel;
import net.runelite.api.clan.ClanChannelMember;
import net.runelite.api.clan.ClanMember;
import net.runelite.api.clan.ClanSettings;
import net.runelite.api.clan.ClanTitle;
import net.runelite.api.events.ClanChannelChanged;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ColorUtil;

@Slf4j
@PluginDescriptor(
	name = ClanWarBoardPlugin.PLUGIN_NAME,
	description = "Lets clan leaders set up wilderness fights while members see the current war board.",
	tags = {"clan", "war", "pvp", "wilderness"}
)
public class ClanWarBoardPlugin extends Plugin
{
	static final String PLUGIN_NAME = "Clan War Board";
	static final String PLUGIN_VERSION = "1.0.0";
	private static final String INSTALL_ID_KEY = "installationId";

	@Inject
	private Client client;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private ClanWarBoardConfig config;

	@Inject
	private ConfigManager configManager;

	@Inject
	private ClanWarBoardApiClient apiClient;

	@Inject
	private ScheduledExecutorService executorService;

	@Inject
	private ClientThread clientThread;

	private ClanWarBoardPanel panel;
	private NavigationButton navButton;
	private final ClanWarBoardTelemetryBuffer telemetryBuffer = new ClanWarBoardTelemetryBuffer();
	private final CombatSignalTracker combatSignals = new CombatSignalTracker();
	private final AtomicBoolean sessionRefreshInFlight = new AtomicBoolean();
	private final AtomicBoolean boardRefreshInFlight = new AtomicBoolean();
	private volatile ClanWarBoardSession session;
	private volatile boolean running;
	private ClanWarBoardState boardState = ClanWarBoardState.offline("Online sync has not refreshed yet");
	private volatile boolean loginMessagePending;
	private String lastClanFingerprint;

	@Override
	protected void startUp()
	{
		running = true;
		panel = new ClanWarBoardPanel(new ClanWarBoardPanel.MatchActionHandler()
		{
			@Override
			public void reloadAll()
			{
				clientThread.invoke(ClanWarBoardPlugin.this::refreshOnlineBoard);
			}

			@Override
			public void submitAvailability(String startsAt, String duration, String combatMin, String combatMax, String notes)
			{
				ClanWarBoardPlugin.this.submitAvailability(startsAt, duration, combatMin, combatMax, notes);
			}

			@Override
			public void submitChallenge(String opponent, String startsAt, String duration, String combatMin, String combatMax, String world, String location, String rules)
			{
				ClanWarBoardPlugin.this.submitChallenge(opponent, startsAt, duration, combatMin, combatMax, world, location, rules);
			}
		});
		navButton = NavigationButton.builder()
			.tooltip(PLUGIN_NAME)
			.icon(loadIcon())
			.priority(5)
			.panel(panel)
			.build();
		clientToolbar.addNavigation(navButton);
		refreshPanel();
		refreshOnlineBoard();
		log.debug("{} started", PLUGIN_NAME);
	}

	@Override
	protected void shutDown()
	{
		running = false;
		session = null;
		combatSignals.reset();
		clientToolbar.removeNavigation(navButton);
		navButton = null;
		panel = null;
		log.debug("{} stopped", PLUGIN_NAME);
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState() == GameState.LOGGED_IN)
		{
			refreshPanel();
			loginMessagePending = config.showLoginMessage();
			refreshOnlineBoard();
		}
		else if (gameStateChanged.getGameState() == GameState.LOGIN_SCREEN
			|| gameStateChanged.getGameState() == GameState.HOPPING)
		{
			combatSignals.reset();
		}
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (ClanWarBoardConfig.CONFIG_GROUP.equals(event.getGroup()))
		{
			refreshPanel();
		}
	}

	@Subscribe
	public void onClanChannelChanged(ClanChannelChanged event)
	{
		if (!event.isGuest())
		{
			refreshClanSnapshotIfChanged();
		}
	}

	@Subscribe
	public void onGameTick(GameTick tick)
	{
		rotateSessionIfNeeded();
		int currentTick = client.getTickCount();
		if (currentTick % 5 == 0)
		{
			refreshClanSnapshotIfChanged();
		}
		ClanAccess access = clanAccess();
		if (telemetryBuffer.shouldHeartbeat(currentTick))
		{
			queueTelemetry("heartbeat", null, 0, access, "periodic_client_presence", "high", "none");
		}
		flushTelemetryIfReady();
	}

	@Subscribe
	public void onHitsplatApplied(HitsplatApplied event)
	{
		if (!(event.getActor() instanceof Player) || event.getHitsplat() == null)
		{
			return;
		}
		int amount = event.getHitsplat().getAmount();
		if (amount <= 0)
		{
			return;
		}
		Player local = client.getLocalPlayer();
		Player target = (Player) event.getActor();
		if (local == null || target.getName() == null)
		{
			return;
		}
		ClanAccess access = clanAccess();
		if (target == local)
		{
			Player attacker = soleInteractingAttacker(local);
			String attackerName = attacker == null ? null : attacker.getName();
			emitCombatReturn(access, attackerName);
			queueTelemetry("damage_taken", attackerName, amount, access,
				attacker == null ? "local_hitsplat_attacker_unresolved" : "local_hitsplat_single_interacting_attacker",
				attacker == null ? "high_amount_low_source" : "medium", relationFor(attackerName));
		}
		else if (event.getHitsplat().isMine())
		{
			emitCombatReturn(access, target.getName());
			combatSignals.recordOutgoingDamage(target.getName(), client.getTickCount());
			boolean ownClan = isOwnClanMember(target.getName());
			queueTelemetry(ownClan ? "friendly_fire_damage" : "damage_dealt", target.getName(), amount, access,
				"local_player_hitsplat", "high", ownClan ? "own_clan" : "non_own_clan");
		}
	}

	@Subscribe
	public void onActorDeath(ActorDeath event)
	{
		if (!(event.getActor() instanceof Player))
		{
			return;
		}
		Player local = client.getLocalPlayer();
		Player dead = (Player) event.getActor();
		ClanAccess access = clanAccess();
		if (local != null && dead == local)
		{
			combatSignals.recordLocalDeath();
			queueTelemetry("death", null, 1, access, "local_actor_death", "high", "self");
			flushTelemetryNow();
		}
		else if (dead.getName() != null && combatSignals.consumeObservedKill(dead.getName(), client.getTickCount()))
		{
			queueTelemetry("kill_candidate", dead.getName(), 1, access,
				"target_death_with_recent_local_damage", "medium", relationFor(dead.getName()));
			flushTelemetryNow();
		}
	}

	private void refreshClanSnapshotIfChanged()
	{
		ClanAccess access = clanAccess();
		int members = clanMemberCount();
		String fingerprint = String.valueOf(access.getClanName()) + "|" + access.getRankValue() + "|" + members;
		if (fingerprint.equals(lastClanFingerprint))
		{
			return;
		}
		lastClanFingerprint = fingerprint;
		refreshPanel();
		refreshOnlineBoard();
	}

	private void refreshPanel()
	{
		if (panel == null)
		{
			return;
		}
		ClanAccess access = clanAccess();
		boolean leaderView = resolveLeaderView(access, config.minimumLeaderRank(), session);
		ClanWarBoardState currentState = boardState.withClanMembers(clanMemberCount());
		SwingUtilities.invokeLater(() ->
		{
			if (running && panel != null)
			{
				panel.update(access.getClanName(), access.getPlayerName(), access.getRankName(), leaderView, currentState);
			}
		});
	}

	private void refreshOnlineBoard()
	{
		if (!boardRefreshInFlight.compareAndSet(false, true))
		{
			return;
		}
		setPanelReloading(true);
		ClanAccess registrationAccess = clanAccess();
		int currentClanMemberCount = clanMemberCount();
		String installationId = installationId();
		executorService.submit(() ->
		{
			ClanWarBoardState completedState;
			try
			{
				if (registrationAccess.getClanName() != null && !registrationAccess.getClanName().trim().isEmpty())
				{
					session = apiClient.register(installationId, registrationAccess, PLUGIN_VERSION, config.publicPlayerTracking());
				}
				completedState = apiClient.fetchBoardState(registrationAccess.getClanName(), currentClanMemberCount, session);
			}
			catch (IOException ex)
			{
				log.debug("Clan War Board API refresh failed", ex);
				completedState = ClanWarBoardState.offline(ex.getMessage());
			}
			ClanWarBoardState refreshedState = completedState;
			clientThread.invoke(() ->
			{
				try
				{
					if (running)
					{
						boardState = refreshedState;
						if (loginMessagePending)
						{
							loginMessagePending = false;
							client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", ColorUtil.wrapWithColorTag(buildLoginMessage(boardState), Color.CYAN), null);
						}
						refreshPanel();
					}
				}
				finally
				{
					boardRefreshInFlight.set(false);
					setPanelReloading(false);
				}
			});
		});
	}

	private void setPanelReloading(boolean reloading)
	{
		SwingUtilities.invokeLater(() ->
		{
			if (running && panel != null)
			{
				panel.setReloading(reloading);
			}
		});
	}

	private void rotateSessionIfNeeded()
	{
		ClanWarBoardSession current = session;
		if (current == null || !current.shouldRotate(Instant.now()) || !sessionRefreshInFlight.compareAndSet(false, true))
		{
			return;
		}
		executorService.submit(() ->
		{
			try
			{
				session = apiClient.rotateSession(current);
			}
			catch (IOException ex)
			{
				log.debug("Clan War Board session rotation failed", ex);
				clientThread.invoke(this::refreshOnlineBoard);
			}
			finally
			{
				sessionRefreshInFlight.set(false);
			}
		});
	}

	private void submitAvailability(String startsAt, String duration, String combatMin, String combatMax, String notes)
	{
		ClanWarBoardSession current = session;
		if (current == null || !current.hasCapability("leader:write"))
		{
			showActionMessage("Leader authorization is not available.", Color.RED);
			return;
		}
		executorService.submit(() ->
		{
			try
			{
				apiClient.postAvailability(current, ClanWarBoardApiClient.availabilityJson(startsAt, duration, combatMin, combatMax, notes));
				showActionMessage("War post published to the board.", Color.GREEN);
				clientThread.invoke(this::refreshOnlineBoard);
			}
			catch (IOException ex)
			{
				showActionMessage("War post failed: " + ex.getMessage(), Color.RED);
			}
		});
	}

	private void submitChallenge(String opponent, String startsAt, String duration, String combatMin, String combatMax, String world, String location, String rules)
	{
		ClanWarBoardSession current = session;
		if (current == null || !current.hasCapability("leader:write"))
		{
			showActionMessage("Leader authorization is not available.", Color.RED);
			return;
		}
		executorService.submit(() ->
		{
			try
			{
				apiClient.postChallenge(current, ClanWarBoardApiClient.challengeJson(opponent, startsAt, duration, combatMin, combatMax, world, location, rules));
				showActionMessage("Private challenge sent.", Color.GREEN);
				clientThread.invoke(this::refreshOnlineBoard);
			}
			catch (IOException ex)
			{
				showActionMessage("Private challenge failed: " + ex.getMessage(), Color.RED);
			}
		});
	}

	private void showActionMessage(String message, Color color)
	{
		clientThread.invoke(() ->
		{
			if (running)
			{
				client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", ColorUtil.wrapWithColorTag(PLUGIN_NAME + ": " + message, color), null);
			}
		});
	}

	private void emitCombatReturn(ClanAccess access, String opponentName)
	{
		if (combatSignals.consumeCombatReturn())
		{
			queueTelemetry("return", opponentName, 1, access,
				"first_combat_event_after_local_death", "high", relationFor(opponentName));
		}
	}

	private Player soleInteractingAttacker(Player local)
	{
		Player candidate = null;
		WorldPoint localPoint = local.getWorldLocation();
		for (Player player : client.getPlayers())
		{
			if (player == null || player == local || player.getInteracting() != local || player.getName() == null)
			{
				continue;
			}
			if (localPoint != null && player.getWorldLocation() != null && player.getWorldLocation().distanceTo(localPoint) > 15)
			{
				continue;
			}
			if (candidate != null)
			{
				return null;
			}
			candidate = player;
		}
		return candidate;
	}

	private String relationFor(String playerName)
	{
		if (playerName == null || playerName.trim().isEmpty())
		{
			return "unattributed";
		}
		return isOwnClanMember(playerName) ? "own_clan" : "non_own_clan";
	}

	private void queueTelemetry(String type, String opponentName, int amount, ClanAccess access,
		String evidence, String confidence, String relation)
	{
		Player local = client.getLocalPlayer();
		WorldPoint location = local == null ? null : local.getWorldLocation();
		telemetryBuffer.add(new ClanWarBoardTelemetryEvent(
			type,
			access.getPlayerName(),
			access.getClanName(),
			opponentName,
			amount,
			client.getWorld(),
			client.getTickCount(),
			System.currentTimeMillis(),
			config.publicPlayerTracking(),
			evidence,
			confidence,
			relation,
			location == null ? 0 : location.getRegionID(),
			location == null ? 0 : location.getX(),
			location == null ? 0 : location.getY(),
			location == null ? 0 : location.getPlane()
		));
	}

	private void flushTelemetryIfReady()
	{
		long now = System.currentTimeMillis();
		if (telemetryBuffer.shouldFlush(now))
		{
			flushTelemetry(now);
		}
	}

	private void flushTelemetryNow()
	{
		flushTelemetry(System.currentTimeMillis());
	}

	private void flushTelemetry(long now)
	{
		ClanWarBoardSession current = session;
		if (current == null)
		{
			return;
		}
		List<ClanWarBoardTelemetryEvent> batch = telemetryBuffer.drain(now);
		if (batch.isEmpty())
		{
			return;
		}
		executorService.submit(() ->
		{
			try
			{
				apiClient.submitTelemetry(current, batch);
			}
			catch (IOException ex)
			{
				telemetryBuffer.requeue(batch);
				log.debug("Clan War Board telemetry upload failed; batch requeued", ex);
			}
		});
	}

	private ClanAccess clanAccess()
	{
		String playerName = localPlayerName();
		ClanSettings settings = client.getClanSettings();
		if (settings != null)
		{
			ClanMember member = playerName == null ? null : settings.findMember(playerName);
			if (member == null)
			{
				return ClanAccess.noRank(playerName, settings.getName());
			}
			int rankValue = member.getRank() == null ? -1 : member.getRank().getRank();
			ClanTitle title = member.getRank() == null ? null : settings.titleForRank(member.getRank());
			return new ClanAccess(playerName, settings.getName(), rankValue, title == null ? null : title.getName());
		}

		ClanChannel clan = client.getClanChannel();
		if (clan == null)
		{
			return ClanAccess.noClan(playerName);
		}
		ClanChannelMember member = playerName == null ? null : clan.findMember(playerName);
		if (member == null)
		{
			return ClanAccess.noRank(playerName, clan.getName());
		}
		int rankValue = member.getRank() == null ? -1 : member.getRank().getRank();
		return new ClanAccess(playerName, clan.getName(), rankValue);
	}

	private boolean isOwnClanMember(String playerName)
	{
		ClanSettings settings = client.getClanSettings();
		return settings != null && playerName != null && settings.findMember(playerName) != null;
	}

	private int clanMemberCount()
	{
		ClanSettings settings = client.getClanSettings();
		return settings == null || settings.getMembers() == null ? 0 : settings.getMembers().size();
	}

	private String installationId()
	{
		String value = configManager.getConfiguration(ClanWarBoardConfig.CONFIG_GROUP, INSTALL_ID_KEY);
		if (value == null || value.trim().isEmpty())
		{
			value = UUID.randomUUID().toString();
			configManager.setConfiguration(ClanWarBoardConfig.CONFIG_GROUP, INSTALL_ID_KEY, value);
		}
		return value;
	}

	private String localPlayerName()
	{
		Player local = client.getLocalPlayer();
		return local == null ? null : local.getName();
	}

	static boolean resolveLeaderView(ClanAccess access, LeaderMinimumRank minimumRank, ClanWarBoardSession session)
	{
		return access.canManageWars(minimumRank) && session != null && session.hasCapability("leader:write");
	}

	static String buildLoginMessage(ClanWarBoardState state)
	{
		int open = state == null ? 0 : state.getAvailableCount();
		StringBuilder message = new StringBuilder(PLUGIN_NAME).append(": ").append(open).append(open == 1 ? " fight needs an opponent" : " fights need an opponent");
		WarBoardFight next = state == null ? null : state.getNextScheduled();
		if (next != null)
		{
			message.append(". Next: ").append(next.getClanId()).append(" vs ").append(next.getOpponentClanId()).append(" at ").append(next.getStartsAt());
		}
		else
		{
			message.append(". No future war is scheduled");
		}
		return message.toString();
	}

	private static BufferedImage loadIcon()
	{
		try (InputStream stream = ClanWarBoardPlugin.class.getResourceAsStream("/icon.png"))
		{
			if (stream != null)
			{
				return ImageIO.read(stream);
			}
		}
		catch (IOException ignored)
		{
			// Use RuneLite's blank fallback if icon decoding fails.
		}
		return new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
	}

	@Provides
	ClanWarBoardConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ClanWarBoardConfig.class);
	}
}
