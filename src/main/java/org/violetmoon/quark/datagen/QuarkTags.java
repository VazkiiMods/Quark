package org.violetmoon.quark.datagen;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.Structure;
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
        //vertical_slab (singular) tag should be deprecated
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
        public static final TagKey<Biome> HAS_ANCIENT_TREES = tag("has_ancient_trees");
        public static final TagKey<Biome> HAS_AZALEA_TREES = tag("has_azalea_trees");

        public static final TagKey<Biome> HAS_CHORUS_VEGETATION = tag("has_chorus_vegetation");
        public static final TagKey<Biome> HAS_CORUNDUM = tag("has_corundum");
        public static final TagKey<Biome> HAS_FAIRY_RINGS = tag("has_fairy_rings");
        public static final TagKey<Biome> HAS_FAIRY_RINGS_RARELY = tag("has_fairy_rings_rarely");

        public static final TagKey<Biome> HAS_FALLEN_ACACIA = tag("has_fallen_acacia");
        public static final TagKey<Biome> HAS_FALLEN_BIRCH = tag("has_fallen_birch");
        public static final TagKey<Biome> HAS_FALLEN_CHERRY = tag("has_fallen_cherry");
        public static final TagKey<Biome> HAS_FALLEN_DARK_OAK = tag("has_fallen_dark_oak");
        public static final TagKey<Biome> HAS_FALLEN_JUNGLE = tag("has_fallen_jungle");
        public static final TagKey<Biome> HAS_FALLEN_MANGROVE = tag("has_fallen_mangrove");
        public static final TagKey<Biome> HAS_FALLEN_OAK = tag("has_fallen_oak");
        public static final TagKey<Biome> HAS_FALLEN_SPRUCE = tag("has_fallen_spruce");
        public static final TagKey<Biome> HAS_LOWER_FALLEN_TREE_DENSITY = tag("has_lower_fallen_tree_density");

        public static final TagKey<Biome> HAS_MONSTER_BOXES = tag("has_monster_boxes");
        public static final TagKey<Biome> HAS_OBSIDIAN_SPIKES = tag("has_obsidian_spikes");

        public static final TagKey<Biome> HAS_SHALE = tag("has_shale");
        public static final TagKey<Biome> HAS_MYALITE = tag("has_myalite");
        public static final TagKey<Biome> HAS_LIMESTONE = tag("has_limestone");
        public static final TagKey<Biome> HAS_JASPER = tag("has_jasper");

        public static final TagKey<Biome> DOES_NOT_HAVE_LAVA_POCKETS = tag("does_not_have_lava_pockets");
        public static final TagKey<Biome> HAS_PERMAFROST = tag("has_permafrost");

        public static final TagKey<Biome> HAS_LIMESTONE_CLUSTERS = tag("has_limestone_clusters");
        public static final TagKey<Biome> HAS_JASPER_CLUSTERS = tag("has_jasper_clusters");
        public static final TagKey<Biome> HAS_MYALITE_CLUSTERS = tag("has_myalite_clusters");
        public static final TagKey<Biome> HAS_CALCITE_CLUSTERS = tag("has_calcite_clusters");

        public static final TagKey<Biome> HAS_SPIRAL_SPIRES = tag("has_spiral_spires");

        public static final TagKey<Biome> HAS_FROSTY_BLOSSOM_TREES = tag("has_frosty_blossom_trees");
        public static final TagKey<Biome> HAS_SERENE_BLOSSOM_TREES = tag("has_serene_blossom_trees");
        public static final TagKey<Biome> HAS_WARM_BLOSSOM_TREES = tag("has_warm_blossom_trees");
        public static final TagKey<Biome> HAS_SUNNY_BLOSSOM_TREES = tag("has_sunny_blossom_trees");
        public static final TagKey<Biome> HAS_FIERY_BLOSSOM_TREES = tag("has_fiery_blossom_trees");

        private static TagKey<Biome> tag(String id) {
            return TagKey.create(Registries.BIOME, Quark.asResource(id));
        }
    }
    public static class Structures{


        private static TagKey<Structure> tag(String id) {
            return TagKey.create(Registries.STRUCTURE, Quark.asResource(id));
        }
    }
}
