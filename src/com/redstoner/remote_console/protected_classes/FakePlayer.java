package com.redstoner.remote_console.protected_classes;

import java.io.InvalidObjectException;
import java.util.NoSuchElementException;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;

import com.redstoner.remote_console.utils.ConfigHandler;
import com.redstoner.remote_console.utils.FakeChatTrigger;

/**
 * This class creates a FakePlayer used for sending chat messages and running commands without having an actual online player.
 * 
 * @author Pepich1851
 */
public class FakePlayer extends CraftPlayer implements Player
{
	private String displayName = "MissingName";
	private User owner = null;
	
	/**
	 * Constructor, creates a new FakePlayer.
	 * 
	 * @param player the corresponding offline player. Will be used to generate the GameProfile.
	 * @param displayName the displayName to be used for the player
	 */
	protected FakePlayer(OfflinePlayer player, String displayName)
	{
		super((CraftServer) Bukkit.getServer(), new FakeEntityPlayer(player.getUniqueId(), player.getName()));
		System.out.println(player.getUniqueId());
		this.displayName = displayName;
	}
	
	/**
	 * Constructor, creates a new FakePlayer.
	 * 
	 * @param player the corresponding offline player. Will be used to generate the GameProfile.
	 * @param displayName the displayName to be used for the player
	 * @param owner the User owning this FakePlayer. Outputs from invoking commands will be forwarded to the owner.
	 */
	protected FakePlayer(OfflinePlayer player, String displayName, User owner)
	{
		super((CraftServer) Bukkit.getServer(), new FakeEntityPlayer(player.getUniqueId(), player.getName()));
		System.out.println(player.getUniqueId());
		this.displayName = displayName;
		this.owner = owner;
	}
	
	/**
	 * Overriding the default sendMessage. If an owner was provided the message will be forwarded, else it will be destroyed to avoid an infinite loop.
	 */
	@Override
	public void sendMessage(String message)
	{
		if (owner != null) owner.sendMessage(message);
	}
	
	/**
	 * Overriding the default sendMessage. If an owner was provided the message will be forwarded, else it will be destroyed to avoid an infinite loop.
	 */
	@Override
	public void sendMessage(String[] messages)
	{
		if (owner != null) for (String message : messages)
			owner.sendMessage(message);
	}
	
	/**
	 * Overriding the default hasPermission call to allow for offline player permissions using PEX.
	 */
	@Override
	public boolean hasPermission(String permission)
	{
		return UserManager.hasOfflinePEXPermission(getName(), permission);
	}
	
	/**
	 * Overriding the default hasPermission call to allow for offline player permissions using PEX.
	 */
	@Override
	public boolean hasPermission(Permission permission)
	{
		return UserManager.hasOfflinePEXPermission(getName(), permission.getName());
	}
	
	/**
	 * To avoid problems, always return true.
	 */
	@Override
	public boolean isPermissionSet(String arg0)
	{
		return true;
	}
	
	/**
	 * To avoid problems, always return true.
	 */
	@Override
	public boolean isPermissionSet(Permission arg0)
	{
		return true;
	}
	
	/**
	 * Overriding the method to avoid NPE. Will put the prefix and suffix in place.
	 */
	@Override
	public String getDisplayName()
	{
		try
		{
			return ConfigHandler.getString("rmc.prefix") + displayName + ConfigHandler.getString("rmc.suffix");
		}
		catch (InvalidObjectException | NoSuchElementException e)
		{
			return displayName;
		}
	}
	
	/**
	 * Overriding the method to avoid NPE. Will set a new displayName.
	 * 
	 * @param displayName
	 */
	@Override
	public void setDisplayName(String displayName)
	{
		this.displayName = displayName;
	}
	
	/**
	 * Overriding the method to allow for custom logging of commands.
	 * 
	 * @param command the command to be run
	 * @return true if the execution was successful
	 */
	@Override
	public boolean performCommand(String command)
	{
		Bukkit.getLogger().info("FakePlayer " + getName() + " issued server command: /" + command);
		return super.performCommand(command);
	}
	
	/**
	 * Custom method that deals with deciding whether a message should be run as a command or put in chat
	 * 
	 * @param input the input to be computed
	 * @return 0 if the message was a chat message, -1 if it was a command and the execution failed, 1 if it was a command and the execution was successful
	 */
	public int compute(String input)
	{
		if (input.startsWith("/"))
			return performCommand(input.replaceFirst("/", "")) ? 1 : -1;
		else
			chat(input);
		return 0;
	}
	
	/**
	 * Overriding the default chat function to allow fake players to interact with chat.
	 */
	@Override
	public void chat(String message)
	{
		FakeChatTrigger trigger = new FakeChatTrigger(this, message);
		Bukkit.getScheduler().runTaskAsynchronously(Main.getPlugin(), trigger);
	}
}
