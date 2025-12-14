package com.DoinkOink.UI;

import com.DoinkOink.LootListerConfig;
import com.DoinkOink.LootListerPlugin;
import com.DoinkOink.Misc.LootListerItem;
import net.runelite.api.Client;
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
import java.util.Iterator;
import java.util.List;

public class LootListerOverlay extends OverlayPanel
{
	private static final int H_PADDING = 2;
	private static final int V_PADDING = 1;
	private static final int TEXT_WIDTH = 22;
	private static final int TEXT_VERTICAL_SCROLL_SPEED = 5;
	private static final int TEXT_HORIZONTAL_SCROLL_SPEED = 10;

	private final Client client;
	private final LootListerConfig config;
	private final LootListerPlugin plugin;

	private List<LootListerItem> items = new ArrayList<>();
	private List<LootListerItem> itemQueue = new ArrayList<>();
	private List<LootListerItem> itemsToRemove = new ArrayList<>();

	private FontMetrics fontMetrics;
	private boolean textVerticalMovement = false;

	private int maxX = 100;
	private static final int DEFAULT_WIDTH = 100;

	@Inject
	private LootListerOverlay(Client _client, LootListerConfig _config, LootListerPlugin _plugin)
	{
		super(_plugin);

		client = _client;
		config = _config;
		plugin = _plugin;

		setPosition(OverlayPosition.BOTTOM_LEFT);
		setPriority(PRIORITY_MED);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		boolean _isTextMoving = false;

		fontMetrics = graphics.getFontMetrics();
		final int _fontHeight = fontMetrics.getHeight();

		for (LootListerItem _item : items) {
			drawItem(graphics, _item, _fontHeight);

			maxX = Math.max(maxX, config.iconSize() + (H_PADDING * 2) + _item.TextWidth);

			// Move the text if it needs to be
			if (_item.CurrentPosition.y < _item.NextPosition.y - TEXT_VERTICAL_SCROLL_SPEED) {
				_item.CurrentPosition.y = _item.NextPosition.y;
			} else if (_item.CurrentPosition.y > _item.NextPosition.y) {
				_item.CurrentPosition.y -= TEXT_VERTICAL_SCROLL_SPEED;
				_isTextMoving = true;
			}

			if (_item.CurrentPosition.x < (_item.NextPosition.x + TEXT_HORIZONTAL_SCROLL_SPEED)) {
				_item.CurrentPosition.x = _item.NextPosition.x;
			} else if (_item.CurrentPosition.x > _item.NextPosition.x) {
				_item.CurrentPosition.x -= TEXT_HORIZONTAL_SCROLL_SPEED;
				_isTextMoving = true;
			}
		}

		// Animate all the items that need to be removed from the screen
		for(int i = itemsToRemove.size()-1; i >= 0; i--) {
			LootListerItem _itemToRemove = itemsToRemove.get(i);

			if (_itemToRemove.CurrentPosition.x >= _itemToRemove.NextPosition.x) {
				itemsToRemove.remove(i);
			} else {
				drawItem(graphics, _itemToRemove, _fontHeight);
				_itemToRemove.CurrentPosition.x += TEXT_HORIZONTAL_SCROLL_SPEED;
			}
		}

		if (!_isTextMoving && itemQueue.size() != 0)
			startScrollItems();

		if (_isTextMoving && !textVerticalMovement) {
			LootListerItem _firstItem = items.get(0);

			if (_firstItem.CurrentPosition.getY() == _firstItem.NextPosition.getY())
				_firstItem = items.get(1);

			double _totalDistance = _firstItem.OriginalPosition.getY() - _firstItem.NextPosition.getY();
			double _remainingDistance = _firstItem.CurrentPosition.getY() - _firstItem.NextPosition.getY();

			if (_remainingDistance / _totalDistance <= 0.5)
				addNextDropToOverlay();
		}

		// Check to see if we need to remove the last item in the list because it's been showing for too long
		if (items.size() != 0) {
			LootListerItem _lastItem = items.get(items.size() - 1);

			if (config.maxDisplayTime() != 0 && _lastItem.TimeDisplayed >= config.maxDisplayTime())
				setItemForRemoval(items.remove(items.size() - 1));
		}

		if (items.size() == 0 && itemQueue.size() != 0)
			addNextDropToOverlay();

		return new Dimension(maxX, config.iconSize() * (config.maxDisplayedItems() + 1));
	}

	public void addDropToQueue(LootListerItem _item)
	{
		if (_item.Price < config.minValueToDisplay())
			return;

		itemQueue.add(0, _item);

		if (items.size() == 0)
			addNextDropToOverlay();
	}

	public void addNextDropToOverlay()
	{
		if (itemQueue.size() == 0)
			return;

		if (items.size() + 1 > config.maxDisplayedItems())
			setItemForRemoval(items.remove(items.size()-1));

		textVerticalMovement = true;

		LootListerItem _itemToAdd = itemQueue.remove(itemQueue.size()-1);

		_itemToAdd.TextWidth = fontMetrics.stringWidth(_itemToAdd.ItemText);
		maxX = getMaxX(config.iconSize() + (H_PADDING * 2) + _itemToAdd.TextWidth);

		_itemToAdd.SetFirstPosition(
			new Point(
				maxX,
				config.iconSize() * (config.maxDisplayedItems())
			),
			new Point(
				maxX - (config.iconSize() + _itemToAdd.TextWidth + (H_PADDING * 2)),
				config.iconSize() * (config.maxDisplayedItems())
			)
		);

		items.add(0, _itemToAdd);
		updateItemHorizontalPositions();
	}

	public void updateItemHorizontalPositions()
	{
		for (int i = 1; i < items.size(); i++) {
			LootListerItem _item = items.get(i);
			_item.SetHorizontalPosition(maxX - (config.iconSize() + _item.TextWidth + (H_PADDING * 2)));
		}
	}

	public void updateDropItemsCount()
	{
		items.clear();
	}

	public void updateOldestItemTimer()
	{
		if (items.size() > 0)
			items.get(items.size()-1).TimeDisplayed += 0.6;
	}

	private void startScrollItems()
	{
		for (LootListerItem _item : items)
		{
			_item.SetNextPosition(new Point(
				_item.CurrentPosition.x,
				_item.CurrentPosition.y - config.iconSize()
			));
		}

		textVerticalMovement = false;
	}

	private void setItemForRemoval(LootListerItem _item)
	{
		itemsToRemove.add(_item);

		_item.SetNextPosition(new Point(
			maxX + 10,
			_item.CurrentPosition.y
		));
	}

	private int getMaxX(int _maxX)
	{
		_maxX = Math.max(DEFAULT_WIDTH, _maxX);

		for(LootListerItem _item : items) {
			_maxX = Math.max(_maxX, config.iconSize() + (H_PADDING * 2) + _item.TextWidth);
		}

		return _maxX;
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
			_item.CurrentPosition.x + config.iconSize() + H_PADDING,
			_item.CurrentPosition.y + (config.iconSize() / 2) + (_fontHeight / 2)
		));

		_textComponent.render(_graphics);
	}

}
