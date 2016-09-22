package com.redstoner.remote_console.protected_classes;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SealedObject;
import javax.crypto.SecretKey;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.redstoner.remote_console.authentication.methods.GoogleAuthentication;
import com.redstoner.remote_console.authentication.methods.IngameAuthentication;
import com.redstoner.remote_console.authentication.methods.PasswordAuthentication;
import com.redstoner.remote_console.authentication.methods.TokenAuthentication;

/**
 * This class represents a user in the remote console environment
 * 
 * @author Pepich
 */

public class User extends Thread
{
	private int status = 0;
	
	// Authentication Methods
	private TokenAuthentication tokenAuth;
	private GoogleAuthentication googleAuth;
	private IngameAuthentication ingameAuth;
	private PasswordAuthentication passwordAuth;
	
	private boolean isAuthenticated = false;
	
	// User properties
	private UUID uuid;
	private File saveFile;
	
	// Connection and data streams
	private final Socket connection;
	private final ObjectInputStream objectIn;
	private final ObjectOutputStream objectOut;
	
	// Ciphers object for encrypted communication
	private Ciphers ciphers;
	
	/**
	 * This constructor will create a new blank user that will load the user-data as soon as a username was provided.
	 * 
	 * @param connection the connection of the user with the server
	 * @throws IOException if something went wrong with the connection
	 */
	
	public User(Socket connection) throws IOException
	{
		// Set up the connection and data streams
		this.connection = connection;
		this.objectIn = new ObjectInputStream(connection.getInputStream());
		this.objectOut = new ObjectOutputStream(connection.getOutputStream());
	}
	
	/**
	 * This method will save the user data to the file system
	 */
	
	public void save()
	{
		tokenAuth.save();
		googleAuth.save();
		ingameAuth.save();
		passwordAuth.save();
	}
	
	/**
	 * This method will load the user data from the file system
	 */
	
	public void load()
	{
		this.tokenAuth = new TokenAuthentication(this);
		this.googleAuth = new GoogleAuthentication(this);
		this.ingameAuth = new IngameAuthentication(this);
		this.passwordAuth = new PasswordAuthentication(this);
	}
	
	/**
	 * This method returns the current authentication status of the user
	 */
	
	public boolean isAuthenticated()
	{
		return isAuthenticated;
	}
	
	/**
	 * This methods tries to authenticate a user with the given parameters
	 * 
	 * @param args the parameters to authenticate the user with
	 * @return if the authentication was successful
	 */
	
	public boolean authenticate(String[] args)
	{
		return isAuthenticated;
	}
	
	/**
	 * DEBUG ONLY! This method will force-authenticate a user. Only available in test-mode.
	 * 
	 * @return if the operation was successfull
	 * @throws UnsupportedOperationException when trying to force-authenticate a user with disabled test-mode
	 */
	
	protected boolean forceAuthenticate() throws UnsupportedOperationException
	{
		// Check if force-authentication is allowed
		if (Main.testMode())
			isAuthenticated = true;
		else
		{
			// Print warning message
			Main.logger
					.warning("Tried to force-authenticate a user when testmode was disabled. Disconnecting user now!");
			disconnect();
			throw new UnsupportedOperationException("Can not force-authenticate a user when not in test-mode.");
		}
		return true;
	}
	
	/**
	 * This method properly exits the connection of the user.
	 */
	
	protected void disconnect()
	{
		// Tell the main-loop to stop
		isRunning = false;
		try
		{
			connection.close();
		}
		catch (IOException e)
		{
			// Catch exception silently in case of user-initiated connection interrupt
		}
		// Save changes
		save();
		// Tell the UserManager about the disconnect
		UserManager.disconnect(this);
	}
	
	/**
	 * This method properly exits the connection of the user after sending the given message.
	 * 
	 * @param message the message to send
	 */
	
	protected void disconnect(String message)
	{
		sendMessage(message);
		disconnect();
	}
	
	/**
	 * This method will return the save-folder for this specific user as an absolute path
	 * 
	 * @return the save-folders location
	 */
	
	public String getSaveLocation()
	{
		return saveFile.getAbsolutePath();
	}
	
