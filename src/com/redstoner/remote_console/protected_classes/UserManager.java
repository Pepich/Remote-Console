package com.redstoner.remote_console.protected_classes;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.UUID;

import org.apache.logging.log4j.core.LogEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import com.redstoner.remote_console.utils.ConfigHandler;

import ru.tehkode.permissions.bukkit.PermissionsEx;

/** This class represents a manager for the users connecting to and being connected to the plugin.
 * 
 * @author Pepich1851 */
public class UserManager extends Thread implements Listener
{
	private final ServerSocket serverSocket;
	private static UserManager instance = null;
	private static ArrayList<User> connectedUsers = new ArrayList<User>();
	private static HashMap<UUID, String> displayNames;
	
	/** This constructor will set up the UserManager. Only to be called through the getInstance method if there is no instance existant yet.
	 * 
	 * @param port the port to listen to for new connections
	 * @throws IOException if something went wrong setting up the port-binding */
	private UserManager() throws IOException
	{
		int port = ConfigHandler.getInt("rmc.port");
		Main.logger.info("Trying to bind to port " + port);
		serverSocket = new ServerSocket(port);
		try
		{
			loadDisplayNames();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		Bukkit.getPluginManager().registerEvents(this, Main.getPlugin());
	}
	
	/** Loads the displayNames from the corresponding file. Run to be on startup only.
	 * 
	 * @throws IOException
	 * @throws FileNotFoundException
	 * @throws ClassNotFoundException */
	@SuppressWarnings("unchecked")
	private static void loadDisplayNames() throws FileNotFoundException, IOException, ClassNotFoundException
	{
		File displayNameFile = new File(Main.getDataLocation(), "displayNames.hmap");
		if (!displayNameFile.exists())
		{
			displayNames = new HashMap<UUID, String>();
			return;
		}
		else
		{
			ObjectInputStream input = new ObjectInputStream(new FileInputStream(displayNameFile));
			displayNames = (HashMap<UUID, String>) input.readObject();
			input.close();
		}
	}
	
	/** Saves the current displayNames to the save file. Deletes and (re)creates if necessary.
	 * 
	 * @throws IOException */
	protected static void saveDisplayNames()
	{
		try
		{
			if (displayNames == null)
				return;
			File displayNameFile = new File(Main.getDataLocation(), "displayNames.hmap");
			if (displayNameFile.exists())
				displayNameFile.delete();
			displayNameFile.createNewFile();
			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(displayNameFile));
			out.writeObject(displayNames);
			out.flush();
			out.close();
		}
		catch (IOException e)
		{}
	}
	
	/** This method will create the singleton instance if necessary and return it afterwards.
	 * 
	 * @return the singleton instance of the UserManager
	 * @throws IOException if something went wrong setting up the port-binding */
	protected static UserManager getInstance() throws IOException
	{
		if (instance == null)
			instance = new UserManager();
		return instance;
	}
	
	private boolean running = false;
	
	/** This method will accept incoming connections and create a user instance for each.
	 * In test-mode this method will generate a single test-user for automated functionality tests. */
	@Override
	public void run()
	{
		running = true;
		while (running)
		{
			User user;
			try
			{
				user = new User(serverSocket.accept());
				user.start();
				connectedUsers.add(user);
			}
			catch (IOException e)
			{
				running = false;
			}
		}
	}
	
	/** This method will stop the execution immediately. */
	public void quit()
	{
		running = false;
		saveDisplayNames();
		try
		{
			serverSocket.close();
		}
		catch (IOException e)
		{}
	}
	
	/** This method will remove a user from the list of connected users.
	 * 
	 * @param user the user to be removed from the list */
	protected static void disconnect(User user)
	{
		connectedUsers.remove(user);
	}
	
	/** This method checks if the user with the given UUID is allowed to authorize them self on the server.
	 * 
	 * @param uuid the UUID to check
	 * @return true if the user with the given UUID has the required permissions to authorize them self */
	protected static boolean mayAuthenticate(UUID uuid)
	{
		try
		{
			String permissionNode = ConfigHandler.getString("rmc.perm.auth");
			Player p = Bukkit.getPlayer(uuid);
			if (p != null)
				return p.hasPermission(permissionNode);
			return hasOfflinePEXPermission(Bukkit.getOfflinePlayer(uuid).getName(), permissionNode);
		}
		catch (InvalidObjectException | NoSuchElementException e)
		{
			return false;
		}
	}
	
