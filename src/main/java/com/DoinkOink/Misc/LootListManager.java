package com.DoinkOink.Misc;

import com.DoinkOink.LootListerConfig;
import com.DoinkOink.UI.LootListerOverlay;

import javax.inject.Inject;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LootListManager {

	private final LootListerConfig config;										// The mods config settings
	private final LootListerOverlay overlay;

	public final List<LootListerItem> Items = new ArrayList<>();				// The currently displayed items
	public final List<LootListerItem> ItemsToRemove = new ArrayList<>();		// The current items being animated before being removed
	private final List<LootListerItem> itemQueue = new ArrayList<>();			// All the items that need to be displayed
	private final Map<String, Integer> npcFilters = new HashMap<>();			// All the per NPC value filters

	private int timeSinceLastItemAdded = 0;

	@Inject
	public LootListManager(LootListerConfig _config, LootListerOverlay _overlay)
	{
		config = _config;
		overlay = _overlay;

		updateNpcFilters();
	}

	// Receive a LootListerItem and decide if it needs to go to the item queue or be immediately displayed
	public void addDropToQueue(LootListerItem item)
	{
		timeSinceLastItemAdded = 0;

		// Set the initial value to check to the configured global amount
		int _minValueToCheck = config.minValueToDisplay();

		// Check to see if the source NPC is in the configured NPC Filter dictionary and if it is change the value to check to the new value
		if (!item.SourceName.isEmpty() && npcFilters.containsKey(item.SourceName))
			_minValueToCheck = npcFilters.get(item.SourceName);
		else if (!item.SourceID.isEmpty() && npcFilters.containsKey(item.SourceID))
			_minValueToCheck = npcFilters.get(item.SourceID);

		// Now check to see if the item's price surpasses the min value
		if (item.Price < _minValueToCheck)
			return;

		itemQueue.add(0, item);

		// If there isn't currently anything in the queue go ahead and add it to the overlay
		if (Items.size() == 0)
			addNextDropToOverlay();
	}

	// Set up the last item in the queue to be displayed on the screen
	private void addNextDropToOverlay()
	{
		// Double check that if there isn't anything in the queue for whatever reason we break out so no errors occur
		if (itemQueue.size() == 0)
			return;

		// If the overlay is currently displaying more than configured we need to set up removal of the last item
		// 	from the overlay.
		if (Items.size() + 1 > config.maxDisplayedItems())
			setItemForRemoval(Items.remove(Items.size()-1));

		// Get the item that will be added to the overlay. This will always be the last item from the queue (FIFO)
		LootListerItem _itemToAdd = itemQueue.remove(itemQueue.size()-1);

		// Set the items text width to be used later
		_itemToAdd.TextWidth = overlay.fontMetrics.stringWidth(_itemToAdd.ItemText);

		// Check to see if add this item will change the current max width
		int _itemWidth = config.iconSize() + (config.spaceBetweenIconAndText() * 2) + _itemToAdd.TextWidth;
		int _overlayWidth = overlay.calcMaxOverlayWidth(Items, _itemWidth);
		System.out.println(_overlayWidth);

		// Set up the initial screen positions of the item based on some animation configurations
		_itemToAdd.SetFirstPosition(
			// Starting Point
			new Point(
				config.sideToAnimateFrom() == LootListerSide.RIGHT ? _overlayWidth : -(_itemToAdd.TextWidth + config.iconSize()),
				config.stackDirection() == LootListerStackSide.BOTTOM ? (config.iconSize() + config.spaceBetweenItems()) * (config.maxDisplayedItems()) : 0
			),
			// Ending Point
			new Point(
				config.sideToAnimateFrom() == LootListerSide.RIGHT ? _overlayWidth - (config.iconSize() + _itemToAdd.TextWidth + (config.spaceBetweenIconAndText() * 2)) : config.spaceBetweenIconAndText(),
				config.stackDirection() == LootListerStackSide.BOTTOM ? (config.iconSize() + config.spaceBetweenItems()) * (config.maxDisplayedItems()) : 0
			)
		);

		// Add the item to the currently displayed items list
		Items.add(0, _itemToAdd);

		// Finally update all currently displayed items X positions in case this item changes the maximum width
		updateItemHorizontalPositions();
	}

	public void updateItems()
	{
		boolean _isItemMoving = false;

		for(LootListerItem _item : Items)
		{
			_item.UpdatePosition(config, false);

			if (!_isItemMoving)
				_isItemMoving = _item.IsTextMoving;
		}

		for(LootListerItem _item : ItemsToRemove) { _item.UpdatePosition(config, true); }

		for(int i = ItemsToRemove.size()-1; i >= 0; i--)
		{
			LootListerItem _itemToRemove = ItemsToRemove.get(i);
			if (_itemToRemove.MarkedForDelete)
				ItemsToRemove.remove(i);
		}

		// Update all items vertical animations if no text is currently moving and there's an item waiting to be added
		//	to the overlay. This will eventually add the next item once the scrolling animation is halfway finished.
		if (!_isItemMoving && itemQueue.size() != 0)
			startScrollItems();

		// If text is currently moving and wasn't just added we need to be checking when we can add the next
		// 	item from the queue.
		if (_isItemMoving && !Items.get(0).JustAdded) {
			LootListerItem _itemToCheck = Items.get(0);

			// If the item to check, either the first or the second based on where we're at in the scrolling animation,
			//	has reached its halfway point we can add the next drop to the overlay.
			if (_itemToCheck.GetVerticalDistancePercentage() <= 0.5)
				addNextDropToOverlay();
		}

		// Check to see if we need to remove the last item in the list because it's been showing for too long
		if (Items.size() != 0) {
			LootListerItem _lastItem = Items.get(Items.size() - 1);

			if (config.maxDisplayTime() != 0 && _lastItem.TicksDisplayed >= config.maxDisplayTime())
				setItemForRemoval(Items.remove(Items.size() - 1));
		}

		// If there's an item  in the queue and there's no items currently being shown add it to the overlay.
		if (Items.size() == 0 && itemQueue.size() != 0)
			addNextDropToOverlay();
	}

	// Set up the next position for all items to start scrolling them up/down
	private void startScrollItems()
	{
		final int _verticalScrollMod = config.stackDirection() == LootListerStackSide.BOTTOM ? -1 : 1;
		for (LootListerItem _item : Items)
		{
			_item.SetNextPosition(new Point(
				_item.CurrentPosition.x,
				_item.CurrentPosition.y + ((config.iconSize() + config.spaceBetweenItems()) * _verticalScrollMod)
			));
		}
	}

	// Update all currently displayed items X positions based on the current max overlay width
	public void updateItemHorizontalPositions()
	{
		// As this will only ever be called when an item is added to the overlay we have to skip the first item
		//	so the horizontal animation doesn't break.
		for (int i = 1; i < Items.size(); i++) {
			LootListerItem _item = Items.get(i);
			// Set the horizontal position based on which direction the text is animating from
			_item.SetHorizontalPosition(
				config.sideToAnimateFrom() == LootListerSide.RIGHT
					? overlay.getMaxOverlayWidth() - (config.iconSize() + _item.TextWidth + (config.spaceBetweenIconAndText() * 2))
					: config.spaceBetweenIconAndText()
			);
		}
	}

	// Removes the given item from the overlay list and adds it to the removal list, so it can still be animated
	//	before being fully removed.
	private void setItemForRemoval(LootListerItem _item)
	{
		ItemsToRemove.add(_item);

		_item.SetNextPosition(new Point(
			config.sideToAnimateFrom() == LootListerSide.RIGHT ? overlay.getMaxOverlayWidth() : -(_item.TextWidth + config.iconSize()),
			_item.CurrentPosition.y
		));
	}

	public void setAllItemsForRemoval()
	{
		for(int i = Items.size()-1; i >= 0; i--)
		{
			setItemForRemoval(Items.remove(i));
		}
	}

	// Remove all items from the screen. This will be called when the player changes config settings as it breaks the
	//	layout if we don't clear them.
	public void clearItemsFromOverlay() { Items.clear(); }

	public void onTick()
	{
		var _displayTimeMethod = config.displayTimeMethod();

		if (_displayTimeMethod == DisplayTimeMethod.ONE_AT_A_TIME)
		{
			Items.get(Items.size()-1).TicksDisplayed += 1;
		}
		else if (_displayTimeMethod == DisplayTimeMethod.BY_DROP_GROUP)
		{
			for(LootListerItem _item : Items) { _item.TicksDisplayed += 1; }
		}
		else if (_displayTimeMethod == DisplayTimeMethod.TIME_SINCE_LAST_DROP)
		{
			timeSinceLastItemAdded += 1;

			if (timeSinceLastItemAdded >= config.maxDisplayTime())
				setAllItemsForRemoval();
		}
	}

	// Update the NPC filtering list to later be used when checking to see if an item should be added to the overlay
	public void updateNpcFilters()
	{
		npcFilters.clear();

		final String[] _filters = config.npcFilters().split(",");
		for (String _filter : _filters) {
			try {
				final String[] _splitFilter = _filter.split(":");

				if (npcFilters.containsKey(_splitFilter[0].trim().toLowerCase()))
					continue;

				npcFilters.put(_splitFilter[0].trim().toLowerCase(), Integer.parseInt(_splitFilter[1].trim()));
			} catch (Exception ignored) { }
		}
	}
}
