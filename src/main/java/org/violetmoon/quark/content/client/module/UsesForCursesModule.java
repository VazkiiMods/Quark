package org.violetmoon.quark.content.client.module;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.renderer.entity.ArmorStandRenderer;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Blocks;

import org.violetmoon.quark.content.tweaks.client.layer.ArmorStandFakePlayerLayer;
import org.violetmoon.zeta.client.event.load.ZAddModelLayers;
import org.violetmoon.zeta.config.Config;
import org.violetmoon.zeta.event.bus.LoadEvent;
import org.violetmoon.zeta.event.load.ZConfigChanged;
import org.violetmoon.zeta.module.ZetaLoadModule;
import org.violetmoon.zeta.module.ZetaModule;
import org.violetmoon.zeta.util.Hint;

import java.util.Arrays;
import java.util.List;

@ZetaLoadModule(category = "client")
public class UsesForCursesModule extends ZetaModule {

	private static final ResourceLocation PUMPKIN_OVERLAY = ResourceLocation.withDefaultNamespace("textures/misc/pumpkinblur.png");

	public static boolean staticEnabled;

	@Config(flag = "use_for_vanishing")
	public static boolean vanishPumpkinOverlay = true;

	@Config(flag = "use_for_binding")
	public static boolean bindArmorStandsWithPlayerHeads = true;

	@Hint(key = "use_for_vanishing", value = "use_for_vanishing")
	Item pumpkin = Items.CARVED_PUMPKIN;

	@Hint(key = "use_for_binding", value = "use_for_binding")
	List<Item> bindingItems = Arrays.asList(Items.ARMOR_STAND, Items.PLAYER_HEAD);

	@LoadEvent
	public final void configChanged(ZConfigChanged event) {
		staticEnabled = isEnabled();
	}

	public static boolean shouldHideArmorStandModel(ItemStack stack) {
		ClientPacketListener listener = Minecraft.getInstance().getConnection();
		if (listener == null || !staticEnabled || !bindArmorStandsWithPlayerHeads || !stack.is(Items.PLAYER_HEAD)) return false;
		return EnchantmentHelper.getTagEnchantmentLevel(listener.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.VANISHING_CURSE), stack) > 0;
	}

	public static boolean shouldHidePumpkinOverlay(ResourceLocation location, Player player) {
		if(!staticEnabled || !vanishPumpkinOverlay || !location.equals(PUMPKIN_OVERLAY)) return false;
		ItemStack stack = player.getInventory().getArmor(3);
		Holder<Enchantment> vanishing = player.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.VANISHING_CURSE);
		return stack.is(Blocks.CARVED_PUMPKIN.asItem()) && EnchantmentHelper.getTagEnchantmentLevel(vanishing, stack) > 0;
	}

	@ZetaLoadModule(clientReplacement = true)
	public static class Client extends UsesForCursesModule {

		@LoadEvent
		public void modelLayers(ZAddModelLayers event) {
			ArmorStandRenderer render = event.getRenderer(EntityType.ARMOR_STAND);
			render.addLayer(new ArmorStandFakePlayerLayer<>(render, event.getEntityModels()));
		}

	}

}
