package com.DoinkOink;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("Loot Lister")
public interface LootListerConfig extends Config
{
	@ConfigItem(
		keyName = "maxDisplayedItems",
		name = "Max Displayed Items",
		description = "How many items can be shown at once."
	)
	default int maxDisplayedItems()
	{
		return 5;
	}

	@ConfigItem(
		keyName = "minValueToDisplay",
		name = "Minimum Value",
		description = "The minimum value an item must be to be displayed."
	)
	default int minValueToDisplay()
	{
		return 1000;
	}

	@ConfigItem(
		keyName = "iconSize",
		name = "Icon Size",
		description = "How big item icons will be."
	)
	default int iconSize()
	{
		return 26;
	}
}
