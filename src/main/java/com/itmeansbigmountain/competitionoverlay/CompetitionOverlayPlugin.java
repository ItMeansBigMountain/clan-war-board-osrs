package com.itmeansbigmountain.competitionoverlay;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@Slf4j
@PluginDescriptor(
	name = CompetitionOverlayPlugin.PLUGIN_NAME,
	description = "Shows OSRS competition reminders and standing/status placeholders.",
	tags = {"competition", "overlay", "clan", "runelite"}
)
public class CompetitionOverlayPlugin extends Plugin
{
	static final String PLUGIN_NAME = "Competition Overlay";

	@Inject
	private Client client;

	@Inject
	private CompetitionOverlayConfig config;

	@Override
	protected void startUp()
	{
		log.debug("{} started", PLUGIN_NAME);
	}

	@Override
	protected void shutDown()
	{
		log.debug("{} stopped", PLUGIN_NAME);
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState() == GameState.LOGGED_IN && config.showLoginMessage())
		{
			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", buildLoginMessage(config), null);
		}
	}

	static String buildLoginMessage(CompetitionOverlayConfig config)
	{
		return PLUGIN_NAME + ": " + config.competitionName() + " - " + config.statusMessage();
	}

	@Provides
	CompetitionOverlayConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(CompetitionOverlayConfig.class);
	}
}