	/** This method checks if the user with the given UUID is allowed to connect to the server.
	 * 
	 * @param uuid the UUID to check
	 * @return true if the user with the given UUID has the required permissions to connect */
	public static boolean mayConnect(UUID uuid)
	{
		try
		{
			String permissionNode = ConfigHandler.getString("rmc.perm.connect");
			Player p = Bukkit.getPlayer(uuid);
			if (p != null)
				return p.hasPermission(permissionNode);
			return hasOfflinePEXPermission(Bukkit.getOfflinePlayer(uuid).getName(), permissionNode);
		}
		catch (InvalidObjectException | NoSuchElementException e)
		{
			return false;
		}
	}
	
	/** Checks if an offline player has a permission using PEX.
	 * 
	 * @param name the players name
	 * @param permission the permission node to be tested
	 * @return whether the player has the permission or not */
	public static boolean hasOfflinePEXPermission(String name, String permission)
	{
		return PermissionsEx.getUser(name).has(permission);
	}
	
	/** This method provides you with a usable player (either FakePlayer or Player) from an UUID.<br />
	 * For this, a FakePlayer will be constructed.<br />
	 * All FakePlayers will be created with their last known displayName for use in outputs.<br />
	 * All FakePlayers will pipe their output to the corresponding client that is owning it.<br />
	 * All FakePlayers will be initialized with proper settings for use, yet it is not guaranteed that all functions will work.
	 * <br />
	 * <br />
	 * You can use this player in all possible occasions, as long as the actual player is online it will be used, only if the actual player is not online<br />
	 * then the FakePlayer will be used instead. This is done by the FakePlayer itself, you do not need to take care of this yourself.<br />
	 * An exception to this is the sendMessage() function, which will not send a message to the online player ever.
	 * 
	 * @param uuid the uuid of the user to get the Player from.
	 * @return a player that can run commands even when not online. */
	public static FakePlayer getPlayer(UUID uuid)
	{
		return new FakePlayer(uuid, Bukkit.getOfflinePlayer(uuid), getLastDisplayName(uuid));
	}
	
	/** This method will get the last known displayName of the user with the given uuid. Will return the current displayName if the user is online.
	 * 
	 * @param uuid the uuid of the user
	 * @return the displayName of the user */
	public static String getLastDisplayName(UUID uuid)
	{
		Player p = Bukkit.getPlayer(uuid);
		if (p != null)
			return p.getDisplayName();
		return displayNames.get(uuid) != null ? displayNames.get(uuid) : Bukkit.getOfflinePlayer(uuid).getName();
	}
	
	/** This method updates the local db of displayNames to be up-to-date for future reference (e.g. creating FakePlayers).
	 * 
	 * @param uuid the uuid of the user
	 * @param displayName their current displayname */
	private static void setLastDisplayName(UUID uuid, String displayName)
	{
		displayNames.put(uuid, displayName);
		saveDisplayNames();
	}
	
	/** This method listens on players disconnecting to update the local displayName db for constructing FakePlayers
	 * 
	 * @param event the quitEvent */
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event)
	{
		setLastDisplayName(event.getPlayer().getUniqueId(), event.getPlayer().getDisplayName());
	}
	
	/** This method listens on the logger and will broadcast to all connected and authorized users
	 * 
	 * @param event the logevent */
	public static void onMessageLogged(LogEvent event)
	{
		for (User user : connectedUsers)
		{
			if (user.isAuthenticated())
				user.sendMessage(event.getMessage().getFormattedMessage());
		}
	}
	
	/** Broadcasts a message to all (authenticated?) users.
	 * 
	 * @param message the message to be broadcaster
	 * @param authOnly if true, send the message only to authenticated users */
	public static void broadcast(String message, boolean authOnly)
	{
		if (message == null)
			return;
		if (message.contains("issued server command: /login"))
			return;
		for (User user : connectedUsers)
			if (!authOnly || user.isAuthenticated())
				user.sendMessage(message);
	}
	
	/** Returns the list of all connected users. They are not necessarily authenticated.
	 * 
	 * @return the list of connected users */
	protected static ArrayList<User> getConnectedUsers()
	{
		return connectedUsers;
	}
}
