package com.redstoner.remote_console.protected_classes;

import java.io.File;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.NoSuchElementException;
import java.util.logging.Logger;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.redstoner.remote_console.utils.ConfigHandler;
import com.redstoner.remote_console.utils.ConsoleReader;

/**
 * This class contains the test-mode entry point as well as the javaPlugin onEnable/onDisable methods
 * 
 * @author Pepich1851
 */

public class Main extends JavaPlugin implements Listener
{
	
	private static UserManager userManager;
	public static Logger logger = null;
	private static Plugin plugin;
	private static File dataLocation;
	
	/**
	 * This method will be called on plugin enable and will set up the required environment
	 */
	@Override
	public void onEnable()
	{
		plugin = this;
		logger = this.getLogger();
		
		ConsoleReader.init();
		
		try
		{
			dataLocation = ConfigHandler.getFile("rmc.datafolder");
		}
		catch (InvalidObjectException | NoSuchElementException e1)
		{
			e1.printStackTrace();
		}
		
		try
		{
			Ciphers.initRSA();
		}
		catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeySpecException
				| ClassNotFoundException | IllegalBlockSizeException | BadPaddingException
				| InvalidAlgorithmParameterException | IOException e)
		{
			e.printStackTrace();
		}
		
		try
		{
			userManager = UserManager.getInstance(ConfigHandler.getInt("rmc.port"));
			userManager.start();
			
		}
		catch (IOException e)
		{
			logger.severe("Could not finish setting up the resources. Exiting now.");
			getPluginLoader().disablePlugin(this);
			return;
		}
		
		Bukkit.getPluginManager().registerEvents(this, this);
	}
	
	/**
	 * Temporary method
	 */
	@Override
	@Deprecated
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		if (cmd.getName().equalsIgnoreCase("test"))
		{
			Player player = UserManager.getPlayer(Bukkit.getOfflinePlayer(args[0]).getUniqueId());
			StringBuilder st = new StringBuilder();
			for (int i = 1; i < args.length; i++)
				st.append(args[i] + " ");
			if (st.toString().startsWith("/"))
				player.performCommand(st.toString().replaceFirst("/", ""));
			else
				player.chat(st.toString());
		}
		return true;
	}
	
	/**
	 * This method will be called on plugin disable and will perform saving and cleaning up
	 */
	@Override
	public void onDisable()
	{
		userManager.quit();
	}
	
	/**
	 * @return the plugin instance
	 */
	public static Plugin getPlugin()
	{
		return plugin;
	}
	
	/**
	 * @return the default save folder for the plugin
	 */
	public static File getDataLocation()
	{
		return dataLocation;
	}
}
