package com.redstoner.remote_console.protected_classes;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;

import com.redstoner.remote_console.authentication.methods.AuthenticationMethod;

/**
 * This class represents a manager for the users connecting to and being connected to the plugin.
 * 
 * @author Pepich
 */

public class UserManager extends Thread
{
	private static UserManager instance = null;
	private static ArrayList<UUID> authorizedUsers;
	
	/**
	 * This constructor will set up the UserManager. Only to be called through the getInstance method if there is no instance existant yet.
	 * 
	 * @param port the port to listen to for new connections
	 * @throws IOException if something went wrong setting up the port-binding
	 */
	
	private UserManager(int port) throws IOException
	{
		if (Main.testMode())
		{
			// No sockets are involved in automated tests
			serverSocket = null;
			// Creating a new ArrayList instead of loading it
			authorizedUsers = new ArrayList<UUID>();
			// Adding the UUID for the test-user
			authorizedUsers.add(Main.testUUID);
		}
		else
			// Binding the serverSocket
			serverSocket = new ServerSocket(port);
	}
	
	/**
	 * This method will create the singleton instance if necessary and return it afterwards.
	 * 
	 * @return the singleton instance of the UserManager
	 * @throws IOException if something went wrong setting up the port-binding
	 */
	
	protected static UserManager getInstance(int port) throws IOException
	{
		// Create instance if it does not exist
		if (instance == null) instance = new UserManager(port);
		// Return singleton object of UserManager
		return instance;
	}
	
	private static HashMap<String, Class<? extends AuthenticationMethod>> authMethods = new HashMap<String, Class<? extends AuthenticationMethod>>();
	
	/**
	 * This method will look up the given name in the AuthenticationMethod list and if found return the corresponding class.
	 * 
	 * @param name the name of the AuthenticationMethod used at registration
	 * @return the class of the AuthenticationMethod
	 */
	
	public Class<? extends AuthenticationMethod> getAuthenticationMethod(String name)
	{
		return authMethods.get(name);
	}
	
	private final ServerSocket serverSocket;
	private static ArrayList<User> connectedUsers = new ArrayList<User>();
	
	/**
	 * This method registers a new Authentication method to allow for dynamic loading.
	 * Authentication methods that were not registered can't be loaded on runtime and will be ignored.
	 * 
	 * @param name the name to assign to the AuthenticationMethod
	 * @param method the class of the AuthenticationMethod that is to be registered
	 */
	
	public static void register(String name, Class<? extends AuthenticationMethod> clazz)
	{
		if (Main.testMode())
			System.out.println("Registered authentication method: " + name);
		else
			Main.logger.info("Registered authentication method: " + name);
		authMethods.put(name, clazz);
	}
	
	private boolean running = false;
	
	/**
	 * This method will accept incoming connections and create a user instance for each.
	 * In test-mode this method will generate a single test-user for automated functionality tests.
	 */
	
	@Override
	public void run()
	{
		running = true;
		while (running)
		{
			if (Main.testMode())
			{
				try
				{
					Thread.sleep(1);
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			}
			else
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
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * This method will stop the execution immediately.
	 */
	
	public void quit()
	{
		running = false;
		for (User user : connectedUsers)
		{
			user.disconnect();
		}
		try
		{
			serverSocket.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * This method will remove a user from the list of connected users.
	 * 
	 * @param user the user to be removed from the list
	 */
	
	protected static void disconnect(User user)
	{
		connectedUsers.remove(user);
	}
	
	/**
	 * This method checks if there is a user with the given name.
	 * 
	 * @param name the username to check
	 * @return true, if a user with the given username exists
	 */
	@Deprecated
	protected static boolean usernameExists(String name)
	{
		return authorizedUsers.contains(Bukkit.getOfflinePlayer(name).getUniqueId());
	}
	
	protected static boolean uuidExists(UUID uuid)
	{
		return authorizedUsers.contains(uuid);
	}
}
