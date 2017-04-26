package com.redstoner.remote_console.protected_classes;

import java.io.InvalidObjectException;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.util.NoSuchElementException;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_11_R1.CraftServer;
import org.bukkit.craftbukkit.v1_11_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.permissions.Permission;

import com.redstoner.remote_console.utils.ConfigHandler;

import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_11_R1.DedicatedPlayerList;

/** This class creates a FakePlayer used for sending chat messages and running commands without having an actual online player.
 * 
 * @author Pepich1851 */
public class FakePlayer extends CraftPlayer implements Player, Listener
{
	private User owner = null;
	private boolean loaded = false;
	private Player player = null;
	private boolean skipEvent = false;
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void AsyncPrePlayerJoin(AsyncPlayerPreLoginEvent e)
	{
		if (skipEvent)
			return;
		if (e.getLoginResult() == Result.ALLOWED)
			if (e.getUniqueId().equals(owner.getUUID()))
			{
				Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), new Runnable()
				{
					@Override
					public void run()
					{
						try
						{
							skipEvent = true;
							Field playerListField = Bukkit.getServer().getClass().getDeclaredField("playerList");
							playerListField.setAccessible(true);
							DedicatedPlayerList playerList = (DedicatedPlayerList) playerListField
									.get(Bukkit.getServer());
							playerList.disconnect(getHandle());
						}
						catch (NoSuchFieldException | SecurityException | IllegalArgumentException
								| IllegalAccessException e2)
						{}
						finally
						{
							skipEvent = false;
						}
					}
				});
			}
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerJoin(PlayerJoinEvent e)
	{
		if (e.getPlayer().getUniqueId().equals(getUniqueId()))
			player = e.getPlayer();
	}
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e)
	{
		if (skipEvent)
			return;
		if (e.getPlayer().getUniqueId().equals(owner.getUUID()))
		{
			Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), new Runnable()
			{
				@Override
				public void run()
				{
					try
					{
						skipEvent = true;
						Field playerListField = Bukkit.getServer().getClass().getDeclaredField("playerList");
						playerListField.setAccessible(true);
						DedicatedPlayerList playerList = (DedicatedPlayerList) playerListField.get(Bukkit.getServer());
						playerList.onPlayerJoin(getHandle(), null);
					}
					catch (NoSuchFieldException | SecurityException | IllegalArgumentException
							| IllegalAccessException e2)
					{}
					finally
					{
						skipEvent = false;
					}
				}
			}, 20);
			player = null;
		}
	}
	
	/** Constructor, creates a new FakePlayer.
	 * 
	 * @param player the corresponding offline player. Will be used to generate the GameProfile.
	 * @param displayName the displayName to be used for the player */
	protected FakePlayer(UUID uuid, FakeEntityPlayer entity)
	{
		super((CraftServer) Bukkit.getServer(), entity);
		Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), new Runnable()
		{
			@Override
			public void run()
			{
				if (Bukkit.getPlayer(uuid) == null)
				{
					try
					{
						skipEvent = true;
						Field playerListField = Bukkit.getServer().getClass().getDeclaredField("playerList");
						playerListField.setAccessible(true);
						DedicatedPlayerList playerList = (DedicatedPlayerList) playerListField.get(Bukkit.getServer());
						playerList.onPlayerJoin(getHandle(), null);
					}
					catch (NoSuchFieldException | SecurityException | IllegalArgumentException
							| IllegalAccessException e2)
					{}
					finally
					{
						skipEvent = false;
					}
				}
				loadData();
				loaded = true;
			}
		});
		while (!loaded)
		{
			try
			{
				Thread.sleep(1);
			}
			catch (InterruptedException e)
			{}
		}
		Bukkit.getPluginManager().registerEvents(this, Main.getPlugin());
	}
	
	/** Constructor, creates a new FakePlayer.
	 * 
	 * @param player the corresponding offline player. Will be used to generate the GameProfile.
	 * @param displayName the displayName to be used for the player
	 * @param owner the User owning this FakePlayer. Outputs from invoking commands will be forwarded to the owner. */
	protected FakePlayer(UUID uuid, User owner, FakeEntityPlayer entity)
	{
		super((CraftServer) Bukkit.getServer(), entity);
		this.owner = owner;
		Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), new Runnable()
		{
			@Override
			public void run()
			{
				if (Bukkit.getPlayer(uuid) == null)
				{
					try
					{
						skipEvent = true;
						Field playerListField = Bukkit.getServer().getClass().getDeclaredField("playerList");
						playerListField.setAccessible(true);
						DedicatedPlayerList playerList = (DedicatedPlayerList) playerListField.get(Bukkit.getServer());
						playerList.onPlayerJoin(getHandle(), null);
					}
					catch (NoSuchFieldException | SecurityException | IllegalArgumentException
							| IllegalAccessException e2)
					{}
					finally
					{
						skipEvent = false;
					}
				}
				loadData();
				loaded = true;
			}
		});
		while (!loaded)
		{
			try
			{
				Thread.sleep(1);
			}
			catch (InterruptedException e)
			{}
		}
		Bukkit.getPluginManager().registerEvents(this, Main.getPlugin());
	}
	
	/** This method does all the required cleanup like removing the listeners and deleting the registered bukkit player entries. */
	protected void delete()
	{
		HandlerList.unregisterAll(this);
		if (player == null)
			Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), new Runnable()
			{
				@Override
				public void run()
				{
					try
					{
						skipEvent = true;
						Field playerListField = Bukkit.getServer().getClass().getDeclaredField("playerList");
						playerListField.setAccessible(true);
						DedicatedPlayerList playerList = (DedicatedPlayerList) playerListField.get(Bukkit.getServer());
						playerList.disconnect(getHandle());
					}
					catch (NoSuchFieldException | SecurityException | IllegalArgumentException
							| IllegalAccessException e2)
					{}
					finally
					{
						skipEvent = false;
					}
				}
			});
	}
	
	/** Overriding the default sendMessage. If an owner was provided the message will be forwarded, else it will be destroyed to avoid an infinite loop. */
	@Override
	public void sendMessage(String message)
	{
		if (owner != null)
			owner.sendMessage(message);
	}
	
	/** Overriding the default sendMessage. If an owner was provided the message will be forwarded, else it will be destroyed to avoid an infinite loop. */
	@Override
	public void sendMessage(String[] messages)
	{
		if (owner != null)
			for (String message : messages)
				owner.sendMessage(message);
	}
	
	/** Overriding the default hasPermission call to allow for offline player permissions using PEX. */
	@Override
	public boolean hasPermission(String permission)
	{
		return UserManager.hasOfflinePEXPermission(getName(), permission);
	}
	
	/** Overriding the default hasPermission call to allow for offline player permissions using PEX. */
	@Override
	public boolean hasPermission(Permission permission)
	{
		return UserManager.hasOfflinePEXPermission(getName(), permission.getName());
	}
	
	/** To avoid problems, always return true. */
	@Override
	public boolean isPermissionSet(String permission)
	{
		return true;
	}
	
	/** To avoid problems, always return true. */
	@Override
	public boolean isPermissionSet(Permission permission)
	{
		return true;
	}
	
	/** Overriding the method to avoid NPE. Will put the prefix and suffix in place. */
	@Override
	public String getDisplayName()
	{
		try
		{
			return ChatColor.translateAlternateColorCodes('&', ConfigHandler.getString("rmc.prefix")
					+ super.getDisplayName() + ConfigHandler.getString("rmc.suffix"));
		}
		catch (InvalidObjectException | NoSuchElementException e)
		{
			return super.getDisplayName();
		}
	}
	
	/** Overriding the method to allow for custom logging of commands.
	 * 
	 * @param command the command to be run
	 * @return true if the execution was successful */
	@Override
	public boolean performCommand(String command)
	{
		if (!Bukkit.isPrimaryThread())
		{
			Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), new Runnable()
			{
				@Override
				public void run()
				{
					result = performSuperCommand(command);
					done = true;
				}
			});
			return waitForCommandExecutor();
		}
		else
			return performSuperCommand(command);
	}
	
	boolean result = false;
	boolean done = false;
	
	private boolean waitForCommandExecutor()
	{
		while (!done)
		{
			try
			{
				Thread.sleep(1);
			}
			catch (InterruptedException e)
			{}
		}
		done = false;
		return result;
	}
	
	private boolean performSuperCommand(String command)
	{
		Bukkit.getLogger().info("FakePlayer " + getName() + " issued server command: /" + command);
		return super.performCommand(command);
	}
	
	/** Custom method that deals with deciding whether a message should be run as a command or put in chat
	 * 
	 * @param input the input to be computed
	 * @return 0 if the message was a chat message, -1 if it was a command and the execution failed, 1 if it was a command and the execution was successful */
	public int compute(String input)
	{
		if (input.startsWith("/"))
			return performCommand(input.replaceFirst("/", "")) ? 1 : -1;
		else
			chat(input);
		return 0;
	}
	
	// /** Overriding the default chat function to allow fake players to interact with chat. */
	// @Override
	// public void chat(String message)
	// {
	// FakeChatTrigger trigger = new FakeChatTrigger(this, message);
	// Bukkit.getScheduler().runTaskAsynchronously(Main.getPlugin(), trigger);
	// }
	/** Override the default check to always return true. Allows tricking plugins into thinking that the player actually is online. */
	@Override
	public boolean isOnline()
	{
		return true;
	}
	
	@Override
	public boolean hasPlayedBefore()
	{
		return true;
	}
	
	/** @return whether the owner is connected or not. Always false when the owner is not set. */
	public boolean ownerConnected()
	{
		if (owner == null)
			return false;
		return owner.isConnected();
	}
	
	@Override
	public InetSocketAddress getAddress()
	{
		if (owner != null)
			return owner.getSocketAddress();
		else
			return null;
	}
}
