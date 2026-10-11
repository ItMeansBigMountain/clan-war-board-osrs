package com.itmeansbigmountain.clanwarboard;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

enum GameModePreset
{
	CWA_KNOCKOUT(FightMode.CWA, "Knockout", "30", "Clan Wars Arena", "No returns; last clan standing; leaders mutually confirm the result."),
	CWA_FIRST_TO_KILLS(FightMode.CWA, "First to kills", "30", "Clan Wars Arena", "Clan Wars first-to-kills rules; leaders lock the target and mutually confirm the result."),
	WILDY_TDM(FightMode.WILDY, "Timed TDM", "20", "", "Timed team deathmatch; returns allowed; leaders mutually confirm the final score. No automatic player tracking."),
	WILDY_KOTH(FightMode.WILDY, "KOTH", "20", "", "King of the Hill using leader-agreed multi-area boundaries and leader-confirmed scoring. No automatic location tracking." );

	private final FightMode mode;
	private final String label;
	private final String defaultDuration;
	private final String defaultLocation;
	private final String rulesTemplate;

	GameModePreset(FightMode mode, String label, String defaultDuration, String defaultLocation, String rulesTemplate)
	{
		this.mode = mode;
		this.label = label;
		this.defaultDuration = defaultDuration;
		this.defaultLocation = defaultLocation;
		this.rulesTemplate = rulesTemplate;
	}

	FightMode getMode() { return mode; }
	String apiValue() { return name().toLowerCase(java.util.Locale.ROOT); }
	String getDefaultDuration() { return defaultDuration; }
	String getDefaultLocation() { return defaultLocation; }
	String getRulesTemplate() { return rulesTemplate; }
	boolean isAutomaticScoring() { return false; }

	static List<GameModePreset> forMode(FightMode mode)
	{
		if (mode == null)
		{
			return Collections.emptyList();
		}
		return Arrays.stream(values()).filter(value -> value.mode == mode).collect(Collectors.toList());
	}

	@Override
	public String toString() { return label; }
}
