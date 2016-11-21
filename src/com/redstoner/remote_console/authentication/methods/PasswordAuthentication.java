package com.redstoner.remote_console.authentication.methods;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.UUID;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import com.redstoner.remote_console.protected_classes.Main;
import com.redstoner.remote_console.utils.ConfigHandler;

/**
 * This class represents the PasswordAuthentication to allow for username/password based authentication
 * 
 * @author Pepich1851
 */

public class PasswordAuthentication extends AuthenticationMethod implements Serializable
{
	private static final long serialVersionUID = -7251017035793213272L;
	
	private byte[] hashedPassword;
	private byte[] salt;
	
	private long expires;
	private boolean valid;
	
	private PasswordAuthentication(UUID uuid)
	{
		super(uuid);
		this.salt = uuid.toString().getBytes();
	}
	
	/**
	 * This method will try to change a user's password
	 * 
	 * @param oldPassword the current password of the user
	 * @param newPassword the new password of the user
	 * @param newPasswordConfirmed a confirmation of the user's new password for security reasons
	 * @return -2 if an exception was thrown, -1 if the oldPassword is wrong, 0 if the new passwords don't match and 1 if the operation was successful
	 */
	public int changePassword(String oldPassword, String newPassword, String newPasswordConfirmed)
	{
		try
		{
			if (newPassword.equals(newPasswordConfirmed))
			{
				if (Arrays.toString(hash(oldPassword, salt)).equals(Arrays.toString(hashedPassword)))
				{
					hashedPassword = hash(newPassword, salt);
					revalidate();
					return 1;
				}
				else
					return -1;
			}
			else
				return 0;
		}
		catch (NoSuchAlgorithmException | InvalidKeySpecException e)
		{
			return -2;
		}
	}
	
	/**
	 * This method will forcefully override a user's password.
	 * 
	 * @param oldPassword the current password of the user
	 * @param newPassword the new password of the user
	 * @param newPasswordConfirmed a confirmation of the user's new password for security reasons
	 * @return -2 if an exception was thrown, 0 if the new passwords don't match and 1 if the operation was successful
	 */
	public int overridePassword(String newPassword, String newPasswordConfirmed)
	{
		try
		{
			if (newPassword.equals(newPasswordConfirmed))
			{
				hashedPassword = hash(newPassword, salt);
				revalidate();
				return 1;
			}
			else
				return 0;
		}
		catch (NoSuchAlgorithmException | InvalidKeySpecException e)
		{
			return -2;
		}
	}
	
	@Override
	public boolean authenticate(String[] args)
	{
		if (args.length == 0)
			return false;
		else
		{
			try
			{
				if (Arrays.toString(hash(args[0], salt)).equals(Arrays.toString(hashedPassword)))
					return true;
				else
					return false;
			}
			catch (NoSuchAlgorithmException | InvalidKeySpecException e)
			{
				e.printStackTrace();
				return false;
			}
		}
	}
	
	@Override
	public void save()
	{
		File saveFile = new File(Main.getDataLocation().getAbsolutePath(), uuid.toString() + "/password-auth.auth");
		try
		{
			if (saveFile.exists()) saveFile.delete();
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
	
	/**
	 * Loads the password authentication of a player using their UUID.
	 * 
	 * @param uuid the UUID of the user
	 * @return their password authentication
	 */
	public static PasswordAuthentication load(UUID uuid)
	{
		File saveFile = new File(Main.getDataLocation().getAbsolutePath(), uuid.toString() + "/password-auth.auth");
		if (!saveFile.exists())
			return new PasswordAuthentication(uuid);
		else
		{
			try
			{
				ObjectInputStream loadStream = new ObjectInputStream(new FileInputStream(saveFile));
				PasswordAuthentication returnAuth = (PasswordAuthentication) loadStream.readObject();
				loadStream.close();
				return returnAuth;
			}
			catch (IOException | ClassNotFoundException e)
			{
				e.printStackTrace();
				return null;
			}
		}
	}
	
	public static byte[] hash(String password, byte[] salt) throws NoSuchAlgorithmException, InvalidKeySpecException
	{
		String algorithm = "PBKDF2WithHmacSHA512";
		int derivedKeyLength = 512;
		int iterations = 50000;
		KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, derivedKeyLength);
		SecretKeyFactory f = SecretKeyFactory.getInstance(algorithm);
		byte[] result = f.generateSecret(spec).getEncoded();
		return result;
	}
	
	/**
	 * Expires the authentication method instantly and forces the user to define a new password upon new login
	 */
	public void expire()
	{
		expires = 0;
	}
	
	/**
	 * @return true if the password has expired ans is required to be changed
	 */
	public boolean isExpired()
	{
		return expires != 0 ? System.currentTimeMillis() > expires : false;
	}
	
	/**
	 * Invalidates the authentication method instantly and prevents it from being used
	 */
	public void invalidate()
	{
		valid = false;
	}
	
	/**
	 * Revalidates the password and calculates a new expiration date.
	 */
	private void revalidate()
	{
		valid = true;
		expires = calculateExpirationDate(System.currentTimeMillis());
	}
	
	/**
	 * Calculates the expiration date for a password using the configuration file and the provided time in millis
	 * 
	 * @param now the current time in millis
	 * @return the expiration date in millis
	 */
	public static long calculateExpirationDate(long now)
	{
		try
		{
			String raw = ConfigHandler.getString("rmc.pwexpire");
			try
			{
				int i = Integer.valueOf(raw);
				if (i == -1)
					return 0;
				else
					return now + i * 1000;
			}
			catch (NumberFormatException e)
			{
				StringBuilder sb = new StringBuilder();
				long time = 0;
				for (int i = 0; i < raw.length(); i++)
				{
					char c = raw.charAt(i);
					if (("" + c).matches("[0-9]"))
						sb.append(c);
					else
					{
						long factor = getFactor(c);
						try
						{
							int value = Integer.valueOf(sb.toString());
							time += value * factor;
						}
						catch (NumberFormatException e2)
						{
							return -1;
						}
						
						sb = new StringBuilder();
					}
				}
				return now + time;
			}
		}
		catch (InvalidObjectException | NoSuchElementException e)
		{
			e.printStackTrace();
		}
		return -1l;
	}
	
	public boolean isValid()
	{
		return valid;
	}
	
	public static long getFactor(char c)
	{
		switch (c)
		{
		case 'S':
			return 1l;
		case 's':
			return 1000l;
		case 'm':
			return 60000l;
		case 'h':
		case 'H':
			return 3600000l;
		case 'd':
		case 'D':
			return 86400000l;
		case 'w':
		case 'W':
			return 604800000l;
		case 'M':
			return 2592000000l;
		case 'y':
		case 'Y':
			return 31536000000l;
		default:
			return 0;
		}
	}
}
