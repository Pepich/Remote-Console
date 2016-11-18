package com.redstoner.remote_console.protected_classes;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;

public class FakePlayer extends CraftPlayer implements Player
{
	private String displayName = "MissingName";
	private User owner = null;
	
	protected FakePlayer(OfflinePlayer player, String displayName)
	{
		super((CraftServer) Bukkit.getServer(), new FakeEntityPlayer(player.getUniqueId(), player.getName()));
		System.out.println(player.getUniqueId());
		this.displayName = displayName;
	}
	
	protected FakePlayer(OfflinePlayer player, String displayName, User owner)
	{
		super((CraftServer) Bukkit.getServer(), new FakeEntityPlayer(player.getUniqueId(), player.getName()));
		System.out.println(player.getUniqueId());
		this.displayName = displayName;
		this.owner = owner;
	}
	
	@Override
	public void sendMessage(String message)
	{
		if (owner == null)
			Bukkit.getLogger().info(message);
		else
			owner.sendMessage(message);
	}
	
	@Override
	public void sendMessage(String[] messages)
	{
		if (owner == null)
			for (String message : messages)
				Bukkit.getLogger().info(message);
		else
			for (String message : messages)
				owner.sendMessage(message);
	}
	
	@Override
	public boolean hasPermission(String permission)
	{
		return UserManager.hasOfflinePEXPermission(getName(), permission);
	}
	
	@Override
	public boolean hasPermission(Permission permission)
	{
		return UserManager.hasOfflinePEXPermission(getName(), permission.getName());
	}
	
	@Override
	public boolean isPermissionSet(String arg0)
	{
		return true;
	}
	
	@Override
	public boolean isPermissionSet(Permission arg0)
	{
		return true;
	}
	
	@Override
	public String getDisplayName()
	{
		return displayName + "§7[C]";
	}
	
	@Override
	public void setDisplayName(String displayName)
	{
		this.displayName = displayName;
	}
	
	@Override
	public boolean performCommand(String command)
	{
		Bukkit.getLogger().info("FakePlayer " + getName() + " issued server command: /" + command);
		return super.performCommand(command);
	}
	
	public void compute(String input)
	{
		if (input.startsWith("/"))
			performCommand(input.replaceFirst("/", ""));
		else
			chat(input);
	}
}
