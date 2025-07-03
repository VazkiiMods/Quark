package org.violetmoon.quark.datagen;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.violetmoon.quark.base.Quark;

public class QuarkTags {
    public static class Blocks {
        public static final TagKey<Block> ANCIENT_LOGS = tag("ancient_logs");
        public static final TagKey<Block> AZALEA_LOGS = tag("azalea_logs");
        public static final TagKey<Block> BEACON_TRANSPARENT = tag("beacon_transparent");
        public static final TagKey<Block> BLOSSOM_LOGS = tag("blossom_logs");
        public static final TagKey<Block> BREAKS_TORETOISE_ORE = tag("breaks_toretoise_ore");
        public static final TagKey<Block> CORUNDUM = tag("corundum");
        public static final TagKey<Block> CRAB_SPAWNABLE = tag("crab_spawnable");
        public static final TagKey<Block> CRYSTAL_LAMP = tag("crystal_lamp");
        public static final TagKey<Block> FALLEN_LOG_CAN_SPAWN_ON = tag("fallen_log_can_spawn_on");
        public static final TagKey<Block> FOXHOUND_SPAWNABLE = tag("foxhound_spawnable");
        public static final TagKey<Block> FRAMED_GLASS_PANES = tag("framed_glass_panes");
        public static final TagKey<Block> HEDGES = tag("hedges");
        public static final TagKey<Block> HOLLOW_LOGS = tag("hollow_logs");
        public static final TagKey<Block> IRON_ROD_IMMUNE = tag("iron_rod_immune");
        public static final TagKey<Block> LADDERS = tag("ladders");
        public static final TagKey<Block> NON_DOUBLE_DOOR = tag("non_double_door");
        public static final TagKey<Block> PICKARANG_IMMUNE = tag("pickarang_immune");
        public static final TagKey<Block> PIKE_TROPHIES = tag("pike_trophies");
        public static final TagKey<Block> PIPES = tag("pipes");
        public static final TagKey<Block> PLANKS_VERTICAL_SLAB = tag("planks_vertical_slab");
        public static final TagKey<Block> POSTS = tag("posts");
        public static final TagKey<Block> SIMPLE_HARVEST_BLACKLISTED = tag("simple_harvest_blacklisted");
        public static final TagKey<Block> STAINED_FRAMED_GLASS_PANES = tag("stained_framed_glass_panes");
        public static final TagKey<Block> STAINED_FRAMED_GLASSES = tag("stained_framed_glasses");
        public static final TagKey<Block> STOOLS = tag("stools");
        public static final TagKey<Block> UNDERGROUND_BIOME_REPLACEABLE = tag("underground_biome_replaceable");
        //vertical_slab tag should be deprecated
        public static final TagKey<Block> VERTICAL_SLABS = tag("vertical_slabs");
        public static final TagKey<Block> WOODEN_VERTICAL_SLABS = tag("wooden_vertical_slabs");
        public static final TagKey<Block> WRAITH_SPAWNABLE = tag("wraith_spawnable");

        private static TagKey<Block> tag(String id) {
            return TagKey.create(Registries.BLOCK, Quark.asResource(id));
        }
    }
    public static class Items {
        public static final TagKey<Item> ANCIENT_LOGS = tag("ancient_logs");
        public static final TagKey<Item> AZALEA_LOGS = tag("azalea_logs");
        public static final TagKey<Item> BACKPACK_BLOCKED = tag("backpack_blocked");
        public static final TagKey<Item> BIG_HARVESTING_HOES = tag("big_harvesting_hoes");
        public static final TagKey<Item> BLOSSOM_LOGS = tag("BLOSSOM_LOGS");
        public static final TagKey<Item> CORUNDUM = tag("corundum");
        public static final TagKey<Item> CRAB_TEMPT_ITEMS = tag("crab_tempt_items");
        public static final TagKey<Item> CRYSTAL_LAMP = tag("crystal_lamp");
        public static final TagKey<Item> FRAMED_GLASS_PANES = tag("framed_glass_panes");
        public static final TagKey<Item> FRAMED_GLASSES = tag("framed_glasses");
        public static final TagKey<Item> GLOW_SHROOM_FEEDABLES = tag("glow_shroom_feedables");
        public static final TagKey<Item> HEDGES = tag("hedges");
        public static final TagKey<Item> HOLLOW_LOGS = tag("hollow_logs");
        public static final TagKey<Item> IGNORES_MULTISHOT = tag("ignores_multishot"); //1.21 ignore -> ignores
        public static final TagKey<Item> LADDERS = tag("ladders");
        public static final TagKey<Item> PARROT_FEED = tag("parrot_feed");
        public static final TagKey<Item> PIPES = tag("pipes");
        public static final TagKey<Item> POSTS = tag("posts");
        public static final TagKey<Item> REACHAROUND_ABLE = tag("reacharound_able");
        public static final TagKey<Item> REVERTABLE_CHESTS = tag("revertable_chests");
        public static final TagKey<Item> REVERTABLE_TRAPPED_CHESTS = tag("revertable_trapped_chests");
        public static final TagKey<Item> SEED_POUCH_FERTILIZERS = tag("seed_pouch_fertilizers");
        public static final TagKey<Item> SEED_POUCH_HOLDABLE = tag("seed_pouch_fertilizers");
        public static final TagKey<Item> SHARDS = tag("shards");
        public static final TagKey<Item> STAINED_FRAMED_GLASS_PANES = tag("stained_framed_glass_panes");
        public static final TagKey<Item> STAINED_FRAME_GLASSES = tag("stained_framed_glasses");
        public static final TagKey<Item> STONE_TOOL_MATERIALS = tag("stone_tool_materials");
        public static final TagKey<Item> STOOLS = tag("stools");
        public static final TagKey<Item> TROWEL_BLACKLIST = tag("trowel_blacklist");
        public static final TagKey<Item> TROWEL_WHITELIST = tag("trowel_whitelist");
        //vertical_slab tag should be deprecated
        public static final TagKey<Item> VERTICAL_SLABS = tag("vertical_slabs");
        public static final TagKey<Item> WOODEN_VERTICAL_SLABS = tag("wooden_vertical_slabs");

        private static TagKey<Item> tag(String id) {
            return TagKey.create(Registries.ITEM, Quark.asResource(id));
        }
    }
    public static class EntityTypes{

    }
    public static class Biomes{

    }
    public static class Structures{

    }
}
