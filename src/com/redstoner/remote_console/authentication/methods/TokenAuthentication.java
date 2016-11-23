package com.redstoner.remote_console.authentication.methods;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.UUID;

import com.redstoner.remote_console.protected_classes.Main;
import com.redstoner.remote_console.utils.ConfigHandler;

/**
 * This class represents the tokenAuthentication to allow for one-time username/token authentication when no password is set
 * or the user has forgotten his password. Will force a password-override if used.
 * 
 * @author Pepich1851
 */

public class TokenAuthentication extends AuthenticationMethod implements Serializable
{
	private static final long serialVersionUID = -8791920149131750868L;
	
	// Complexity 1: 0-26; 2: 0-52; 3: 0-62; 4: 0-chars.length
	private static char[] chars = new char[] { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N',
			'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i',
			'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3',
			'4', '5', '6', '7', '8', '9', '!', '?', '_', '.', ',', '=', '/', '\\', '$', '%', '&', '(', ')', '[', ']',
			'{', '}', '@', '"', '+', '*', '#' };
	private String token = null;
	private boolean enabled = false;
	
	private TokenAuthentication(UUID uuid)
	{
		super(uuid);
		save();
	}
	
	@Override
	public boolean authenticate(String[] args)
	{
		if (args == null) return false;
		if (args.length == 0) return false;
		if (token == null)
			return false;
		else if (args[0].equals(token))
		{
			enabled = false;
			save();
			return true;
		}
		return false;
	}
	
	@Override
	public void save()
	{
		File saveFile = new File(Main.getDataLocation(), uuid.toString() + "/token-auth.auth");
		File saveFolder = new File(Main.getDataLocation(), uuid.toString());
		saveFolder.mkdirs();
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
	
	public static TokenAuthentication load(UUID uuid)
	{
		File saveFile = new File(Main.getDataLocation(), uuid.toString() + "/token-auth.auth");
		if (!saveFile.exists())
			return new TokenAuthentication(uuid);
		else
		{
			try
			{
				ObjectInputStream saveStream = new ObjectInputStream(new FileInputStream(saveFile));
				TokenAuthentication returnAuth = (TokenAuthentication) saveStream.readObject();
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
	
	public String getRandomToken()
	{
		try
		{
			int length = ConfigHandler.getInt("rmc.tokenlenght");
			int complexity = ConfigHandler.getInt("rmc.tokencomplexity");
			int upper = 0;
			switch (complexity)
			{
			case 1:
				upper = 26;
				break;
			case 2:
				upper = 52;
				break;
			case 3:
				upper = 62;
				break;
			case 4:
				upper = chars.length;
			}
			StringBuilder sb = new StringBuilder();
			Random random = new Random();
			for (int i = 1; i <= length; i++)
			{
				sb.append(chars[random.nextInt(upper)]);
				if (i % 4 == 0 && i != length) sb.append("-");
			}
			token = sb.toString();
			enabled = true;
			save();
		}
		catch (InvalidObjectException | NoSuchElementException e)
		{
			e.printStackTrace();
		}
		return token;
	}
	
	public boolean isEnabled()
	{
		return enabled;
	}
}
