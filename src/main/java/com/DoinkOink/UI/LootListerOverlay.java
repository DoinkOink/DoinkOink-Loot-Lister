package com.DoinkOink.UI;

import com.DoinkOink.LootListerConfig;
import com.DoinkOink.LootListerPlugin;
import com.DoinkOink.Misc.LootListerItem;
import net.runelite.api.Client;
import net.runelite.api.ItemComposition;
import net.runelite.client.game.ItemStack;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
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

	private final Client client;
	private final LootListerConfig config;
	private final LootListerPlugin plugin;

	private List<LootListerItem> items = new ArrayList<>();

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
		int maxX = 0;
		int curY = 0;

		final FontMetrics _fontMetrics = graphics.getFontMetrics();
		final int _fontHeight = _fontMetrics.getHeight();

		for(LootListerItem _item : items)
		{
			graphics.drawImage(_item.Image, 0, curY, config.iconSize(), config.iconSize(), null);

			final int _stringWidth = _fontMetrics.stringWidth(_item.ItemText);
			final TextComponent _textComponent = new TextComponent();

			_textComponent.setColor(Color.yellow);
			_textComponent.setText(_item.ItemText);
			_textComponent.setOutline(true);
			_textComponent.setPosition(new Point(
				config.iconSize() + H_PADDING, //+ (TEXT_WIDTH - _stringWidth),
				curY + (config.iconSize() / 2) + (_fontHeight / 2)
			));

			_textComponent.render(graphics);

			curY += Math.max(config.iconSize(), _fontHeight);
			maxX = Math.max(maxX, config.iconSize() + (H_PADDING * 2) + _stringWidth);
		}

		return new Dimension(maxX, curY);
	}

	public void addDropToOverlay(LootListerItem _item)
	{
		if (_item.Price < config.minValueToDisplay())
			return;

		items.add(_item);

		if (items.size() > config.maxDisplayedItems())
			items.remove(0);
	}

	public void updateDropItemsCount()
	{
		while(items.size() > config.maxDisplayedItems())
		{
			items.remove(0);
		}
	}

}
