package org.violetmoon.quark.base.components;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;

import java.util.function.UnaryOperator;

public class QuarkDataComponents {
    public static final DataComponentType<Boolean> IS_PATHFINDER = register(
            ResourceLocation.fromNamespaceAndPath("quark", "is_pathfinder"), builder -> builder.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL)
    );
    public static final DataComponentType<Boolean> IS_ANGRY = register(
            ResourceLocation.fromNamespaceAndPath("quark", "is_angry"), builder -> builder.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL)
    );

    public static final DataComponentType<Integer> TAG_POS_X = register(
            ResourceLocation.fromNamespaceAndPath("quark", "tag_pos_x"), builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.INT)
    );
    public static final DataComponentType<Integer> TAG_POS_Y = register(
            ResourceLocation.fromNamespaceAndPath("quark", "tag_pos_y"), builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.INT)
    );

    private static <T> DataComponentType<T> register(ResourceLocation id, UnaryOperator<DataComponentType.Builder<T>> builderOp) {
        return Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, id, builderOp.apply(DataComponentType.builder()).build());
    }
}
