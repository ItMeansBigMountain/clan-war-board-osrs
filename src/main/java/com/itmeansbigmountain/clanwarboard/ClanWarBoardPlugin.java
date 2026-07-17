package com.itmeansbigmountain.clanwarboard;

import com.google.inject.Provides;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.swing.SwingUtilities;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.api.events.ActorDeath;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.api.clan.ClanChannel;
import net.runelite.api.clan.ClanChannelMember;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;

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

	private ClanWarBoardPanel panel;
	private NavigationButton navButton;
	private final ClanWarBoardApiClient apiClient = new ClanWarBoardApiClient();
	private final ClanWarBoardTelemetryBuffer telemetryBuffer = new ClanWarBoardTelemetryBuffer();
	private ClanWarBoardApiStatus apiStatus = ClanWarBoardApiStatus.offline("Online Sync has not refreshed yet");

	@Override
	protected void startUp()
	{
		panel = new ClanWarBoardPanel();
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
			refreshOnlineBoard();
			if (config.showLoginMessage())
			{
				client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", buildLoginMessage(config, clanAccess()), null);
			}
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
	public void onGameTick(GameTick tick)
	{
		int currentTick = client.getTickCount();
		ClanAccess access = clanAccess();
		if (telemetryBuffer.shouldHeartbeat(currentTick))
		{
			queueTelemetry("heartbeat", null, 0, access);
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
			queueTelemetry("damage_taken", null, amount, access);
		}
		else if (event.getHitsplat().isMine())
		{
			queueTelemetry("damage_dealt", target.getName(), amount, access);
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
			queueTelemetry("death", null, 1, access);
			flushTelemetryNow();
		}
		else if (dead.getName() != null)
		{
			queueTelemetry("kill_candidate", dead.getName(), 1, access);
			flushTelemetryNow();
		}
	}

	private void refreshPanel()
	{
		if (panel == null)
		{
			return;
		}
		ClanAccess access = clanAccess();
		boolean leaderView = resolveLeaderView(access, config.minimumLeaderRank(), config.developmentRoleOverride());
		panel.update(config, access.getClanName(), access.getPlayerName(), access.getRankName(), leaderView, apiStatus);
	}

	private void refreshOnlineBoard()
	{
		ClanAccess registrationAccess = clanAccess();
		String installationId = installationId();
		CompletableFuture.supplyAsync(() ->
		{
			try
			{
				apiClient.register(config.serviceUrl(), installationId, registrationAccess, PLUGIN_VERSION, config.publicPlayerTracking());
				return apiClient.fetchStatus(config.serviceUrl());
			}
			catch (IOException | InterruptedException ex)
			{
				if (ex instanceof InterruptedException)
				{
					Thread.currentThread().interrupt();
				}
				log.debug("Clan War Board API refresh failed", ex);
				return ClanWarBoardApiStatus.offline(ex.getMessage());
			}
		}).thenAccept(status -> SwingUtilities.invokeLater(() ->
		{
			apiStatus = status;
			refreshPanel();
		}));
	}

	private void queueTelemetry(String type, String opponentName, int amount, ClanAccess access)
	{
		telemetryBuffer.add(new ClanWarBoardTelemetryEvent(
			type,
			access.getPlayerName(),
			access.getClanName(),
			opponentName,
			amount,
			client.getWorld(),
			client.getTickCount(),
			System.currentTimeMillis(),
			config.publicPlayerTracking()
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
		List<ClanWarBoardTelemetryEvent> batch = telemetryBuffer.drain(now);
		if (batch.isEmpty())
		{
			return;
		}
		CompletableFuture.runAsync(() ->
		{
			try
			{
				apiClient.submitTelemetry(config.serviceUrl(), batch);
			}
			catch (IOException | InterruptedException ex)
			{
				if (ex instanceof InterruptedException)
				{
					Thread.currentThread().interrupt();
				}
				log.debug("Clan War Board telemetry upload failed", ex);
			}
		});
	}

	private ClanAccess clanAccess()
	{
		String playerName = localPlayerName();
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

	static boolean resolveLeaderView(ClanAccess access, LeaderMinimumRank minimumRank, DevelopmentRoleOverride override)
	{
		if (override == DevelopmentRoleOverride.PRETEND_LEADER)
		{
			return true;
		}
		if (override == DevelopmentRoleOverride.PRETEND_MEMBER)
		{
			return false;
		}
		return access.canManageWars(minimumRank);
	}

	static String buildLoginMessage(ClanWarBoardConfig config, ClanAccess access)
	{
		String mode = resolveLeaderView(access, config.minimumLeaderRank(), config.developmentRoleOverride()) ? "leader setup unlocked" : "member view";
		return PLUGIN_NAME + ": " + config.warName() + " vs " + config.opponentClan() + " at " + config.hotspot() + " on world " + config.warWorld() + " (" + mode + ")";
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
