package com.itmeansbigmountain.clanwarboard;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.awt.image.BufferedImage;
import java.awt.Component;
import java.awt.Container;
import java.io.File;
import javax.imageio.ImageIO;

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
		assertEquals("Sets up CWA and Wilderness clan fights with rankings and post-fight analysis.", descriptor.description());
		assertArrayEquals(new String[] {"clan", "war", "pvp", "cwa", "wilderness"}, descriptor.tags());
	}

	@Test
	public void configGroupAndDefaultsAreStable()
	{
		ConfigGroup group = ClanWarBoardConfig.class.getAnnotation(ConfigGroup.class);
		ClanWarBoardConfig config = new ClanWarBoardConfig() {};

		assertEquals("clanwarboard", group.value());
		assertEquals(LeaderMinimumRank.ADMINISTRATOR, config.minimumLeaderRank());
		assertFalse(config.shareWarTelemetry());
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
	public void boardRefreshCadenceIsBoundedAndReviewFriendly()
	{
		assertEquals(60L, ClanWarBoardPlugin.AUTO_REFRESH_SECONDS);
		assertEquals("Open posts: 12", ClanWarBoardPanel.openFightCountLabel(12));
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
	public void osPartyInspiredPanelRendersRepresentativeBoardState() throws Exception
	{
		WarBoardFight open = new WarBoardFight("open", "rivals", null, "2026-08-04T20:00:00Z", 30, 70, 126, "Matched opts", "open");
		WarBoardFight scheduled = new WarBoardFight("scheduled", "trapistan", "rivals", "2026-08-05T20:00:00Z", 30, 70, 126, "Matched opts", "scheduled");
		WarBoardFight completed = new WarBoardFight("completed", "trapistan", "rivals", "2026-08-01T20:00:00Z", 30, 70, 126, "Complete", "completed");
		ClanWarBoardState sample = new ClanWarBoardState(ClanWarBoardApiStatus.online("Connected", 2, 3), 11, 60,
			Collections.singletonList(open), Collections.singletonList(scheduled), Collections.singletonList(completed));
		ClanWarBoardPanel panel = new ClanWarBoardPanel(new ClanWarBoardPanel.MatchActionHandler()
		{
			@Override public void reloadAll() { }
			@Override public void submitAvailability(String startsAt, String duration, String combatMin, String combatMax, String notes) { }
			@Override public void submitChallenge(String opponent, String startsAt, String duration, String combatMin, String combatMax, String world, String location, String rules) { }
		});
		panel.update("TRAPISTAN", "Oyama", "General", true, sample);
		panel.setSize(ClanWarBoardPanel.PANEL_WIDTH, 700);
		layoutTree(panel);
		BufferedImage image = new BufferedImage(ClanWarBoardPanel.PANEL_WIDTH, 700, BufferedImage.TYPE_INT_ARGB);
		panel.paint(image.getGraphics());
		File screenshot = new File("build/reports/clan-war-board-panel.png");
		screenshot.getParentFile().mkdirs();
		ImageIO.write(image, "png", screenshot);
		assertTrue(panel.getComponentCount() >= 2);
		assertTrue(image.getRGB(10, 10) != 0);
	}

	private static void layoutTree(Container container)
	{
		container.doLayout();
		for (Component child : container.getComponents())
		{
			if (child instanceof Container)
			{
				layoutTree((Container) child);
			}
		}
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
	public void failedRefreshPreservesLastGoodBoardAndMarksItOffline()
	{
		WarBoardFight open = new WarBoardFight("open", "rivals", null, "2026-08-04T20:00:00Z", 30, 70, 126, "", "open");
		ClanWarBoardState online = new ClanWarBoardState(ClanWarBoardApiStatus.online("Connected", 2, 1), 11, 60,
			Collections.singletonList(open), Collections.emptyList(), Collections.emptyList());
		ClanWarBoardState stale = online.withOfflineStatus("connection reset");
		assertFalse(stale.getStatus().isOnline());
		assertTrue(stale.getStatus().getMessage().contains("connection reset"));
		assertEquals(1, stale.getAvailable().size());
		assertEquals(11, stale.getInstalledMembers());
		assertEquals(60, stale.getClanMembers());
		assertEquals(2, stale.getStatus().getClanCount());
		assertEquals(1, stale.getStatus().getOpenFightCount());
	}

	@Test
	public void nextScheduledFightIsChronologicalNotResponseOrder()
	{
		WarBoardFight later = new WarBoardFight("later", "a", "b", "2026-08-05T20:00:00Z", 30, 70, 126, "", "scheduled");
		WarBoardFight earlier = new WarBoardFight("earlier", "a", "b", "2026-08-04T20:00:00Z", 30, 70, 126, "", "scheduled");
		ClanWarBoardState state = new ClanWarBoardState(ClanWarBoardApiStatus.online("Connected", 1, 0), 1, 2,
			Collections.emptyList(), java.util.Arrays.asList(later, earlier), Collections.emptyList());
		assertEquals("earlier", state.getNextScheduled().getId());
	}

	@Test
	public void matchDraftValidationRejectsSilentFallbacksAndInvalidRanges()
	{
		assertEquals("Start time is required.", MatchDraftValidator.validateAvailability("", "30", "70", "126"));
		assertEquals("Start time must be ISO-8601 UTC.", MatchDraftValidator.validateAvailability("tomorrow", "30", "70", "126"));
		assertEquals("Duration must be between 1 and 180 minutes.", MatchDraftValidator.validateAvailability("2026-08-04T20:00:00Z", "0", "70", "126"));
		assertEquals("Combat minimum cannot exceed combat maximum.", MatchDraftValidator.validateAvailability("2026-08-04T20:00:00Z", "30", "126", "70"));
		assertEquals("World must be between 301 and 599.", MatchDraftValidator.validateChallenge("rivals", "2026-08-04T20:00:00Z", "30", "70", "126", "not-a-world", "Wilderness"));
		assertEquals("Location is required for a private challenge.", MatchDraftValidator.validateChallenge("rivals", "2026-08-04T20:00:00Z", "30", "70", "126", "330", ""));
		assertNull(MatchDraftValidator.validateChallenge("rivals", "2026-08-04T20:00:00Z", "30", "70", "126", "330", "Ghorrock"));
	}

	@Test
	public void refreshResultsOnlyApplyToTheCapturedClanIdentity()
	{
		assertTrue(ClanWarBoardPlugin.isRefreshContextCurrent("Oyama|TRAPISTAN", "Oyama|TRAPISTAN"));
		assertFalse(ClanWarBoardPlugin.isRefreshContextCurrent("Oyama|TRAPISTAN", "Oyama|OTHER"));
		assertFalse(ClanWarBoardPlugin.isRefreshContextCurrent("Oyama|TRAPISTAN", "Other|TRAPISTAN"));
	}

	@Test
	public void failedRefreshNeverCarriesCachedPrivateStateAcrossIdentityChange()
	{
		WarBoardFight oldFight = new WarBoardFight("old", "trapistan", "rivals", "2026-08-04T20:00:00Z", 30, 70, 126, "private", "scheduled");
		ClanWarBoardState oldState = new ClanWarBoardState(ClanWarBoardApiStatus.online("Connected", 2, 0), 11, 60,
			Collections.emptyList(), Collections.singletonList(oldFight), Collections.emptyList(),
			new PlayerWarMetrics(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11));
		ClanWarBoardState sameIdentity = ClanWarBoardPlugin.failureState(oldState, "Oyama|TRAPISTAN", 4L, "Oyama|TRAPISTAN", 4L, "offline");
		ClanWarBoardState changedIdentity = ClanWarBoardPlugin.failureState(oldState, "Oyama|TRAPISTAN", 4L, "Oyama|OTHER", 5L, "offline");
		ClanWarBoardState abaIdentity = ClanWarBoardPlugin.failureState(oldState, "Oyama|TRAPISTAN", 4L, "Oyama|TRAPISTAN", 6L, "offline");
		assertEquals(1, sameIdentity.getScheduled().size());
		assertEquals(11, sameIdentity.getPlayerMetrics().getEventsTracked());
		assertTrue(changedIdentity.getScheduled().isEmpty());
		assertEquals(0, changedIdentity.getPlayerMetrics().getEventsTracked());
		assertTrue(abaIdentity.getScheduled().isEmpty());
		assertEquals(0, abaIdentity.getPlayerMetrics().getEventsTracked());
	}

	@Test
	public void leaderSessionMustBeBoundToCurrentIdentityGeneration()
	{
		ClanWarBoardSession leader = new ClanWarBoardSession("secret", Instant.now().plusSeconds(3600), Collections.singleton("leader:write"));
		assertTrue(ClanWarBoardPlugin.canUseSession(leader, "Oyama|TRAPISTAN", 4L, "Oyama|TRAPISTAN", 4L, "leader:write"));
		assertFalse(ClanWarBoardPlugin.canUseSession(leader, "Oyama|TRAPISTAN", 4L, "Oyama|OTHER", 5L, "leader:write"));
		assertFalse(ClanWarBoardPlugin.canUseSession(leader, "Oyama|TRAPISTAN", 4L, "Oyama|TRAPISTAN", 6L, "leader:write"));
	}

	@Test
	public void asynchronousWorkRejectsAbaIdentityReuse()
	{
		assertTrue(ClanWarBoardPlugin.isIdentityCurrent("Oyama|TRAPISTAN", 4L, "Oyama|TRAPISTAN", 4L));
		assertFalse(ClanWarBoardPlugin.isIdentityCurrent("Oyama|TRAPISTAN", 4L, "Oyama|TRAPISTAN", 6L));
	}

	@Test
	public void duplicateMatchActionsAreRejectedUntilCompletion()
	{
		java.util.concurrent.atomic.AtomicBoolean inFlight = new java.util.concurrent.atomic.AtomicBoolean();
		assertTrue(ClanWarBoardPlugin.tryBeginAction(inFlight));
		assertFalse(ClanWarBoardPlugin.tryBeginAction(inFlight));
		inFlight.set(false);
		assertTrue(ClanWarBoardPlugin.tryBeginAction(inFlight));
	}

	@Test
	public void refreshedFightSnapshotsReconcileByStableId()
	{
		WarBoardFight refreshed = new WarBoardFight("fight-1", "rivals", "trapistan", "2026-08-04T20:00:00Z", 45, 70, 126, "new terms", "scheduled");
		ClanWarBoardState state = new ClanWarBoardState(ClanWarBoardApiStatus.online("Connected", 2, 0), 1, 2,
			Collections.emptyList(), Collections.singletonList(refreshed), Collections.emptyList());
		assertEquals(refreshed, state.findFightById("fight-1"));
		assertNull(state.findFightById("removed"));
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

	@Test(expected = IllegalArgumentException.class)
	public void registrationResponseRejectsMissingCredentials()
	{
		ClanWarBoardApiClient.parseSession("{\"capabilities\":[\"member:read\"]}");
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
	public void combatSignalsRequireRecentDamageAndCountOneReturnPerDeath()
	{
		CombatSignalTracker tracker = new CombatSignalTracker();
		tracker.recordOutgoingDamage("Enemy", 100);
		assertTrue(tracker.consumeObservedKill(" enemy ", 119));
		assertFalse(tracker.consumeObservedKill("Enemy", 120));

		tracker.recordOutgoingDamage("Late", 200);
		assertFalse(tracker.consumeObservedKill("Late", 221));
		tracker.recordLocalDeath();
		assertTrue(tracker.consumeCombatReturn());
		assertFalse(tracker.consumeCombatReturn());
	}

	@Test
	public void enrichedTelemetryCarriesEvidenceLocationAndOpponent()
	{
		ClanWarBoardTelemetryEvent event = new ClanWarBoardTelemetryEvent("damage_dealt", "Oyama", "TRAPISTAN", "Enemy",
			31, 330, 123, 456L, true, "local_player_hitsplat", "high", "non_own_clan", 12850, 3200, 3600, 0);
		String json = event.toJson();
		assertTrue(json.contains("\"opponentName\":\"Enemy\""));
		assertTrue(json.contains("\"evidence\":\"local_player_hitsplat\""));
		assertTrue(json.contains("\"confidence\":\"high\""));
		assertTrue(json.contains("\"regionId\":12850"));
		assertTrue(json.contains("\"x\":3200"));
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
		buffer.clear();
		assertEquals(0, buffer.size());
		assertTrue(buffer.shouldHeartbeat(1));
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

	@Test
	public void cwaIsPrimaryAndModePayloadsStaySeparate()
	{
		ClanWarBoardPanel panel = new ClanWarBoardPanel(new ClanWarBoardPanel.MatchActionHandler()
		{
			@Override public void reloadAll() { }
			@Override public void submitAvailability(String startsAt, String duration, String combatMin, String combatMax, String notes) { }
			@Override public void submitChallenge(String opponent, String startsAt, String duration, String combatMin, String combatMax, String world, String location, String rules) { }
		});
		assertEquals(FightMode.CWA, panel.getMode());
		String cwa = ClanWarBoardApiClient.challengeJson(FightMode.CWA, "Rivals", "2026-08-20T20:00:00Z", "30", "70", "126", "330", "Clan Wars Arena", "Matched opts");
		String wildy = ClanWarBoardApiClient.challengeJson(FightMode.WILDY, "Rivals", "2026-08-20T20:00:00Z", "30", "70", "126", "330", "Ghorrock", "Returns");
		assertTrue(cwa.contains("\"mode\":\"cwa\""));
		assertTrue(cwa.contains("\"returnsAllowed\":false"));
		assertTrue(wildy.contains("\"mode\":\"wildy\""));
		assertTrue(wildy.contains("\"returnsAllowed\":true"));
	}

	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(ClanWarBoardPlugin.class);
		RuneLite.main(args);
	}
}
