package com.redstoner.remote_console.authentication.methods;

import com.redstoner.remote_console.protected_classes.User;
import com.redstoner.remote_console.protected_classes.UserManager;

/**
 * This class represents the IngameAuthentication for IP and ingame confirmation based authentication
 * 
 * @author Pepich
 */

public class IngameAuthentication extends AuthenticationMethod
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 7482553379914872319L;
	
	public IngameAuthentication(User user)
	{
		super(user);
	}
	
	@Override
	public boolean authenticate(String[] args)
	{
		// TODO Add code to make confirmation request to user
		return false;
	}
	
	/**
	 * This method is empty, as saving an input based AuthenticationMethod makes no sense.
	 */
	
	@Override
	public void save()
	{
	
	}
	
	public static void register()
	{
		UserManager.register("Ingame-Auth", IngameAuthentication.class);
	}
	
	/**
	 * This method is empty, as loading an input based AuthenticationMethod makes no sense.
	 * 
	 * @return true
	 */
	
	@Override
	protected boolean load()
	{
		return false;
	}
	
	/**
	 * This method is empty, as initializing a new empty input based AuthenticationMethod makes no sense.
	 * 
	 * @return true
	 */
	
	@Override
	public boolean init()
	{
		return true;
	}
}
