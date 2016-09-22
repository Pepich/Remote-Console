package com.redstoner.remote_console.authentication.methods;

import java.io.Serializable;

import com.redstoner.remote_console.protected_classes.Main;
import com.redstoner.remote_console.protected_classes.User;

/**
 * This class represents a parent for all AuthenticationMethods
 * 
 * @author Pepich
 */

public abstract class AuthenticationMethod implements Serializable
{
	private static final long serialVersionUID = 3934618157298913318L;
	
	private boolean enabled = false;
	
	protected final transient User owner;
	
	/**
	 * This method tries to authenticate the user owning this AuthenticationMethod with the given parameters
	 * 
	 * @param args the parameters to use for authentication
	 * @return weather the authentication was successful or not
	 */
	
	public abstract boolean authenticate(String[] args);
	
	/**
	 * Saves the authentication method to a file.
	 */
	
	public abstract void save();
	
	/**
	 * Loads the authentication method of the owner.
	 * 
	 * @return if the loading was successful
	 */
	
	protected abstract boolean load();
	
	/**
	 * This method will change the enabled status of an authentication method.
	 * 
	 * @param enabled weather to enable the authentication method or not.
	 */
	
	public void setEnabled(boolean enabled)
	{
		this.enabled = enabled;
	}
	
	/**
	 * This method will check if the authentication method is enabled for use by the user
	 * 
	 * @return weather the authentication method is enabled or not
	 */
	
	public boolean isEnabled()
	{
		return this.enabled;
	}
	
	/**
	 * This method is a default implementation of a register method that will force inheriting classes to implement it.
	 * This implementation is required due to static modifier.
	 */
	
	public static void register()
	{
		if (Main.testMode())
			System.err.println(
					"[WARNING] Tried to register an AuthenticationMethod which does not properly implement registration.");
		else
			Main.logger.warning(
					"Tried to register an AuthenticationMethod which does not properly implement registration.");
	}
	
	/**
	 * This constructor ensures that every AuthenticationMethod is assigned an owner
	 * 
	 * @param user the owner of this AuthenticationMethod
	 */
	
	public AuthenticationMethod(User user)
	{
		this.owner = user;
		if (!load()) init();
	}
	
	/**
	 * Initializes a new instance instead of loading it
	 * 
	 * @return true if the initialization was successful
	 */
	
	public abstract boolean init();
}
