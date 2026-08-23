package com.itmeansbigmountain.clanwarboard;

import com.google.inject.Provides;
import java.awt.image.BufferedImage;
import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.swing.SwingUtilities;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@PluginDescriptor(
	name = ClanWarBoardPlugin.PLUGIN_NAME,
	description = "Sets up CWA and Wilderness clan fights with rankings and post-fight analysis.",
	tags = {"clan", "war", "pvp", "cwa", "wilderness"}
)
public class ClanWarBoardPlugin extends Plugin
{
	private static final Logger log = LoggerFactory.getLogger(ClanWarBoardPlugin.class);
	static final String PLUGIN_NAME = "Clan War Board";
	static final String PLUGIN_VERSION = "1.0.0";
	static final long AUTO_REFRESH_SECONDS = 60L;
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
	private ScheduledFuture<?> autoRefreshTask;

	private final AtomicBoolean sessionRefreshInFlight = new AtomicBoolean();
	private final AtomicBoolean boardRefreshInFlight = new AtomicBoolean();
	private final AtomicBoolean matchActionInFlight = new AtomicBoolean();
	private volatile ClanWarBoardSession session;
	private volatile String sessionContext = "|";
	private volatile long sessionGeneration;
	private volatile String activeContext = "|";
	private volatile long identityGeneration;
	private volatile boolean running;

	private volatile ClanWarBoardState boardState = ClanWarBoardState.offline("Online sync has not refreshed yet");
	private volatile String boardStateContext = "|";
	private volatile long boardStateGeneration;
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

			@Override
			public void submitAvailability(FightMode mode, String startsAt, String duration, String combatMin, String combatMax, String notes)
			{
				ClanWarBoardPlugin.this.submitAvailability(mode, startsAt, duration, combatMin, combatMax, notes);
			}

			@Override
			public void submitChallenge(FightMode mode, String opponent, String startsAt, String duration, String combatMin, String combatMax, String world, String location, String rules)
			{
				ClanWarBoardPlugin.this.submitChallenge(mode, opponent, startsAt, duration, combatMin, combatMax, world, location, rules);
			}

			@Override
			public void submitChallengeAction(String challengeId, String action, String reason)
			{
				ClanWarBoardPlugin.this.submitChallengeAction(challengeId, action, reason);
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
		autoRefreshTask = executorService.scheduleWithFixedDelay(
			() -> clientThread.invoke(this::refreshOnlineBoard),
			AUTO_REFRESH_SECONDS, AUTO_REFRESH_SECONDS, TimeUnit.SECONDS);
		log.debug("{} started", PLUGIN_NAME);
	}

