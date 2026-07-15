package com.itmeansbigmountain.clanwarboard;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup(ClanWarBoardConfig.CONFIG_GROUP)
public interface ClanWarBoardConfig extends Config
{
	String CONFIG_GROUP = "clanwarboard";
	String DEFAULT_WAR_NAME = "Weekend Wilderness War";
	String DEFAULT_OPPONENT_CLAN = "Rival Clan";
	String DEFAULT_WAR_DATE = "Saturday 8 PM EST";
	String DEFAULT_WORLD = "330";
	String DEFAULT_HOTSPOT = "Lava Dragons";
	String DEFAULT_RULES = "Multi only. Returns allowed. Hold the hotspot.";
	String DEFAULT_SERVICE_URL = ClanWarBoardApiClient.DEFAULT_SERVICE_URL;

	@ConfigItem(
		keyName = "minimumLeaderRank",
		name = "Leader Rank Needed",
		description = "Minimum clan rank required to create/edit clan war plans in the panel",
		position = 0
	)
	default LeaderMinimumRank minimumLeaderRank()
	{
		return LeaderMinimumRank.ADMINISTRATOR;
	}

	@ConfigItem(
		keyName = "warName",
		name = "War Name",
		description = "Name of the planned clan war",
		position = 1
	)
	default String warName()
	{
		return DEFAULT_WAR_NAME;
	}

	@ConfigItem(
		keyName = "opponentClan",
		name = "Opponent Clan",
		description = "Clan you are setting up the fight against",
		position = 2
	)
	default String opponentClan()
	{
		return DEFAULT_OPPONENT_CLAN;
	}

	@ConfigItem(
		keyName = "warDate",
		name = "Date / Time",
		description = "When the fight is planned",
		position = 3
	)
	default String warDate()
	{
		return DEFAULT_WAR_DATE;
	}

	@ConfigItem(
		keyName = "warWorld",
		name = "World",
		description = "World for the fight",
		position = 4
	)
	default String warWorld()
	{
		return DEFAULT_WORLD;
	}

	@ConfigItem(
		keyName = "hotspot",
		name = "Hotspot",
		description = "Wilderness hotspot or meeting zone for the fight",
		position = 5
	)
	default String hotspot()
	{
		return DEFAULT_HOTSPOT;
	}

	@ConfigItem(
		keyName = "rules",
		name = "Rules / Notes",
		description = "Short rules, gear, and return notes shown to members",
		position = 6
	)
	default String rules()
	{
		return DEFAULT_RULES;
	}

	@ConfigItem(
		keyName = "publicPlayerTracking",
		name = "Show My Player Stats Publicly",
		description = "If enabled, your player name can appear on public Clan War Board website performance pages. If disabled, war telemetry still syncs but public website player tracking is private.",
		position = 7
	)
	default boolean publicPlayerTracking()
	{
		return false;
	}

	@ConfigItem(
		keyName = "serviceUrl",
		name = "Service URL",
		description = "Clan War Board service base URL used for required online sync, clan lookup, fight telemetry, and public fight board data",
		position = 8
	)
	default String serviceUrl()
	{
		return DEFAULT_SERVICE_URL;
	}

	@ConfigItem(
		keyName = "showLoginMessage",
		name = "Show Login Message",
		description = "Show a short Clan War Board reminder after logging in",
		position = 9
	)
	default boolean showLoginMessage()
	{
		return true;
	}
}
