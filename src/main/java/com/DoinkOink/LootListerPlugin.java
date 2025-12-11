package com.DoinkOink;

import com.DoinkOink.Misc.LootListerItem;
import com.DoinkOink.UI.LootListerOverlay;
import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.ItemComposition;
import net.runelite.api.MenuAction;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.ItemStack;
import net.runelite.client.game.SkillIconManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.loottracker.LootReceived;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.OverlayMenuEntry;
import net.runelite.client.util.AsyncBufferedImage;

import java.awt.image.BufferedImage;

import static net.runelite.api.MenuAction.RUNELITE_OVERLAY_CONFIG;
import static net.runelite.client.ui.overlay.OverlayManager.OPTION_CONFIGURE;

@Slf4j
@PluginDescriptor(
	name = "Loot Lister"
)
public class LootListerPlugin extends Plugin {
	@Inject
	private Client client;

	@Inject
	private LootListerConfig config;

	@Inject
	private LootListerOverlay listerOverlay;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private ItemManager itemManager;

	@Inject
	private SkillIconManager iconManager;

	@Override
	protected void startUp() throws Exception
	{
		OverlayMenuEntry _menuEntry = new OverlayMenuEntry(RUNELITE_OVERLAY_CONFIG, OPTION_CONFIGURE, "Loot Lister Overlay");

		listerOverlay.getMenuEntries().add(_menuEntry);

		overlayManager.add(listerOverlay);
	}

	//@Override
	//protected void shutDown() throws Exception
	//{
	//	log.debug("Example stopped!");
	//}

	//@Subscribe
	//public void onGameStateChanged(GameStateChanged gameStateChanged)
	//{
	//	if (gameStateChanged.getGameState() == GameState.LOGGED_IN)
	//	{
	//
	//	}
	//}

	@Subscribe
	public void onConfigChanged(final ConfigChanged event)
	{
		listerOverlay.updateDropItemsCount();
	}

	@Subscribe
	public void onLootReceived(final LootReceived event)
	{
		for (ItemStack _item : event.getItems()) {
			ItemComposition _ic = itemManager.getItemComposition(_item.getId());
			int _realId = _ic.getNote() == -1 ? _ic.getId() : _ic.getLinkedNoteId();
			int _price = itemManager.getItemPrice(_realId);
			BufferedImage _image = itemManager.getImage(_realId, _item.getQuantity(), false);

			listerOverlay.addDropToQueue(new LootListerItem(_realId, _ic.getName(), _item.getQuantity(), _price, _image));
		}
	}

	@Provides
	LootListerConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(LootListerConfig.class);
	}
}
