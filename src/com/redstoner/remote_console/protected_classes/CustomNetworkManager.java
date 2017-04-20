package com.redstoner.remote_console.protected_classes;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

import net.minecraft.server.v1_11_R1.EnumProtocolDirection;
import net.minecraft.server.v1_11_R1.NetworkManager;

public class CustomNetworkManager extends NetworkManager
{
	public CustomNetworkManager(EnumProtocolDirection enumprotocoldirection)
	{
		super(enumprotocoldirection);
	}
	
	@Override
	public void stopReading()
	{}
	
	@Override
	public SocketAddress getSocketAddress()
	{
		return new InetSocketAddress("localhost", 9000);
	}
}
