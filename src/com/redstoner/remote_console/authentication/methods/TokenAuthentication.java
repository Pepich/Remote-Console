package com.redstoner.remote_console.authentication.methods;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.UUID;

import com.redstoner.remote_console.protected_classes.Main;
import com.redstoner.remote_console.protected_classes.UserManager;

/**
 * This class represents the tokenAuthentication to allow for one-time username/token authentication when no password is set
 * or the user has forgotten his password. Will force a password-override if used.
 * 
 * @author Pepich1851
 */

public class TokenAuthentication extends AuthenticationMethod
{
	private static final long serialVersionUID = -8791920149131750868L;
	private final String token;
	
	private TokenAuthentication(UUID uuid)
	{
		super(uuid);
		this.token = getRandomToken();
	}
	
	@Override
	public boolean authenticate(String[] args)
	{
		if (args.length == 0)
			return false;
		else if (args[0].equals(token))
		{
			File saveFile = new File(Main.getDataLocation().getAbsolutePath() + uuid.toString() + "/token-auth.auth");
			saveFile.delete();
			return true;
		}
		return false;
	}
	
	@Override
	public void save()
	{
		File saveFile = new File(Main.getDataLocation().getAbsolutePath() + uuid.toString() + "/token-auth.auth");
		File saveFolder = new File(Main.getDataLocation().getAbsolutePath() + uuid.toString());
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
	
	public static void register()
	{
		UserManager.registerAuthMethod("Token-Auth", TokenAuthentication.class);
	}
	
	public static TokenAuthentication load(UUID uuid)
	{
		File saveFile = new File(Main.getDataLocation().getAbsolutePath() + uuid.toString() + "/token-auth.auth");
		if (!saveFile.exists())
			return null;
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
	
	public static String getRandomToken()
	{
		return "";
	}
}
