package org.violetmoon.quark.addons.oddities.module;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import org.violetmoon.quark.addons.oddities.block.CrateBlock;
import org.violetmoon.quark.addons.oddities.block.be.CrateBlockEntity;
import org.violetmoon.quark.addons.oddities.client.screen.CrateScreen;
import org.violetmoon.quark.addons.oddities.inventory.CrateMenu;
import org.violetmoon.quark.base.Quark;
import org.violetmoon.zeta.client.event.load.ZClientSetup;
import org.violetmoon.zeta.config.Config;
import org.violetmoon.zeta.event.bus.LoadEvent;
import org.violetmoon.zeta.event.load.ZRegister;
import org.violetmoon.zeta.module.ZetaLoadModule;
import org.violetmoon.zeta.module.ZetaModule;
import org.violetmoon.zeta.util.Hint;

@ZetaLoadModule(category = "oddities")
public class CrateModule extends ZetaModule {

	public static BlockEntityType<CrateBlockEntity> blockEntityType;
	public static MenuType<CrateMenu> menuType;

	@Hint(content = "maxItems")
	public static Block crate;

	@Config
	public static int maxItems = 640;

	@LoadEvent
	public final void register(ZRegister event) {
		crate = new CrateBlock(this);

		menuType = IMenuTypeExtension.create(CrateMenu::fromNetwork);
		Quark.ZETA.registry.register(menuType, "crate", Registries.MENU);

		blockEntityType = BlockEntityType.Builder.of(CrateBlockEntity::new, crate).build(null);
		Quark.ZETA.registry.register(blockEntityType, "crate", Registries.BLOCK_ENTITY_TYPE);
	}

	@ZetaLoadModule(clientReplacement = true)
	public static class Client extends CrateModule {
		@LoadEvent
		public final void clientSetup(ZClientSetup event) {
			MenuScreens.register(menuType, CrateScreen::new);
		}
	}
}
