package com.itmeansbigmountain.clanwarboard;

enum FightMode
{
	CWA("CWA", "Clan Wars Arena", false),
	WILDY("Wildy", "Wilderness", true);

	private final String shortLabel;
	private final String label;
	private final boolean returnsAllowed;

	FightMode(String shortLabel, String label, boolean returnsAllowed)
	{
		this.shortLabel = shortLabel;
		this.label = label;
		this.returnsAllowed = returnsAllowed;
	}

	String getShortLabel() { return shortLabel; }
	String getLabel() { return label; }
	boolean isReturnsAllowed() { return returnsAllowed; }
	String apiValue() { return name().toLowerCase(java.util.Locale.ROOT); }
	FightMode other() { return this == CWA ? WILDY : CWA; }
}
