package com.itmeansbigmountain.clanwarboard;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import javax.inject.Inject;
import java.io.IOException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

final class ClanWarBoardApiClient
{
	static final String DEFAULT_SERVICE_URL = "https://salmon-dune-01c80c60f.7.azurestaticapps.net";
	private static final String USER_AGENT = "ClanWarBoard-RuneLite/1.0";
	private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

	private final OkHttpClient httpClient;
	private final Gson gson;

	@Inject
	ClanWarBoardApiClient(OkHttpClient httpClient, Gson gson)
	{
		this.httpClient = httpClient;
		this.gson = gson;
	}

	ClanWarBoardState fetchBoardState(String clanName, int clanMemberCount) throws IOException
	{
		String health = get("/api/health");
		if (!health.contains("\"ok\": true") && !health.contains("\"ok\":true"))
		{
			throw new IOException("Clan War Board health check did not return ok");
		}
		String clans = get("/api/clans");
		String availability = get("/api/public/availability");
		JsonArray clanRows = gson.fromJson(clans, JsonObject.class).getAsJsonArray("clans");
		int installedMembers = 0;
		String normalizedClan = normalizeClanId(clanName);
		for (JsonElement element : clanRows)
		{
			JsonObject row = element.getAsJsonObject();
			if (normalizedClan.equals(string(row, "clan_id")))
			{
				installedMembers = integer(row, "member_count");
			}
		}
		List<WarBoardFight> open = parseFights(availability, "availability");
		List<WarBoardFight> scheduled = parseFights(availability, "scheduled");
		List<WarBoardFight> history = parseFights(availability, "history");
		ClanWarBoardApiStatus status = ClanWarBoardApiStatus.online("Connected to Clan War Board", clanRows.size(), open.size());
		return new ClanWarBoardState(status, installedMembers, clanMemberCount, open, scheduled, history);
	}

	ClanWarBoardSession register(String installId, ClanAccess access, String pluginVersion, boolean publicStats) throws IOException
	{
		if (access == null || access.getClanName() == null || access.getClanName().trim().isEmpty())
		{
			throw new IOException("Clan membership is required before registration");
		}
		return parseSession(post("/api/plugin/register", registrationJson(installId, access, pluginVersion, publicStats), Collections.emptyMap()));
	}

	ClanWarBoardSession rotateSession(ClanWarBoardSession session) throws IOException
	{
		return parseSession(post("/api/plugin/session/rotate", "{}", authenticatedHeaders(session.getToken())));
	}

	String postAvailability(ClanWarBoardSession session, String json) throws IOException
	{
		return post("/api/plugin/availability", json, authenticatedHeaders(session.getToken()));
	}

	String postChallenge(ClanWarBoardSession session, String json) throws IOException
	{
		return post("/api/plugin/challenges", json, authenticatedHeaders(session.getToken()));
	}

	String updateChallenge(ClanWarBoardSession session, String challengeId, String json) throws IOException
	{
		return post("/api/plugin/challenges/" + challengeId + "/actions", json, authenticatedHeaders(session.getToken()));
	}

	void submitTelemetry(ClanWarBoardSession session, List<ClanWarBoardTelemetryEvent> events) throws IOException
	{
		if (events == null || events.isEmpty())
		{
			return;
		}
		StringBuilder payload = new StringBuilder();
		payload.append("{\"schemaVersion\":1,\"events\":[");
		for (int i = 0; i < events.size(); i++)
		{
			if (i > 0)
			{
				payload.append(',');
			}
			payload.append(events.get(i).toJson());
		}
		payload.append("]}");
		post("/api/plugin/events/batch", payload.toString(), authenticatedHeaders(session.getToken()));
	}

	static String availabilityJson(String startsAt, String duration, String combatMin, String combatMax, String notes)
	{
		return "{\"startsAt\":\"" + jsonEscape(startsAt) + "\",\"durationMinutes\":" + number(duration, "30") +
			",\"combatMin\":" + number(combatMin, "70") + ",\"combatMax\":" + number(combatMax, "126") +
			",\"notes\":\"" + jsonEscape(notes) + "\"}";
	}

	static String challengeJson(String opponent, String startsAt, String duration, String combatMin, String combatMax, String world, String location, String rules)
	{
		return "{\"opponentClanId\":\"" + jsonEscape(opponent) + "\",\"terms\":{" +
			"\"location\":\"" + jsonEscape(location) + "\",\"world\":" + number(world, "0") +
			",\"startsAt\":\"" + jsonEscape(startsAt) + "\",\"combatMin\":" + number(combatMin, "70") +
			",\"combatMax\":" + number(combatMax, "126") + ",\"durationMinutes\":" + number(duration, "30") +
			",\"rules\":\"" + jsonEscape(rules) + "\"}}";
	}

