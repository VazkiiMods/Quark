package org.violetmoon.quark.content.tools.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.violetmoon.quark.content.tools.module.AncientTomesModule;

import javax.annotation.Nonnull;

import static org.violetmoon.quark.content.tools.module.AncientTomesModule.validEnchants;

/**
 * @author WireSegal
 * Created at 1:48 PM on 7/4/20.
 */
public class EnchantTome extends LootItemConditionalFunction {
	public EnchantTome(LootItemCondition[] conditions) {
		super(conditions);
	}

	@Override
	@Nonnull
	public LootItemFunctionType getType() {
		return AncientTomesModule.tomeEnchantType;
	}

	@Override
	@Nonnull
	public ItemStack run(@Nonnull ItemStack stack, LootContext context) {
		Enchantment enchantment = validEnchants.get(context.getRandom().nextInt(validEnchants.size()));
		EnchantedBookItem.addEnchantment(stack, new EnchantmentInstance(enchantment, enchantment.getMaxLevel()));
		return stack;
	}

	public static class Serializer extends LootItemConditionalFunction.Serializer<EnchantTome> {
		@Override
		@Nonnull
		public EnchantTome deserialize(@Nonnull JsonObject object, @Nonnull JsonDeserializationContext deserializationContext, @Nonnull LootItemCondition[] conditionsIn) {
			return new EnchantTome(conditionsIn);
		}
	}
}
