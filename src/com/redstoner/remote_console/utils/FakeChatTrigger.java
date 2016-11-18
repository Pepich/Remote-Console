package com.redstoner.remote_console.utils;

import java.util.HashSet;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import com.redstoner.remote_console.protected_classes.FakePlayer;

public class FakeChatTrigger implements Runnable
{
	private final FakePlayer player;
	private final String message;
	
	public FakeChatTrigger(FakePlayer player, String message)
	{
		this.player = player;
		this.message = message;
	}
	
	@Override
	public void run()
	{
		@SuppressWarnings({ "rawtypes", "unchecked" })
		AsyncPlayerChatEvent event = new AsyncPlayerChatEvent(true, player, message,
				new HashSet(Bukkit.getOnlinePlayers()))
		{};
		Bukkit.getServer().getPluginManager().callEvent(event);
		
		String s = String.format(event.getFormat(), event.getPlayer().getDisplayName(), event.getMessage());
		Bukkit.getLogger().info(s);
		for (final Player p : event.getRecipients())
			p.sendMessage(s);
	}
}
