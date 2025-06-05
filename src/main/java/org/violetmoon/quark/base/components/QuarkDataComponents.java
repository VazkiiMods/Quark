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
import net.minecraft.world.item.ItemStack;

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

    public static final DataComponentType<String> SKULL_OWNER = register(
            ResourceLocation.fromNamespaceAndPath("quark", "skull_owner"), builder -> builder.persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8)
    );

    public static final DataComponentType<String> TAG_RUNE_COLOR = register(
            ResourceLocation.fromNamespaceAndPath("quark", "rune_color"), builder -> builder.persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8)
    );

    public static final DataComponentType<Long> TAG_PLACING_SEED = register(
            ResourceLocation.fromNamespaceAndPath("quark", "placing_seed"), builder -> builder.persistent(Codec.LONG).networkSynchronized(ByteBufCodecs.VAR_LONG)
    );

    public static final DataComponentType<ItemStack> TAG_LAST_STACK = register(
            ResourceLocation.fromNamespaceAndPath("quark", "last_stack"), builder -> builder.persistent(ItemStack.CODEC).networkSynchronized(ItemStack.STREAM_CODEC)
    );

    public static final DataComponentType<String> TAG_BIOME = register(
            ResourceLocation.fromNamespaceAndPath("quark", "targetBiome"), builder -> builder.persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8)
    );

    public static final DataComponentType<Integer> TAG_COLOR = register(
            ResourceLocation.fromNamespaceAndPath("quark", "targetBiomeColor"), builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.INT)
    );

    public static final DataComponentType<Boolean> IS_UNDERGROUND = register(
            ResourceLocation.fromNamespaceAndPath("quark", "targetBiomeUnderground"), builder -> builder.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL)
    );

    public static final DataComponentType<Boolean> IS_SEARCHING = register(
            ResourceLocation.fromNamespaceAndPath("quark", "isSearchingForBiome"), builder -> builder.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL)
    );

    public static final DataComponentType<Integer> TAG_SOURCE_X = register(
            ResourceLocation.fromNamespaceAndPath("quark", "searchSourceX"), builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.INT)
    );

    public static final DataComponentType<Integer> TAG_SOURCE_Z = register(
            ResourceLocation.fromNamespaceAndPath("quark", "searchSourceZ"), builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.INT)
    );

    public static final DataComponentType<Integer> TAG_POS_X = register(
            ResourceLocation.fromNamespaceAndPath("quark", "searchPosX"), builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.INT)
    );

    public static final DataComponentType<Integer> TAG_POS_Z = register(
            ResourceLocation.fromNamespaceAndPath("quark", "searchPosZ"), builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.INT)
    );

    public static final DataComponentType<Integer> TAG_POS_LEG = register(
            ResourceLocation.fromNamespaceAndPath("quark", "searchPosLeg"), builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.INT)
    );

    public static final DataComponentType<Integer> TAG_POS_LEG_INDEX = register(
            ResourceLocation.fromNamespaceAndPath("quark", "searchPosLegIndex"), builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.INT)
    );

    private static <T> DataComponentType<T> register(ResourceLocation id, UnaryOperator<DataComponentType.Builder<T>> builderOp) {
        return Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, id, builderOp.apply(DataComponentType.builder()).build());
    }
}
