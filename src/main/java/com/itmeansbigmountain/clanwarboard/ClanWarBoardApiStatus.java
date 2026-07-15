package com.itmeansbigmountain.clanwarboard;

final class ClanWarBoardApiStatus
{
	private final boolean online;
	private final String message;
	private final int clanCount;
	private final int openFightCount;

	ClanWarBoardApiStatus(boolean online, String message, int clanCount, int openFightCount)
	{
		this.online = online;
		this.message = message;
		this.clanCount = clanCount;
		this.openFightCount = openFightCount;
	}

	static ClanWarBoardApiStatus offline(String message)
	{
		return new ClanWarBoardApiStatus(false, message, 0, 0);
	}

	static ClanWarBoardApiStatus online(String message, int clanCount, int openFightCount)
	{
		return new ClanWarBoardApiStatus(true, message, clanCount, openFightCount);
	}

	boolean isOnline()
	{
		return online;
	}

	String getMessage()
	{
		return message;
	}

	int getClanCount()
	{
		return clanCount;
	}

	int getOpenFightCount()
	{
		return openFightCount;
	}
}