	public boolean isRunning = false;
	
	@Override
	public void run()
	{
		// Set self to running mode
		isRunning = true;
		
		/*
		 *  Connection status			| Expected message			| Answer
		 *  ----------------------------------------------------------------------------------------------------------
		 *  0 = Handshake				| "USR-CON-BGN"				| "SRV-REQ-RSA" (1)
		 * 		PLAINTEXT				|							|
		 *  ----------------------------------------------------------------------------------------------------------
		 *  1 = RSA integrity check		| "xxxxxUSR-RSA-BGNxxxxx"	| "SRV-REQ-AES" (2)
		 *  	RSA ENCRYPTED			|							|
		 *  -----------------------------------------------------------------------------------------------------------------
		 *  2 = AES key exchange		| <AES KEY>					| "SRV-REQ-USN" (3)
		 *  	RSA ENCRYPTED			|							|
		 *  -----------------------------------------------------------------------------------------------------------------
		 *  3 = Awaiting username		| <xxxxxusernamexxxxx>		| If username exists: "SRV-REQ-AUT" (4) or "SRV-REQ-IGA" (14)
		 *  	AES ENCRYPTED			|							| Else: "SRV-REQ-USN" (3)
		 *  ----------------------------------------------------------------------------------------------------------
		 *  4 = Awaiting authentication | <authentication>			| If authentication OK: "SRV-REQ-CMD" (6) or "SRV-REQ-2FA" (5)
		 *  	AES ENCRYPTED			|							| Else: "SRV-REQ-AUT" (4)
		 *  							|							| After three wrong attempts: disconnect()
		 *  ----------------------------------------------------------------------------------------------------------
		 *  5 = Awaiting 2FA			| <authentication>			| If authentication OK: "SRV-REQ-CMD" (6)
		 *  	AES ENCRYPTED			|							| Else: "SRV-REQ-2FA" (5)
		 *  							|							| After three wrong attempts: disconnect()
		 *  ----------------------------------------------------------------------------------------------------------
		 *  14 = Offering IGA			| <yes/no>					| If IGA was used: "SRV-REQ-CMD" (6)
		 *  	AES ENCRYPTED			|							| Else: "SRV-REQ-AUT" (4)
		 *  
		 *  6 = Authentication successful - awaiting commands. Further communication is AES ENCRYPTED.
		 *  
		 *  "xxxxx" resembles a five character long random sequence that in a best case scenario was generated cryptographically secure.
		 */
		
		int authAttempts = 0;
		while ((status != 5) && isRunning)
		{
			switch (status)
			{
			case 0:
				// Expecting PLAINTEXT "USR-CON-BGN"
				try
				{
					String input = (String) objectIn.readObject();
					if (input.equals("USR-CON-BGN"))
						status++;
					else
						disconnect("Unexpected message received, closing connection.");
				}
				catch (ClassNotFoundException | IOException e)
				{
					e.printStackTrace();
					disconnect("An unexpected exception occured, closing connection.");
				}
				break;
			case 1:
				// Expecting RSA ENCRYPTED "xxxxxUSR-RSA-BGNxxxxx"
				try
				{
					objectOut.writeObject("SRV-REQ-RSA");
					objectOut.flush();
					
					String input = (String) ((SealedObject) objectIn.readObject()).getObject(Ciphers.RSA_DECODE);
					if (input.substring(5, input.length() - 5).equals("USR-RSA-BGN"))
						status++;
					else
						disconnect("Unexpected message received, closing connection.");
				}
				catch (BadPaddingException | IOException | IllegalBlockSizeException | ClassNotFoundException
						| StringIndexOutOfBoundsException e)
				{
					e.printStackTrace();
					disconnect("An unexpected exception occured, closing connection.");
				}
				break;
			case 2:
				// Expecting RSA ENCRYPTED <AES KEY>
				try
				{
					objectOut.writeObject("SRV-REQ-AES");
					objectOut.flush();
					
					// Check if the key is a proper AES key and initialize the ciphers object
					this.ciphers = new Ciphers(
							(SecretKey) ((SealedObject) objectIn.readObject()).getObject(Ciphers.RSA_DECODE));
					status++;
				}
				catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException
						| InvalidAlgorithmParameterException | ClassNotFoundException | IllegalBlockSizeException
						| BadPaddingException | IOException e)
				{
					disconnect("An unexpected exception occured, closing connection.");
					e.printStackTrace();
				}
				break;
			case 3:
				// Expecting AES ENCRYPTED <xxxxxusernamexxxxx>
				try
				{
					objectOut.writeObject(new SealedObject("SRV-REQ-USN", ciphers.getNextAESEncode()));
					objectOut.flush();
					
					String input = ((String) ((SealedObject) objectIn.readObject())
							.getObject(ciphers.getNextAESDecode()));
					input = input.substring(5, input.length() - 5);
					uuid = getUUID(input);
					
					// Check if the user is authorized to view console
					if (UserManager.uuidExists(uuid))
					{
						Player p = Bukkit.getPlayer(uuid);
						if (p == null) status++;
						if (p.getAddress().getHostString().toString().equals(connection.getInetAddress().toString()))
							status = 14;
						else
							status++;
						load();
					}
				}
				catch (InvalidKeyException | ClassNotFoundException | IllegalBlockSizeException | BadPaddingException
						| InvalidAlgorithmParameterException | NoSuchAlgorithmException | NoSuchPaddingException
						| IOException | StringIndexOutOfBoundsException e)
				{
					e.printStackTrace();
					disconnect("An unexpected exception occured, closing connection.");
				}
				break;
			case 4:
				// Expecting AES ENCRYPTED <authentication>
				
				try
				{
					objectOut.writeObject(new SealedObject("SRV-REQ-AUT", ciphers.getNextAESEncode()));
					objectOut.flush();
					
					String input = (String) ((SealedObject) objectIn.readObject())
							.getObject(ciphers.getNextAESDecode());
					if (authenticate(new String[] { input }))
						if (googleAuth.isEnabled())
							status = 5;
						else
							status = 6;
					else
						authAttempts++;
					if (authAttempts == 3) disconnect("Too many wrong authentication attempts.");
				}
				catch (InvalidKeyException | ClassNotFoundException | IllegalBlockSizeException | BadPaddingException
						| InvalidAlgorithmParameterException | NoSuchAlgorithmException | NoSuchPaddingException
						| IOException e)
				{
					e.printStackTrace();
					disconnect("An unexpected exception occured, closing connection.");
				}
				break;
			case 5:
				// Expecting AES ENCRYPTED <2FA>
				break;
			case 14:
				// Excepting AES ENCRYPTED <yes/no>
				break;
			}
		}
		
