package org.violetmoon.quark.content.tools.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantment;
import org.jetbrains.annotations.NotNull;
import org.violetmoon.quark.base.QuarkClient;
import org.violetmoon.quark.content.tools.module.AncientTomesModule;
import org.violetmoon.zeta.item.ZetaItem;
import org.violetmoon.zeta.module.ZetaModule;
import org.violetmoon.zeta.registry.CreativeTabManager;
import org.violetmoon.zeta.util.ZetaSide;

import java.util.ArrayList;
import java.util.List;

public class AncientTomeItem extends ZetaItem implements CreativeTabManager.AppendsUniquely {

	public AncientTomeItem(ZetaModule module) {
		super("ancient_tome", module, new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
		CreativeTabManager.addToCreativeTab(CreativeModeTabs.INGREDIENTS, this);
	}

	@Override
	public boolean isEnchantable(@NotNull ItemStack stack) {
		return false;
	}

	@Override
	public boolean isFoil(@NotNull ItemStack stack) {
		return true;
	}

	public static ItemStack getEnchantedItemStack(Holder<Enchantment> ench) {
		ItemStack stack = new ItemStack(AncientTomesModule.ancient_tome);
		stack.enchant(ench, ench.value().getMaxLevel());
		return stack;
	}

	public static Component getFullTooltipText(Holder<Enchantment> ench) {
		return Component.translatable("quark.misc.ancient_tome_tooltip", Component.translatable(ench.value().description().getString()), Component.translatable("enchantment.level." + (ench.value().getMaxLevel() + 1))).withStyle(ChatFormatting.GRAY);
	}

	@Override
	public void appendHoverText(@NotNull ItemStack stack, Item.TooltipContext tooltipContext, @NotNull List<Component> tooltip, @NotNull TooltipFlag flagIn) {
		super.appendHoverText(stack, tooltipContext, tooltip, flagIn);

		Holder<Enchantment> ench = AncientTomesModule.getTomeEnchantment(stack);
		if(ench != null)
			tooltip.add(getFullTooltipText(ench));
		else
			tooltip.add(Component.translatable("quark.misc.ancient_tome_tooltip_any").withStyle(ChatFormatting.GRAY));

		if(AncientTomesModule.curseGear) {
			tooltip.add(Component.translatable("quark.misc.ancient_tome_tooltip_curse").withStyle(ChatFormatting.RED));
		}
	}

	@Override
	public List<ItemStack> appendItemsToCreativeTab() {
		List<ItemStack> items = new ArrayList<>();

		if (getModule().zeta().side == ZetaSide.CLIENT) {
			QuarkClient.ZETA_CLIENT.hackilyGetCurrentClientLevelRegistryAccess().registry(Registries.ENCHANTMENT).get().asHolderIdMap().forEach(ench -> {
				if (!AncientTomesModule.sanityCheck || ench.value().getMaxLevel() != 1) {
					if (!AncientTomesModule.isInitialized() || AncientTomesModule.validEnchants.contains(ench)) {
						items.add(getEnchantedItemStack(ench));
					}
				}
			});
        }
        return items;
    }
}
