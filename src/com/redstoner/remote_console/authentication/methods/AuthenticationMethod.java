package com.redstoner.remote_console.authentication.methods;

import java.io.Serializable;
import java.util.UUID;

import com.redstoner.remote_console.protected_classes.Main;

/**
 * This class represents a parent for all AuthenticationMethods
 * 
 * @author Pepich
 */

public abstract class AuthenticationMethod implements Serializable
{
	private static final long serialVersionUID = 3934618157298913318L;
	
	protected final transient UUID uuid;
	
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
	 * This method is a default implementation of a register method that will force inheriting classes to implement it.
	 * This implementation is required due to static modifier.
	 */
	
	public static void register()
	{
		Main.logger
				.warning("Tried to register an AuthenticationMethod which does not properly implement registration.");
	}
	
	/**
	 * This constructor ensures that every AuthenticationMethod is assigned an owner
	 * 
	 * @param user the owner of this AuthenticationMethod
	 */
	
	public AuthenticationMethod(UUID uuid)
	{
		this.uuid = uuid;
	}
}
