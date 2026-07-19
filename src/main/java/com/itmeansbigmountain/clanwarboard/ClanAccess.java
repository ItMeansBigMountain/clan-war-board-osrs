package com.itmeansbigmountain.clanwarboard;

class ClanAccess
{
	private final String playerName;
	private final String clanName;
	private final int rankValue;
	private final String rankName;

	ClanAccess(String playerName, String clanName, int rankValue)
	{
		this(playerName, clanName, rankValue, null);
	}

	ClanAccess(String playerName, String clanName, int rankValue, String rankName)
	{
		this.playerName = playerName;
		this.clanName = clanName;
		this.rankValue = rankValue;
		this.rankName = rankName;
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

	int getRankValue()
	{
		return rankValue;
	}

	String getRankName()
	{
		if (rankName != null && !rankName.trim().isEmpty())
		{
			return rankName;
		}
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
