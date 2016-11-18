package com.redstoner.remote_console.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

public class ConfigHandler
{
	private static final File propertiesFile = new File("plugins/rmc-config.properties");
	private static final Properties properties = new Properties();
	
	static
	{
		try
		{
			if (!propertiesFile.exists())
			{
				propertiesFile.createNewFile();
				BufferedWriter writer = new BufferedWriter(new FileWriter(propertiesFile));
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(System.class.getResourceAsStream("defaultconfig.properties")));
				String line = "";
				while ((line = reader.readLine()) != null)
				{
					writer.write(line);
					writer.newLine();
				}
				writer.flush();
				reader.close();
				writer.close();
			}
			properties.load((new FileInputStream(propertiesFile)));
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public static Properties getConfig()
	{
		return properties;
	}
}
