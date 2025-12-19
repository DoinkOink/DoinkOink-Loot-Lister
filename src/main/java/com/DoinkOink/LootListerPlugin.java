package com.DoinkOink;

import com.DoinkOink.Misc.LootListerItem;
import com.DoinkOink.UI.LootListerOverlay;
import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Item;
import net.runelite.api.ItemComposition;
import net.runelite.api.NPCComposition;
import net.runelite.api.Player;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.events.PlayerLootReceived;
import net.runelite.client.events.ServerNpcLoot;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.ItemStack;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.loottracker.LootReceived;
import net.runelite.client.plugins.loottracker.PluginLootReceived;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.OverlayMenuEntry;
import net.runelite.client.util.Text;
import net.runelite.http.api.loottracker.LootRecordType;

import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.List;

import static net.runelite.api.MenuAction.RUNELITE_OVERLAY_CONFIG;
import static net.runelite.client.ui.overlay.OverlayManager.OPTION_CONFIGURE;

@Slf4j
@PluginDescriptor(
	name = "Loot Lister"
)
public class LootListerPlugin extends Plugin {
	@Inject
	private LootListerOverlay listerOverlay;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private ItemManager itemManager;

	@Override
	protected void startUp() throws Exception
	{
		OverlayMenuEntry _menuEntry = new OverlayMenuEntry(RUNELITE_OVERLAY_CONFIG, OPTION_CONFIGURE, "Loot Lister Overlay");

		listerOverlay.getMenuEntries().add(_menuEntry);

		overlayManager.add(listerOverlay);
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		listerOverlay.clearItemsFromOverlay();
	}

	@Subscribe
	public void onConfigChanged(final ConfigChanged event)
	{
		final List<String> _configKeysToClear = List.of(
			"iconSize",
			"maxDisplayedItems",
			"maxDisplayTime",
			"spaceBetweenIconAndText",
			"spaceBetweenItems",
			"sideToAnimateFrom",
			"stackDirection"
		);

		if (_configKeysToClear.contains(event.getKey()))
			listerOverlay.clearItemsFromOverlay();
	}

	//@Subscribe
	//public void onLootReceived(final LootReceived event)
	//{
	//	for (ItemStack _item : event.getItems()) {
	//		ItemComposition _ic = itemManager.getItemComposition(_item.getId());
	//		int _realId = _ic.getNote() == -1 ? _ic.getId() : _ic.getLinkedNoteId();
	//		int _price = Math.max(itemManager.getItemPrice(_realId), _ic.getHaPrice());
	//		BufferedImage _image = itemManager.getImage(_realId, _item.getQuantity(), false);
	//
	//		listerOverlay.addDropToQueue(new LootListerItem(_realId, _ic.getName(), _item.getQuantity(), _price, _image));
	//	}
	//}

	@Subscribe
	public void onServerNpcLoot(final ServerNpcLoot event)
	{
		final NPCComposition _npc = event.getComposition();

		addLoot(_npc, event.getItems());
	}

	@Subscribe
	public void onPlayerLootReceived(final PlayerLootReceived playerLootReceived)
	{
		addLoot(null, playerLootReceived.getItems());
	}

	private void addLoot(NPCComposition _npc, Collection<ItemStack> _items)
	{
		for (ItemStack _item : _items) {
			ItemComposition _ic = itemManager.getItemComposition(_item.getId());
			int _realId = _ic.getNote() == -1 ? _ic.getId() : _ic.getLinkedNoteId();
			int _price = Math.max(itemManager.getItemPrice(_realId), _ic.getHaPrice());
			BufferedImage _image = itemManager.getImage(_realId, _item.getQuantity(), false);

			listerOverlay.addDropToQueue(new LootListerItem(_realId, _npc, _ic.getName(), _item.getQuantity(), _price, _image));
		}
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		listerOverlay.updateOldestItemTimer();
	}

	@Provides
	LootListerConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(LootListerConfig.class);
	}
}
