package com.redstoner.remote_console.protected_classes;

import java.util.UUID;

public class FakeEntityPlayerManager
{
	private FakeEntityPlayerManager()
	{}
	
	public static FakeEntityPlayer getFakeEntityPlayer(UUID uuid, String name, String displayname, User owner)
	{
		return new FakeEntityPlayer(uuid, name, displayname, owner);
	}
}
