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
		keyName = "shareWarTelemetry",
		name = "Share War Telemetry",
		description = "Optional: send confirmed-fight combat events, opponent names, world, time, and location to the Clan War Board Azure service. Disabled sends no telemetry.",
		position = 1
	)
	default boolean shareWarTelemetry()
	{
		return false;
	}

	@ConfigItem(
		keyName = "publicPlayerTracking",
		name = "Show My Player Stats Publicly",
		description = "When telemetry sharing is enabled, show your display name with public fight statistics. Disabled keeps your public telemetry identity private.",
		position = 2
	)
	default boolean publicPlayerTracking()
	{
		return false;
	}

	@ConfigItem(
		keyName = "showLoginMessage",
		name = "Show Login Message",
		description = "Show available-fight and next-war information after logging in",
		position = 3
	)
	default boolean showLoginMessage()
	{
		return true;
	}
}
