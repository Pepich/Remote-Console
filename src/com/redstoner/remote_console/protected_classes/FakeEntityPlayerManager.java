package com.redstoner.remote_console.protected_classes;

import java.util.HashMap;
import java.util.UUID;

public class FakeEntityPlayerManager
{
	private static final HashMap<UUID, FakeEntityPlayer> playerUUIDCache = new HashMap<UUID, FakeEntityPlayer>();
	private static final HashMap<String, UUID> playerNameCache = new HashMap<String, UUID>();
	
	private FakeEntityPlayerManager()
	{}
	
	public static FakeEntityPlayer getFakeEntityPlayer(UUID uuid, String displayName)
	{
		if (uuid == null)
		{
			uuid = playerNameCache.get(displayName);
			if (uuid == null)
				return null;
		}
		else
		{
			playerNameCache.put(displayName, uuid);
		}
		if (playerUUIDCache.containsKey(uuid))
			return playerUUIDCache.get(uuid);
		else
		{
			FakeEntityPlayer result = new FakeEntityPlayer(uuid, displayName);
			playerUUIDCache.put(uuid, result);
			return result;
		}
	}
}
