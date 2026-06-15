package org.violetmoon.quark.content.tools.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;
import org.violetmoon.quark.content.tools.item.PathfindersQuillItem;
import org.violetmoon.quark.content.tools.module.PathfinderMapsModule;

import java.util.List;

public class PathfindersQuillFunction extends LootItemConditionalFunction {
	public static final MapCodec<PathfindersQuillFunction> CODEC = RecordCodecBuilder.mapCodec(
	instance -> commonFields(instance)
			.apply(instance, PathfindersQuillFunction::new)
	);

	public PathfindersQuillFunction(List<LootItemCondition> conditions) {
		super(conditions);
	}

	@Override
	@NotNull
	public LootItemFunctionType<PathfindersQuillFunction> getType() {
		return PathfinderMapsModule.pathfindersQuillFunction; //quark:pathfinders_quill
	}

	@Override
	@NotNull
	public ItemStack run(@NotNull ItemStack stack, LootContext context) {
		PathfindersQuillItem item = (PathfindersQuillItem) PathfinderMapsModule.pathfinders_quill;
		List<ItemStack> stacks = item.appendItemsToCreativeTab(null);
		ItemStack newStack = stacks.get(context.getRandom().nextInt(stacks.size()));

		return newStack;
    }
}
