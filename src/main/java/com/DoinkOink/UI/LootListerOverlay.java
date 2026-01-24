package com.DoinkOink.UI;

import com.DoinkOink.LootListerConfig;
import com.DoinkOink.LootListerPlugin;
import com.DoinkOink.Misc.LootListManager;
import com.DoinkOink.Misc.LootListerItem;
import com.DoinkOink.Misc.LootListerSide;
import com.DoinkOink.Misc.LootListerStackSide;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.TextComponent;

import javax.inject.Inject;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LootListerOverlay extends OverlayPanel
{
	public static final int DEFAULT_OVERLAY_WIDTH = 100;

	private final LootListerConfig config;										// The mods config settings
	private final LootListerPlugin plugin;
	public LootListManager manager;

	private final List<LootListerItem> items = new ArrayList<>();				// The currently displayed items
	private final List<LootListerItem> itemQueue = new ArrayList<>();			// All the items that need to be displayed
	private final List<LootListerItem> itemsToRemove = new ArrayList<>();		// The current items being animated before being removed

	private final Map<String, Integer> npcFilters = new HashMap<>();			// All the per NPC value filters

	public FontMetrics fontMetrics;												// The current graphics font metrics which will be used to get an items text width
	private int currentOverlayWidth = DEFAULT_OVERLAY_WIDTH;					// How wide the current overlay is

	@Inject
	private LootListerOverlay(LootListerPlugin _plugin, LootListerConfig _config)
	{
		super(_plugin);

		plugin = _plugin;
		config = _config;

		setPosition(OverlayPosition.BOTTOM_LEFT);
		setPriority(PRIORITY_MED);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		fontMetrics = graphics.getFontMetrics();

		final int _fontHeight = fontMetrics.getHeight();

		// Draw all normal items first
		for (LootListerItem _item : manager.Items) {
			// Draw the item to the screen
			drawItem(graphics, _item, _fontHeight);
		}

		// Then draw all the items that are being removed
		for (LootListerItem _item : manager.ItemsToRemove) {
			drawItem(graphics, _item, _fontHeight);
		}

		// Always return the current max size of the overlay.
		return new Dimension(currentOverlayWidth, (config.iconSize() + config.spaceBetweenItems()) * (config.maxDisplayedItems() + 1));
	}

	private void drawItem(Graphics2D graphics, LootListerItem item, int fontHeight)
	{
		final TextComponent _textComponent = new TextComponent();

		graphics.drawImage(
			item.Image,
			item.CurrentPosition.x,
			item.CurrentPosition.y,
			config.iconSize(),
			config.iconSize(),
			null
		);

		_textComponent.setColor(config.textColor());
		_textComponent.setText(item.ItemText);
		_textComponent.setOutline(true);
		_textComponent.setPosition(new Point(
			item.CurrentPosition.x + config.iconSize() + config.spaceBetweenIconAndText(),
			item.CurrentPosition.y + (config.iconSize() / 2) + (fontHeight / 2)
		));

		_textComponent.render(graphics);
	}

	public int calcMaxOverlayWidth(List<LootListerItem> items)
	{
		return calcMaxOverlayWidth(items, 0);
	}

	public int calcMaxOverlayWidth(List<LootListerItem> items, int initialWidth)
	{
		// Check the given width with the default overlay width to see what is larger.
		//	This way the overlay's width will never be smaller than the default size.
		int maxX = Math.max(DEFAULT_OVERLAY_WIDTH, initialWidth);

		for(LootListerItem _item : items) {
			maxX = Math.max(maxX, config.iconSize() + (config.spaceBetweenIconAndText() * 2) + _item.TextWidth);
		}

		return currentOverlayWidth = maxX;
	}

	public int getMaxOverlayWidth() { return currentOverlayWidth; }
}
