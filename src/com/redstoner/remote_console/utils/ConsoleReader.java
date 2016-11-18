package com.redstoner.remote_console.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.layout.PatternLayout;

import com.redstoner.remote_console.protected_classes.Main;
import com.redstoner.remote_console.protected_classes.UserManager;

/**
 * This class is responsible for grabbing, processing and forwarding the console output to the UserManager.
 * 
 * @author Pepich1851
 */

public class ConsoleReader extends AbstractAppender
{
	/**
	 * Constructor, creates the log appender to read the console output.
	 */
	protected ConsoleReader()
	{
		super("Log4JAppender", null, PatternLayout.createLayout("[%d{HH:mm:ss} %level]: %msg", null, null, null, null),
				false);
	}
	
	/**
	 * Initialize the console reader, appends it to the logger.
	 */
	public static void init()
	{
		Main.logger.info("Initializing console reader.");
		Logger log = (Logger) LogManager.getRootLogger();
		ConsoleReader reader = new ConsoleReader();
		log.addAppender(reader);
	}
	
	/**
	 * Always running, return true.
	 */
	@Override
	public boolean isStarted()
	{
		return true;
	}
	
	/**
	 * Gets called when a logevent happens. Notifies the usermanager about the event.
	 */
	@Override
	public void append(LogEvent event)
	{
		UserManager.broadcast(event.getMessage().getFormattedMessage(), true);
		//		for (Player p : Bukkit.getOnlinePlayers())
		//		{
		//			p.sendMessage(resolveColors(event.getMessage().getFormattedMessage()));
		//		}
	}
	
	/**
	 * Debug only. Will resolve escape codes back to minecraft colors codes for ingame output
	 * 
	 * @param message the message to resolve
	 * @return the converted message
	 */
	@SuppressWarnings("unused")
	private String resolveColors(String message)
	{
		message = message.replace("[0;30;22m", "§0");
		message = message.replace("[0;34;22m", "§1");
		message = message.replace("[0;32;22m", "§2");
		message = message.replace("[0;36;22m", "§3");
		message = message.replace("[0;31;22m", "§4");
		message = message.replace("[0;35;22m", "§5");
		message = message.replace("[0;33;22m", "§6");
		message = message.replace("[0;37;22m", "§7");
		message = message.replace("[0;30;1m", "§8");
		message = message.replace("[0;34;1m", "§9");
		message = message.replace("[0;32;1m", "§a");
		message = message.replace("[0;36;1m", "§b");
		message = message.replace("[0;31;1m", "§c");
		message = message.replace("[0;35;1m", "§d");
		message = message.replace("[0;33;1m", "§e");
		message = message.replace("[0;37;1m", "§f");
		message = message.replace("[5m", "§k");
		message = message.replace("[21m", "§l");
		message = message.replace("[9m", "§m");
		message = message.replace("[4m", "§n");
		message = message.replace("[3m", "§o");
		
		message = message.replace("[m", "§r");
		
		return message;
	}
}
