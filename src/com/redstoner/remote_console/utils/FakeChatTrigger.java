package com.redstoner.remote_console.utils;

import java.util.HashSet;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import com.redstoner.remote_console.protected_classes.FakePlayer;

import net.md_5.bungee.api.ChatColor;

/**
 * This class deals with sending chat messages of fake players by creating a call-able chat event for fake players.
 * 
 * @author Pepich1851
 */

public class FakeChatTrigger implements Runnable
{
	private final FakePlayer player;
	private final String message;
	
	/**
	 * Constructor, creating the fake event trigger
	 * 
	 * @param player the FakePlayer sending the message
	 * @param message the message to send
	 */
	public FakeChatTrigger(FakePlayer player, String message)
	{
		this.player = player;
		this.message = message;
	}
	
	/**
	 * This method deals with creating, calling and processing the event in an asynchronous manner.
	 */
	@Override
	public void run()
	{
		@SuppressWarnings({ "rawtypes", "unchecked" })
		AsyncPlayerChatEvent event = new AsyncPlayerChatEvent(true, player, message,
				new HashSet(Bukkit.getOnlinePlayers()))
		{};
		Bukkit.getServer().getPluginManager().callEvent(event);
		
		String s = String.format(event.getFormat(), event.getPlayer().getDisplayName(), event.getMessage());
		if (event.isCancelled()) return;
		for (final Player p : event.getRecipients())
			p.sendMessage(ChatColor.translateAlternateColorCodes('&', s));
		Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', s));
	}
}
