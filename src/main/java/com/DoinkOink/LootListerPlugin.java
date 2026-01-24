package com.DoinkOink;

import com.DoinkOink.Misc.LootListManager;
import com.DoinkOink.Misc.LootListerItem;
import com.DoinkOink.UI.LootListerOverlay;
import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ItemComposition;
import net.runelite.api.NPCComposition;
import net.runelite.api.events.ClientTick;
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
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.OverlayMenuEntry;

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
	private LootListerConfig config;

	@Inject
	private LootListerOverlay listerOverlay;

	private LootListManager lootListManager;

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

		lootListManager = new LootListManager(config, listerOverlay);
		listerOverlay.manager = lootListManager;
	}

	@Override
	protected void shutDown() throws Exception
	{
		lootListManager.clearItemsFromOverlay();

		overlayManager.remove(listerOverlay);
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		lootListManager.clearItemsFromOverlay();
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
			lootListManager.setAllItemsForRemoval();

		if (event.getKey().equals("npcFilters"))
			lootListManager.updateNpcFilters();
	}

	@Subscribe
	public void onGameTick(GameTick event) { lootListManager.onTick(); }

	@Subscribe
	public void onClientTick(ClientTick event) { lootListManager.updateItems(); }

	// Add loot to the queue when an NPC is killed/looted
	@Subscribe
	public void onServerNpcLoot(final ServerNpcLoot event)
	{
		final NPCComposition _npc = event.getComposition();

		addLoot(_npc, event.getItems());
	}

	// Add loot to the queue when a player is killed in PvP
	@Subscribe
	public void onPlayerLootReceived(final PlayerLootReceived playerLootReceived)
	{
		addLoot(null, playerLootReceived.getItems());
	}

	// Receives the npc and the loot it dropped and creates a LootListerItem per
	//	item that was dropped and add it to the item queue.
	private void addLoot(NPCComposition _npc, Collection<ItemStack> _items)
	{
		for (ItemStack _item : _items) {
			ItemComposition _ic = itemManager.getItemComposition(_item.getId());
			int _realId = _ic.getNote() == -1 ? _ic.getId() : _ic.getLinkedNoteId();
			int _price = Math.max(itemManager.getItemPrice(_realId), _ic.getHaPrice());
			BufferedImage _image = itemManager.getImage(_realId, _item.getQuantity(), false);

			lootListManager.addDropToQueue(new LootListerItem(_realId, _npc, _ic.getName(), _item.getQuantity(), _price, _image));
		}
	}

	@Provides
	LootListerConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(LootListerConfig.class);
	}
}
