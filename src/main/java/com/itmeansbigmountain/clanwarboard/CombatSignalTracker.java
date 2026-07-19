package com.itmeansbigmountain.clanwarboard;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

final class CombatSignalTracker
{
	static final int KILL_ATTRIBUTION_TICKS = 20;
	private final Map<String, Integer> recentOutgoingDamage = new HashMap<>();
	private boolean returnPending;

	void recordOutgoingDamage(String playerName, int tick)
	{
		String key = normalize(playerName);
		if (!key.isEmpty())
		{
			recentOutgoingDamage.put(key, tick);
		}
		prune(tick);
	}

	boolean consumeObservedKill(String playerName, int tick)
	{
		String key = normalize(playerName);
		Integer damagedAt = recentOutgoingDamage.remove(key);
		prune(tick);
		return damagedAt != null && tick >= damagedAt && tick - damagedAt <= KILL_ATTRIBUTION_TICKS;
	}

	void recordLocalDeath()
	{
		returnPending = true;
	}

	boolean consumeCombatReturn()
	{
		if (!returnPending)
		{
			return false;
		}
		returnPending = false;
		return true;
	}

	void reset()
	{
		recentOutgoingDamage.clear();
		returnPending = false;
	}

	private void prune(int currentTick)
	{
		recentOutgoingDamage.entrySet().removeIf(entry -> currentTick < entry.getValue()
			|| currentTick - entry.getValue() > KILL_ATTRIBUTION_TICKS);
	}

	private static String normalize(String playerName)
	{
		return playerName == null ? "" : playerName.trim().replace('\u00a0', ' ').toLowerCase(Locale.ROOT);
	}
}
