package com.redstoner.remote_console.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.bukkit.command.CommandSender;

import com.redstoner.remote_console.protected_classes.Main;

public class LogHandler
{
	public static int search(CommandSender sender, String regex, String fileName)
	{
		int matches = 0;
		try
		{
			if (!regex.startsWith("^")) regex = "^.*" + regex;
			if (!regex.endsWith("$")) regex += ".*$";
			boolean singleFile = true;
			if (fileName.contains("*")) singleFile = false;
			File logFolder = ConfigHandler.getFile("rmc.logs.path");
			for (File file : logFolder.listFiles())
			{
				if (file.getName().matches(fileName.replaceAll("\\*", ".*")))
				{
					Main.logger.info("Found matching file: " + fileName);
					if (file.getName().endsWith(".gz"))
					{
						ZipFile zip = new ZipFile(file);
						Enumeration<? extends ZipEntry> zipEntries = zip.entries();
						while (zipEntries.hasMoreElements())
						{
							ZipEntry entry = zipEntries.nextElement();
							if (entry.isDirectory()) continue;
							BufferedReader inputReader = new BufferedReader(
									new InputStreamReader(zip.getInputStream(entry)));
							String line = "";
							while ((line = inputReader.readLine()) != null)
							{
								if (line.matches(regex))
								{
									sender.sendMessage((singleFile ? "" : "§7" + file.getName() + ": ") + "&fline");
									matches++;
								}
							}
							inputReader.close();
						}
						zip.close();
					}
					else
					{
						BufferedReader inputReader = new BufferedReader(new FileReader(file));
						String line = "";
						while ((line = inputReader.readLine()) != null)
						{
							if (line.matches(regex))
							{
								sender.sendMessage((singleFile ? "" : "§7" + file.getName() + ": ") + "§f" + line);
								matches++;
							}
						}
						inputReader.close();
					}
					continue;
				}
			}
		}
		catch (NoSuchElementException | IOException e)
		{
			return -1;
		}
		return matches;
	}
}
