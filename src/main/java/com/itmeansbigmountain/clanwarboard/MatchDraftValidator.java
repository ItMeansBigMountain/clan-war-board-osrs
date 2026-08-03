package com.itmeansbigmountain.clanwarboard;

import java.time.Instant;
import java.time.format.DateTimeParseException;

final class MatchDraftValidator
{
	private MatchDraftValidator()
	{
	}

	static String validateAvailability(String startsAt, String duration, String combatMin, String combatMax)
	{
		String common = validateCommon(startsAt, duration, combatMin, combatMax);
		return common;
	}

	static String validateChallenge(String opponent, String startsAt, String duration, String combatMin,
		String combatMax, String world, String location)
	{
		if (blank(opponent))
		{
			return "Opponent clan is required for a private challenge.";
		}
		String common = validateCommon(startsAt, duration, combatMin, combatMax);
		if (common != null)
		{
			return common;
		}
		Integer worldNumber = integer(world);
		if (worldNumber == null || worldNumber < 301 || worldNumber > 599)
		{
			return "World must be between 301 and 599.";
		}
		if (blank(location))
		{
			return "Location is required for a private challenge.";
		}
		return null;
	}

	private static String validateCommon(String startsAt, String duration, String combatMin, String combatMax)
	{
		if (blank(startsAt))
		{
			return "Start time is required.";
		}
		try
		{
			Instant.parse(startsAt.trim());
		}
		catch (DateTimeParseException ex)
		{
			return "Start time must be ISO-8601 UTC.";
		}
		Integer durationMinutes = integer(duration);
		if (durationMinutes == null || durationMinutes < 1 || durationMinutes > 180)
		{
			return "Duration must be between 1 and 180 minutes.";
		}
		Integer minimum = integer(combatMin);
		Integer maximum = integer(combatMax);
		if (minimum == null || minimum < 3 || minimum > 126)
		{
			return "Combat minimum must be between 3 and 126.";
		}
		if (maximum == null || maximum < 3 || maximum > 126)
		{
			return "Combat maximum must be between 3 and 126.";
		}
		if (minimum > maximum)
		{
			return "Combat minimum cannot exceed combat maximum.";
		}
		return null;
	}

	private static Integer integer(String value)
	{
		try
		{
			return Integer.valueOf(value == null ? "" : value.trim());
		}
		catch (NumberFormatException ex)
		{
			return null;
		}
	}

	private static boolean blank(String value)
	{
		return value == null || value.trim().isEmpty();
	}
}
