package com.itmeansbigmountain.clanwarboard;

final class PlayerWarMetrics
{
	private final int fightsObserved;
	private final int observedKills;
	private final int deaths;
	private final int returns;
	private final int opponentDamage;
	private final int friendlyFireDamage;
	private final int damageInflicted;
	private final int damageReceived;
	private final int thirdPartyDamage;
	private final int activitySamples;
	private final int eventsTracked;

	PlayerWarMetrics(int fightsObserved, int observedKills, int deaths, int returns, int opponentDamage,
		int friendlyFireDamage, int damageInflicted, int damageReceived, int thirdPartyDamage,
		int activitySamples, int eventsTracked)
	{
		this.fightsObserved = nonnegative(fightsObserved);
		this.observedKills = nonnegative(observedKills);
		this.deaths = nonnegative(deaths);
		this.returns = nonnegative(returns);
		this.opponentDamage = nonnegative(opponentDamage);
		this.friendlyFireDamage = nonnegative(friendlyFireDamage);
		this.damageInflicted = nonnegative(damageInflicted);
		this.damageReceived = nonnegative(damageReceived);
		this.thirdPartyDamage = nonnegative(thirdPartyDamage);
		this.activitySamples = nonnegative(activitySamples);
		this.eventsTracked = nonnegative(eventsTracked);
	}

	static PlayerWarMetrics empty()
	{
		return new PlayerWarMetrics(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
	}

	private static int nonnegative(int value)
	{
		return Math.max(0, value);
	}

	int getFightsObserved() { return fightsObserved; }
	int getObservedKills() { return observedKills; }
	int getDeaths() { return deaths; }
	int getReturns() { return returns; }
	int getOpponentDamage() { return opponentDamage; }
	int getFriendlyFireDamage() { return friendlyFireDamage; }
	int getDamageInflicted() { return damageInflicted; }
	int getDamageReceived() { return damageReceived; }
	int getThirdPartyDamage() { return thirdPartyDamage; }
	int getActivitySamples() { return activitySamples; }
	int getEventsTracked() { return eventsTracked; }
}
