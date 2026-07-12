package com.itmeansbigmountain.competitionoverlay;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import net.runelite.client.RuneLite;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.externalplugins.ExternalPluginManager;
import net.runelite.client.plugins.PluginDescriptor;
import org.junit.Test;

public class CompetitionOverlayPluginTest
{
	@Test
	public void pluginDescriptorMatchesClanWarBoardDirection()
	{
		PluginDescriptor descriptor = CompetitionOverlayPlugin.class.getAnnotation(PluginDescriptor.class);

		assertEquals("Clan War Board", descriptor.name());
		assertEquals("Lets clan leaders set up wilderness fights while members see the current war board.", descriptor.description());
		assertArrayEquals(new String[] {"clan", "war", "pvp", "wilderness"}, descriptor.tags());
	}

	@Test
	public void configGroupAndDefaultsAreStable()
	{
		ConfigGroup group = CompetitionOverlayConfig.class.getAnnotation(ConfigGroup.class);
		CompetitionOverlayConfig config = new CompetitionOverlayConfig() {};

		assertEquals("competitionoverlay", group.value());
		assertEquals(LeaderMinimumRank.ADMINISTRATOR, config.minimumLeaderRank());
		assertEquals("Weekend Wilderness War", config.warName());
		assertEquals("Rival Clan", config.opponentClan());
		assertEquals("Saturday 8 PM EST", config.warDate());
		assertEquals("330", config.warWorld());
		assertEquals("Lava Dragons", config.hotspot());
		assertEquals("Multi only. Returns allowed. Hold the hotspot.", config.rules());
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
		CompetitionOverlayConfig config = new CompetitionOverlayConfig()
		{
			@Override
			public String warName()
			{
				return "Lava Dragon War";
			}

			@Override
			public String opponentClan()
			{
				return "Rival Clan";
			}

			@Override
			public String hotspot()
			{
				return "Lava Dragons";
			}

			@Override
			public String warWorld()
			{
				return "330";
			}
		};

		assertEquals("Clan War Board: Lava Dragon War vs Rival Clan at Lava Dragons on world 330 (leader setup unlocked)",
			CompetitionOverlayPlugin.buildLoginMessage(config, new ClanAccess("Oyama", "TRAPISTAN", 100)));
		assertEquals("Clan War Board: Lava Dragon War vs Rival Clan at Lava Dragons on world 330 (member view)",
			CompetitionOverlayPlugin.buildLoginMessage(config, new ClanAccess("Member", "TRAPISTAN", 50)));
	}

	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(CompetitionOverlayPlugin.class);
		RuneLite.main(args);
	}
}
