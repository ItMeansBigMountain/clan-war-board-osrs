package com.itmeansbigmountain.clanwarboard;

final class WarBoardFight
{
	private final String id;
	private final String clanId;
	private final String opponentClanId;
	private final String startsAt;
	private final int durationMinutes;
	private final int combatMin;
	private final int combatMax;
	private final String notes;
	private final String status;

	WarBoardFight(String id, String clanId, String opponentClanId, String startsAt, int durationMinutes,
		int combatMin, int combatMax, String notes, String status)
	{
		this.id = id;
		this.clanId = clanId;
		this.opponentClanId = opponentClanId;
		this.startsAt = startsAt;
		this.durationMinutes = durationMinutes;
		this.combatMin = combatMin;
		this.combatMax = combatMax;
		this.notes = notes;
		this.status = status;
	}

	String getId() { return id; }
	String getClanId() { return clanId; }
	String getOpponentClanId() { return opponentClanId; }
	String getStartsAt() { return startsAt; }
	int getDurationMinutes() { return durationMinutes; }
	int getCombatMin() { return combatMin; }
	int getCombatMax() { return combatMax; }
	String getNotes() { return notes; }
	String getStatus() { return status; }
	boolean needsOpponent() { return opponentClanId == null || opponentClanId.trim().isEmpty(); }
}
