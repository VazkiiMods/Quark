package org.violetmoon.quark.base.proxy;

import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.base.capability.CapabilityHandler;
import org.violetmoon.quark.base.components.QuarkDataComponents;
import org.violetmoon.quark.base.config.QuarkGeneralConfig;
import org.violetmoon.quark.base.handler.ContributorRewardHandler;
import org.violetmoon.quark.base.handler.QuarkRemapHandler;
import org.violetmoon.quark.base.handler.QuarkSounds;
import org.violetmoon.quark.base.handler.WoodSetHandler;
import org.violetmoon.quark.base.network.QuarkNetwork;
import org.violetmoon.quark.base.recipe.ExclusionRecipe;
import org.violetmoon.quark.content.building.recipe.MixedExclusionRecipe;
import org.violetmoon.zeta.event.bus.LoadEvent;
import org.violetmoon.zeta.event.load.ZRegister;
import org.violetmoon.zeta.module.ZetaCategory;
import org.violetmoon.zetaimplforge.module.ModFileScanDataModuleFinder;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;

public class CommonProxy {

	public static boolean jingleTheBells = false;

	public void start() {
		// GLOBAL EVENT LISTENERS
		Quark.ZETA.loadBus
				.subscribe(ContributorRewardHandler.class)
				.subscribe(QuarkSounds.class)
				.subscribe(WoodSetHandler.class)
				.subscribe(QuarkDataComponents.class)
				.subscribe(QuarkRemapHandler.class)
				.subscribe(this);

		Quark.ZETA.playBus
				.subscribe(CapabilityHandler.class)
				.subscribe(ContributorRewardHandler.class);

		// OTHER RANDOM SHIT
		QuarkNetwork.init();

		// MODULES
		Quark.ZETA.loadModules(
				List.of(
						new ZetaCategory("automation", Items.REDSTONE),
						new ZetaCategory("building", Items.BRICKS),
						new ZetaCategory("management", Items.CHEST),
						new ZetaCategory("tools", Items.IRON_PICKAXE),
						new ZetaCategory("tweaks", Items.NAUTILUS_SHELL),
						new ZetaCategory("world", Items.GRASS_BLOCK),
						new ZetaCategory("mobs", Items.PIG_SPAWN_EGG),
						new ZetaCategory("client", Items.ENDER_EYE),
						new ZetaCategory("experimental", Items.TNT),
						new ZetaCategory("oddities", Items.CHORUS_FRUIT, Quark.ODDITIES_ID)
				),
				new ModFileScanDataModuleFinder(Quark.MOD_ID), //forge only
				QuarkGeneralConfig.INSTANCE
		);

		LocalDateTime now = LocalDateTime.now();
		if(now.getMonth() == Month.DECEMBER && now.getDayOfMonth() >= 16 || now.getMonth() == Month.JANUARY && now.getDayOfMonth() <= 2)
			jingleTheBells = true;
	}

	//TODO find a better place for this little one-off thing, lol
	@LoadEvent
	public void recipe(ZRegister event) {
		event.getRegistry().register(ExclusionRecipe.SERIALIZER, "exclusion", Registries.RECIPE_SERIALIZER);
		event.getRegistry().register(MixedExclusionRecipe.SERIALIZER, "mixed_exclusion", Registries.RECIPE_SERIALIZER);
	}

	/**
	 * Use an item WITHOUT sending the use to the server. This will cause ghost interactions if used incorrectly!
	 */
	public InteractionResult clientUseItem(Player player, Level level, InteractionHand hand, BlockHitResult hit) {
		return InteractionResult.PASS;
	}

	public boolean isClientPlayerHoldingShift() {
		return false;
	}

	public float getVisualTime() {
		return 0f;
	}

	public @Nullable RegistryAccess hackilyGetCurrentClientLevelRegistryAccess() {
		return null;
	}
}
