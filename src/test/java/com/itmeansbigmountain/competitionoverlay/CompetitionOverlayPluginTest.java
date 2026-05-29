package com.itmeansbigmountain.competitionoverlay;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class CompetitionOverlayPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(CompetitionOverlayPlugin.class);
		RuneLite.main(args);
	}
}