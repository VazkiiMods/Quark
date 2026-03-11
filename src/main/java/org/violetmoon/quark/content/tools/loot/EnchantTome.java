package org.violetmoon.quark.content.tools.loot;

import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import org.jetbrains.annotations.NotNull;

import org.violetmoon.quark.content.tools.item.AncientTomeItem;
import org.violetmoon.quark.content.tools.module.AncientTomesModule;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author WireSegal
 *         Created at 1:48 PM on 7/4/20.
 */
public class EnchantTome extends LootItemConditionalFunction {
	public static final MapCodec<EnchantTome> CODEC = RecordCodecBuilder.mapCodec(
	instance -> commonFields(instance)
			.and(
					instance.group(
							RegistryCodecs.homogeneousList(Registries.ENCHANTMENT).optionalFieldOf("options").forGetter(thisEnchantTomeFunction -> thisEnchantTomeFunction.options),
							Codec.BOOL.optionalFieldOf("only_valid", false).forGetter(thisEnchantTomeFunction -> thisEnchantTomeFunction.onlyValid)
					)
			)
			.apply(instance, EnchantTome::new)
	);

	private final Optional<HolderSet<Enchantment>> options;
	private final boolean onlyValid;

	public EnchantTome(List<LootItemCondition> conditions, Optional<HolderSet<Enchantment>> options, boolean onlyValid) {
		super(conditions);
		this.options = options;
		this.onlyValid = onlyValid;
	}

	@Override
	@NotNull
	public LootItemFunctionType<EnchantTome> getType() {
		return AncientTomesModule.tomeEnchantType; //quark:tome_enchant
	}

	@Override
	@NotNull
	public ItemStack run(@NotNull ItemStack stack, LootContext context) {
		if(options.isEmpty()){
			//AncientTomesModule::onLootTableLoad will use this route,
			//or it will default to this if you don't specify "options" in a loot table
			Holder<Enchantment> enchantment = AncientTomesModule.validEnchants.get(context.getRandom().nextInt(AncientTomesModule.validEnchants.size()));
			return AncientTomeItem.getEnchantedItemStack(enchantment);
		}
		else{
			List<Holder<Enchantment>> possibleEnchantments = new ArrayList<>();
			for(Holder<Enchantment> enchantment : options.get()){
				if(onlyValid){
					//if true, the function should check the config to make sure the enchantment is valid
					if(AncientTomesModule.validEnchants.contains(enchantment)) {
						if (!(AncientTomesModule.sanityCheck && enchantment.value().getMaxLevel() == 1)) {
							possibleEnchantments.add(enchantment);
						}
					}
				}
				else {
					possibleEnchantments.add(enchantment);
				}
			}
			Holder<Enchantment> enchantment = possibleEnchantments.get(context.getRandom().nextInt(possibleEnchantments.size()));
			return AncientTomeItem.getEnchantedItemStack(enchantment);
		}
    }
}
