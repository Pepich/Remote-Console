package com.redstoner.remote_console.protected_classes;

import java.util.UUID;

import com.mojang.authlib.GameProfile;

import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.MinecraftServer;
import net.minecraft.server.v1_8_R3.PlayerInteractManager;
import net.minecraft.server.v1_8_R3.WorldServer;

public class FakeEntityPlayer extends EntityPlayer
{
	
	public FakeEntityPlayer(MinecraftServer minecraftserver, WorldServer worldserver, GameProfile gameprofile,
			PlayerInteractManager playerinteractmanager)
	{
		super(minecraftserver, worldserver, gameprofile, playerinteractmanager);
	}
	
	public FakeEntityPlayer(UUID uuid, String displayName)
	{
		super(MinecraftServer.getServer(), MinecraftServer.getServer().getWorldServer(0),
				new GameProfile(uuid, displayName), new PlayerInteractManager(MinecraftServer.getServer().getWorld()));
	}
}
