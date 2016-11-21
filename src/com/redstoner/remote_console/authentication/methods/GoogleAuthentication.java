package com.redstoner.remote_console.authentication.methods;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.UUID;

import org.apache.commons.codec.binary.Base32;

import com.redstoner.remote_console.protected_classes.Main;
import com.redstoner.remote_console.utils.TOTP;

/**
 * This class represents the google authentication for 2FA
 * 
 * @author Pepich1851
 */

public class GoogleAuthentication extends AuthenticationMethod implements Serializable
{
	private static final long serialVersionUID = 5531038871418983654L;
	private String secretKey;
	private ArrayList<String> restoreKeys;
	private boolean enabled = false;
	
	private GoogleAuthentication(UUID uuid)
	{
		super(uuid);
		secretKey = getRandomSecretKey();
		for (int i = 0; i < 8; i++)
			restoreKeys.add(getRandomSecretKey());
		enabled = false;
	}
	
	@Override
	public boolean authenticate(String[] args)
	{
		if (args.length == 0)
			return false;
		else if (args[0].equals(TOTP.getTOTPCode(secretKey)))
			return true;
		else if (restoreKeys.remove(args[0]))
			return true;
		else
			return false;
	}
	
	@Override
	public void save()
	{
		File saveFile = new File(Main.getDataLocation().getAbsolutePath() + uuid.toString() + "/google-auth.auth");
		try
		{
			saveFile.createNewFile();
			ObjectOutputStream saveStream = new ObjectOutputStream(new FileOutputStream(saveFile));
			saveStream.writeObject(this);
			saveStream.flush();
			saveStream.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public static GoogleAuthentication load(UUID uuid)
	{
		File saveFile = new File(Main.getDataLocation().getAbsolutePath() + uuid.toString() + "/google-auth.auth");
		if (!saveFile.exists())
			return new GoogleAuthentication(uuid);
		else
		{
			try
			{
				ObjectInputStream saveStream = new ObjectInputStream(new FileInputStream(saveFile));
				GoogleAuthentication returnAuth = (GoogleAuthentication) saveStream.readObject();
				saveStream.close();
				return returnAuth;
			}
			catch (IOException | ClassNotFoundException e)
			{
				e.printStackTrace();
				return null;
			}
		}
	}
	
	public static String getRandomSecretKey()
	{
		SecureRandom random = new SecureRandom();
		byte[] bytes = new byte[20];
		random.nextBytes(bytes);
		Base32 base32 = new Base32();
		String secretKey = base32.encodeToString(bytes);
		// make the secret key more human-readable by lower-casing and
		// inserting spaces between each group of 4 characters
		return secretKey.toLowerCase().replaceAll("(.{4})(?=.{4})", "$1 ");
	}
	
	/**
	 * This method returns true if 2FA is enabled for the user.
	 * 
	 * @return true if enabled
	 */
	public boolean isEnabled()
	{
		return enabled;
	}
	
	/**
	 * Enables and if necessary initializes google authentication for the user.
	 * 
	 * @return true if a new key and a new set of restore keys were generated
	 */
	public boolean enable()
	{
		enabled = true;
		if (secretKey == null)
		{
			secretKey = getRandomSecretKey();
			restoreKeys = new ArrayList<String>();
			for (int i = 0; i < 8; i++)
				restoreKeys.add(getRandomSecretKey());
			return true;
		}
		return false;
	}
	
	/**
	 * Disables and if wanted clears google authentication for the user.
	 * 
	 * @param clear if set to true, existing data will be deleted
	 */
	public void disable(boolean clear)
	{
		if (clear)
		{
			secretKey = null;
			restoreKeys = null;
		}
		enabled = false;
	}
	
	/**
	 * This method provides a users restore keys.
	 * 
	 * @return the keys
	 */
	public ArrayList<String> getRestoreKeys()
	{
		return restoreKeys;
	}
	
	/**
	 * This method will provide the secret key of a user
	 * 
	 * @return the key
	 */
	public String getSecretKey()
	{
		return secretKey;
	}
}
