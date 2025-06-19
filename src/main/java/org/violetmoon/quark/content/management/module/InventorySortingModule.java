package org.violetmoon.quark.content.management.module;

import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;

import org.violetmoon.quark.base.QuarkClient;
import org.violetmoon.quark.base.client.handler.InventoryButtonHandler;
import org.violetmoon.quark.base.network.message.SortInventoryMessage;
import org.violetmoon.quark.content.management.client.screen.widgets.MiniInventoryButton;
import org.violetmoon.zeta.client.event.load.ZKeyMapping;
import org.violetmoon.zeta.config.Config;
import org.violetmoon.zeta.event.bus.LoadEvent;
import org.violetmoon.zeta.module.ZetaLoadModule;
import org.violetmoon.zeta.module.ZetaModule;

import java.util.function.BooleanSupplier;

@ZetaLoadModule(category = "management")
public class InventorySortingModule extends ZetaModule {

	@Config
	public static boolean enablePlayerInventory = true;
	@Config
	public static boolean enablePlayerInventoryInChests = true;
	@Config
	public static boolean enableChests = true;
	@Config(description = "Play a click when sorting inventories using keybindings")
	public static boolean satisfyingClick = true;

	@ZetaLoadModule(clientReplacement = true)
	public static class Client extends InventorySortingModule {
		@LoadEvent
		public void registerKeybinds(ZKeyMapping event) {
			KeyMapping sortPlayer = event.init("quark.keybind.sort_player", null, QuarkClient.INV_GROUP);

			InventoryButtonHandler.addButtonProvider(this, InventoryButtonHandler.ButtonTargetType.PLAYER_INVENTORY, 0,
					sortPlayer,
					(screen) -> {
						if(enablePlayerInventory) {
							if(satisfyingClick)
								click();
							CatnipServices.NETWORK.sendToServer(new SortInventoryMessage(true));
						}
					},
					provider("sort", true, () -> enablePlayerInventory),
					() -> enablePlayerInventory);

			InventoryButtonHandler.addButtonProvider(this, InventoryButtonHandler.ButtonTargetType.CONTAINER_PLAYER_INVENTORY, 0,
					sortPlayer,
					(screen) -> {
						if(enablePlayerInventoryInChests) {
							if(satisfyingClick)
								click();
							CatnipServices.NETWORK.sendToServer(new SortInventoryMessage(true));
						}
					},
					provider("sort_inventory", true, () -> enablePlayerInventoryInChests),
					() -> enablePlayerInventoryInChests);

			InventoryButtonHandler.addButtonProvider(event, this, InventoryButtonHandler.ButtonTargetType.CONTAINER_INVENTORY, 0,
					"quark.keybind.sort_container",
					(screen) -> {
						if(enableChests) {
							if(satisfyingClick)
								click();
							CatnipServices.NETWORK.sendToServer(new SortInventoryMessage(false));
						}
					},
					provider("sort_container", false, () -> enableChests),
					() -> enableChests);
		}

		private InventoryButtonHandler.ButtonProvider provider(String tooltip, boolean forcePlayer, BooleanSupplier condition) {
			return (parent, x, y) -> !condition.getAsBoolean() ? null : new MiniInventoryButton(parent, 0, tooltip.equals("sort_container") ? parent.getXSize() - 18 : x, tooltip.equals("sort_container") ? 5 : y, "quark.gui.button." + tooltip, (b) -> CatnipServices.NETWORK.sendToServer(new SortInventoryMessage(forcePlayer)));
		}

		private void click() {
			Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
		}
	}
}
