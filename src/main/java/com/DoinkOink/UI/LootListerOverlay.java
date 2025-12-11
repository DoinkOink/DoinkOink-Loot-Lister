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
import java.util.List;

public class LootListerOverlay extends OverlayPanel
{
	private static final int H_PADDING = 2;
	private static final int V_PADDING = 1;
	private static final int TEXT_WIDTH = 22;
	private static final int TEXT_SCROLL_SPEED = 3;

	private final Client client;
	private final LootListerConfig config;
	private final LootListerPlugin plugin;

	private List<LootListerItem> items = new ArrayList<>();
	private List<LootListerItem> itemQueue = new ArrayList<>();

	private FontMetrics fontMetrics;
	private boolean waitForTextMovement = false;

	private int maxX = 100;

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
		int curY = 0;
		boolean _addNextItem = items.size() == 0;
		boolean _isTextMoving = false;

		fontMetrics = graphics.getFontMetrics();
		final int _fontHeight = fontMetrics.getHeight();

		for(LootListerItem _item : items)
		{
			final int _stringWidth = fontMetrics.stringWidth(_item.ItemText);
			final TextComponent _textComponent = new TextComponent();

			graphics.drawImage(
				_item.Image,
				maxX - (config.iconSize() + _stringWidth + (H_PADDING * 2)),
				_item.CurrentPosition.y,
				config.iconSize(),
				config.iconSize(),
				null
			);

			_textComponent.setColor(Color.yellow);
			_textComponent.setText(_item.ItemText);
			_textComponent.setOutline(true);
			_textComponent.setPosition(new Point(
				maxX - (_stringWidth + H_PADDING),
				_item.CurrentPosition.y + (config.iconSize() / 2) + (_fontHeight / 2)
			));

			_textComponent.render(graphics);

			curY += Math.max(config.iconSize(), _fontHeight);
			maxX = Math.max(maxX, config.iconSize() + (H_PADDING * 2) + _stringWidth);

			// Move the text if it needs to be
			if (_item.CurrentPosition.y > _item.NextPosition.y) {
				_item.CurrentPosition.y -= TEXT_SCROLL_SPEED;
				_isTextMoving = true;
			} else if (_item.CurrentPosition.y < _item.NextPosition.y) {
				_item.CurrentPosition.y = _item.NextPosition.y;
			}
		}

		if (!_isTextMoving && itemQueue.size() != 0)
			startScrollItems();

		if (_isTextMoving && !waitForTextMovement) {
			LootListerItem _firstItem = items.get(0);
			double _totalDistance = _firstItem.OriginalPosition.getY() - _firstItem.NextPosition.getY();
			double _remainingDistance = _firstItem.CurrentPosition.getY() - _firstItem.NextPosition.getY();

			if (_remainingDistance <= 0)
				addNextDropToOverlay();
		}

		return new Dimension(maxX, config.iconSize() * config.maxDisplayedItems());
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

		waitForTextMovement = true;

		LootListerItem _itemToAdd = itemQueue.remove(itemQueue.size()-1);

		_itemToAdd.SetFirstPosition(new Point(
			0,
			config.iconSize() * (config.maxDisplayedItems() - 1)
		));

		items.add(0, _itemToAdd);

		if (items.size() > config.maxDisplayedItems())
			items.remove(items.size()-1);
	}

	public void updateDropItemsCount()
	{
		while(items.size() > config.maxDisplayedItems())
		{
			items.remove(items.size()-1);
		}
	}

	//private void updateItemPositions()
	//{
	//	final int _maxHeight = config.iconSize() * config.maxDisplayedItems();
	//
	//	for (int i = 0; i < items.size(); i++) {
	//		LootListerItem _item = items.get(i);
	//
	//		if (fontMetrics != null)
	//			_item.TextWidth = fontMetrics.stringWidth(_item.ItemText);
	//
	//		final Point _point = new Point(
	//			0,
	//			_maxHeight - ((i+1) * config.iconSize())
	//		);
	//
	//		if (i == 0)
	//			_item.SetFirstPosition(_point);
	//		else
	//			_item.SetNextPosition(_point);
	//	}
	//}

	private void startScrollItems()
	{
		for (LootListerItem _item : items)
		{
			_item.SetNextPosition(new Point(
				_item.CurrentPosition.x,
				_item.CurrentPosition.y - config.iconSize()
			));
		}

		waitForTextMovement = false;
	}

}