		while (isRunning)
		{
			// TODO: Add command handling
			try
			{
				Thread.sleep(1);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * This method will send a message to the user at any given time, respecting the current encryption status between server and client.
	 * 
	 * @param message the message to be sent
	 */
	
	protected void sendMessage(String message)
	{
		// if status < 3 then there was no encrypted connection established yet -> send in plaintext. Else, send encrypted.
		if (status < 3)
		{
			try
			{
				objectOut.writeObject("MSG: " + message);
				objectOut.flush();
			}
			catch (IOException e)
			{
				e.printStackTrace();
				disconnect("An unexpected exception occured, closing connection.");
			}
		}
		else
		{
			try
			{
				objectOut.writeObject(new SealedObject("MSG: " + message, ciphers.getNextAESEncode()));
				objectOut.flush();
			}
			catch (InvalidKeyException | IllegalBlockSizeException | NoSuchAlgorithmException | NoSuchPaddingException
					| InvalidAlgorithmParameterException | IOException e)
			{
				e.printStackTrace();
				disconnect("An unexpected exception occured, closing connection.");
			}
		}
	}
	
	/**
	 * This method will look up the UUID of any given username.
	 * 
	 * @param username the username to look up the UUID of
	 * @return null if the user does not exist, the UUID if it does
	 */
	
	@Deprecated
	public static UUID getUUID(String username)
	{
		return Bukkit.getOfflinePlayer(username).getUniqueId();
	}
	
	public UUID getUUID()
	{
		return uuid;
	}
}
