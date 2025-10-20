package org.violetmoon.quark.content.tools.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.violetmoon.quark.content.tools.module.PathfinderMapsModule;

public record InBiomeCondition(ResourceLocation target) implements LootItemCondition {

	public static final MapCodec<InBiomeCondition> CODEC = RecordCodecBuilder.mapCodec(
			builder -> builder.group(ResourceLocation.CODEC.fieldOf("target").forGetter(InBiomeCondition::target))
					.apply(builder, InBiomeCondition::new));

	@Override
	public boolean test(LootContext lootContext) {
		Vec3 pos = lootContext.getParam(LootContextParams.ORIGIN);
		return lootContext.getLevel().getBiome(BlockPos.containing(pos)).is(target);
	}

	@Override
	@NotNull
	public LootItemConditionType getType() {
		return PathfinderMapsModule.inBiomeConditionType;
	}
}