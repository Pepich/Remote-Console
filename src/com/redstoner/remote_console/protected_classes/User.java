package com.redstoner.remote_console.protected_classes;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
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
import com.redstoner.remote_console.utils.ConfigHandler;

/** This class represents a user in the remote console environment
 * 
 * @author Pepich1851 */
public class User extends Thread
{
	private boolean isRunning = false;
	private int status = 0;
	private TokenAuthentication tokenAuth;
	private GoogleAuthentication googleAuth;
	private IngameAuthentication ingameAuth;
	private PasswordAuthentication passwordAuth;
	private boolean isAuthenticated = false;
	private UUID uuid;
	private File saveFile;
	private final Socket connection;
	private final ObjectInputStream objectIn;
	private final ObjectOutputStream objectOut;
	private Ciphers ciphers;
	private FakePlayer player;
	private boolean disconnecting = false;
	boolean registered = false;
	
	/** This constructor will create a new blank user that will load the user-data as soon as a username was provided.
	 * 
	 * @param connection the connection of the user with the server
	 * @throws IOException if something went wrong with the connection */
	public User(Socket connection) throws IOException
	{
		Main.logger.info("New client connected with IP: " + connection.getInetAddress());
		this.connection = connection;
		connection.getOutputStream().flush();
		this.objectOut = new ObjectOutputStream(connection.getOutputStream());
		objectOut.flush();
		this.objectIn = new ObjectInputStream(connection.getInputStream());
	}
	
	/** This method will save the user data to the file system */
	public void save()
	{
		if (tokenAuth != null)
			tokenAuth.save();
		if (googleAuth != null)
			googleAuth.save();
		if (ingameAuth != null)
			ingameAuth.save();
		if (passwordAuth != null)
			passwordAuth.save();
	}
	
	/** This method will load the user data from the file system */
	public void load()
	{
		Main.logger.info("Loading userdata of uuid " + uuid);
		this.tokenAuth = TokenAuthentication.load(uuid);
		this.googleAuth = GoogleAuthentication.load(uuid);
		this.ingameAuth = IngameAuthentication.load(uuid);
		this.passwordAuth = PasswordAuthentication.load(uuid);
	}
	
	/** This method returns the current authentication status of the user */
	public boolean isAuthenticated()
	{
		return isAuthenticated;
	}
	
	/** This method properly exits the connection of the user. */
	protected void disconnect()
	{
		// Tell the UserManager about the disconnect
		UserManager.disconnect(this);
		// Save changes
		save();
		// Send AC message and destroy the player object
		if (player != null)
		{
			player.performCommand("ac I'm no longer on console :(");
			player.delete();
		}
		player = null;
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
	}
	
	/** This method properly exits the connection of the user after sending the given message.
	 * 
	 * @param message the message to send */
	protected void disconnect(String message)
	{
		isRunning = false;
		if (!disconnecting)
		{
			disconnecting = true;
			sendMessage(message);
		}
		disconnect();
	}
	
	/** This method will return the save-folder for this specific user as an absolute path
	 * 
	 * @return the save-folders location */
	public String getSaveLocation()
	{
		return saveFile.getAbsolutePath();
	}
	
