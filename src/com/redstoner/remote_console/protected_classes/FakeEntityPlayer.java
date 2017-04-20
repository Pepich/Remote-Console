package com.redstoner.remote_console.protected_classes;

import java.util.UUID;

import com.mojang.authlib.GameProfile;

import net.minecraft.server.v1_11_R1.EntityPlayer;
import net.minecraft.server.v1_11_R1.MinecraftServer;
import net.minecraft.server.v1_11_R1.PlayerInteractManager;
import net.minecraft.server.v1_11_R1.WorldServer;

/** This class creates a FakeEntityPlayer to be used for creating a FakePlayer without having an online player to start with.
 * 
 * @author Pepich1851 */
public class FakeEntityPlayer extends EntityPlayer
{
	/** Constructor to allow creation of a FakePlayer without an actual online player.
	 * 
	 * @param minecraftserver the MinecraftServer instance
	 * @param worldserver the WorldServer instance
	 * @param gameprofile the GameProfile of the user
	 * @param playerinteractmanager the PlayerInteractManager of the user */
	public FakeEntityPlayer(MinecraftServer minecraftserver, WorldServer worldserver, GameProfile gameprofile,
			PlayerInteractManager playerinteractmanager)
	{
		super(minecraftserver, worldserver, gameprofile, playerinteractmanager);
	}
	
	/** Constructor to allow creation of a FakePlayer without an actual online player.
	 * This version of the constructor will generate the missing values from the given UUID and displayName to invoke the original constructor.
	 * 
	 * @param uuid the UUID of the player
	 * @param displayName the displayName to be used for the player */
	public FakeEntityPlayer(UUID uuid, String displayName)
	{
		super(MinecraftServer.getServer(), MinecraftServer.getServer().getWorldServer(0),
				new GameProfile(uuid, displayName), new PlayerInteractManager(MinecraftServer.getServer().getWorld()));
	}
}
