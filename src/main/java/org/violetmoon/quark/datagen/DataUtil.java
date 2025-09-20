package org.violetmoon.quark.datagen;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.ItemAbilities;
import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.base.handler.WoodSetHandler;
import org.violetmoon.quark.base.util.CorundumColor;
import org.violetmoon.quark.content.world.module.AncientWoodModule;
import org.violetmoon.quark.content.world.module.AzaleaWoodModule;
import org.violetmoon.quark.content.world.module.BlossomTreesModule;

import java.util.ArrayList;
import java.util.List;


public class DataUtil {

    static List<WoodSetHandler.WoodSet> QuarkWoodSets = List.of(AzaleaWoodModule.woodSet, AncientWoodModule.woodSet, BlossomTreesModule.woodSet);

    public static Block axeStrip(Block block){
        if(block == Blocks.OAK_WOOD){
            return Blocks.STRIPPED_OAK_WOOD;
        }
        else if(block == Blocks.SPRUCE_WOOD){
            return Blocks.STRIPPED_SPRUCE_WOOD;
        }
        else if(block == Blocks.BIRCH_WOOD){
            return Blocks.STRIPPED_BIRCH_WOOD;
        }
        else if(block == Blocks.JUNGLE_WOOD){
            return Blocks.STRIPPED_JUNGLE_WOOD;
        }
        else if(block == Blocks.ACACIA_WOOD){
            return Blocks.STRIPPED_ACACIA_WOOD;
        }
        else if(block == Blocks.DARK_OAK_WOOD){
            return Blocks.STRIPPED_DARK_OAK_WOOD;
        }
        else if(block == Blocks.MANGROVE_WOOD){
            return Blocks.STRIPPED_MANGROVE_WOOD;
        }
        else if(block == Blocks.CHERRY_WOOD){
            return Blocks.STRIPPED_CHERRY_WOOD;
        }
        else if(block == Blocks.CRIMSON_HYPHAE){
            return Blocks.STRIPPED_CRIMSON_HYPHAE;
        }
        else if(block == Blocks.WARPED_HYPHAE){
            return Blocks.STRIPPED_WARPED_HYPHAE;
        }

        else if(block == AncientWoodModule.woodSet.wood){
            return AncientWoodModule.woodSet.strippedWood;
        }
        else if(block == AzaleaWoodModule.woodSet.wood){
            return AzaleaWoodModule.woodSet.strippedWood;
        }
        else if(block == BlossomTreesModule.woodSet.wood){
            return BlossomTreesModule.woodSet.strippedWood;
        }

        return null;
        /*
        * why does this not work lol

       return switch(block){
            case Blocks.OAK_WOOD -> Blocks.STRIPPED_OAK_WOOD;
            case Blocks.SPRUCE_WOOD -> Blocks.STRIPPED_SPRUCE_WOOD;
            case Blocks.BIRCH_WOOD -> Blocks.STRIPPED_BIRCH_WOOD;
            case Blocks.JUNGLE_WOOD -> Blocks.STRIPPED_JUNGLE_WOOD;
            default -> throw new IllegalStateException("Unexpected axe strip block: " + block);
        };
         */
    }

    public static Item getDyeItemFromCorondumColor(CorundumColor corundumColor){
        return switch (corundumColor){
            case RED -> Items.RED_DYE;
            case ORANGE -> Items.ORANGE_DYE;
            case YELLOW -> Items.YELLOW_DYE;
            case GREEN -> Items.GREEN_DYE;
            case BLUE -> Items.LIGHT_BLUE_DYE;
            case INDIGO -> Items.BLUE_DYE;
            case VIOLET -> Items.PINK_DYE;
            case WHITE -> Items.WHITE_DYE;
            case BLACK -> Items.BLACK_DYE;
        };
    }

