package com.itmeansbigmountain.clanwarboard;

final class ClanWarBoardTelemetryEvent
{
	private final String type;
	private final String playerName;
	private final String clanName;
	private final String opponentName;
	private final int amount;
	private final int world;
	private final int tick;
	private final long timestamp;
	private final boolean playerPublic;

	ClanWarBoardTelemetryEvent(String type, String playerName, String clanName, String opponentName, int amount, int world, int tick, long timestamp, boolean playerPublic)
	{
		this.type = type;
		this.playerName = playerName;
		this.clanName = clanName;
		this.opponentName = opponentName;
		this.amount = amount;
		this.world = world;
		this.tick = tick;
		this.timestamp = timestamp;
		this.playerPublic = playerPublic;
	}

	String toJson()
	{
		return "{"
			+ jsonField("type", type) + ","
			+ jsonField("playerName", playerPublic ? playerName : "private") + ","
			+ jsonField("clanName", clanName) + ","
			+ jsonField("opponentName", opponentName) + ","
			+ "\"amount\":" + amount + ","
			+ "\"world\":" + world + ","
			+ "\"tick\":" + tick + ","
			+ "\"timestamp\":" + timestamp + ","
			+ "\"playerPublic\":" + playerPublic
			+ "}";
	}

	static String jsonField(String key, String value)
	{
		return "\"" + escape(key) + "\":\"" + escape(value == null ? "" : value) + "\"";
	}

	static String escape(String value)
	{
		return value.replace("\\", "\\\\").replace("\"", "\\\"");
	}
}