	/** This method will start the user thread in an asynchronous manner to deal with the connection. It will authenticate the user and then process the user inputs. */
	@SuppressWarnings("deprecation")
	@Override
	public void run()
	{
		isRunning = true;
		/* Connection status | Expected message | Answer
		 * ----------------------------------------------------------------------------------------------------------
		 * 0 = Handshake | "USR-CON-BGN" | "SRV-REQ-RSA" (1)
		 * PLAINTEXT | |
		 * ----------------------------------------------------------------------------------------------------------
		 * 1 = RSA integrity check | "xxxxxUSR-RSA-BGNxxxxx" | "SRV-REQ-AES" (2)
		 * RSA ENCRYPTED | |
		 * -----------------------------------------------------------------------------------------------------------------
		 * 2 = AES key exchange | <AES KEY> | "SRV-REQ-USN" (3)
		 * RSA ENCRYPTED | |
		 * -----------------------------------------------------------------------------------------------------------------
		 * 3 = Awaiting username | <xxxxxusernamexxxxx> | If username exists: "SRV-REQ-AUT" (4) or "SRV-REQ-IGA" (14) or "USR-NO-AUT" (6)
		 * AES ENCRYPTED | | Else: "SRV-REQ-USN" (3)
		 * ----------------------------------------------------------------------------------------------------------
		 * 4 = Awaiting authentication | <authentication> | If authentication OK: "SRV-REQ-CMD" (6) or "SRV-REQ-2FA" (5)
		 * AES ENCRYPTED | | Else: "SRV-REQ-AUT" (4)
		 * | | After three wrong attempts: disconnect()
		 * ----------------------------------------------------------------------------------------------------------
		 * 5 = Awaiting 2FA | <authentication> | If authentication OK: "SRV-REQ-CMD" (6)
		 * AES ENCRYPTED | | Else: "SRV-REQ-2FA" (5)
		 * | | After three wrong attempts: disconnect()
		 * ----------------------------------------------------------------------------------------------------------
		 * 14 = Offering IGA | <yes/no> | If IGA was used: "SRV-REQ-CMD" (6)
		 * AES ENCRYPTED | | Else: "SRV-REQ-AUT" (4)
		 * 6 = Authentication successful - awaiting commands. Further communication is AES ENCRYPTED.
		 * "xxxxx" resembles a five character long random sequence that is supposed to be generated through a cryptographic secure algorithm */
		int authAttempts = 0;
		while ((status != 6) && isRunning)
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
						{
							disconnect("Unexpected message received, closing connection.");
							return;
						}
					}
					catch (ClassNotFoundException | IOException e)
					{
						disconnect("An unexpected exception occured, closing connection.");
						return;
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
						{
							disconnect("Unexpected message received, closing connection.");
							return;
						}
					}
					catch (BadPaddingException | IOException | IllegalBlockSizeException | ClassNotFoundException
							| StringIndexOutOfBoundsException e)
					{
						disconnect("An unexpected exception occured, closing connection.");
						return;
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
						return;
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
						uuid = Bukkit.getOfflinePlayer(input).getUniqueId();
						if (uuid == null)
						{
							disconnect("Username can not be empty");
							return;
						}
						// Check if the user is authorized to view console
						if (UserManager.mayConnect(uuid))
						{
							// After having received the UUID and confirmed that the user is allowed to be authorized, invoke the load() method to load the authorization methods
							load();
							Player p = Bukkit.getPlayer(uuid);
							if (p == null)
							{
								status++;
								break;
							}
							if (ConfigHandler.getBoolean("rmc.ignauth.checkIP"))
								if (p.getAddress().getHostString().toString()
										.equals(connection.getInetAddress().getHostAddress()))
									status = 14;
								else
									status++;
							else
								status = 14;
							if (!UserManager.mayAuthenticate(uuid))
							{
								objectOut.writeObject(new SealedObject("USR-NO-AUT", ciphers.getNextAESEncode()));
								objectOut.flush();
								status = 6;
								break;
							}
						}
						else
						{
							disconnect(
									"No user with the required permissions to connect to the server could be found!");
							return;
						}
					}
					catch (InvalidKeyException | ClassNotFoundException | IllegalBlockSizeException
							| BadPaddingException | InvalidAlgorithmParameterException | NoSuchAlgorithmException
							| NoSuchPaddingException | IOException | StringIndexOutOfBoundsException e)
					{
						disconnect("An unexpected exception occured, closing connection.");
						return;
					}
					break;
				case 4:
					// Expecting AES ENCRYPTED <authentication>
					try
					{
						objectOut.writeObject(new SealedObject("SRV-REQ-AUT", ciphers.getNextAESEncode()));
						objectOut.flush();
						String type = tokenAuth.isEnabled() ? "TKN" : "PWD";
						objectOut.writeObject(new SealedObject("SRV-REQ-" + type, ciphers.getNextAESEncode()));
						objectOut.flush();
						String input = (String) ((SealedObject) objectIn.readObject())
								.getObject(ciphers.getNextAESDecode());
						boolean auth = false;
						if (type.equals("TKN"))
						{
							if (tokenAuth.authenticate(new String[] {input}))
							{
								for (int result = 0; result != 1;)
								{
									objectOut.writeObject(new SealedObject("SRV-REQ-PWO", ciphers.getNextAESEncode()));
									objectOut.flush();
									String[] input2 = (String[]) ((SealedObject) objectIn.readObject())
											.getObject(ciphers.getNextAESDecode());
									if (input2.length != 2)
										continue;
									result = passwordAuth.overridePassword(input2[0], input2[1]);
									if (result == -2)
									{
										sendCmdResult("An unexpected error has occured. Please try again later.");
										continue;
									}
									if (result == 0)
										continue;
								}
								status = 6;
							}
							continue;
						}
						else
						{
							if (passwordAuth != null)
								auth = passwordAuth.authenticate(new String[] {input});
							else
							{
								disconnect("Couldn't find a working authentication method.");
								return;
							}
						}
						if (auth)
						{
							authAttempts = 0;
							if (googleAuth.isEnabled())
								status = 5;
							else
								status = 6;
						}
						else
						{
							authAttempts++;
						}
						if (authAttempts == 3)
						{
							disconnect("Too many invalid authentication attempts!");
							return;
						}
					}
					catch (InvalidKeyException | ClassNotFoundException | IllegalBlockSizeException
							| BadPaddingException | InvalidAlgorithmParameterException | NoSuchAlgorithmException
							| NoSuchPaddingException | IOException e)
					{
						disconnect("An unexpected exception occured, closing connection.");
						return;
					}
					break;
				case 5:
					try
					{
						objectOut.writeObject(new SealedObject("SRV-REQ-2FA", ciphers.getNextAESEncode()));
						objectOut.flush();
						String[] input = ((String[]) ((SealedObject) objectIn.readObject())
								.getObject(ciphers.getNextAESDecode()));
						if (googleAuth.authenticate(input))
						{
							authAttempts = 0;
						}
						else
						{
							authAttempts++;
						}
						if (authAttempts == 3)
						{
							disconnect("Too many invalid authentication attempts!");
							return;
						}
					}
					catch (InvalidKeyException | ClassNotFoundException | IllegalBlockSizeException
							| BadPaddingException | InvalidAlgorithmParameterException | NoSuchAlgorithmException
							| NoSuchPaddingException | IOException e)
					{
						disconnect("An unexpected exception occured, closing connection.");
						return;
					}
					break;
				case 14:
					try
					{
						objectOut.writeObject(new SealedObject("SRV-REQ-AUT", ciphers.getNextAESEncode()));
						objectOut.flush();
						objectOut.writeObject(new SealedObject("SRV-REQ-IGA", ciphers.getNextAESEncode()));
						objectOut.flush();
						String input = (String) (((SealedObject) objectIn.readObject())
								.getObject(ciphers.getNextAESDecode()));
						if (input.equals("yes"))
						{
							if (ingameAuth.authenticate(null))
								status = 6;
							else
							{
								status = 4;
							}
						}
						else if (input.equals("no"))
						{
							status = 4;
						}
					}
					catch (InvalidKeyException | ClassNotFoundException | IllegalBlockSizeException
							| BadPaddingException | InvalidAlgorithmParameterException | NoSuchAlgorithmException
							| NoSuchPaddingException | IOException e)
					{
						disconnect("An unexpected exception occured, closing connection.");
						return;
					}
					break;
			}
		}
		player = new FakePlayer(uuid, Bukkit.getOfflinePlayer(uuid), UserManager.getLastDisplayName(uuid), this);
		player.performCommand("ac I'm now on console :D");
		if (!passwordAuth.isValid())
		{
			try
			{
				for (int result = 0; result != 1;)
				{
					objectOut.writeObject(new SealedObject("SRV-REQ-PWO", ciphers.getNextAESEncode()));
					objectOut.flush();
					String[] input = ((String[]) ((SealedObject) objectIn.readObject())
							.getObject(ciphers.getNextAESDecode()));
					result = passwordAuth.changePassword(input[0], input[1], input[2]);
					if (result == -2)
					{
						disconnect("An unexpected exception occured, closing connection.");
						return;
					}
				}
			}
			catch (InvalidKeyException | IllegalBlockSizeException | NoSuchAlgorithmException | NoSuchPaddingException
					| InvalidAlgorithmParameterException | IOException | ClassNotFoundException
					| BadPaddingException e1)
			{
				disconnect("An unexpected exception occured, closing connection.");
				return;
			}
		}
		try
		{
			objectOut.writeObject(new SealedObject("SRV-REQ-CMD", ciphers.getNextAESEncode()));
			objectOut.flush();
		}
		catch (InvalidKeyException | IllegalBlockSizeException | NoSuchAlgorithmException | NoSuchPaddingException
				| InvalidAlgorithmParameterException | IOException e1)
		{
			disconnect("An unexpected exception occured, closing connection.");
			return;
		}
		isAuthenticated = true;
		while (isRunning)
		{
			try
			{
				String input = (String) ((SealedObject) objectIn.readObject()).getObject(ciphers.getNextAESDecode());
				if (input.startsWith("MSG:"))
					player.compute(input.replaceFirst("MSG:", "").replaceAll("  ", " ").trim());
				if (input.startsWith("CMD:"))
					sendCmdResult(compute(input.replaceFirst("CMD:", "").replaceAll("  ", " ").trim()));
			}
			catch (InvalidKeyException | IllegalBlockSizeException | NoSuchAlgorithmException | NoSuchPaddingException
					| InvalidAlgorithmParameterException | IOException | ClassNotFoundException | BadPaddingException e)
			{
				disconnect("An unexpected exception occured, closing connection.");
				return;
			}
		}
		disconnect("Execution was stopped.");
		return;
	}
	
	/** This method will compute a given input string after the user has been authenticated.
	 * 
	 * @param input the user input
	 * @return the result of the process */
	private String compute(String input)
	{
		if (input.equals("test"))
		{
			return "Test successfull :D";
		}
		else if (input.equals("enable2FA"))
		{
			player.compute("/console 2fa enable");
			return "Ran \"/console 2fa enable";
		}
		else if (input.equals("disable2FA"))
		{
			player.compute("/console 2fa disable");
			return "Ran \"/console 2fa disable";
		}
		else if (input.equals("get2FAsecret"))
		{
			player.compute("/console 2fa-restore");
			return "Ran \"/console 2fa-restore";
		}
		else if (input.equals("help"))
		{
			return "Chat and run commands as you would normally do, use the navbar to run special commands :)";
		}
		else if (input.startsWith("cgpass"))
		{
			String[] args = input.split(" ");
			if (args.length == 1)
			{
				try
				{
					objectOut.writeObject(new SealedObject("SRV-REQ-PWC", ciphers.getNextAESEncode()));
					objectOut.flush();
				}
				catch (InvalidKeyException | IllegalBlockSizeException | NoSuchAlgorithmException
						| NoSuchPaddingException | InvalidAlgorithmParameterException | IOException e)
				{
					disconnect("An unexpected exception occured, closing connection.");
				}
				return null;
			}
			if (args.length < 4)
				return "Not enough parameters specified";
			if (args.length > 4)
				return "Too many parameters specified";
			int result = passwordAuth.changePassword(args[1], args[2], args[3]);
			if (result == -2)
				return "An unexpected error has occured. Please try again later.";
			if (result == -1)
				return "You have entered the wrong password. Please try again.";
			if (result == 0)
				return "The passwords you entered do not match. Please try again.";
			if (result == 1)
				return "Your password has been changed successfully.";
		}
		else if (input.equals("exit"))
		{
			disconnect("Bye o/");
		}
		return null;
	}
	
	/** This method will send a command result to the client.
	 * 
	 * @param message the message to be sent */
	private synchronized void sendCmdResult(String message)
	{
		if (message == null)
			return;
		try
		{
			objectOut.writeObject(new SealedObject("CMD: " + message, ciphers.getNextAESEncode()));
			objectOut.flush();
		}
		catch (InvalidKeyException | IllegalBlockSizeException | NoSuchAlgorithmException | NoSuchPaddingException
				| InvalidAlgorithmParameterException | IOException e)
		{
			disconnect("An unexpected exception occured, closing connection.");
		}
	}
	
	/** This method will send a message to the user at any given time, respecting the current encryption status between server and client.
	 * 
	 * @param message the message to be sent */
	protected synchronized void sendMessage(String message)
	{
		// If status < 3 then there was no encrypted connection established yet -> send in plain text. Else, send encrypted.
		if (status < 3)
		{
			try
			{
				objectOut.writeObject("MSG: " + message);
				objectOut.flush();
			}
			catch (IOException e)
			{
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
				disconnect("An unexpected exception occured, closing connection.");
			}
		}
	}
	
	/** @return the uuid of the user */
	public UUID getUUID()
	{
		return uuid;
	}
	
	/** @return the IP address of the connection */
	public InetAddress getIP()
	{
		return connection.getInetAddress();
	}
	
	/** @return whether the connection is still alive or not. */
	public boolean isConnected()
	{
		return connection.isConnected();
	}
}
