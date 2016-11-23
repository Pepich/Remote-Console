package com.redstoner.remote_console.utils;

import com.redstoner.remote_console.protected_classes.UserManager;

public class ConsoleBroadcaster extends Thread
{
	final String message;
	boolean hasRun = false;
	
	public ConsoleBroadcaster(String message)
	{
		this.message = message;
	}
	
	@Override
	public void run()
	{
		if (!hasRun)
		{
			UserManager.broadcast(message, true);
		}
		hasRun = false;
		try
		{
			this.finalize();
		}
		catch (Throwable e)
		{}
	}
}
