package com.DoinkOink;

import com.DoinkOink.Misc.LootListerSide;
import com.DoinkOink.Misc.LootListerStackSide;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup("Loot Lister")
public interface LootListerConfig extends Config
{
	@ConfigSection(
		name = "Display",
		description = "Display settings.",
		position = 0
	)
	String displaySection = "Display";

	@ConfigSection(
		name = "Animation",
		description = "Animation settings.",
		position = 1
	)
	String animationSection = "Animation";

	@ConfigItem(
		keyName = "maxDisplayedItems",
		name = "Max Displayed Items",
		description = "How many items can be shown at once.",
		section = displaySection
	)
	default int maxDisplayedItems()
	{
		return 5;
	}

	@ConfigItem(
		keyName = "minValueToDisplay",
		name = "Minimum Value",
		description = "The minimum value an item must be to be displayed.",
		section = displaySection
	)
	default int minValueToDisplay() { return 1000; }

	@ConfigItem(
		keyName = "iconSize",
		name = "Icon Size",
		description = "How big item icons will be.",
		section = displaySection
	)
	default int iconSize()
	{
		return 26;
	}

	@ConfigItem(
		keyName = "maxDisplayTime",
		name = "Max display time",
		description = "How long the last item in the list will be displayed on screen. If set to 0 it will show until the max item displayed has been reached.",
		section = displaySection
	)
	default double maxDisplayTime() { return 10; }

	@ConfigItem(
		keyName = "spaceBetweenItems",
		name =  "Space Between Items",
		description = "How much vertical space is between each displayed item.",
		section = displaySection
	)
	default int spaceBetweenItems() { return 2; }

	@ConfigItem(
		keyName = "spaceBetweenIconAndText",
		name =  "Space Between Icons and Text",
		description = "How much space is between an items icon and text.",
		section = displaySection
	)
	default int spaceBetweenIconAndText() { return 2; }

	@ConfigItem(
		keyName = "sideToAnimateFrom",
		name = "Direction to appear from",
		description = "Which side the items should appear and disappear from.",
		section = animationSection,
		position = 0
	)
	default LootListerSide sideToAnimateFrom() { return LootListerSide.RIGHT;}

	@ConfigItem(
		keyName = "stackDirection",
		name = "Item stack direction",
		description = "What direction items should stack in the list.",
		section = animationSection,
		position = 1
	)
	default LootListerStackSide stackDirection() { return LootListerStackSide.BOTTOM; }

	@ConfigItem(
		keyName = "horizontalTextSpeed",
		name =  "Horizontal Scroll Speed",
		description = "How fast the text will scroll left and right.",
		section = animationSection,
		position = 2
	)
	default int horizontalTextSpeed() { return 10; }

	@ConfigItem(
		keyName = "verticalTextSpeed",
		name =  "Vertical Scroll Speed",
		description = "How fast the text will scroll up and down.",
		section = animationSection,
		position = 3
	)
	default int verticalTextSpeed() { return 5; }
}
