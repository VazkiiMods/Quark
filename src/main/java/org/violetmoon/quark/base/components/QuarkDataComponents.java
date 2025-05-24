package org.violetmoon.quark.base.components;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Unit;

import java.util.function.UnaryOperator;

public class QuarkDataComponents {

    public static final DataComponentType<Boolean> IS_PATHFINDER = register(
            ResourceLocation.fromNamespaceAndPath("quark", "is_pathfinder"), builder -> builder.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL)
    );
    public static final DataComponentType<Boolean> IS_ANGRY = register(
            ResourceLocation.fromNamespaceAndPath("quark", "is_angry"), builder -> builder.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL)
    );

    public static final DataComponentType<BlockPos> BOUNDS_POS = register(
            ResourceLocation.fromNamespaceAndPath("quark", "bounds_pos"), builder -> builder.persistent(BlockPos.CODEC).networkSynchronized(BlockPos.STREAM_CODEC)
    );

    public static final DataComponentType<Unit> QUARK_MUSIC_DISC = register(
            ResourceLocation.fromNamespaceAndPath("quark", "quark_music_disc"), builder -> builder.persistent(Unit.CODEC).networkSynchronized(StreamCodec.unit(Unit.INSTANCE))
    );

    private static <T> DataComponentType<T> register(ResourceLocation id, UnaryOperator<DataComponentType.Builder<T>> builderOp) {
        return Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, id, builderOp.apply(DataComponentType.builder()).build());
    }
}
