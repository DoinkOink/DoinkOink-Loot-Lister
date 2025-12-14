package com.DoinkOink;

import com.DoinkOink.Misc.LootListerSide;
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

	@ConfigItem(
		keyName = "maxDisplayTime",
		name = "Max display time",
		description = "How long the last item in the list will be displayed on screen. If set to 0 it will show until the max item displayed has been reached."
	)
	default double maxDisplayTime() { return 1; }

	@ConfigItem(
		keyName = "sideToAnimateFrom",
		name = "Side To Animate From",
		description = "Which side the items should appear and disappear from."
	)
	default LootListerSide sideToAnimateFrom() { return LootListerSide.RIGHT;}
}
