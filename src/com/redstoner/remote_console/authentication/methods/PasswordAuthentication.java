package com.redstoner.remote_console.authentication.methods;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import com.redstoner.remote_console.protected_classes.Main;
import com.redstoner.remote_console.protected_classes.User;
import com.redstoner.remote_console.protected_classes.UserManager;

/**
 * This class represents the PasswordAuthentication to allow for username/password based authentication
 * 
 * @author Pepich
 */

public class PasswordAuthentication extends AuthenticationMethod
{
	private static final long serialVersionUID = -7251017035793213272L;
	
	private byte[] hashedPassword;
	private transient byte[] salt;
	
	public PasswordAuthentication(User user)
	{
		super(user);
		this.salt = user.getUUID().toString().getBytes();
	}
	
	protected int setPassword(String oldPassword, String newPassword, String newPasswordConfirmed)
	{
		try
		{
			if (newPassword.equals(newPasswordConfirmed))
			{
				if (hash(oldPassword, salt) == hashedPassword)
				{
					hashedPassword = hash(newPassword, salt);
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
	
	@Override
	public boolean authenticate(String[] args)
	{
		if (args.length == 0)
			return false;
		else
		{
			try
			{
				if (hash(args[0], salt) == hashedPassword)
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
		File saveFile = new File(owner.getSaveLocation() + "password.sav");
		if (Main.testMode()) saveFile.deleteOnExit();
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
	
	public static void register()
	{
		UserManager.register("Password-Auth", PasswordAuthentication.class);
	}
	
	@Override
	protected boolean load()
	{
		File saveFile = new File(owner.getSaveLocation() + "token.sav");
		if (!saveFile.exists())
			return false;
		else
		{
			try
			{
				ObjectInputStream saveStream = new ObjectInputStream(new FileInputStream(saveFile));
				PasswordAuthentication temp = (PasswordAuthentication) saveStream.readObject();
				if (temp != null)
				{
					this.hashedPassword = temp.hashedPassword;
					this.setEnabled(temp.isEnabled());
				}
				saveStream.close();
				return true;
			}
			catch (IOException | ClassNotFoundException e)
			{
				e.printStackTrace();
				return false;
			}
		}
	}
	
	@Override
	public boolean init()
	{
		hashedPassword = null;
		setEnabled(false);
		return true;
	}
	
	public static byte[] hash(String password, byte[] salt) throws NoSuchAlgorithmException, InvalidKeySpecException
	{
		String algorithm = "PBKDF2WithHmacSHA512";
		int derivedKeyLength = 512;
		int iterations = 10000;
		
		KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, derivedKeyLength);
		SecretKeyFactory f = SecretKeyFactory.getInstance(algorithm);
		return f.generateSecret(spec).getEncoded();
	}
}
