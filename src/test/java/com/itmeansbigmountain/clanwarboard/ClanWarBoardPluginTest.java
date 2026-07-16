package com.itmeansbigmountain.clanwarboard;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
		assertEquals("", config.warName());
		assertEquals("", config.opponentClan());
		assertEquals("", config.warDate());
		assertEquals("", config.warWorld());
		assertEquals("", config.hotspot());
		assertEquals("", config.rules());
		assertFalse(config.publicPlayerTracking());
		assertEquals(ClanWarBoardApiClient.DEFAULT_SERVICE_URL, config.serviceUrl());
		assertTrue(config.showLoginMessage());
	}

	@Test
	public void clanRankGateSeparatesLeadersFromMembers()
	{
		assertTrue(new ClanAccess("Oyama", "TRAPISTAN", 126).canManageWars(LeaderMinimumRank.ADMINISTRATOR));
		assertTrue(new ClanAccess("Deputy", "TRAPISTAN", 125).canManageWars(LeaderMinimumRank.DEPUTY_OWNER));
		assertFalse(new ClanAccess("Member", "TRAPISTAN", 50).canManageWars(LeaderMinimumRank.ADMINISTRATOR));
		assertFalse(ClanAccess.noClan("Solo").canManageWars(LeaderMinimumRank.ADMINISTRATOR));
	}

	@Test
	public void loginMessageUsesWarDetailsAndRankMode()
	{
		ClanWarBoardConfig config = new ClanWarBoardConfig()
		{
			@Override
			public String warName()
			{
				return "Configured War";
			}

			@Override
			public String opponentClan()
			{
				return "Configured Opponent";
			}

			@Override
			public String hotspot()
			{
				return "Configured Hotspot";
			}

			@Override
			public String warWorld()
			{
				return "330";
			}
		};

		assertEquals("Clan War Board: Configured War vs Configured Opponent at Configured Hotspot on world 330 (leader setup unlocked)",
			ClanWarBoardPlugin.buildLoginMessage(config, new ClanAccess("Oyama", "TRAPISTAN", 100)));
		assertEquals("Clan War Board: Configured War vs Configured Opponent at Configured Hotspot on world 330 (member view)",
			ClanWarBoardPlugin.buildLoginMessage(config, new ClanAccess("Member", "TRAPISTAN", 50)));
	}

	@Test
	public void apiClientHelpersAreStable()
	{
		assertEquals("https://example.com", ClanWarBoardApiClient.normalizeBaseUrl("https://example.com///"));
		assertEquals(ClanWarBoardApiClient.DEFAULT_SERVICE_URL, ClanWarBoardApiClient.normalizeBaseUrl(" "));
		assertEquals(2, ClanWarBoardApiClient.countOccurrences("{\"clan_id\":1},{\"clan_id\":2}", "\"clan_id\""));
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
	}

	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(ClanWarBoardPlugin.class);
		RuneLite.main(args);
	}
}
