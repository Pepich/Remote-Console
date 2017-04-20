package com.redstoner.remote_console.protected_classes;

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
}
