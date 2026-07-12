package com.itmeansbigmountain.competitionoverlay;

class ClanAccess
{
	private final String playerName;
	private final String clanName;
	private final int rankValue;

	ClanAccess(String playerName, String clanName, int rankValue)
	{
		this.playerName = playerName;
		this.clanName = clanName;
		this.rankValue = rankValue;
	}

	static ClanAccess noClan(String playerName)
	{
		return new ClanAccess(playerName, null, -1);
	}

	static ClanAccess noRank(String playerName, String clanName)
	{
		return new ClanAccess(playerName, clanName, -1);
	}

	boolean canManageWars(LeaderMinimumRank minimumRank)
	{
		return rankValue >= minimumRank.getRankValue();
	}

	String getPlayerName()
	{
		return playerName;
	}

	String getClanName()
	{
		return clanName;
	}

	String getRankName()
	{
		if (rankValue >= 126)
		{
			return "Owner";
		}
		if (rankValue >= 125)
		{
			return "Deputy owner";
		}
		if (rankValue >= 100)
		{
			return "Administrator";
		}
		if (rankValue >= 0)
		{
			return "Member rank " + rankValue;
		}
		return "No clan rank detected";
	}
}