	@Override
	protected void shutDown()
	{
		running = false;
		if (autoRefreshTask != null)
		{
			autoRefreshTask.cancel(false);
			autoRefreshTask = null;
		}
		session = null;
		sessionContext = "|";
		sessionGeneration = 0L;
		activeContext = "|";
		identityGeneration++;
		boardStateContext = "|";
		boardStateGeneration = 0L;

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
			bindIdentity("|");
			refreshPanel();
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
		ClanAccess access = clanAccess();
		bindIdentity(refreshContext(access));
		rotateSessionIfNeeded();
		if (client.getTickCount() % 5 == 0)
		{
			refreshClanSnapshotIfChanged();
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
		bindIdentity(refreshContext(access));
		boolean leaderView = resolveLeaderView(access, config.minimumLeaderRank(),
			canUseSession(session, sessionContext, sessionGeneration, activeContext, identityGeneration, "leader:write") ? session : null);
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
		String refreshContext = refreshContext(registrationAccess);
		bindIdentity(refreshContext);
		long refreshGeneration = identityGeneration;
		ClanWarBoardState previousState = boardState;
		String previousStateContext = boardStateContext;
		long previousStateGeneration = boardStateGeneration;
		int currentClanMemberCount = clanMemberCount();
		String installationId = installationId();
		submitAsync(executorService, () ->
		{
			ClanWarBoardState completedState;
			ClanWarBoardSession refreshedSession = null;
			try
			{
				if (registrationAccess.getClanName() != null && !registrationAccess.getClanName().trim().isEmpty())
				{
					refreshedSession = apiClient.register(installationId, registrationAccess, PLUGIN_VERSION);
				}
				completedState = apiClient.fetchBoardState(registrationAccess.getClanName(), currentClanMemberCount, refreshedSession);
			}
			catch (IOException ex)
			{
				log.debug("Clan War Board API refresh failed", ex);
				completedState = failureState(previousState, previousStateContext, previousStateGeneration,
					refreshContext, refreshGeneration, ex.getMessage());

			}
			ClanWarBoardState refreshedState = completedState;
			ClanWarBoardSession completedSession = refreshedSession;

			clientThread.invoke(() ->
			{
				boolean contextCurrent = isIdentityCurrent(refreshContext, refreshGeneration,
					refreshContext(clanAccess()), identityGeneration);
				try
				{
					if (running && contextCurrent)
					{
						session = completedSession;
						sessionContext = refreshContext;
						sessionGeneration = refreshGeneration;
						boardState = refreshedState;
						boardStateContext = refreshContext;
						boardStateGeneration = refreshGeneration;

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
					if (running && !contextCurrent)
					{
						refreshOnlineBoard();
					}
				}
			});
		}, () ->
		{
			boardRefreshInFlight.set(false);
			setPanelReloading(false);
		});
	}

	private static String refreshContext(ClanAccess access)
	{
		return access == null ? "|" : String.valueOf(access.getPlayerName()) + "|" + String.valueOf(access.getClanName());
	}

	private void bindIdentity(String context)
	{
		String normalized = context == null ? "|" : context;
		if (normalized.equals(activeContext))
		{
			return;
		}
		activeContext = normalized;
		identityGeneration++;
		session = null;
		sessionContext = "|";
		sessionGeneration = 0L;

		boardState = ClanWarBoardState.offline("Waiting for this clan's service refresh");
		boardStateContext = normalized;
		boardStateGeneration = identityGeneration;
	}

	static boolean isRefreshContextCurrent(String expected, String current)
	{
		return expected != null && expected.equals(current);
	}

	static boolean isIdentityCurrent(String expectedContext, long expectedGeneration,
		String currentContext, long currentGeneration)
	{
		return isRefreshContextCurrent(expectedContext, currentContext) && expectedGeneration == currentGeneration;
	}

	static ClanWarBoardState failureState(ClanWarBoardState previousState, String previousContext,
		long previousGeneration, String refreshContext, long refreshGeneration, String message)
	{
		return previousState != null && isIdentityCurrent(previousContext, previousGeneration, refreshContext, refreshGeneration)
			? previousState.withOfflineStatus(message)
			: ClanWarBoardState.offline(message);
	}

	static boolean canUseSession(ClanWarBoardSession candidate, String candidateContext, long candidateGeneration,
		String currentContext, long currentGeneration, String capability)
	{
		return candidate != null && isIdentityCurrent(candidateContext, candidateGeneration, currentContext, currentGeneration)
			&& candidate.hasCapability(capability);
	}

	static boolean tryBeginAction(AtomicBoolean inFlight)
	{
		return inFlight != null && inFlight.compareAndSet(false, true);
	}

	static boolean submitAsync(ScheduledExecutorService executor, Runnable task, Runnable rejectionCleanup)
	{
		try
		{
			executor.submit(task);
			return true;
		}
		catch (RejectedExecutionException ex)
		{
			if (rejectionCleanup != null)
			{
				rejectionCleanup.run();
			}
			log.debug("Clan War Board executor rejected async work", ex);
			return false;
		}
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
		String rotationContext = sessionContext;
		long rotationGeneration = sessionGeneration;
		if (current == null || !isIdentityCurrent(rotationContext, rotationGeneration, activeContext, identityGeneration)
			|| !current.shouldRotate(Instant.now()) || !sessionRefreshInFlight.compareAndSet(false, true))
		{
			return;
		}
		submitAsync(executorService, () ->
		{
			try
			{
				ClanWarBoardSession rotated = apiClient.rotateSession(current);
				clientThread.invoke(() ->
				{
					if (running && session == current
						&& isIdentityCurrent(rotationContext, rotationGeneration, activeContext, identityGeneration)
						&& isIdentityCurrent(rotationContext, rotationGeneration, sessionContext, sessionGeneration))
					{
						session = rotated;
					}
				});
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
		}, () -> sessionRefreshInFlight.set(false));
	}

	private void submitAvailability(String startsAt, String duration, String combatMin, String combatMax, String notes)
	{
		submitAvailability(FightMode.CWA, startsAt, duration, combatMin, combatMax, notes);
	}

	private void submitAvailability(FightMode mode, String startsAt, String duration, String combatMin, String combatMax, String notes)
	{
		ClanWarBoardSession current = session;
		String actionContext = sessionContext;
		long actionGeneration = sessionGeneration;
		if (!isActionIdentityCurrent(current, actionContext, actionGeneration))
		{
			showActionMessage("Leader authorization is not available.", Color.RED);
			return;
		}
		if (!tryBeginAction(matchActionInFlight))
		{
			showActionMessage("A fight update is already being sent.", Color.CYAN);
			return;
		}
		clientThread.invoke(() ->
		{
			if (!isLiveActionIdentityCurrent(current, actionContext, actionGeneration))
			{
				matchActionInFlight.set(false);
				return;
			}
			submitAsync(executorService, () ->
			{
				try
				{
					apiClient.postAvailability(current, ClanWarBoardApiClient.availabilityJson(mode, startsAt, duration, combatMin, combatMax, notes));
					completeActionIfCurrent(current, actionContext, actionGeneration, "War post published to the board.", Color.GREEN, true);
				}
				catch (IOException ex)
				{
					completeActionIfCurrent(current, actionContext, actionGeneration, "War post failed: " + ex.getMessage(), Color.RED, false);
				}
				finally
				{
					matchActionInFlight.set(false);
				}
			}, () -> matchActionInFlight.set(false));
		});
	}

	private void submitChallenge(String opponent, String startsAt, String duration, String combatMin, String combatMax, String world, String location, String rules)
	{
		submitChallenge(FightMode.CWA, opponent, startsAt, duration, combatMin, combatMax, world, location, rules);
	}

	private void submitChallenge(FightMode mode, String opponent, String startsAt, String duration, String combatMin, String combatMax, String world, String location, String rules)
	{
		ClanWarBoardSession current = session;
		String actionContext = sessionContext;
		long actionGeneration = sessionGeneration;
		if (!isActionIdentityCurrent(current, actionContext, actionGeneration))
		{
			showActionMessage("Leader authorization is not available.", Color.RED);
			return;
		}
		if (!tryBeginAction(matchActionInFlight))
		{
			showActionMessage("A fight update is already being sent.", Color.CYAN);
			return;
		}
		clientThread.invoke(() ->
		{
			if (!isLiveActionIdentityCurrent(current, actionContext, actionGeneration))
			{
				matchActionInFlight.set(false);
				return;
			}
			submitAsync(executorService, () ->
			{
				try
				{
					apiClient.postChallenge(current, ClanWarBoardApiClient.challengeJson(mode, opponent, startsAt, duration, combatMin, combatMax, world, location, rules));
					completeActionIfCurrent(current, actionContext, actionGeneration, "Private challenge sent.", Color.GREEN, true);
				}
				catch (IOException ex)
				{
					completeActionIfCurrent(current, actionContext, actionGeneration, "Private challenge failed: " + ex.getMessage(), Color.RED, false);
				}
				finally
				{
					matchActionInFlight.set(false);
				}
			}, () -> matchActionInFlight.set(false));
		});
	}

	private void submitChallengeAction(String challengeId, String action, String reason)
	{
		ClanWarBoardSession current = session;
		String actionContext = sessionContext;
		long actionGeneration = sessionGeneration;
		if (!isActionIdentityCurrent(current, actionContext, actionGeneration))
		{
			showActionMessage("Leader authorization is not available.", Color.RED);
			return;
		}
		if ("dispute".equals(action) && (reason == null || reason.trim().isEmpty()))
		{
			showActionMessage("A dispute reason is required.", Color.RED);
			return;
		}
		if (!tryBeginAction(matchActionInFlight))
		{
			showActionMessage("A fight update is already being sent.", Color.CYAN);
			return;
		}
		submitAsync(executorService, () ->
		{
			try
			{
				apiClient.postChallengeAction(current, challengeId, ClanWarBoardApiClient.challengeActionJson(action, reason));
				completeActionIfCurrent(current, actionContext, actionGeneration, "Fight " + action + " submitted.", Color.GREEN, true);
			}
			catch (IOException | IllegalArgumentException ex)
			{
				completeActionIfCurrent(current, actionContext, actionGeneration, "Fight update failed: " + ex.getMessage(), Color.RED, false);
			}
			finally
			{
				matchActionInFlight.set(false);
			}
		}, () -> matchActionInFlight.set(false));
	}

	private boolean isActionIdentityCurrent(ClanWarBoardSession candidate, String context, long generation)
	{
		return session == candidate && canUseSession(candidate, context, generation,
			activeContext, identityGeneration, "leader:write");
	}

	private boolean isLiveActionIdentityCurrent(ClanWarBoardSession candidate, String context, long generation)
	{
		ClanAccess liveAccess = clanAccess();
		bindIdentity(refreshContext(liveAccess));
		return liveAccess.canManageWars(config.minimumLeaderRank())
			&& isActionIdentityCurrent(candidate, context, generation);
	}

	private void completeActionIfCurrent(ClanWarBoardSession candidate, String context, long generation,
		String message, Color color, boolean refresh)
	{
		clientThread.invoke(() ->
		{
			if (!running || !isLiveActionIdentityCurrent(candidate, context, generation))
			{
				return;
			}
			showActionMessage(message, color);
			if (refresh)
			{
				refreshOnlineBoard();
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
