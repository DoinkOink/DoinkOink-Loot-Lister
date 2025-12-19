package com.DoinkOink.UI;

import com.DoinkOink.LootListerConfig;
import com.DoinkOink.LootListerPlugin;
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
	private static final int DEFAULT_OVERLAY_WIDTH = 100;

	private final LootListerConfig config;										// The mods config settings

	private final List<LootListerItem> items = new ArrayList<>();				// The currently displayed items
	private final List<LootListerItem> itemQueue = new ArrayList<>();			// All the items that need to be displayed
	private final List<LootListerItem> itemsToRemove = new ArrayList<>();		// The current items being animated before being removed

	private final Map<String, int> npcFilters = new HashMap<>();				// All of the per NPC value filters

	private FontMetrics fontMetrics;											// The current graphics font metrics which will be used to get an items text width
	private int currentOverlayWidth = DEFAULT_OVERLAY_WIDTH;					// How wide the current overlay is

	@Inject
	private LootListerOverlay(LootListerConfig _config, LootListerPlugin _plugin)
	{
		super(_plugin);

		config = _config;

		setPosition(OverlayPosition.BOTTOM_LEFT);
		setPriority(PRIORITY_MED);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		fontMetrics = graphics.getFontMetrics();

		final int _fontHeight = fontMetrics.getHeight();
		final int _verticalScrollMod = config.stackDirection() == LootListerStackSide.BOTTOM ? -1 : 1;
		final int _horizontalScrollMod = config.sideToAnimateFrom() == LootListerSide.LEFT ? -1 : 1;

		boolean _isTextMoving = false;

		for (LootListerItem _item : items) {
			// Draw the item to the screen
			drawItem(graphics, _item, _fontHeight);

			// Set the current max width based on this items text width alongside the configured icon size.
			currentOverlayWidth = Math.max(currentOverlayWidth, config.iconSize() + (config.spaceBetweenIconAndText() * 2) + _item.TextWidth);

			// Vertically animate the text if the CurrentPosition isn't the NextPosition
			if (_item.GetRemainingVerticalDistance() < config.verticalTextSpeed()) {
				_item.CurrentPosition.y = _item.NextPosition.y;
			} else if (_item.GetRemainingVerticalDistance() > 0) {
				_item.CurrentPosition.y += config.verticalTextSpeed() * _verticalScrollMod;
				_isTextMoving = true;
			}

			// Horizontally animate the text if the CurrentPosition isn't the NextPosition
			if (_item.GetRemainingHorizontalDistance() < config.horizontalTextSpeed()) {
				_item.CurrentPosition.x = _item.NextPosition.x;
				_item.JustAdded = false;
			} else if (_item.GetRemainingHorizontalDistance() > 0) {
				_item.CurrentPosition.x += config.horizontalTextSpeed() * _horizontalScrollMod;
				_isTextMoving = true;
			}
		}

		// Animate all the items that need to be removed from the screen
		for(int i = itemsToRemove.size()-1; i >= 0; i--) {
			LootListerItem _itemToRemove = itemsToRemove.get(i);

			if (_itemToRemove.GetRemainingHorizontalDistance() <= 0) {
				itemsToRemove.remove(i);
			} else {
				drawItem(graphics, _itemToRemove, _fontHeight);
				_itemToRemove.CurrentPosition.x -= config.horizontalTextSpeed() * _horizontalScrollMod;
			}
		}

		// Update all items vertical animations if no text is currently moving and there's an item waiting to be added
		//	to the overlay. This will eventually add the next item once the scrolling animation is halfway finished.
		if (!_isTextMoving && itemQueue.size() != 0)
			startScrollItems();

		// If text is currently moving and wasn't just added we need to be checking when we can add the next
		// 	item from the queue.
		if (_isTextMoving && !items.get(0).JustAdded) {
			LootListerItem _itemToCheck = items.get(0);

			// If the item to check, either the first or the second based on where we're at in the scrolling animation,
			//	has reached its halfway point we can add the next drop to the overlay.
			if (_itemToCheck.GetVerticalDistancePercentage() <= 0.5)
				addNextDropToOverlay();
		}

		// Check to see if we need to remove the last item in the list because it's been showing for too long
		if (items.size() != 0) {
			LootListerItem _lastItem = items.get(items.size() - 1);

			if (config.maxDisplayTime() != 0 && _lastItem.TimeDisplayed >= config.maxDisplayTime())
				setItemForRemoval(items.remove(items.size() - 1));
		}

		// If there's an item  in the queue and there's no items currently being shown add it to the overlay.
		if (items.size() == 0 && itemQueue.size() != 0)
			addNextDropToOverlay();

		// Always return the current max size of the overlay.
		return new Dimension(currentOverlayWidth, (config.iconSize() + config.spaceBetweenItems()) * (config.maxDisplayedItems() + 1));
	}

	private void drawItem(Graphics2D _graphics, LootListerItem _item, int _fontHeight)
	{
		final TextComponent _textComponent = new TextComponent();

		_graphics.drawImage(
			_item.Image,
			_item.CurrentPosition.x,
			_item.CurrentPosition.y,
			config.iconSize(),
			config.iconSize(),
			null
		);

		_textComponent.setColor(Color.yellow);
		_textComponent.setText(_item.ItemText);
		_textComponent.setOutline(true);
		_textComponent.setPosition(new Point(
			_item.CurrentPosition.x + config.iconSize() + config.spaceBetweenIconAndText(),
			_item.CurrentPosition.y + (config.iconSize() / 2) + (_fontHeight / 2)
		));

		_textComponent.render(_graphics);
	}

	public void addDropToQueue(LootListerItem _item)
	{
		// Set the initial value to check to the configured global ammount
		int _minValueToCheck = config.minValueToDisplay();
		
		// Check to see if the source NPC is in the configured NPC Filter dictionary and if it is change the value to check to the new value
		if (_item.SourceName != null && npcFilters.containsKey(_item.SourceName)) {
			_minValueToCheck = npcFilters.get(_item.SourceName);
		else if (_item.SourceID != null && npcFilters.containsKey(_item.SourceID))
			_minValueToCheck = npcFilters.get(_item.SourceID);
		
		// Now check to see if the item's price surpasses the min value
		if (_item.Price < _minValueToCheck)
			return;

		itemQueue.add(0, _item);

		// If there isn't currently anything in the queue go ahead and add it to the overlay
		if (items.size() == 0)
			addNextDropToOverlay();
	}

	// Set up the last item in the queue to be displayed on the screen
	public void addNextDropToOverlay()
	{
		// Double check that if there isn't anything in the queue for whatever reason we break out so no errors occur
		if (itemQueue.size() == 0)
			return;

		// If the overlay is currently displaying more than configured we need to set up removal of the last item
		// 	from the overlay.
		if (items.size() + 1 > config.maxDisplayedItems())
			setItemForRemoval(items.remove(items.size()-1));

		// Get the item that will be added to the overlay. This will always be the last item from the queue (FIFO)
		LootListerItem _itemToAdd = itemQueue.remove(itemQueue.size()-1);

		// Set the items text width to be used later
		_itemToAdd.TextWidth = fontMetrics.stringWidth(_itemToAdd.ItemText);

		// Check to see if add this item will change the current max width
		currentOverlayWidth = getMaxX(config.iconSize() + (config.spaceBetweenIconAndText() * 2) + _itemToAdd.TextWidth);

		// Set up the initial screen positions of the item based on some animation configurations
		_itemToAdd.SetFirstPosition(
			// Starting Point
			new Point(
				config.sideToAnimateFrom() == LootListerSide.LEFT ? currentOverlayWidth : -(_itemToAdd.TextWidth + config.iconSize()),
				config.stackDirection() == LootListerStackSide.BOTTOM ? (config.iconSize() + config.spaceBetweenItems()) * (config.maxDisplayedItems()) : 0
			),
			// Ending Point
			new Point(
				config.sideToAnimateFrom() == LootListerSide.LEFT ? currentOverlayWidth - (config.iconSize() + _itemToAdd.TextWidth + (config.spaceBetweenIconAndText() * 2)) : config.spaceBetweenIconAndText(),
				config.stackDirection() == LootListerStackSide.BOTTOM ? (config.iconSize() + config.spaceBetweenItems()) * (config.maxDisplayedItems()) : 0
			)
		);

		// Add the item to the currently displayed items list
		items.add(0, _itemToAdd);

		// Finally update all currently displayed items X positions in case this item changes the maximum width
		updateItemHorizontalPositions();
	}

	// Update all currently displayed items X positions based on the current max overlay width
	public void updateItemHorizontalPositions()
	{
		// As this will only ever be called when an item is added to the overlay we have to skip the first item
		//	so the horizontal animation doesn't break.
		for (int i = 1; i < items.size(); i++) {
			LootListerItem _item = items.get(i);
			// Set the horizontal position based on which direction the text is animating from
			_item.SetHorizontalPosition(
				config.sideToAnimateFrom() == LootListerSide.LEFT
					? currentOverlayWidth - (config.iconSize() + _item.TextWidth + (config.spaceBetweenIconAndText() * 2))
					: config.spaceBetweenIconAndText()
			);
		}
	}

	// Remove all items from the screen. This will be called when the player changes config settings as it breaks the
	//	layout if we don't clear them.
	public void clearItemsFromOverlay()
	{
		items.clear();
	}

	// Update the NPC filtering list to later be used when checking to see if an item should be added to the overlay
	public void updateNpcFilters()
	{
 		npcFilters.clear();

		final String[] _filters = config.npcFilters().split(",");
		for (String _filter : _filters) {
			try {
				final String[] _splitFilter = _filter.split(":");
				npcFilters.add(_splitFilter[0].trim().toLowerCase(), Integer.parseInt(_splitFilter[1].trim()));
			} catch (Exception e) {
				System.out.println("Error occured when updating NPC Filters: " + e.getMessage());
				continue;
			}
		}
	}

	// Add 0.6s to the last items timer every tick, so it can be removed from the screen if configured to do so.
	public void updateOldestItemTimer()
	{
		if (items.size() > 0)
			items.get(items.size()-1).TimeDisplayed += 0.6;
	}

	// Set up the next position for all items to start scrolling them up/down
	private void startScrollItems()
	{
		final int _verticalScrollMod = config.stackDirection() == LootListerStackSide.BOTTOM ? -1 : 1;
		for (LootListerItem _item : items)
		{
			_item.SetNextPosition(new Point(
				_item.CurrentPosition.x,
				_item.CurrentPosition.y + ((config.iconSize() + config.spaceBetweenItems()) * _verticalScrollMod)
			));
		}
	}

	// Removes the given item from the overlay list and adds it to the removal list, so it can still be animated
	//	before being fully removed.
	private void setItemForRemoval(LootListerItem _item)
	{
		itemsToRemove.add(_item);

		_item.SetNextPosition(new Point(
			config.sideToAnimateFrom() == LootListerSide.LEFT ? currentOverlayWidth : -(_item.TextWidth + config.iconSize()),
			_item.CurrentPosition.y
		));
	}

	// Go through all currently displayed items and get the maximum width the overlay needs to be.
	private int getMaxX(int _maxX)
	{
		// Check the given width with the default overlay width to see what is larger.
		//	This way the overlay's width will never be smaller than the default size.
		_maxX = Math.max(DEFAULT_OVERLAY_WIDTH, _maxX);

		for(LootListerItem _item : items) {
			_maxX = Math.max(_maxX, config.iconSize() + (config.spaceBetweenIconAndText() * 2) + _item.TextWidth);
		}

		return _maxX;
	}

}
