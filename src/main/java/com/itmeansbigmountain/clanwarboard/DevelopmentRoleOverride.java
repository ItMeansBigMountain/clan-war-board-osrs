package com.itmeansbigmountain.clanwarboard;

public enum DevelopmentRoleOverride
{
	AUTOMATIC("Automatic (real clan rank)"),
	PRETEND_LEADER("Pretend clan leader"),
	PRETEND_MEMBER("Pretend clan member");

	private final String displayName;

	DevelopmentRoleOverride(String displayName)
	{
		this.displayName = displayName;
	}

	@Override
	public String toString()
	{
		return displayName;
	}
}
