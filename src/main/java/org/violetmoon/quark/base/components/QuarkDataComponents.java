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

    // Tater
    public static final DataComponentType<Boolean> IS_ANGRY = register(
            ResourceLocation.fromNamespaceAndPath("quark", "is_angry"), builder -> builder.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL)
    );

    // Abacus
    public static final DataComponentType<BlockPos> BOUNDS_POS = register(
            ResourceLocation.fromNamespaceAndPath("quark", "bounds_pos"), builder -> builder.persistent(BlockPos.CODEC).networkSynchronized(BlockPos.STREAM_CODEC)
    );

    public static final DataComponentType<Unit> QUARK_MUSIC_DISC = register(
            ResourceLocation.fromNamespaceAndPath("quark", "quark_music_disc"), builder -> builder.persistent(Unit.CODEC).networkSynchronized(StreamCodec.unit(Unit.INSTANCE))
    );

    //Snow golem player heads
    public static final DataComponentType<String> SKULL_OWNER = register(
            ResourceLocation.fromNamespaceAndPath("quark", "skull_owner"), builder -> builder.persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8)
    );

    // Runes
    public static final DataComponentType<String> TAG_RUNE_COLOR = register(
            ResourceLocation.fromNamespaceAndPath("quark", "rune_color"), builder -> builder.persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8)
    );

    // Trowel
    public static final DataComponentType<Long> TAG_PLACING_SEED = register(
            ResourceLocation.fromNamespaceAndPath("quark", "placing_seed"), builder -> builder.persistent(Codec.LONG).networkSynchronized(ByteBufCodecs.VAR_LONG)
    );

    public static final DataComponentType<ItemStack> TAG_LAST_STACK = register(
            ResourceLocation.fromNamespaceAndPath("quark", "last_stack"), builder -> builder.persistent(ItemStack.CODEC).networkSynchronized(ItemStack.STREAM_CODEC)
    );

    // Pathfinder Quill
    public static final DataComponentType<Boolean> IS_PATHFINDER = register(
            ResourceLocation.fromNamespaceAndPath("quark", "is_pathfinder"), builder -> builder.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL)
    );

    public static final DataComponentType<String> TAG_BIOME = register(
            ResourceLocation.fromNamespaceAndPath("quark", "target_biome"), builder -> builder.persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8)
    );

    public static final DataComponentType<Integer> TAG_COLOR = register(
            ResourceLocation.fromNamespaceAndPath("quark", "target_biome_color"), builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.INT)
    );

    public static final DataComponentType<Boolean> IS_UNDERGROUND = register(
            ResourceLocation.fromNamespaceAndPath("quark", "target_biome_underground"), builder -> builder.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL)
    );

    public static final DataComponentType<Boolean> IS_SEARCHING = register(
            ResourceLocation.fromNamespaceAndPath("quark", "is_searching_for_biome"), builder -> builder.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL)
    );

    public static final DataComponentType<Integer> TAG_SOURCE_X = register(
            ResourceLocation.fromNamespaceAndPath("quark", "search_source_x"), builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.INT)
    );

    public static final DataComponentType<Integer> TAG_SOURCE_Z = register(
            ResourceLocation.fromNamespaceAndPath("quark", "search_source_z"), builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.INT)
    );

    public static final DataComponentType<Integer> TAG_POS_X = register(
            ResourceLocation.fromNamespaceAndPath("quark", "search_pox_x"), builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.INT)
    );

    public static final DataComponentType<Integer> TAG_POS_Z = register(
            ResourceLocation.fromNamespaceAndPath("quark", "search_pos_z"), builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.INT)
    );

    public static final DataComponentType<Integer> TAG_POS_LEG = register(
            ResourceLocation.fromNamespaceAndPath("quark", "search_pos_leg"), builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.INT)
    );

    public static final DataComponentType<Integer> TAG_POS_LEG_INDEX = register(
            ResourceLocation.fromNamespaceAndPath("quark", "search_pos_leg_index"), builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.INT)
    );

    // Seed pouch
    public static final DataComponentType<ItemStack> STORED_ITEM = register(
            ResourceLocation.fromNamespaceAndPath("quark", "storedItem"), builder -> builder.persistent(ItemStack.CODEC).networkSynchronized(ItemStack.STREAM_CODEC)
    );

    public static final DataComponentType<Integer> ITEM_COUNT = register(
            ResourceLocation.fromNamespaceAndPath("quark", "itemCount"), builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.INT)
    );

    // For Tater
    public static final DataComponentType<Integer> TICKS = register(
            ResourceLocation.fromNamespaceAndPath("quark", "ticks"), builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.INT)
    );

    // For Compasses that work everywhere
    public static final DataComponentType<Boolean> IS_POS_SET = register(
            ResourceLocation.fromNamespaceAndPath("quark", "is_pos_set"), builder -> builder.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL)
    );

    public static final DataComponentType<Integer> NETHER_TARGET_X = register(
            ResourceLocation.fromNamespaceAndPath("quark", "nether_target_x"), builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.INT)
    );

    public static final DataComponentType<Integer> NETHER_TARGET_Z = register(
            ResourceLocation.fromNamespaceAndPath("quark", "nether_target_z"), builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.INT)
    );




    private static <T> DataComponentType<T> register(ResourceLocation id, UnaryOperator<DataComponentType.Builder<T>> builderOp) {
        return Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, id, builderOp.apply(DataComponentType.builder()).build());
    }
}
