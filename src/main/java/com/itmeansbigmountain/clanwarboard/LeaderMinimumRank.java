package com.itmeansbigmountain.clanwarboard;

public enum LeaderMinimumRank
{
	ADMINISTRATOR("Administrator", 100),
	DEPUTY_OWNER("Deputy owner", 125),
	OWNER("Owner", 126);

	private final String displayName;
	private final int rankValue;

	LeaderMinimumRank(String displayName, int rankValue)
	{
		this.displayName = displayName;
		this.rankValue = rankValue;
	}

	public int getRankValue()
	{
		return rankValue;
	}

	@Override
	public String toString()
	{
		return displayName;
	}
}
