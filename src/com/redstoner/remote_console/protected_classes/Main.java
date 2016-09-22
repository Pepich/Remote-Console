package com.redstoner.remote_console.protected_classes;

import java.io.IOException;
import java.util.UUID;
import java.util.logging.Logger;

import org.bukkit.plugin.java.JavaPlugin;

import com.redstoner.remote_console.authentication.methods.GoogleAuthentication;
import com.redstoner.remote_console.authentication.methods.IngameAuthentication;
import com.redstoner.remote_console.authentication.methods.PasswordAuthentication;
import com.redstoner.remote_console.authentication.methods.TokenAuthentication;

/**
 * This class contains the test-mode entry point as well as the javaPlugin onEnable/onDisable methods
 * 
 * @author Pepich
 */

public class Main extends JavaPlugin
{
	private static UserManager userManager;
	
	private static boolean testMode = false;
	public static UUID testUUID = UUID.fromString("52e0a62c-799a-45f7-ae59-02ac206e8ae6");
	
	public static Logger logger = null;
	
	/**
	 * This method is used for automated functionality tests, it should do something when you just run it :P
	 * If it errors out then some code went bad ;)
	 * 
	 * @param args currently getting ignored
	 */
	
	public static void main(String[] args)
	{
		// Enabling test-mode
		testMode = true;
		// Register the AuthenticationMethods
		registerClasses();
		
		// Set up an empty, test UserManager
		try
		{
			userManager = UserManager.getInstance(9000);
			userManager.start();
		}
		catch (IOException e)
		{
			e.printStackTrace();
			System.exit(20);
		}
	}
	
	/**
	 * This method will be called on plugin enable and will set up the required environment
	 */
	
	@Override
	public void onEnable()
	{
		// Grab and store logger for ease of use
		logger = this.getLogger();
		// Register the AuthenticationMethods
		registerClasses();
		
		// Set up the UserManager
		try
		{
			userManager = UserManager.getInstance(9000);
			userManager.start();
		}
		catch (IOException e)
		{
			e.printStackTrace();
			// If binding to the port doesn't work, disable the plugin
			// TODO: Add error message
			getPluginLoader().disablePlugin(this);
			return;
		}
	}
	
	/**
	 * This method will be called on plugin disable and will perform saving and cleaning up
	 */
	
	@Override
	public void onDisable()
	{
		// Properly exist the UserManager
		userManager.quit();
	}
	
	/**
	 * This method will register all necessary classes that are getting loaded on runtime
	 */
	
	public static void registerClasses()
	{
		GoogleAuthentication.register();
		IngameAuthentication.register();
		PasswordAuthentication.register();
		TokenAuthentication.register();
	}
	
	public static boolean testMode()
	{
		return testMode;
	}
}
