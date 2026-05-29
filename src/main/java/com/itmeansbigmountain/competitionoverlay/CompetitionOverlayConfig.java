package com.itmeansbigmountain.competitionoverlay;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup(CompetitionOverlayConfig.CONFIG_GROUP)
public interface CompetitionOverlayConfig extends Config
{
	String CONFIG_GROUP = "competitionoverlay";
	String DEFAULT_COMPETITION_NAME = "Clan Competition";
	String DEFAULT_STATUS_MESSAGE = "Open the Competition Overlay panel for current standings.";

	@ConfigItem(
		keyName = "competitionName",
		name = "Competition Name",
		description = "Name of the competition to reference in login/status messages",
		position = 0
	)
	default String competitionName()
	{
		return DEFAULT_COMPETITION_NAME;
	}

	@ConfigItem(
		keyName = "showLoginMessage",
		name = "Show Login Message",
		description = "Show a short Competition Overlay reminder after logging in",
		position = 1
	)
	default boolean showLoginMessage()
	{
		return true;
	}

	@ConfigItem(
		keyName = "statusMessage",
		name = "Status Message",
		description = "Short status text displayed in the login reminder until live standings are wired in",
		position = 2
	)
	default String statusMessage()
	{
		return DEFAULT_STATUS_MESSAGE;
	}
}
