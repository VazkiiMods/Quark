package org.violetmoon.quark.content.tools.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.jetbrains.annotations.NotNull;
import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.base.QuarkClient;
import org.violetmoon.quark.base.components.QuarkDataComponents;
import org.violetmoon.quark.content.tools.module.AncientTomesModule;
import org.violetmoon.zeta.item.ZetaItem;
import org.violetmoon.zeta.module.ZetaModule;
import org.violetmoon.zeta.registry.CreativeTabManager;

import java.util.ArrayList;
import java.util.List;

public class AncientTomeItem extends ZetaItem implements CreativeTabManager.AppendsUniquely {

	public AncientTomeItem(ZetaModule module) {
		super("ancient_tome", module, new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
		CreativeTabManager.addToTab(CreativeModeTabs.INGREDIENTS, this);
	}

	@Override
	public boolean isEnchantable(@NotNull ItemStack stack) {
		return false;
	}

	@Override
	public boolean isFoil(@NotNull ItemStack stack) {
		return true;
	}

	public static ItemStack getEnchantedItemStack(Holder<Enchantment> enchantment) {
		ItemStack stack = new ItemStack(AncientTomesModule.ancient_tome);
        ItemEnchantments.Mutable enchantments = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
        enchantments.set(enchantment, enchantment.value().getMaxLevel());
        stack.set(QuarkDataComponents.TOME_ENCHANTMENTS, enchantments.toImmutable());
		return stack;
	}

	public static Component getFullTooltipText(Holder<Enchantment> ench) {
		return Component.translatable("quark.misc.ancient_tome_tooltip", Component.translatable(ench.value().description().getString()), Component.translatable("enchantment.level." + (ench.value().getMaxLevel() + AncientTomesModule.maxLimitBreakLevels))).withStyle(ChatFormatting.GRAY);
	}

	@Override
	public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext tooltipContext, @NotNull List<Component> tooltips, @NotNull TooltipFlag flag) {
		super.appendHoverText(stack, tooltipContext, tooltips, flag);
		Holder<Enchantment> ench = AncientTomesModule.getTomeEnchantment(stack);
        Component component = ench != null ? getFullTooltipText(ench) : Component.translatable("quark.misc.ancient_tome_tooltip_any").withStyle(ChatFormatting.GRAY);
        tooltips.add(component);

		if (AncientTomesModule.curseGear) {
			tooltips.add(Component.translatable("quark.misc.ancient_tome_tooltip_curse").withStyle(ChatFormatting.RED));
		}
	}

	@Override
	public List<ItemStack> appendItemsToCreativeTab(RegistryAccess access) {
		List<ItemStack> items = new ArrayList<>();

        access.registry(Registries.ENCHANTMENT).get().asHolderIdMap().forEach(ench -> {
            if (!AncientTomesModule.sanityCheck || ench.value().getMaxLevel() != 1) {
                if (!AncientTomesModule.isInitialized() && AncientTomesModule.validEnchants.contains(ench)) {
                    items.add(getEnchantedItemStack(ench));
                }
            }
        });

        return items;
    }
}
