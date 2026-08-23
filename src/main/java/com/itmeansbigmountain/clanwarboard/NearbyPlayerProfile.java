package com.itmeansbigmountain.clanwarboard;

final class NearbyPlayerProfile
{
	private final String clanName;
	private final Integer cwaRating;
	private final Integer wildyRating;

	NearbyPlayerProfile(String clanName, Integer cwaRating, Integer wildyRating)
	{
		this.clanName = clanName == null ? "" : clanName.trim();
		this.cwaRating = cwaRating;
		this.wildyRating = wildyRating;
	}

	String overheadText(FightMode mode)
	{
		FightMode selectedMode = mode == null ? FightMode.CWA : mode;
		Integer rating = selectedMode == FightMode.WILDY ? wildyRating : cwaRating;
		String label = selectedMode == FightMode.WILDY ? "Wildy" : "CWA";
		return clanName + " · " + label + " " + (rating == null ? "unrated" : rating);
	}
}
