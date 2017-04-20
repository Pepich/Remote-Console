package com.redstoner.remote_console.protected_classes;

import java.io.InvalidObjectException;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.util.NoSuchElementException;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.craftbukkit.v1_11_R1.CraftServer;
import org.bukkit.craftbukkit.v1_11_R1.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.permissions.Permission;
import org.bukkit.potion.PotionEffect;

import com.redstoner.remote_console.utils.ConfigHandler;
import com.redstoner.remote_console.utils.FakeChatTrigger;

import net.minecraft.server.v1_11_R1.DedicatedPlayerList;
import net.minecraft.server.v1_11_R1.EnumProtocolDirection;

/** This class creates a FakePlayer used for sending chat messages and running commands without having an actual online player.
 * 
 * @author Pepich1851 */
public class FakePlayer extends CraftPlayer implements Listener
{
	private String displayName = "MissingName";
	private String name = "MissingName";
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
						playerList.a(new CustomNetworkManager(EnumProtocolDirection.SERVERBOUND), getHandle());
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
	protected FakePlayer(UUID uuid, OfflinePlayer player, String displayName)
	{
		super((CraftServer) Bukkit.getServer(), FakeEntityPlayerManager.getFakeEntityPlayer(uuid, player.getName()));
		this.displayName = displayName;
		this.name = player.getName();
		Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), new Runnable()
		{
			@Override
			public void run()
			{
				if (Bukkit.getPlayer(player.getUniqueId()) == null)
				{
					try
					{
						skipEvent = true;
						Field playerListField = Bukkit.getServer().getClass().getDeclaredField("playerList");
						playerListField.setAccessible(true);
						DedicatedPlayerList playerList = (DedicatedPlayerList) playerListField.get(Bukkit.getServer());
						playerList.a(new CustomNetworkManager(EnumProtocolDirection.SERVERBOUND), getHandle());
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
	protected FakePlayer(UUID uuid, OfflinePlayer player, String displayName, User owner)
	{
		super((CraftServer) Bukkit.getServer(), FakeEntityPlayerManager.getFakeEntityPlayer(uuid, player.getName()));
		this.displayName = displayName;
		this.name = player.getName();
		this.owner = owner;
		Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), new Runnable()
		{
			@Override
			public void run()
			{
				if (Bukkit.getPlayer(player.getUniqueId()) == null)
				{
					try
					{
						skipEvent = true;
						Field playerListField = Bukkit.getServer().getClass().getDeclaredField("playerList");
						playerListField.setAccessible(true);
						DedicatedPlayerList playerList = (DedicatedPlayerList) playerListField.get(Bukkit.getServer());
						playerList.a(new CustomNetworkManager(EnumProtocolDirection.SERVERBOUND), getHandle());
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
		HandlerList.unregisterAll(this);
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
	
	/** Fixing NPE on getting the name; */
	@Override
	public String getName()
	{
		return name;
	}
	
	/** Overriding the method to avoid NPE. Will put the prefix and suffix in place. */
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
	
	/** Overriding the method to avoid NPE. Will set a new displayName.
	 * 
	 * @param displayName */
	@Override
	public void setDisplayName(String displayName)
	{
		this.displayName = displayName;
	}
	
	/** Overriding the method to allow for custom logging of commands.
	 * 
	 * @param command the command to be run
	 * @return true if the execution was successful */
	@Override
	public boolean performCommand(String command)
	{
		Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), new Runnable()
		{
			@Override
			public void run()
			{
				Bukkit.getLogger().info("FakePlayer " + getName() + " issued server command: /" + command);
				result = performSuperCommand(command);
				done = true;
			}
		});
		return waitForCommandExecutor();
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
	
	/** Overriding the default chat function to allow fake players to interact with chat. */
	@Override
	public void chat(String message)
	{
		FakeChatTrigger trigger = new FakeChatTrigger(this, message);
		Bukkit.getScheduler().runTaskAsynchronously(Main.getPlugin(), trigger);
	}
	
	/** Override the default check to always return true. Allows tricking plugins into thinking that the player actually is online. */
	@Override
	public boolean isOnline()
	{
		return true;
	}
	
	@Override
	public boolean teleport(Entity destination)
	{
		return false;
	}
	
	@Override
	public boolean teleport(Location destination)
	{
		return false;
	}
	
	@Override
	public boolean teleport(Entity destination, TeleportCause cause)
	{
		return false;
	}
	
	@Override
	public boolean teleport(Location destination, TeleportCause cause)
	{
		return false;
	}
	
	@Override
	public boolean addPotionEffect(PotionEffect effect)
	{
		return false;
	}
	
	@Override
	public boolean addPotionEffect(PotionEffect effect, boolean force)
	{
		return false;
	}
	
	@Override
	public boolean canSee(Player player)
	{
		return false;
	}
	
	@Override
	public void closeInventory()
	{}
	
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
		return owner.getSocketAddress();
	}
}
