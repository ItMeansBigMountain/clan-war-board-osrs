package com.itmeansbigmountain.clanwarboard;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup(ClanWarBoardConfig.CONFIG_GROUP)
public interface ClanWarBoardConfig extends Config
{
	String CONFIG_GROUP = "clanwarboard";

	@ConfigItem(
		keyName = "minimumLeaderRank",
		name = "Leader Rank Needed",
		description = "Minimum observed clan rank eligible to request server-authorized leader tools",
		position = 0
	)
	default LeaderMinimumRank minimumLeaderRank()
	{
		return LeaderMinimumRank.ADMINISTRATOR;
	}

	@ConfigItem(
		keyName = "publicPlayerTracking",
		name = "Show My Player Stats Publicly",
		description = "This sends your display name, clan, observed rank, IP address, and fight telemetry to the Clan War Board Azure service. Disabled keeps your public player identity private.",
		position = 1
	)
	default boolean publicPlayerTracking()
	{
		return false;
	}

	@ConfigItem(
		keyName = "showLoginMessage",
		name = "Show Login Message",
		description = "Show available-fight and next-war information after logging in",
		position = 2
	)
	default boolean showLoginMessage()
	{
		return true;
	}
}