	private List<WarBoardFight> parseFights(String json, String collection)
	{
		JsonObject root = gson.fromJson(json, JsonObject.class);
		JsonArray rows = root == null || !root.has(collection) ? new JsonArray() : root.getAsJsonArray(collection);
		List<WarBoardFight> fights = new ArrayList<>();
		for (JsonElement element : rows)
		{
			JsonObject row = element.getAsJsonObject();
			fights.add(new WarBoardFight(string(row, "id"), string(row, "creatorClanId"), string(row, "opponentClanId"),
				string(row, "startsAt"), integer(row, "durationMinutes"), integer(row, "combatMin"), integer(row, "combatMax"),
				string(row, "notes"), string(row, "status")));
		}
		return fights;
	}

	private static String string(JsonObject object, String name)
	{
		return object != null && object.has(name) && !object.get(name).isJsonNull() ? object.get(name).getAsString() : "";
	}

	private static int integer(JsonObject object, String name)
	{
		try
		{
			return object != null && object.has(name) ? object.get(name).getAsInt() : 0;
		}
		catch (RuntimeException ignored)
		{
			return 0;
		}
	}

	private static String normalizeClanId(String value)
	{
		return value == null ? "" : value.trim().toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
	}

	private static String number(String value, String fallback)
	{
		try
		{
			return Integer.toString(Integer.parseInt(value == null ? "" : value.trim()));
		}
		catch (NumberFormatException ignored)
		{
			return fallback;
		}
	}

	static String registrationJson(String installId, ClanAccess access, String pluginVersion, boolean publicStats)
	{
		return "{\"installId\":\"" + jsonEscape(installId) +
			"\",\"playerName\":\"" + jsonEscape(access.getPlayerName()) +
			"\",\"clanName\":\"" + jsonEscape(access.getClanName()) +
			"\",\"clanRank\":" + access.getRankValue() +
			",\"pluginVersion\":\"" + jsonEscape(pluginVersion) +
			"\",\"publicStats\":" + publicStats + "}";
	}

	static Map<String, String> authenticatedHeaders(String token)
	{
		Map<String, String> headers = new LinkedHashMap<>();
		headers.put("Authorization", "Bearer " + token);
		headers.put("X-CWB-Timestamp", Long.toString(Instant.now().getEpochSecond()));
		headers.put("X-CWB-Nonce", UUID.randomUUID().toString());
		return headers;
	}

	static ClanWarBoardSession parseSession(String json)
	{
		String token = jsonString(json, "sessionToken");
		String expiresAt = jsonString(json, "expiresAt");
		String rawCapabilities = jsonArray(json, "capabilities");
		Set<String> capabilities = rawCapabilities.isEmpty() ? Collections.emptySet() : Arrays.stream(rawCapabilities.split(","))
			.map(value -> value.trim().replace("\"", ""))
			.filter(value -> !value.isEmpty())
			.collect(Collectors.toSet());
		return new ClanWarBoardSession(token, expiresAt.isEmpty() ? null : OffsetDateTime.parse(expiresAt).toInstant(), capabilities);
	}

	private String get(String path) throws IOException
	{
		Request request = new Request.Builder()
			.url(DEFAULT_SERVICE_URL + path)
			.header("Accept", "application/json")
			.header("User-Agent", USER_AGENT)
			.header("X-Clan-War-Board-Client", "runelite")
			.build();
		return execute(request);
	}

	private String post(String path, String json, Map<String, String> headers) throws IOException
	{
		Request.Builder builder = new Request.Builder()
			.url(DEFAULT_SERVICE_URL + path)
			.header("Accept", "application/json")
			.header("User-Agent", USER_AGENT)
			.header("X-Clan-War-Board-Client", "runelite")
			.post(RequestBody.create(JSON, json));
		headers.forEach(builder::header);
		return execute(builder.build());
	}

	private String execute(Request request) throws IOException
	{
		try (Response response = httpClient.newCall(request).execute())
		{
			if (!response.isSuccessful())
			{
				throw new IOException("Clan War Board API returned HTTP " + response.code());
			}
			return response.body() == null ? "" : response.body().string();
		}
	}

	private static String jsonString(String json, String key)
	{
		String marker = "\"" + key + "\":\"";
		int start = json == null ? -1 : json.indexOf(marker);
		if (start < 0)
		{
			return "";
		}
		start += marker.length();
		int end = json.indexOf('"', start);
		return end < 0 ? "" : json.substring(start, end);
	}

	private static String jsonArray(String json, String key)
	{
		String marker = "\"" + key + "\":[";
		int start = json == null ? -1 : json.indexOf(marker);
		if (start < 0)
		{
			return "";
		}
		start += marker.length();
		int end = json.indexOf(']', start);
		return end < 0 ? "" : json.substring(start, end);
	}

	private static String jsonEscape(String value)
	{
		if (value == null)
		{
			return "";
		}
		return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
	}

	static int countOccurrences(String text, String token)
	{
		if (text == null || text.isEmpty() || token == null || token.isEmpty())
		{
			return 0;
		}
		int count = 0;
		int offset = 0;
		while ((offset = text.indexOf(token, offset)) >= 0)
		{
			count++;
			offset += token.length();
		}
		return count;
	}
}
