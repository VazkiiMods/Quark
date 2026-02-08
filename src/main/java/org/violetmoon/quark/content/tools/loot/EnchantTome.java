package org.violetmoon.quark.content.tools.loot;

import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.functions.ToggleTooltips;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import org.jetbrains.annotations.NotNull;

import org.violetmoon.quark.content.tools.item.AncientTomeItem;
import org.violetmoon.quark.content.tools.module.AncientTomesModule;

import java.util.List;
import java.util.stream.Stream;

import static org.violetmoon.quark.content.tools.module.AncientTomesModule.validEnchants;

/**
 * @author WireSegal
 *         Created at 1:48 PM on 7/4/20.
 */
public class EnchantTome extends LootItemConditionalFunction {
	public static final MapCodec<EnchantTome> CODEC = RecordCodecBuilder.mapCodec(
	instance -> commonFields(instance)
			.apply(instance, EnchantTome::new)
	);
	public EnchantTome(List<LootItemCondition> conditions) {
		super(conditions);
	}

	@Override
	@NotNull
	public LootItemFunctionType<EnchantTome> getType() {
		return AncientTomesModule.tomeEnchantType;
	}

	@Override
	@NotNull
	public ItemStack run(@NotNull ItemStack stack, LootContext context) {
		Holder<Enchantment> enchantment = validEnchants.get(context.getRandom().nextInt(validEnchants.size()));
        return AncientTomeItem.getEnchantedItemStack(enchantment);
	}
}
