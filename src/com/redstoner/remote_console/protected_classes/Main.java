package com.redstoner.remote_console.protected_classes;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.redstoner.remote_console.authentication.methods.GoogleAuthentication;
import com.redstoner.remote_console.authentication.methods.IngameAuthentication;
import com.redstoner.remote_console.authentication.methods.PasswordAuthentication;
import com.redstoner.remote_console.authentication.methods.TokenAuthentication;
import com.redstoner.remote_console.utils.ConsoleReader;

/**
 * This class contains the test-mode entry point as well as the javaPlugin onEnable/onDisable methods
 * 
 * @author Pepich
 */

public class Main extends JavaPlugin implements Listener
{
	
	private static UserManager userManager;
	
	public static Logger logger = null;
	
	private static Plugin plugin;
	
	/**
	 * This method will be called on plugin enable and will set up the required environment
	 */
	@Override
	public void onEnable()
	{
		plugin = this;
		logger = this.getLogger();
		registerClasses();
		
		ConsoleReader.init();
		
		try
		{
			userManager = UserManager.getInstance(9000);
			userManager.start();
		}
		catch (IOException e)
		{
			e.printStackTrace();
			// TODO: Add error message
			getPluginLoader().disablePlugin(this);
			return;
		}
		
		Bukkit.getPluginManager().registerEvents(this, this);
	}
	
	@SuppressWarnings("deprecation")
	@Override
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
	 * This method will register all necessary classes that are getting loaded on runtime
	 */
	public static void registerClasses()
	{
		GoogleAuthentication.register();
		IngameAuthentication.register();
		PasswordAuthentication.register();
		TokenAuthentication.register();
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
		return new File("plugins/remoteconsole/");
	}
}
