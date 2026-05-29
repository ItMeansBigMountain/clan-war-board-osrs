package com.itmeansbigmountain.competitionoverlay;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import net.runelite.client.RuneLite;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.externalplugins.ExternalPluginManager;
import net.runelite.client.plugins.PluginDescriptor;
import org.junit.Test;

public class CompetitionOverlayPluginTest
{
	@Test
	public void pluginDescriptorMatchesPluginHubMetadata()
	{
		PluginDescriptor descriptor = CompetitionOverlayPlugin.class.getAnnotation(PluginDescriptor.class);

		assertEquals("Competition Overlay", descriptor.name());
		assertEquals("Shows OSRS competition reminders and standing/status placeholders.", descriptor.description());
		assertArrayEquals(new String[] {"competition", "overlay", "clan", "runelite"}, descriptor.tags());
	}

	@Test
	public void configGroupAndDefaultsAreStable()
	{
		ConfigGroup group = CompetitionOverlayConfig.class.getAnnotation(ConfigGroup.class);
		CompetitionOverlayConfig config = new CompetitionOverlayConfig() {};

		assertEquals("competitionoverlay", group.value());
		assertEquals("Clan Competition", config.competitionName());
		assertTrue(config.showLoginMessage());
		assertEquals("Open the Competition Overlay panel for current standings.", config.statusMessage());
	}

	@Test
	public void loginMessageUsesConfiguredCompetitionAndStatus()
	{
		CompetitionOverlayConfig config = new CompetitionOverlayConfig()
		{
			@Override
			public String competitionName()
			{
				return "Bingo Week";
			}

			@Override
			public String statusMessage()
			{
				return "Team A leads by 42 points";
			}
		};

		assertEquals("Competition Overlay: Bingo Week - Team A leads by 42 points", CompetitionOverlayPlugin.buildLoginMessage(config));
	}

	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(CompetitionOverlayPlugin.class);
		RuneLite.main(args);
	}
}
