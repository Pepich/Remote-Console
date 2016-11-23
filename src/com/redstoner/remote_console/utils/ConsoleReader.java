package com.redstoner.remote_console.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.layout.PatternLayout;

import com.redstoner.remote_console.protected_classes.Main;

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
		ConsoleBroadcaster cb = new ConsoleBroadcaster(event.getMessage().getFormattedMessage());
		cb.start();
	}
}
