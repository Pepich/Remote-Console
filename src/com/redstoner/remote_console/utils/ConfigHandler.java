package com.redstoner.remote_console.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InvalidObjectException;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Properties;

import com.redstoner.remote_console.protected_classes.Main;

/**
 * This class is responsible for loading the config and dealing with launch parameters.
 * It will not repair missing values , but silently use the default values instead.
 * If the rmc.repOTG value is set to true, it will repair invalid values it encounters by at first, trying to make sense out of the invalid value and if that doesn't work, restore it with the default value.
 * If rmc.repOTG is not set, it will throw an error and load the default value, but it will not touch the config file.
 * 
 * @author Pepich1851
 */

public class ConfigHandler
{
	private static final File propertiesFile = new File("plugins/rmc-config.properties");
	private static final Properties defaultProps = new Properties();
	private static final Properties properties = new Properties(defaultProps);
	
	private static boolean hasBeenModified = false;
	private static boolean repairOTG = true;
	
	static
	{
		try
		{
			InputStreamReader defaultIn = new InputStreamReader(
					ConfigHandler.class.getResourceAsStream("/defaultconfig.properties"));
			defaultProps.load(defaultIn);
			defaultIn.close();
			FileInputStream customIn = new FileInputStream(propertiesFile);
			properties.load(customIn);
			customIn.close();
			try
			{
				repairOTG = getBoolean("rmc.rotg");
			}
			catch (NoSuchElementException | InvalidObjectException e)
			{
			
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * To be called when the plugin gets disabled. Saves the config if it has been modified.
	 */
	public static void disable()
	{
		if (hasBeenModified)
		{
			try
			{
				FileOutputStream customOut = new FileOutputStream(propertiesFile);
				properties.store(customOut, "");
				customOut.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Reads an integer from the config file.
	 * 
	 * @param path the path to the parameter to be read.
	 * @return the int.
	 * @throws NoSuchElementException if the specified path could not be found
	 * @throws InvalidObjectException if the object from the config could not be deserialized into an int
	 */
	public static int getInt(String path) throws NoSuchElementException, InvalidObjectException
	{
		if (properties.getProperty(path) == null) throw new NoSuchElementException("Could not find object at " + path);
		try
		{
			int i = Integer.valueOf(properties.getProperty(path));
			return i;
		}
		catch (NumberFormatException e)
		{
			if (repairOTG)
			{
				Main.logger.warning("Found invalid value at " + path + ", attempting to restore the default value.");
				properties.setProperty(path, defaultProps.getProperty(path));
				hasBeenModified = true;
				try
				{
					int i = Integer.valueOf(properties.getProperty(path));
					return i;
				}
				catch (NumberFormatException e2)
				{
					throw new InvalidObjectException(
							"The value found at " + path + "could not be deserialized into an int.");
				}
			}
			else
				throw new InvalidObjectException(
						"The value found at " + path + "could not be deserialized into an int.");
		}
	}
	
	/**
	 * Reads a boolean from the config file.
	 * 
	 * @param path the path to the parameter to be read.
	 * @return the boolean.
	 * @throws NoSuchElementException if the specified path could not be found
	 * @throws InvalidObjectException if the object from the config could not be deserialized into a boolean
	 */
	public static boolean getBoolean(String path) throws NoSuchElementException, InvalidObjectException
	{
		if (properties.getProperty(path) == null) throw new NoSuchElementException("Could not find object at " + path);
		String raw = properties.getProperty(path);
		if (raw.equalsIgnoreCase("true") || raw.equalsIgnoreCase("t") || raw.equalsIgnoreCase("yes")
				|| raw.equalsIgnoreCase("y") || raw.equals("1") || raw.equals("+"))
			return true;
		if (raw.equalsIgnoreCase("false") || raw.equalsIgnoreCase("f") || raw.equalsIgnoreCase("no")
				|| raw.equalsIgnoreCase("n") || raw.equals("0") || raw.equals("-"))
			return false;
		else
		{
			if (repairOTG)
			{
				Main.logger.warning("Found invalid value at " + path + ", attempting to restore the default value.");
				raw = defaultProps.getProperty(path);
				properties.setProperty(path, raw);
				hasBeenModified = true;
				if (raw.equalsIgnoreCase("true") || raw.equalsIgnoreCase("t") || raw.equalsIgnoreCase("yes")
						|| raw.equalsIgnoreCase("y") || raw.equals("1") || raw.equals("+"))
					return true;
				if (raw.equalsIgnoreCase("false") || raw.equalsIgnoreCase("f") || raw.equalsIgnoreCase("no")
						|| raw.equalsIgnoreCase("n") || raw.equals("0") || raw.equals("-"))
					return false;
				else
					throw new InvalidObjectException(
							"The value found at " + path + "could not be deserialized into an int.");
			}
			else
				throw new InvalidObjectException(
						"The value found at " + path + "could not be deserialized into an int.");
		}
		
	}
	
	/**
	 * Reads a String from the config file.
	 * 
	 * @param path the path to the parameter to be read.
	 * @return the String.
	 * @throws NoSuchElementException if the specified path could not be found
	 * @throws InvalidObjectException if the object from the config could not be deserialized into a String
	 */
	public static String getString(String path) throws NoSuchElementException, InvalidObjectException
	{
		if (properties.getProperty(path) == null) throw new NoSuchElementException("Could not find object at " + path);
		return properties.getProperty(path);
	}
	
	/**
	 * Reads an Array of String from the config file. Invokes getStringArray(path, (char)0);.
	 * 
	 * @param path the path to the parameter to be read.
	 * @return the Array of String.
	 * @throws NoSuchElementException if the specified path could not be found
	 * @throws InvalidObjectException if the object from the config could not be deserialized into an Array of String
	 */
	public static String[] getStringArray(String path) throws NoSuchElementException, InvalidObjectException
	{
		return getStringArray(path, (char) 0);
	}
	
	/**
	 * Reads an Array of String from the config file.
	 * 
	 * @param path the path to the parameter to be read.
	 * @param startingChar a char to be expected in front of the array.
	 * @return the Array of String.
	 * @throws NoSuchElementException if the specified path could not be found
	 * @throws InvalidObjectException if the object from the config could not be deserialized into an Array of String
	 */
	public static String[] getStringArray(String path, char startingChar)
			throws NoSuchElementException, InvalidObjectException
	{
		String raw = properties.getProperty(path);
		if (raw == null) throw new NoSuchElementException("Could not find object at " + path);
		if (startingChar != 0)
		{
			if (raw.startsWith("" + startingChar))
				raw = raw.substring(1);
			else
				throw new InvalidObjectException("The value found at " + path
						+ " could not be deserialized into an Array of String. Error at char 1, expected "
						+ startingChar + " but found '" + raw.charAt(0) + "'.");
		}
		if (raw.startsWith("[") && raw.endsWith("]"))
		{
			boolean error = false;
			CharSequence[] expected = null;
			ArrayList<String> strings = new ArrayList<String>();
			StringBuilder sb = new StringBuilder();
			boolean inString = false, escaped = false;
			int i = 1;
			char c = 0;
			for (i = 1; i < raw.length(); i++)
			{
				c = raw.charAt(i);
				if (inString)
				{
					if (escaped)
					{
						if (c == '\\' || c == '"')
						{
							sb.append(c);
							escaped = false;
						}
						else
						{
							error = true;
							expected = new CharSequence[] { "\"", "\\" };
							break;
						}
					}
					else if (c == '"')
					{
						inString = !inString;
					}
					else if (c == '\\')
					{
						escaped = true;
					}
					else
						sb.append(c);
				}
				else if (c == ',')
				{
					strings.add(sb.toString());
					i++;
					if (raw.charAt(i) == '"')
					{
						sb = new StringBuilder();
						inString = true;
					}
					else
					{
						error = true;
						expected = new CharSequence[] { "\"" };
						break;
					}
				}
				else if (c == ']' && i == raw.length() - 1)
				{
					strings.add(sb.toString());
				}
				else
				{
					error = true;
					expected = new CharSequence[] { "," };
					break;
				}
			}
			
			if (error) throw new InvalidObjectException(
					"The value found at " + path + " could not be deserialized into an Array of String. Error at char "
							+ i + ", expected " + String.join(" or ", expected) + " but found '" + c + "'.");
			return strings.toArray(new String[] {});
		}
		else
			throw new InvalidObjectException(
					"The value found at " + path + " could not be deserialized into an Array of String.");
					
	}
	
	/**
	 * Reads a File from the config file. Will invoke getFile(path, false).
	 * 
	 * @param path the path to the parameter to be read.
	 * @return the boolean.
	 * @throws NoSuchElementException if the specified path could not be found
	 * @throws InvalidObjectException if the object from the config could not be deserialized into a valid Path
	 */
	public static File getFile(String path) throws NoSuchElementException, InvalidObjectException
	{
		try
		{
			return getFile(path, false);
		}
		catch (NoSuchFileException e)
		{
			return null;
		}
	}
	
	/**
	 * Reads a File from the config file.
	 * 
	 * @param path the path to the parameter to be read.
	 * @param checkIfExists set to true if you only want the file back if it already exists. If true and the file does not exist, it will generate an exception.
	 * @return the boolean.
	 * @throws NoSuchElementException if the specified path could not be found
	 * @throws InvalidObjectException if the object from the config could not be deserialized into a valid Path
	 * @throws NoSuchFileException if the file does not exist but the requesting parameter is set.
	 */
	public static File getFile(String path, boolean checkIfExists)
			throws NoSuchElementException, InvalidObjectException, NoSuchFileException
	{
		String location = getString(path);
		if (location == null) throw new NoSuchElementException("Could not find object at " + path);
		File f = new File(location);
		if (checkIfExists && !f.exists()) throw new NoSuchFileException("Could not find file at " + location + ".");
		return f;
	}
	
}
