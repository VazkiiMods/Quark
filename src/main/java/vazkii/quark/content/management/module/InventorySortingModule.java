package vazkii.quark.content.management.module;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import vazkii.quark.base.QuarkClient;
import vazkii.quark.base.client.handler.InventoryButtonHandler;
import vazkii.quark.base.module.config.Config;
import vazkii.quark.base.network.QuarkNetwork;
import vazkii.quark.base.network.message.SortInventoryMessage;
import vazkii.quark.content.management.client.screen.widgets.MiniInventoryButton;
import vazkii.zeta.client.event.ZKeyMapping;
import vazkii.zeta.event.bus.LoadEvent;
import vazkii.zeta.module.ZetaLoadModule;
import vazkii.zeta.module.ZetaModule;

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

	public static class Client extends InventorySortingModule {
		@LoadEvent
		public void registerKeybinds(ZKeyMapping event) {
			KeyMapping sortPlayer = event.init("quark.keybind.sort_player", null, QuarkClient.INV_GROUP);

			InventoryButtonHandler.addButtonProvider(this, InventoryButtonHandler.ButtonTargetType.PLAYER_INVENTORY, 0,
					sortPlayer,
					(screen) -> {
						if (enablePlayerInventory) {
							if (satisfyingClick)
								click();
							QuarkNetwork.sendToServer(new SortInventoryMessage(true));
						}
					},
					provider("sort", true, () -> enablePlayerInventory),
					() -> enablePlayerInventory);

			InventoryButtonHandler.addButtonProvider(this, InventoryButtonHandler.ButtonTargetType.CONTAINER_PLAYER_INVENTORY, 0,
					sortPlayer,
					(screen) -> {
						if (enablePlayerInventoryInChests) {
							if (satisfyingClick)
								click();
							QuarkNetwork.sendToServer(new SortInventoryMessage(true));
						}
					},
					provider("sort_inventory", true, () -> enablePlayerInventoryInChests),
					() -> enablePlayerInventoryInChests);

			InventoryButtonHandler.addButtonProvider(event, this, InventoryButtonHandler.ButtonTargetType.CONTAINER_INVENTORY, 0,
					"quark.keybind.sort_container",
					(screen) -> {
						if (enableChests) {
							if (satisfyingClick)
								click();
							QuarkNetwork.sendToServer(new SortInventoryMessage(false));
						}
					},
					provider("sort_container", false, () -> enableChests),
					() -> enableChests);
		}

		private InventoryButtonHandler.ButtonProvider provider(String tooltip, boolean forcePlayer, BooleanSupplier condition) {
			return (parent, x, y) -> !condition.getAsBoolean() ? null :
					new MiniInventoryButton(parent, 0, x, y, "quark.gui.button." + tooltip, (b) -> QuarkNetwork.sendToServer(new SortInventoryMessage(forcePlayer)));
		}

		private void click() {
			Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
		}
	}
}
