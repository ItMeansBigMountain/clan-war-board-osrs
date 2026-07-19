package com.itmeansbigmountain.clanwarboard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class ClanWarBoardState
{
	private final ClanWarBoardApiStatus status;
	private final int installedMembers;
	private final int clanMembers;
	private final List<WarBoardFight> available;
	private final List<WarBoardFight> scheduled;
	private final List<WarBoardFight> history;
	private final PlayerWarMetrics playerMetrics;

	ClanWarBoardState(ClanWarBoardApiStatus status, int installedMembers, int clanMembers,
		List<WarBoardFight> available, List<WarBoardFight> scheduled, List<WarBoardFight> history)
	{
		this(status, installedMembers, clanMembers, available, scheduled, history, PlayerWarMetrics.empty());
	}

	ClanWarBoardState(ClanWarBoardApiStatus status, int installedMembers, int clanMembers,
		List<WarBoardFight> available, List<WarBoardFight> scheduled, List<WarBoardFight> history,
		PlayerWarMetrics playerMetrics)
	{
		this.status = status;
		this.installedMembers = Math.max(0, installedMembers);
		this.clanMembers = Math.max(0, clanMembers);
		this.available = immutable(available);
		this.scheduled = immutable(scheduled);
		this.history = immutable(history);
		this.playerMetrics = playerMetrics == null ? PlayerWarMetrics.empty() : playerMetrics;
	}

	static ClanWarBoardState offline(String message)
	{
		return new ClanWarBoardState(ClanWarBoardApiStatus.offline(message), 0, 0,
			Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
	}

	private static List<WarBoardFight> immutable(List<WarBoardFight> value)
	{
		return Collections.unmodifiableList(new ArrayList<>(value == null ? Collections.emptyList() : value));
	}

	ClanWarBoardState withClanMembers(int memberCount)
	{
		return new ClanWarBoardState(status, installedMembers, memberCount, available, scheduled, history, playerMetrics);
	}

	ClanWarBoardApiStatus getStatus() { return status; }
	int getInstalledMembers() { return installedMembers; }
	int getClanMembers() { return clanMembers; }
	List<WarBoardFight> getAvailable() { return available; }
	List<WarBoardFight> getScheduled() { return scheduled; }
	List<WarBoardFight> getHistory() { return history; }
	PlayerWarMetrics getPlayerMetrics() { return playerMetrics; }
	int getAvailableCount() { return available.size(); }
	WarBoardFight getNextScheduled() { return scheduled.isEmpty() ? null : scheduled.get(0); }
}
