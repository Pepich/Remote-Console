package com.redstoner.remote_console.protected_classes;

import java.io.File;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.NoSuchElementException;
import java.util.UUID;
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

import com.redstoner.remote_console.authentication.methods.GoogleAuthentication;
import com.redstoner.remote_console.authentication.methods.TokenAuthentication;
import com.redstoner.remote_console.utils.ConfigHandler;
import com.redstoner.remote_console.utils.ConsoleReader;
import com.redstoner.remote_console.utils.LogHandler;

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
		try
		{
			if (cmd.getName().equalsIgnoreCase("remoteconsole")
					&& sender.hasPermission(ConfigHandler.getString("rmc.perm")))
			{
				if (args[0].equalsIgnoreCase("gettoken")
						&& sender.hasPermission(ConfigHandler.getString("rmc.perm.gettoken")))
				{
					UUID uuid = null;
					if (sender instanceof Player)
					{
						uuid = ((Player) sender).getUniqueId();
						logger.info("The token for you is: " + TokenAuthentication.load(uuid).getRandomToken());
					}
					else
					{
						uuid = Bukkit.getOfflinePlayer(args[1]).getUniqueId();
						logger.info(
								"The token for " + args[1] + " is: " + TokenAuthentication.load(uuid).getRandomToken());
					}
				}
				else if (args[0].equalsIgnoreCase("list")
						&& sender.hasPermission(ConfigHandler.getString("rmc.perm.list")))
				{
					sender.sendMessage("§e RMC: There is a total of §a" + UserManager.getConnectedUsers().size()
							+ " §e users connected right now:");
					for (User user : UserManager.getConnectedUsers())
					{
						sender.sendMessage("§e " + Bukkit.getOfflinePlayer(user.getUUID()).getName()
								+ (user.isAuthenticated() ? " §a(" : " §c(Not ") + "Authenticated(");
					}
				}
				else if (args[0].equals("2fa-restore")
						&& sender.hasPermission(ConfigHandler.getString("rmc.perm.auth")))
				{
					UUID uuid = null;
					if (sender instanceof Player)
						uuid = ((Player) sender).getUniqueId();
					else
						uuid = Bukkit.getOfflinePlayer(args[1]).getUniqueId();
						
					GoogleAuthentication gAuth = GoogleAuthentication.load(uuid);
					if (gAuth.isEnabled())
					{
						sender.sendMessage(" §eRMC: &aFound enabled 2FA. Attempting to retrieve restore data...");
						int keyAmount = gAuth.getRestoreKeys().size();
						sender.sendMessage(
								" §eRMC: &eFound " + (keyAmount > 0 ? "§a" : "§c") + keyAmount + " §erestore keys:");
						if (keyAmount == 0)
						{
							sender.sendMessage(" §eRMC: Trying to generate a new restore key...");
							String key = GoogleAuthentication.getRandomSecretKey();
							gAuth.getRestoreKeys().add(key);
							sender.sendMessage(" §eRMC: &a" + key);
						}
						else
						{
							for (String key : gAuth.getRestoreKeys())
								sender.sendMessage(" §eRMC: &a" + key);
						}
					}
					else
					{
						sender.sendMessage(
								" §eRMC: &cCould not find any 2FA data for the account. Perhaps 2FA is not enabled?");
					}
				}
				
				else if (args[0].equals("2fa-secret") && sender.hasPermission(ConfigHandler.getString("rmc.perm.auth")))
				{
					UUID uuid = null;
					if (sender instanceof Player)
						uuid = ((Player) sender).getUniqueId();
					else
						uuid = Bukkit.getOfflinePlayer(args[1]).getUniqueId();
						
					GoogleAuthentication gAuth = GoogleAuthentication.load(uuid);
					if (gAuth.isEnabled())
					{
						sender.sendMessage(" §eRMC: &aFound enabled 2FA. Attempting to retrieve restore data...");
						String key = gAuth.getSecretKey();
						if (key == null)
						{
							sender.sendMessage(" §eRMC: &cCouldn't find any secret key.");
							sender.sendMessage(" §eRMC: Trying to generate a new secret key...");
							gAuth.enable();
							key = gAuth.getSecretKey();
							sender.sendMessage(" §eRMC: The new secret key is: &a" + key);
							sender.sendMessage(" §eRMC: We also generated a new set of restore keys:");
							
							for (String key2 : gAuth.getRestoreKeys())
								sender.sendMessage(" §eRMC: &a" + key2);
						}
						else
						{
							sender.sendMessage(" §eRMC: §aFound a secret key: " + key);
						}
					}
					else
					{
						sender.sendMessage(
								" §eRMC: &cCould not find any 2FA data for the account. Perhaps 2FA is not enabled?");
					}
				}
				
				else if (args[0].equalsIgnoreCase("2fa"))
				{
					if (args[1].equalsIgnoreCase("enable"))
					{
						UUID uuid = null;
						if (sender instanceof Player)
							uuid = ((Player) sender).getUniqueId();
						else
							uuid = Bukkit.getOfflinePlayer(args[1]).getUniqueId();
							
						GoogleAuthentication gAuth = GoogleAuthentication.load(uuid);
						
						if (!gAuth.isEnabled())
						{
							if (gAuth.enable())
							{
								sender.sendMessage(" §eRMC: Generating a new secret key...");
								gAuth.enable();
								String key = gAuth.getSecretKey();
								sender.sendMessage(" §eRMC: The new secret key is: &a" + key);
								sender.sendMessage(" §eRMC: We also generated a new set of restore keys:");
								
								for (String key2 : gAuth.getRestoreKeys())
									sender.sendMessage(" §eRMC: &a" + key2);
							}
							else
							{
								sender.sendMessage(
										" §eRMC: &aPrevious settings were restored, your old keys are still valid.");
							}
							sender.sendMessage(" §eRMC: 2FA §aenabled.");
						}
						else
						{
							sender.sendMessage(" §eRMC: &c2FA was already enabled!");
						}
					}
					else if (args[1].equalsIgnoreCase("disable"))
					{
						UUID uuid = null;
						if (sender instanceof Player)
							uuid = ((Player) sender).getUniqueId();
						else
							uuid = Bukkit.getOfflinePlayer(args[1]).getUniqueId();
							
						GoogleAuthentication gAuth = GoogleAuthentication.load(uuid);
						
						if (gAuth.isEnabled())
						{
							if (args.length == 3)
							{
								boolean clear = ConfigHandler.toBoolean(args[2]);
								if (clear)
								{
									sender.sendMessage(
											" §eRMC: &cClearing settings, a new key will be generated when you enable 2AF again.");
								}
								else
								{
									sender.sendMessage(
											" §eRMC: &cNot clearing settings, your keys will still be valid when you enable 2AF again.");
								}
								sender.sendMessage(" §eRMC: 2FA §cdisabled.");
							}
							else
							{
								gAuth.disable(false);
							}
						}
						else
						{
							sender.sendMessage(" §eRMC: &c2FA was already disabled!");
						}
					}
				}
				else if (args[0].equalsIgnoreCase("search")
						&& sender.hasPermission(ConfigHandler.getString("rmc.perm.logs.search")))
				{
					if (!(sender instanceof Player))
					{
						sender.sendMessage(" §4DO NOT EVER RUN THIS FROM CONSOLE!");
						return true;
					}
					StringBuilder regexBuilder = new StringBuilder();
					for (int i = 2; i < args.length; i++)
						regexBuilder.append(args[i]);
					int matches = LogHandler.search(sender, regexBuilder.toString(), args[1]);
					if (matches == -1)
					{
						sender.sendMessage(
								" §eRMC: §cSomething went wrong, the search returned -1... Please check your settings!");
					}
					else
					{
						sender.sendMessage(
								" §eRMC: Found " + (matches > 0 ? "§a" : "§c") + matches + " §ematches total.");
					}
				}
			}
		}
		catch (InvalidObjectException | NoSuchElementException e)
		{
			sender.sendMessage("§cSomething went wrong: " + e.getMessage());
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
