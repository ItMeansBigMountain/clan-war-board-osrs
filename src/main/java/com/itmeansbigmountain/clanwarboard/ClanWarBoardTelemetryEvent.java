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
	private final String evidence;
	private final String confidence;
	private final String relation;
	private final int regionId;
	private final int x;
	private final int y;
	private final int plane;

	ClanWarBoardTelemetryEvent(String type, String playerName, String clanName, String opponentName,
		int amount, int world, int tick, long timestamp, boolean playerPublic)
	{
		this(type, playerName, clanName, opponentName, amount, world, tick, timestamp, playerPublic,
			"legacy_client_observation", "unknown", "unknown", 0, 0, 0, 0);
	}

	ClanWarBoardTelemetryEvent(String type, String playerName, String clanName, String opponentName,
		int amount, int world, int tick, long timestamp, boolean playerPublic, String evidence,
		String confidence, String relation, int regionId, int x, int y, int plane)
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
		this.evidence = evidence;
		this.confidence = confidence;
		this.relation = relation;
		this.regionId = regionId;
		this.x = x;
		this.y = y;
		this.plane = plane;
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
			+ "\"playerPublic\":" + playerPublic + ","
			+ jsonField("evidence", evidence) + ","
			+ jsonField("confidence", confidence) + ","
			+ jsonField("relation", relation) + ","
			+ "\"regionId\":" + regionId + ","
			+ "\"x\":" + x + ","
			+ "\"y\":" + y + ","
			+ "\"plane\":" + plane
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
