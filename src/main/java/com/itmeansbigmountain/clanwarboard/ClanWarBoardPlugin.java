package com.itmeansbigmountain.clanwarboard;

import com.google.inject.Provides;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.api.clan.ClanChannel;
import net.runelite.api.clan.ClanChannelMember;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
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

	@Inject
	private Client client;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private ClanWarBoardConfig config;

	private ClanWarBoardPanel panel;
	private NavigationButton navButton;

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
			if (config.showLoginMessage())
			{
				client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", buildLoginMessage(config, clanAccess()), null);
			}
		}
	}

	private void refreshPanel()
	{
		if (panel == null)
		{
			return;
		}
		ClanAccess access = clanAccess();
		panel.update(config, access.getClanName(), access.getPlayerName(), access.getRankName(), access.canManageWars(config.minimumLeaderRank()));
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

	private String localPlayerName()
	{
		Player local = client.getLocalPlayer();
		return local == null ? null : local.getName();
	}

	static String buildLoginMessage(ClanWarBoardConfig config, ClanAccess access)
	{
		String mode = access.canManageWars(config.minimumLeaderRank()) ? "leader setup unlocked" : "member view";
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
