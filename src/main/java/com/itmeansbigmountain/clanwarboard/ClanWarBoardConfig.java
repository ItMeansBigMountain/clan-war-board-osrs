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
		keyName = "showLoginMessage",
		name = "Show Login Message",
		description = "Show available-fight and next-war information after logging in",
		position = 1
	)
	default boolean showLoginMessage()
	{
		return true;
	}
}
