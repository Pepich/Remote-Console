package com.redstoner.remote_console.authentication.methods;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.redstoner.remote_console.protected_classes.Main;
import com.redstoner.remote_console.protected_classes.User;
import com.redstoner.remote_console.protected_classes.UserManager;

/**
 * This class represents the tokenAuthentication to allow for one-time username/token authentication when no password is set
 * or the user has forgotten his password. Will force a password-override if used.
 * 
 * @author Pepich
 */

public class TokenAuthentication extends AuthenticationMethod
{
	private static final long serialVersionUID = -8791920149131750868L;
	String token;
	
	public TokenAuthentication(User user)
	{
		super(user);
	}
	
	@Override
	public boolean authenticate(String[] args)
	{
		if (args.length == 0)
			return false;
		else if (args[0].equals(token))
		{
			setEnabled(false);
			token = "";
			return true;
		}
		return false;
	}
	
	@Override
	public void save()
	{
		File saveFile = new File(owner.getSaveLocation() + "token.sav");
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
		UserManager.register("Token-Auth", TokenAuthentication.class);
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
				TokenAuthentication temp = (TokenAuthentication) saveStream.readObject();
				if (temp != null)
				{
					this.token = temp.token;
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
		token = "";
		setEnabled(false);
		return true;
	}
}
