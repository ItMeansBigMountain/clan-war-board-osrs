package com.itmeansbigmountain.clanwarboard;

import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

final class ClanWarBoardSession
{
	private final String token;
	private final Instant expiresAt;
	private final Set<String> capabilities;

	ClanWarBoardSession(String token, Instant expiresAt, Set<String> capabilities)
	{
		this.token = token == null ? "" : token;
		this.expiresAt = expiresAt;
		this.capabilities = Collections.unmodifiableSet(new HashSet<>(capabilities));
	}

	String getToken()
	{
		return token;
	}

	Instant getExpiresAt()
	{
		return expiresAt;
	}

	Set<String> getCapabilities()
	{
		return capabilities;
	}

	boolean hasCapability(String capability)
	{
		return capabilities.contains(capability);
	}

	boolean shouldRotate(Instant now)
	{
		return expiresAt == null || !expiresAt.isAfter(now.plusSeconds(300));
	}
}
