package com.redstoner.remote_console.authentication.methods;

import java.util.UUID;

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
	
	private IngameAuthentication(UUID uuid)
	{
		super(uuid);
	}
	
	@Override
	public boolean authenticate(String[] args)
	{
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
		UserManager.registerAuthMethod("Ingame-Auth", IngameAuthentication.class);
	}
	
	/**
	 * This method is empty, as loading an input based AuthenticationMethod makes no sense.
	 * 
	 * @return true
	 */
	
	public static IngameAuthentication load(UUID uuid)
	{
		return new IngameAuthentication(uuid);
	}
}
