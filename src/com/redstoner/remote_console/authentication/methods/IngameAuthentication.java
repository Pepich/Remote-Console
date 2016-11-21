package com.redstoner.remote_console.authentication.methods;

import java.io.InvalidObjectException;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import com.redstoner.remote_console.protected_classes.Main;
import com.redstoner.remote_console.utils.ConfigHandler;

/**
 * This class represents the IngameAuthentication for ingame confirmation based authentication
 * 
 * @author Pepich1851
 */

public class IngameAuthentication extends AuthenticationMethod implements Listener
{
	
	private static final long serialVersionUID = 7482553379914872319L;
	private int requestedInputs = 0;
	private ArrayBlockingQueue<String> inputQueue = new ArrayBlockingQueue<String>(3);
	
	private IngameAuthentication(UUID uuid)
	
	{
		super(uuid);
		Bukkit.getPluginManager().registerEvents(this, Main.getPlugin());
	}
	
	@Override
	public boolean authenticate(String[] args)
	{
		if (args != null) Bukkit.getPlayer(uuid).sendMessage("§e RMC: " + String.join(" ", args));
		Bukkit.getPlayer(uuid).sendMessage("§e RMC: There has been a request to authenticate your RMC session.");
		Bukkit.getPlayer(uuid).sendMessage("§e RMC: You got §a60s §eto confirm the request, else it will be denied.");
		Bukkit.getPlayer(uuid).sendMessage("§7 RMC: Possible inputs: Yes/No (y/n), 1/0, +/-, true/false (t/f)");
		
		while (true)
			try
			{
				String input = getHaltingPlayerInput();
				if (input == null)
				{
					Bukkit.getPlayer(uuid).sendMessage("§e RMC: §cYour request has timed out!");
					return false;
				}
				if (ConfigHandler.toBoolean(input))
				{
					Bukkit.getPlayer(uuid).sendMessage("§e RMC: §aYour request has been accepted!");
					return true;
				}
				else
				{
					Bukkit.getPlayer(uuid).sendMessage("§e RMC: §aYour request has been denied!");
					return false;
				}
			}
			catch (InvalidObjectException e)
			{
				Bukkit.getPlayer(uuid).sendMessage("§e RMC: Invalid input specified. Please try again!");
			}
	}
	
	private String getHaltingPlayerInput()
	{
		requestedInputs++;
		String result = null;
		try
		{
			result = inputQueue.poll(60, TimeUnit.SECONDS);
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		requestedInputs--;
		return result;
	}
	
	/**
	 * This method is empty, as saving an input based AuthenticationMethod makes no sense.
	 */
	
	@Override
	public void save()
	{
	
	}
	
	/**
	 * This method is empty, as loading an input based AuthenticationMethod makes no sense.
	 * 
	 * @return true
	 */
	public static IngameAuthentication load(UUID uuid)
	{
		return new IngameAuthentication(uuid);
	}
	
	/**
	 * Processes the player input event to allow for feedback to the plugin
	 * 
	 * @param event the chat event
	 */
	@EventHandler
	public void onPlayerChat(AsyncPlayerChatEvent event)
	{
		if (requestedInputs > 0)
		{
			if (event.getPlayer().getUniqueId().equals(uuid))
				if (inputQueue.offer(event.getMessage())) event.setCancelled(true);
		}
	}
	
}
