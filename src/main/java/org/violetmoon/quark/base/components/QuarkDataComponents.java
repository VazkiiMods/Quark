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
import net.minecraft.world.item.component.CustomData;
import org.violetmoon.quark.base.Quark;

import java.util.function.UnaryOperator;

public class QuarkDataComponents {

    // Tater
    public static final DataComponentType<Boolean> IS_ANGRY = register(
            Quark.asResource("is_angry"), builder -> builder.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL)
    );

    // Abacus
    public static final DataComponentType<BlockPos> BOUNDS_POS = register(
            Quark.asResource("bounds_pos"), builder -> builder.persistent(BlockPos.CODEC).networkSynchronized(BlockPos.STREAM_CODEC)
    );

    public static final DataComponentType<Unit> QUARK_MUSIC_DISC = register(
            Quark.asResource("quark_music_disc"), builder -> builder.persistent(Unit.CODEC).networkSynchronized(StreamCodec.unit(Unit.INSTANCE))
    );

    //Snow golem player heads
    public static final DataComponentType<String> SKULL_OWNER = register(
            Quark.asResource("skull_owner"), builder -> builder.persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8)
    );

    // Runes
    public static final DataComponentType<String> TAG_RUNE_COLOR = register(
            Quark.asResource("rune_color"), builder -> builder.persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8)
    );

    // Trowel
    public static final DataComponentType<Long> TAG_PLACING_SEED = register(
            Quark.asResource("placing_seed"), builder -> builder.persistent(Codec.LONG).networkSynchronized(ByteBufCodecs.VAR_LONG)
    );

    public static final DataComponentType<ItemStack> TAG_LAST_STACK = register(
            Quark.asResource("last_stack"), builder -> builder.persistent(ItemStack.CODEC).networkSynchronized(ItemStack.STREAM_CODEC)
    );

    // Pathfinder Quill
    public static final DataComponentType<Boolean> IS_PATHFINDER = register(
            Quark.asResource("is_pathfinder"), builder -> builder.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL)
    );

    public static final DataComponentType<String> TAG_BIOME = register(
            Quark.asResource("target_biome"), builder -> builder.persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8)
    );

    public static final DataComponentType<Integer> TAG_COLOR = register(
            Quark.asResource("target_biome_color"), builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.INT)
    );

    public static final DataComponentType<Boolean> IS_UNDERGROUND = register(
            Quark.asResource("target_biome_underground"), builder -> builder.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL)
    );

    public static final DataComponentType<Boolean> IS_SEARCHING = register(
            Quark.asResource("is_searching_for_biome"), builder -> builder.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL)
    );

    public static final DataComponentType<Integer> TAG_SOURCE_X = register(
            Quark.asResource("search_source_x"), builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.INT)
    );

    public static final DataComponentType<Integer> TAG_SOURCE_Z = register(
            Quark.asResource("search_source_z"), builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.INT)
    );

    public static final DataComponentType<Integer> TAG_POS_X = register(
            Quark.asResource("search_pox_x"), builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.INT)
    );

    public static final DataComponentType<Integer> TAG_POS_Z = register(
            Quark.asResource("search_pos_z"), builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.INT)
    );

    public static final DataComponentType<Integer> TAG_POS_LEG = register(
            Quark.asResource("search_pos_leg"), builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.INT)
    );

    public static final DataComponentType<Integer> TAG_POS_LEG_INDEX = register(
            Quark.asResource("search_pos_leg_index"), builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.INT)
    );

    public static final DataComponentType<Integer> MAP_COLOR = register(
            Quark.asResource("display"), builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.INT)
    );

    // Seed pouch
    public static final DataComponentType<ItemStack> STORED_ITEM = register(
            Quark.asResource("stored_item"), builder -> builder.persistent(ItemStack.CODEC).networkSynchronized(ItemStack.STREAM_CODEC)
    );

    public static final DataComponentType<Integer> ITEM_COUNT = register(
            Quark.asResource("item_count"), builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.INT)
    );

    // For Tater
    public static final DataComponentType<Integer> TICKS = register(
            Quark.asResource("ticks"), builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.INT)
    );

    // For "Compasses Work Everywhere"
    public static final DataComponentType<Boolean> IS_POS_SET = register(
            Quark.asResource("compass_position_set"), builder -> builder.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL)
    );

    public static final DataComponentType<Boolean> WAS_IN_NETHER = register(
            Quark.asResource("compass_in_nether"), builder -> builder.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL)
    );

    public static final DataComponentType<Integer> NETHER_TARGET_X = register(
            Quark.asResource("nether_x"), builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.INT)
    );

    public static final DataComponentType<Integer> NETHER_TARGET_Z = register(
            Quark.asResource("nether_z"), builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.INT)
    );

    public static final DataComponentType<Boolean> IS_COMPASS_CALCULATED = register(
            Quark.asResource("is_compass_calculated"), builder -> builder.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL)
    );

    //todo: Consider merging this and the one above?
    public static final DataComponentType<Boolean> IS_CLOCK_CALCULATED = register(
            Quark.asResource("is_clock_calculated"), builder -> builder.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL)
    );

    public static final DataComponentType<Boolean> TABLE_ONLY_ENCHANTS = register(
            Quark.asResource("only_show_table_enchantments"), builder -> builder.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL)
    );

    //todo: Probably should transfer this to an actual specialized thing.
    public static final DataComponentType<CustomData> STACK_MATRIX = register(
            Quark.asResource("enchanting_matrix"), builder -> builder.persistent(CustomData.CODEC_WITH_ID).networkSynchronized(CustomData.STREAM_CODEC)
    );

    public static final DataComponentType<Boolean> EXCITED = register(
            Quark.asResource("excited"), builder -> builder.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL)
    );

    public static final DataComponentType<CustomData> SLIME_NBT = register(
            Quark.asResource("slime_nbt"), builder -> builder.persistent(CustomData.CODEC_WITH_ID).networkSynchronized(CustomData.STREAM_CODEC)
    );

    public static final DataComponentType<Boolean> IGNORE = register(
            Quark.asResource("ignore"), builder -> builder.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL)
    );



    private static <T> DataComponentType<T> register(ResourceLocation id, UnaryOperator<DataComponentType.Builder<T>> builderOp) {
        return Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, id, builderOp.apply(DataComponentType.builder()).build());
    }
}