    public static Block getWoolFromDyeColor(DyeColor dyeColor){
        //please excuse disgusting switch table
        return switch (dyeColor) {
            case BLACK -> Blocks.BLACK_WOOL;
            case BLUE -> Blocks.BLUE_WOOL;
            case BROWN -> Blocks.BROWN_WOOL;
            case YELLOW -> Blocks.YELLOW_WOOL;
            case CYAN -> Blocks.CYAN_WOOL;
            case GRAY -> Blocks.GRAY_WOOL;
            case GREEN -> Blocks.GREEN_WOOL;
            case WHITE -> Blocks.WHITE_WOOL;
            case ORANGE -> Blocks.ORANGE_WOOL;
            case MAGENTA -> Blocks.MAGENTA_WOOL;
            case LIGHT_BLUE -> Blocks.LIGHT_BLUE_WOOL;
            case LIME -> Blocks.LIME_WOOL;
            case PINK -> Blocks.PINK_WOOL;
            case LIGHT_GRAY -> Blocks.LIGHT_GRAY_WOOL;
            case PURPLE -> Blocks.PURPLE_WOOL;
            case RED -> Blocks.RED_WOOL;
        };
    }

    public static Block getTerrracottaFromDyeColor(DyeColor dyeColor){
        //please excuse disgusting switch table
        return switch (dyeColor) {
            case BLACK -> Blocks.BLACK_TERRACOTTA;
            case BLUE -> Blocks.BLUE_TERRACOTTA;
            case BROWN -> Blocks.BROWN_TERRACOTTA;
            case YELLOW -> Blocks.YELLOW_TERRACOTTA;
            case CYAN -> Blocks.CYAN_TERRACOTTA;
            case GRAY -> Blocks.GRAY_TERRACOTTA;
            case GREEN -> Blocks.GREEN_TERRACOTTA;
            case WHITE -> Blocks.WHITE_TERRACOTTA;
            case ORANGE -> Blocks.ORANGE_TERRACOTTA;
            case MAGENTA -> Blocks.MAGENTA_TERRACOTTA;
            case LIGHT_BLUE -> Blocks.LIGHT_BLUE_TERRACOTTA;
            case LIME -> Blocks.LIME_TERRACOTTA;
            case PINK -> Blocks.PINK_TERRACOTTA;
            case LIGHT_GRAY -> Blocks.LIGHT_GRAY_TERRACOTTA;
            case PURPLE -> Blocks.PURPLE_TERRACOTTA;
            case RED -> Blocks.RED_TERRACOTTA;
        };
    }

    public static TagKey<Item> getLogTagFromLog(Block standardLog){
        if(standardLog == Blocks.OAK_LOG){
            return ItemTags.OAK_LOGS;
        }
        else if(standardLog == Blocks.SPRUCE_LOG){
            return ItemTags.SPRUCE_LOGS;
        }
        else if(standardLog == Blocks.BIRCH_LOG){
            return ItemTags.BIRCH_LOGS;
        }
        else if(standardLog == Blocks.JUNGLE_LOG){
            return ItemTags.JUNGLE_LOGS;
        }
        else if(standardLog == Blocks.ACACIA_LOG){
            return ItemTags.ACACIA_LOGS;
        }
        else if(standardLog == Blocks.DARK_OAK_LOG){
            return ItemTags.DARK_OAK_LOGS;
        }
        else if(standardLog == Blocks.MANGROVE_LOG){
            return ItemTags.MANGROVE_LOGS;
        }
        else if(standardLog == Blocks.CHERRY_LOG){
            return ItemTags.CHERRY_LOGS;
        }

        else if(standardLog == AncientWoodModule.woodSet.wood){
            return Quark.asTagKey(Registries.ITEM, "ancient_logs");
        }
        else if(standardLog == AzaleaWoodModule.woodSet.wood){
            return Quark.asTagKey(Registries.ITEM, "azalea_logs");
        }
        else if(standardLog == BlossomTreesModule.woodSet.wood){
            return Quark.asTagKey(Registries.ITEM, "blossom_logs");
        }

        return null;
    }
}
