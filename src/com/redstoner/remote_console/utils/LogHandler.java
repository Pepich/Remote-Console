package com.redstoner.remote_console.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.zip.GZIPInputStream;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LogHandler extends Thread
{
	private CommandSender sender;
	private String regex, fileName;
	private static ArrayList<CommandSender> stillSearching = new ArrayList<CommandSender>();
	
	private LogHandler(CommandSender sender, String regex, String fileName)
	{
		this.sender = sender;
		this.regex = regex;
		this.fileName = fileName;
	}
	
	public static void doSearch(CommandSender sender, String regex, String fileName)
	{
		if (stillSearching.contains(sender))
		{
			sender.sendMessage(" §eRMC: §4DO NOT EVER TRY TO QUERY TWO SEARCHES AT ONCE. Go die...");
			if (sender instanceof Player) ((Player) sender).setHealth(0);
			return;
		}
		stillSearching.add(sender);
		LogHandler instance = new LogHandler(sender, regex, fileName);
		instance.start();
	}
	
	/**
	 * Searches the logs for a certain regex and forwards any matches to the sender.
	 * 
	 * @param sender the issuer of the search
	 * @param regex the regex to search for. Will be wrapped in "^.*" and ".*$" if it is missing line delimiters
	 * @param fileName the name of the files to search through. May contain wildcards.
	 */
	private void search(CommandSender sender, String regex, String fileName)
	{
		long starttime = System.currentTimeMillis();
		int matches = 0;
		sender.sendMessage(" §eRMC: Starting log search for §a" + regex + " §ein §a" + fileName
				+ " §enow. Please don't query another search untils this one is done.");
		try
		{
			if (!regex.startsWith("^")) regex = "^.*" + regex;
			if (!regex.endsWith("$")) regex += ".*$";
			boolean singleFile = true;
			if (fileName.contains("*")) singleFile = false;
			File logFolder = ConfigHandler.getFile("rmc.logs.path");
			fileName = fileName.replace("*", ".*");
			for (File file : logFolder.listFiles())
			{
				if (file.getName().matches(fileName))
				{
					if (file.getName().endsWith(".gz"))
					{
						
						BufferedReader inputReader = new BufferedReader(
								new InputStreamReader(new GZIPInputStream(new FileInputStream(file))));
						matches += searchStream(inputReader, regex, sender, singleFile, file.getName());
						inputReader.close();
					}
					else
					{
						BufferedReader inputReader = new BufferedReader(new FileReader(file));
						matches += searchStream(inputReader, regex, sender, singleFile, file.getName());
						inputReader.close();
					}
					continue;
				}
			}
		}
		catch (NoSuchElementException | IOException e)
		{
			sender.sendMessage(" §eRMC: §cSomething went wrong, the search returned -1... Please check your settings!");
			stillSearching.remove(sender);
			return;
		}
		stillSearching.remove(sender);
		sender.sendMessage(" §eRMC: Your search took a total of " + (System.currentTimeMillis() - starttime) + "ms.");
		sender.sendMessage(" §eRMC: Found " + (matches > 0 ? "§a" : "§c") + matches + " §ematches total.");
		return;
	}
	
	/**
	 * This function searches through an InputStream to find a regex. If it finds a match, it will forward that match to the sender and increase the match counter.
	 * 
	 * @param inputReader the input reader containing the data
	 * @param regex the regex to search for
	 * @param sender the issuer of the search
	 * @param singleFile true if only a single file is being searched, false if the original filename contained wildcards.
	 * @param filename the name of the file that is currently being searched
	 * @return how many matches it found
	 * @throws IOException if something goes wrong
	 */
	private static int searchStream(BufferedReader inputReader, String regex, CommandSender sender, boolean singleFile,
			String filename) throws IOException
	{
		int matches = 0;
		String line = "";
		while ((line = inputReader.readLine()) != null)
		{
			if (line.matches(regex))
			{
				sender.sendMessage("§b> " + (singleFile ? "" : "§7" + filename + ": ") + "§f" + resolveColors(line));
				matches++;
			}
		}
		return matches;
	}
	
	/**
	 * Will resolve escape codes back to minecraft colors codes for ingame output
	 * 
	 * @param message the message to resolve
	 * @return the converted message
	 */
	private static String resolveColors(String message)
	{
		message = message.replace("[0;30;22m", "§0");
		message = message.replace("[0;34;22m", "§1");
		message = message.replace("[0;32;22m", "§2");
		message = message.replace("[0;36;22m", "§3");
		message = message.replace("[0;31;22m", "§4");
		message = message.replace("[0;35;22m", "§5");
		message = message.replace("[0;33;22m", "§6");
		message = message.replace("[0;37;22m", "§7");
		message = message.replace("[0;30;1m", "§8");
		message = message.replace("[0;34;1m", "§9");
		message = message.replace("[0;32;1m", "§a");
		message = message.replace("[0;36;1m", "§b");
		message = message.replace("[0;31;1m", "§c");
		message = message.replace("[0;35;1m", "§d");
		message = message.replace("[0;33;1m", "§e");
		message = message.replace("[0;37;1m", "§f");
		
		message = message.replace("[5m", "§k");
		message = message.replace("[21m", "§l");
		message = message.replace("[9m", "§m");
		message = message.replace("[4m", "§n");
		message = message.replace("[3m", "§o");
		
		message = message.replace("[m", "§r");
		
		return message;
	}
	
	@Override
	public void run()
	{
		search(sender, regex, fileName);
	}
}
