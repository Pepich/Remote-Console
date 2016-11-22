package com.redstoner.remote_console.authentication.methods;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.UUID;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import com.redstoner.remote_console.protected_classes.Main;

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
	 * Invalidates the authentication method instantly and prevents it from being used
	 */
	public void invalidate()
	{
		valid = false;
		save();
	}
	
	/**
	 * Revalidates the password
	 */
	private void revalidate()
	{
		valid = true;
		save();
	}
	
	public boolean isValid()
	{
		return valid;
	}
}
