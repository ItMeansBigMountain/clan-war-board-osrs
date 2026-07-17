package com.itmeansbigmountain.clanwarboard;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

final class ClanWarBoardApiClient
{
	static final String DEFAULT_SERVICE_URL = "https://salmon-dune-01c80c60f.7.azurestaticapps.net";
	private static final Duration TIMEOUT = Duration.ofSeconds(8);
	private static final String USER_AGENT = "ClanWarBoard-RuneLite/1.0";

	private final HttpClient httpClient;

	ClanWarBoardApiClient()
	{
		this(HttpClient.newBuilder().connectTimeout(TIMEOUT).build());
	}

	ClanWarBoardApiClient(HttpClient httpClient)
	{
		this.httpClient = httpClient;
	}

	ClanWarBoardApiStatus fetchStatus(String baseUrl) throws IOException, InterruptedException
	{
		String normalized = normalizeBaseUrl(baseUrl);
		String health = get(normalized + "/api/health");
		if (!health.contains("\"ok\": true") && !health.contains("\"ok\":true"))
		{
			throw new IOException("Clan War Board health check did not return ok");
		}

		String clans = get(normalized + "/api/clans");
		String availability = get(normalized + "/api/public/availability");
		return ClanWarBoardApiStatus.online("Connected to Clan War Board", countOccurrences(clans, "\"clan_id\""), countOccurrences(availability, "\"creatorClanName\""));
	}

	void register(String baseUrl, String installId, ClanAccess access, String pluginVersion, boolean publicStats) throws IOException, InterruptedException
	{
		if (access == null || access.getClanName() == null || access.getClanName().trim().isEmpty())
		{
			return;
		}
		post(normalizeBaseUrl(baseUrl) + "/api/plugin/register", registrationJson(installId, access, pluginVersion, publicStats));
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

	private static String jsonEscape(String value)
	{
		if (value == null)
		{
			return "";
		}
		return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
	}

	void submitTelemetry(String baseUrl, List<ClanWarBoardTelemetryEvent> events) throws IOException, InterruptedException
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
		post(normalizeBaseUrl(baseUrl) + "/api/plugin/events/batch", payload.toString());
	}

	private String get(String url) throws IOException, InterruptedException
	{
		HttpRequest request = HttpRequest.newBuilder(URI.create(url))
			.timeout(TIMEOUT)
			.header("Accept", "application/json")
			.header("User-Agent", USER_AGENT)
			.header("X-Clan-War-Board-Client", "runelite")
			.GET()
			.build();
		HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
		if (response.statusCode() < 200 || response.statusCode() >= 300)
		{
			throw new IOException("Clan War Board API returned HTTP " + response.statusCode());
		}
		return response.body();
	}

	private String post(String url, String json) throws IOException, InterruptedException
	{
		HttpRequest request = HttpRequest.newBuilder(URI.create(url))
			.timeout(TIMEOUT)
			.header("Accept", "application/json")
			.header("Content-Type", "application/json")
			.header("User-Agent", USER_AGENT)
			.header("X-Clan-War-Board-Client", "runelite")
			.POST(HttpRequest.BodyPublishers.ofString(json))
			.build();
		HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
		if (response.statusCode() < 200 || response.statusCode() >= 300)
		{
			throw new IOException("Clan War Board API returned HTTP " + response.statusCode());
		}
		return response.body();
	}

	static String normalizeBaseUrl(String baseUrl)
	{
		String value = baseUrl == null || baseUrl.trim().isEmpty() ? DEFAULT_SERVICE_URL : baseUrl.trim();
		while (value.endsWith("/"))
		{
			value = value.substring(0, value.length() - 1);
		}
		return value;
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
