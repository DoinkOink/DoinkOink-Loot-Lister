package com.example;

import com.DoinkOink.LootListerPlugin;
import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class LootListerTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(LootListerPlugin.class);
		RuneLite.main(args);
	}
}