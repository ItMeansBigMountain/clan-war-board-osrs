package com.itmeansbigmountain.clanwarboard;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class ClanWarBoardTelemetryBuffer
{
	static final int MAX_EVENTS_PER_BATCH = 50;
	static final Duration MIN_FLUSH_INTERVAL = Duration.ofSeconds(10);
	static final int HEARTBEAT_TICK_INTERVAL = 100;
	private final List<ClanWarBoardTelemetryEvent> pending = new ArrayList<>();
	private long lastFlushMillis = 0L;
	private int lastHeartbeatTick = -HEARTBEAT_TICK_INTERVAL;

	void add(ClanWarBoardTelemetryEvent event)
	{
		if (event == null)
		{
			return;
		}
		pending.add(event);
		if (pending.size() > MAX_EVENTS_PER_BATCH * 4)
		{
			pending.subList(0, pending.size() - MAX_EVENTS_PER_BATCH * 4).clear();
		}
	}

	boolean shouldHeartbeat(int tick)
	{
		if (tick - lastHeartbeatTick < HEARTBEAT_TICK_INTERVAL)
		{
			return false;
		}
		lastHeartbeatTick = tick;
		return true;
	}

	boolean shouldFlush(long nowMillis)
	{
		return !pending.isEmpty() && (pending.size() >= MAX_EVENTS_PER_BATCH || nowMillis - lastFlushMillis >= MIN_FLUSH_INTERVAL.toMillis());
	}

	List<ClanWarBoardTelemetryEvent> drain(long nowMillis)
	{
		if (pending.isEmpty())
		{
			return Collections.emptyList();
		}
		int end = Math.min(MAX_EVENTS_PER_BATCH, pending.size());
		List<ClanWarBoardTelemetryEvent> batch = new ArrayList<>(pending.subList(0, end));
		pending.subList(0, end).clear();
		lastFlushMillis = nowMillis;
		return batch;
	}

	int size()
	{
		return pending.size();
	}
}
