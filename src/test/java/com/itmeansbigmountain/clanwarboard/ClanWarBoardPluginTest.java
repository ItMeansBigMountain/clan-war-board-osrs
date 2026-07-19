package com.itmeansbigmountain.clanwarboard;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import net.runelite.client.RuneLite;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.externalplugins.ExternalPluginManager;
import net.runelite.client.plugins.PluginDescriptor;
import org.junit.Test;

public class ClanWarBoardPluginTest
{
	@Test
	public void pluginDescriptorMatchesClanWarBoardDirection()
	{
		PluginDescriptor descriptor = ClanWarBoardPlugin.class.getAnnotation(PluginDescriptor.class);

		assertEquals("Clan War Board", descriptor.name());
		assertEquals("Lets clan leaders set up wilderness fights while members see the current war board.", descriptor.description());
		assertArrayEquals(new String[] {"clan", "war", "pvp", "wilderness"}, descriptor.tags());
	}

	@Test
	public void configGroupAndDefaultsAreStable()
	{
		ConfigGroup group = ClanWarBoardConfig.class.getAnnotation(ConfigGroup.class);
		ClanWarBoardConfig config = new ClanWarBoardConfig() {};

		assertEquals("clanwarboard", group.value());
		assertEquals(LeaderMinimumRank.ADMINISTRATOR, config.minimumLeaderRank());
		assertFalse(config.publicPlayerTracking());
		assertTrue(config.showLoginMessage());
		Set<String> methodNames = java.util.Arrays.stream(ClanWarBoardConfig.class.getMethods())
			.map(java.lang.reflect.Method::getName)
			.collect(Collectors.toSet());
		assertFalse(methodNames.contains("serviceUrl"));
		assertFalse(methodNames.contains("developmentRoleOverride"));
		assertFalse(methodNames.contains("warName"));
		assertFalse(methodNames.contains("opponentClan"));
		assertFalse(methodNames.contains("warDate"));
		assertFalse(methodNames.contains("warWorld"));
		assertFalse(methodNames.contains("hotspot"));
		assertFalse(methodNames.contains("rules"));
	}

	@Test
	public void panelModeUsesObservedClanRankOnly()
	{
		ClanAccess leader = new ClanAccess("Leader", "TRAPISTAN", 126);
		ClanAccess member = new ClanAccess("Member", "TRAPISTAN", 50);
		ClanWarBoardSession leaderSession = new ClanWarBoardSession("token", Instant.now().plusSeconds(600), Collections.singleton("leader:write"));

		assertFalse(ClanWarBoardPlugin.resolveLeaderView(leader, LeaderMinimumRank.ADMINISTRATOR, null));
		assertTrue(ClanWarBoardPlugin.resolveLeaderView(leader, LeaderMinimumRank.ADMINISTRATOR, leaderSession));
		assertFalse(ClanWarBoardPlugin.resolveLeaderView(member, LeaderMinimumRank.ADMINISTRATOR, leaderSession));
	}

	@Test
	public void clanRankGateSeparatesLeadersFromMembers()
	{
		assertTrue(new ClanAccess("Oyama", "TRAPISTAN", 126).canManageWars(LeaderMinimumRank.ADMINISTRATOR));
		assertTrue(new ClanAccess("Deputy", "TRAPISTAN", 125).canManageWars(LeaderMinimumRank.DEPUTY_OWNER));
		assertFalse(new ClanAccess("Member", "TRAPISTAN", 50).canManageWars(LeaderMinimumRank.ADMINISTRATOR));
		assertFalse(ClanAccess.noClan("Solo").canManageWars(LeaderMinimumRank.ADMINISTRATOR));
		assertEquals("General", new ClanAccess("Oyama", "Rs Venom", 3, "General").getRankName());
	}

	@Test
	public void loginMessageUsesRealBoardCountsAndUpcomingFight()
	{
		WarBoardFight scheduled = new WarBoardFight("1", "trapistan", "rivals", "2026-07-20T20:00:00Z", 30, 70, 126, "", "scheduled");
		ClanWarBoardState state = new ClanWarBoardState(ClanWarBoardApiStatus.online("Connected", 2, 3), 11, 13,
			Collections.singletonList(new WarBoardFight("2", "other", null, "2026-07-21T20:00:00Z", 30, 70, 126, "", "open")),
			Collections.singletonList(scheduled), Collections.emptyList());
		String message = ClanWarBoardPlugin.buildLoginMessage(state);
		assertTrue(message.contains("1 fight needs an opponent"));
		assertTrue(message.contains("Next: trapistan vs rivals"));
	}

	@Test
	public void membersCannotOpenUnopposedPostsButLeadersCan()
	{
		WarBoardFight open = new WarBoardFight("1", "other", null, "2026-07-21T20:00:00Z", 30, 70, 126, "", "open");
		WarBoardFight scheduled = new WarBoardFight("2", "other", "trapistan", "2026-07-22T20:00:00Z", 30, 70, 126, "", "scheduled");
		assertFalse(ClanWarBoardPanel.canOpenFight(open, false));
		assertTrue(ClanWarBoardPanel.canOpenFight(open, true));
		assertTrue(ClanWarBoardPanel.canOpenFight(scheduled, false));
		assertTrue(ClanWarBoardPanel.canCreateFight(true));
		assertFalse(ClanWarBoardPanel.canCreateFight(false));
	}

	@Test
	public void lateClanRosterReplacesStaleZeroCoverageDenominator()
	{
		ClanWarBoardState stale = new ClanWarBoardState(ClanWarBoardApiStatus.online("Connected", 1, 0), 1, 0,
			Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
		ClanWarBoardState refreshed = stale.withClanMembers(60);
		assertEquals(1, refreshed.getInstalledMembers());
		assertEquals(60, refreshed.getClanMembers());
	}

	@Test
	public void registrationPayloadCarriesRealClanAndPrivacyWithoutDevelopmentAuthority()
	{
		String json = ClanWarBoardApiClient.registrationJson("11111111-1111-4111-8111-111111111111", new ClanAccess("Oyama", "TRAPISTAN", 126), "1.0.0", false);
		assertTrue(json.contains("\"clanName\":\"TRAPISTAN\""));
		assertTrue(json.contains("\"clanRank\":126"));
		assertTrue(json.contains("\"publicStats\":false"));
		assertFalse(json.contains("pretend"));
	}

	@Test
	public void apiClientHelpersAreStable()
	{
		assertTrue(ClanWarBoardApiClient.DEFAULT_SERVICE_URL.startsWith("https://"));
		assertEquals(2, ClanWarBoardApiClient.countOccurrences("{\"clan_id\":1},{\"clan_id\":2}", "\"clan_id\""));
	}

	@Test
	public void authenticatedHeadersAreFreshAndUnique()
	{
		Map<String, String> first = ClanWarBoardApiClient.authenticatedHeaders("secret-token");
		Map<String, String> second = ClanWarBoardApiClient.authenticatedHeaders("secret-token");
		assertEquals("Bearer secret-token", first.get("Authorization"));
		assertTrue(first.containsKey("X-CWB-Timestamp"));
		assertFalse(first.get("X-CWB-Nonce").equals(second.get("X-CWB-Nonce")));
	}

	@Test
	public void registrationResponseParsesSessionAndCapabilities()
	{
		ClanWarBoardSession session = ClanWarBoardApiClient.parseSession("{\"sessionToken\":\"token-123\",\"expiresAt\":\"2026-07-20T20:00:00+00:00\",\"capabilities\":[\"member:read\",\"leader:write\"]}");
		assertEquals("token-123", session.getToken());
		assertTrue(session.hasCapability("leader:write"));
		assertFalse(session.hasCapability("admin"));
	}

	@Test
	public void telemetryEventsRespectWebsitePrivacy()
	{
		ClanWarBoardTelemetryEvent privateEvent = new ClanWarBoardTelemetryEvent("damage_dealt", "Oyama", "TRAPISTAN", "Enemy", 31, 330, 123, 456L, false);
		ClanWarBoardTelemetryEvent publicEvent = new ClanWarBoardTelemetryEvent("damage_dealt", "Oyama", "TRAPISTAN", "Enemy", 31, 330, 123, 456L, true);

		assertTrue(privateEvent.toJson().contains("\"playerName\":\"private\""));
		assertFalse(privateEvent.toJson().contains("Oyama"));
		assertTrue(publicEvent.toJson().contains("\"playerName\":\"Oyama\""));
		assertTrue(privateEvent.toJson().contains("\"world\":330"));
	}

	@Test
	public void telemetryBufferBatchesAndThrottles()
	{
		ClanWarBoardTelemetryBuffer buffer = new ClanWarBoardTelemetryBuffer();
		assertTrue(buffer.shouldHeartbeat(100));
		assertFalse(buffer.shouldHeartbeat(150));
		for (int i = 0; i < ClanWarBoardTelemetryBuffer.MAX_EVENTS_PER_BATCH + 1; i++)
		{
			buffer.add(new ClanWarBoardTelemetryEvent("heartbeat", "Oyama", "TRAPISTAN", null, 0, 330, i, i, false));
		}
		assertTrue(buffer.shouldFlush(1L));
		assertEquals(ClanWarBoardTelemetryBuffer.MAX_EVENTS_PER_BATCH, buffer.drain(1L).size());
		assertEquals(1, buffer.size());
		List<ClanWarBoardTelemetryEvent> failed = buffer.drain(20_000L);
		assertEquals(1, failed.size());
		buffer.requeue(failed);
		assertEquals(1, buffer.size());
	}

	@Test
	public void playerMetricsPreserveAllTrackedDamageCategories()
	{
		PlayerWarMetrics metrics = new PlayerWarMetrics(2, 5, 3, 7, 410, 12, 422, 355, 9, 80, 120);
		assertEquals(410, metrics.getOpponentDamage());
		assertEquals(12, metrics.getFriendlyFireDamage());
		assertEquals(422, metrics.getDamageInflicted());
		assertEquals(355, metrics.getDamageReceived());
		assertEquals(9, metrics.getThirdPartyDamage());
	}

	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(ClanWarBoardPlugin.class);
		RuneLite.main(args);
	}
}
