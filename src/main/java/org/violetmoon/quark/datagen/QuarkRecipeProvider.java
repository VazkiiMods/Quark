package org.violetmoon.quark.datagen;

import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.PlayerTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StainedGlassBlock;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.conditions.IConditionBuilder;
import org.jetbrains.annotations.NotNull;
import org.violetmoon.quark.addons.oddities.module.*;
import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.base.handler.WoodSetHandler;
import org.violetmoon.quark.base.util.CorundumColor;
import org.violetmoon.quark.content.automation.module.*;
import org.violetmoon.quark.content.building.block.RainbowLampBlock;
import org.violetmoon.quark.content.building.module.*;
import org.violetmoon.quark.content.experimental.module.VariantSelectorModule;
import org.violetmoon.quark.content.mobs.module.StonelingsModule;
import org.violetmoon.quark.content.tools.module.*;
import org.violetmoon.quark.content.tweaks.module.GlassShardModule;
import org.violetmoon.quark.content.world.module.*;
import org.violetmoon.zeta.block.IZetaBlock;
import org.violetmoon.zeta.config.FlagCondition;
import org.violetmoon.zeta.util.MiscUtil;
import org.violetmoon.zeta.util.VanillaWoods;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class QuarkRecipeProvider extends RecipeProvider implements IConditionBuilder {
    public QuarkRecipeProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> holderLookupProvider) {
        super(packOutput, holderLookupProvider);
    }

    public static FlagCondition zCond(String name){
        return new FlagCondition(name, Optional.empty());
    }
    //TODO continue defining config flag requirements as needed for each recipe added.

    @Override
    protected void buildRecipes(@NotNull RecipeOutput recipeOutput){
        //CATEGORY: Automation
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ChuteModule.chute)
                .pattern("WWW")
                .pattern("SWS")
                .pattern(" S ")
                .define('W', ItemTags.PLANKS)
                .define('S', Tags.Items.RODS_WOODEN)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput.withConditions(zCond("chute")), "quark:automation/crafting/chute");
        //crafter is vanilla now :)
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, EnderWatcherModule.ender_watcher)
                .pattern("BRB")
                .pattern("RER")
                .pattern("BRB")
                .define('R', Tags.Items.DUSTS_REDSTONE)
                .define('B', Blocks.OBSIDIAN.asItem())
                .define('E', Items.ENDER_EYE)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput.withConditions(zCond("ender_watcher")), "quark:automation/crafting/ender_watcher");
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, FeedingTroughModule.feeding_trough)
                .pattern("#W#")
                .pattern("###")
                .define('#', ItemTags.PLANKS)
                .define('W', Items.WHEAT)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput.withConditions(zCond("feeding_trough")), "quark:automation/crafting/feeding_trough");
        ShapelessRecipeBuilder.shapeless(RecipeCategory.REDSTONE, MetalButtonsModule.gold_button)
                .requires(ItemTags.WOODEN_BUTTONS)
                .requires(Tags.Items.INGOTS_GOLD)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput.withConditions(zCond("gold_metal_button")), "quark:automation/crafting/gold_button");
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, GravisandModule.gravisand)
                .pattern("SSS")
                .pattern("SES")
                .pattern("SSS")
                .define('S', Tags.Items.SANDS_COLORLESS)
                .define('E', Tags.Items.ENDER_PEARLS)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput.withConditions(zCond("gravisand")), "quark:automation/crafting/gravisand");
        ShapelessRecipeBuilder.shapeless(RecipeCategory.REDSTONE, MetalButtonsModule.iron_button)
                .requires(ItemTags.WOODEN_BUTTONS)
                .requires(Tags.Items.INGOTS_IRON)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput.withConditions(zCond("iron_metal_button")), "quark:automation/crafting/iron_button");
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, IronRodModule.iron_rod)
                .pattern("I")
                .pattern("I")
                .pattern("R")
                .define('I', Tags.Items.INGOTS_IRON)
                .define('R', Blocks.END_ROD)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput.withConditions(zCond("iron_rod"), not(zCond("iron_rod_pre_end"))), "quark:automation/crafting/iron_rod");
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, IronRodModule.iron_rod)
                .pattern("I")
                .pattern("I")
                .pattern("I")
                .define('I', Tags.Items.INGOTS_IRON)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput.withConditions(zCond("iron_rod"), zCond("iron_rod_pre_end")), "quark:automation/crafting/iron_rod_pre_end");
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, NetherBrickFenceGateModule.netherBrickFenceGate)
                .pattern("#W#")
                .pattern("#W#")
                .define('#', Tags.Items.BRICKS_NETHER) //TODO check if this is block or singular brick item
                .define('W', Blocks.NETHER_BRICKS.asItem())
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput.withConditions(zCond("nether_brick_fence_gate")), "quark:automation/crafting/nether_brick_fence_gate");
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ObsidianPlateModule.obsidian_plate)
                .pattern("OO")
                .define('O', Tags.Items.OBSIDIANS)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput.withConditions(zCond("obsidian_plate")), "quark:automation/crafting/obsidian_plate");
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, RedstoneRandomizerModule.redstone_randomizer)
                .pattern(" X ")
                .pattern("XBX")
                .pattern("III")
                .define('X', Items.REDSTONE_TORCH)
                .define('I', Items.STONE)
                .define('B', Items.PRISMARINE_CRYSTALS)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput.withConditions(zCond("redstone_randomizer")), "quark:automation/crafting/redstone_randomizer");
        //etc
        //CATEGORY: Building
            //chests (NOTE: has some from World Category)
        for (Map.Entry<Block, Block> chestEntry : VariantChestsModule.regularChests.entrySet()) {
            String dir = "quark:building/chests/" + BuiltInRegistries.BLOCK.getKey(chestEntry.getValue()).getPath();
            for(WoodSetHandler.WoodSet set : DataUtil.QuarkWorldWoodSets){
                if(chestEntry.getKey() == set.planks){
                    dir = "quark:world/crafting/woodsets/" + set.name + "/" + set.name + "_chest";
                }
            }

            chestRecipe(chestEntry.getValue().asItem(), chestEntry.getKey()).unlockedBy("test", PlayerTrigger.TriggerInstance.tick()).save(recipeOutput, dir);
        }

        for (Map.Entry<Block, Block> chestEntry : VariantChestsModule.trappedChests.entrySet()) {
            String dir = "quark:building/chests/" + BuiltInRegistries.BLOCK.getKey(chestEntry.getValue()).getPath();
            for(WoodSetHandler.WoodSet set : DataUtil.QuarkWorldWoodSets){
                if(chestEntry.getKey() == set.planks){
                    dir = "quark:world/crafting/woodsets/" + set.name + "/" + set.name + "_trapped_chest";
                }
            }

            trappedChestRecipe(chestEntry.getValue(), DataUtil.getChestFromTrappedChest(chestEntry.getValue())).unlockedBy("test", PlayerTrigger.TriggerInstance.tick()).save(recipeOutput, dir);
        }
        //TODO 8 logs = 4 chests recipes; zCond("wood_to_chest_recipes")
        //TODO https://github.com/VazkiiMods/Quark/blob/master/src/main/resources/data/quark/recipes/tweaks/crafting/utility/chests/mixed_chest_wood_but_without_exclusions.json

            //compressed
        compressUncompress(Items.APPLE, CompressedBlocksModule.apple, recipeOutput, null, "apple_crate");
        compressUncompress(Items.BEETROOT, CompressedBlocksModule.beetroot, recipeOutput, null, "beetroot_crate");
        compressUncompress(Items.SWEET_BERRIES, CompressedBlocksModule.berry, recipeOutput, null, "berry_sack");
        compressUncompress(Items.LEATHER, CompressedBlocksModule.leather, recipeOutput, null, "bonded_leather");
        compressUncompress(Items.RABBIT_HIDE, CompressedBlocksModule.hide, recipeOutput, null, "bonded_rabbit_hide");
        compressUncompress(Items.CACTUS, CompressedBlocksModule.cactus, recipeOutput, null, "cactus_block");
        compressUncompress(Items.CARROT, CompressedBlocksModule.carrot, recipeOutput, null, "carrot_crate");
        compressUncompress(Items.CHARCOAL, CompressedBlocksModule.charcoal_block, recipeOutput, null, "charcoal_block");
        compressUncompress(Items.CHORUS_FRUIT, CompressedBlocksModule.chorus, recipeOutput, null, "chorus_fruit_block");
        compressUncompress(Items.COCOA_BEANS, CompressedBlocksModule.cocoa, recipeOutput, null, "cocoa_beans_sack");
        compressUncompress(Items.GLOW_BERRIES, CompressedBlocksModule.glowberry, recipeOutput, null, "glowberry_sack");
        compressUncompress(Items.GOLDEN_APPLE, CompressedBlocksModule.golden_apple_crate, recipeOutput, null, "golden_apple_crate");
        compressUncompress(Items.GOLDEN_CARROT, CompressedBlocksModule.golden_carrot, recipeOutput, null, "golden_carrot_crate");
        compressUncompress(Items.GUNPOWDER, CompressedBlocksModule.gunpowder, recipeOutput, null, "gunpowder_sack");
        ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, Blocks.NETHER_WART_BLOCK)
                .requires(Items.NETHER_WART)
                .requires(Items.NETHER_WART)
                .requires(Items.NETHER_WART)
                .requires(Items.NETHER_WART)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput.withConditions(zCond("nether_wart_sack")), "quark:building/crafting/compressed/vanilla_nether_wart_block_4x4");
        compressUncompress(Items.NETHER_WART, CompressedBlocksModule.wart, recipeOutput, null, "nether_wart_sack");
        compressUncompress(Items.POTATO, CompressedBlocksModule.potato, recipeOutput, null, "potato_crate");
        compressUncompress(Items.STICK, CompressedBlocksModule.stick_block, recipeOutput, null, "stick_block");
        compressUncompress(Items.SUGAR_CANE, CompressedBlocksModule.sugarCane, recipeOutput, null, "sugar_cane_block");
            //furnaces
        variantFurnace(Blocks.BLACKSTONE, VariantFurnacesModule.blackstoneFurnace, recipeOutput, "blackstone");
        variantFurnace(Blocks.DEEPSLATE, VariantFurnacesModule.deepslateFurnace, recipeOutput, "deepslate");
        //TODO mixed furnace recipe
            //glass
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, FramedGlassModule.framed_glass, 8)
                .pattern("IGI")
                .pattern("G G")
                .pattern("IGI")
                .define('G', Tags.Items.GLASS_BLOCKS_COLORLESS)
                .define('I', Tags.Items.INGOTS_IRON)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput.withConditions(zCond("framed_glass")), "quark:building/crafting/glass/framed_glass"); //1.21 moved from quark:building/crafting/framed_glass.json
        for(DyeColor dyeColor : FramedGlassModule.blockMap.keySet()){
            dyedFramedGlassRecipe(FramedGlassModule.blockMap.get(dyeColor).getBlock(), dyeColor)
                    .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput.withConditions(zCond("framed_glass")), "quark:building/glass/" + dyeColor.getName() + "_framed_glass");
        }
            //hollowlogs
        for(Block sourceLog : HollowLogsModule.logMap.keySet()){
            ICondition condition = zCond("hollow_logs");

            if(sourceLog.getDescriptionId().contains("ancient")){
                condition = and(zCond("hollow_logs"), zCond("ancient_wood"));
            }
            else if(sourceLog.getDescriptionId().contains("azalea")){
                condition = and(zCond("hollow_logs"), zCond("azalea_wood"));
            }

            hollowLogRecipe(HollowLogsModule.logMap.get(sourceLog), sourceLog).unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput.withConditions(condition), "quark:building/crafting/hollowlogs/hollow_" + sourceLog.getDescriptionId().replaceAll("block..*.[.]", ""));
        }
            //lamps
        for(RainbowLampBlock rbl : RainbowLampsModule.lamps){
            CorundumColor color = RainbowLampsModule.lampMap.get(rbl);
            corundomLampRecipe(rbl, color).unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput.withConditions(zCond("rainbow_lamps"), zCond("corundum"), zCond("rainbow_lamp_corundum")), "quark:building/crafting/lamps/" + color.name + "_corundum_lamp");
            crystalLampRecipe(rbl, color).unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput.withConditions(zCond("rainbow_lamps"), or(not(zCond("corundum")), not(zCond("rainbow_lamp_corundum")))), "quark:building/crafting/lamps/" + color.name + "_crsytal_lamp");
        }
            //panes
        for(DyeColor dyeColor : FramedGlassModule.paneMap.keySet()){
            paneRecipe(FramedGlassModule.blockMap.get(dyeColor).getBlock(), FramedGlassModule.blockMap.get(dyeColor).getBlock())
                    .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput.withConditions(zCond("framed_glass")), "quark:building/panes/" + dyeColor.getName() + "_framed_glass_pane");
        }
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, JapanesePaletteModule.paperWall, 6)
                .pattern("###")
                .pattern("PPP")
                .pattern("###")
                .define('#', Items.BAMBOO)
                .define('P', Items.PAPER)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput.withConditions(zCond("paper_decor")), "quark:building/crafting/panes/paper_wall");
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, JapanesePaletteModule.paperWallBig, 4)
                .pattern("##")
                .pattern("##")
                .define('#', JapanesePaletteModule.paperWall)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput.withConditions(zCond("paper_decor")), "quark:tweaks/crafting/panes/paper_wall_big");
            //shingles
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ShinglesModule.blocks.getFirst(), 2)
                .pattern("##")
                .define('#', Blocks.TERRACOTTA)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput.withConditions(zCond("shingles")), "quark:building/crafting/shingles/shingles");
        for(DyeColor dyeColor : ShinglesModule.blockMap.keySet()){
            colorShingles(ShinglesModule.blockMap.get(dyeColor).getBlock(), dyeColor, recipeOutput.withConditions(zCond("shingles")));
        }
            //slabs
            //TODO the rest of the slabs
        slabBuilder(RecipeCategory.BUILDING_BLOCKS, Quark.ZETA.variantRegistry.slabs.get(DuskboundBlocksModule.blocks.get(0)), Ingredient.of(DuskboundBlocksModule.blocks.get(0)))
                .unlockedBy(getHasName(DuskboundBlocksModule.blocks.get(0)), has(DuskboundBlocksModule.blocks.get(0)))
                .save(recipeOutput.withConditions(zCond("duskbound_blocks")), "quark:building/crafting/slabs/duskbound_slab");
                //shingles slabs
        slabBuilder(RecipeCategory.BUILDING_BLOCKS, Quark.ZETA.variantRegistry.slabs.get(ShinglesModule.blocks.getFirst()), Ingredient.of(ShinglesModule.blocks.getFirst()))
                .unlockedBy(getHasName(ShinglesModule.blocks.getFirst()), has(ShinglesModule.blocks.getFirst()))
                .save(recipeOutput.withConditions(zCond("shingles")), "quark:building/crafting/slabs/shingles_slab");
        for(DyeColor dyeColor : ShinglesModule.blockMap.keySet()){
            slabBuilder(RecipeCategory.BUILDING_BLOCKS, Quark.ZETA.variantRegistry.slabs.get(ShinglesModule.blockMap.get(dyeColor)), Ingredient.of(ShinglesModule.blockMap.get(dyeColor).getBlock().asItem()))
                    .unlockedBy(getHasName(ShinglesModule.blockMap.get(dyeColor).getBlock().asItem()), has(ShinglesModule.blockMap.get(dyeColor).getBlock().asItem()))
                    .save(recipeOutput.withConditions(zCond("shingles")), "quark:building/crafting/slabs/" + dyeColor.getName() + "_shingles_slab");
        }
                //more bricks slabs
        slabBuilder(RecipeCategory.BUILDING_BLOCKS, Quark.ZETA.variantRegistry.slabs.get(MoreBrickTypesModule.blocks.get(0)), Ingredient.of(MoreBrickTypesModule.blocks.get(0)))
                .unlockedBy(getHasName(MoreBrickTypesModule.blocks.get(0)), has(MoreBrickTypesModule.blocks.get(0)))
                .save(recipeOutput.withConditions(zCond("blue_nether_bricks")), "quark:building/crafting/slabs/blue_nether_bricks_slab");
        slabBuilder(RecipeCategory.BUILDING_BLOCKS, Quark.ZETA.variantRegistry.slabs.get(MoreBrickTypesModule.blocks.get(1)), Ingredient.of(MoreBrickTypesModule.blocks.get(1)))
                .unlockedBy(getHasName(MoreBrickTypesModule.blocks.get(1)), has(MoreBrickTypesModule.blocks.get(1)))
                .save(recipeOutput.withConditions(zCond("sandstone_bricks")), "quark:building/crafting/slabs/sandstone_bricks_slab");
        slabBuilder(RecipeCategory.BUILDING_BLOCKS, Quark.ZETA.variantRegistry.slabs.get(MoreBrickTypesModule.blocks.get(2)), Ingredient.of(MoreBrickTypesModule.blocks.get(2)))
                .unlockedBy(getHasName(MoreBrickTypesModule.blocks.get(2)), has(MoreBrickTypesModule.blocks.get(2)))
                .save(recipeOutput.withConditions(zCond("sandstone_bricks")), "quark:building/crafting/slabs/red_sandstone_bricks_slab");
        slabBuilder(RecipeCategory.BUILDING_BLOCKS, Quark.ZETA.variantRegistry.slabs.get(MoreBrickTypesModule.blocks.get(3)), Ingredient.of(MoreBrickTypesModule.blocks.get(3)))
                .unlockedBy(getHasName(MoreBrickTypesModule.blocks.get(3)), has(MoreBrickTypesModule.blocks.get(3)))
                .save(recipeOutput.withConditions(and(zCond("soul_sandstone"), zCond("sandstone_bricks"))), "quark:building/crafting/slabs/soul_sandstone_bricks_slab");
        slabBuilder(RecipeCategory.BUILDING_BLOCKS, Quark.ZETA.variantRegistry.slabs.get(MoreBrickTypesModule.blocks.get(4)), Ingredient.of(MoreBrickTypesModule.blocks.get(4)))
                .unlockedBy(getHasName(MoreBrickTypesModule.blocks.get(4)), has(MoreBrickTypesModule.blocks.get(4)))
                .save(recipeOutput.withConditions(zCond("cobblestone_bricks")), "quark:building/crafting/slabs/cobblestone_bricks_slab");
        slabBuilder(RecipeCategory.BUILDING_BLOCKS, Quark.ZETA.variantRegistry.slabs.get(MoreBrickTypesModule.blocks.get(5)), Ingredient.of(MoreBrickTypesModule.blocks.get(5)))
                .unlockedBy(getHasName(MoreBrickTypesModule.blocks.get(5)), has(MoreBrickTypesModule.blocks.get(5)))
                .save(recipeOutput.withConditions(zCond("cobblestone_bricks")), "quark:building/crafting/slabs/mossy_cobblestone_bricks_slab");
        slabBuilder(RecipeCategory.BUILDING_BLOCKS, Quark.ZETA.variantRegistry.slabs.get(MoreBrickTypesModule.blocks.get(6)), Ingredient.of(MoreBrickTypesModule.blocks.get(6)))
                .unlockedBy(getHasName(MoreBrickTypesModule.blocks.get(6)), has(MoreBrickTypesModule.blocks.get(6)))
                .save(recipeOutput.withConditions(zCond("blackstone_bricks")), "quark:building/crafting/slabs/blackstone_bricks_slab");
        slabBuilder(RecipeCategory.BUILDING_BLOCKS, Quark.ZETA.variantRegistry.slabs.get(MoreBrickTypesModule.blocks.get(7)), Ingredient.of(MoreBrickTypesModule.blocks.get(7)))
                .unlockedBy(getHasName(MoreBrickTypesModule.blocks.get(7)), has(MoreBrickTypesModule.blocks.get(7)))
                .save(recipeOutput.withConditions(zCond("dirt_bricks")), "quark:building/crafting/slabs/dirt_bricks_slab");
        slabBuilder(RecipeCategory.BUILDING_BLOCKS, Quark.ZETA.variantRegistry.slabs.get(MoreBrickTypesModule.blocks.get(8)), Ingredient.of(MoreBrickTypesModule.blocks.get(8)))
                .unlockedBy(getHasName(MoreBrickTypesModule.blocks.get(8)), has(MoreBrickTypesModule.blocks.get(8)))
                .save(recipeOutput.withConditions(zCond("netherrack_bricks")), "quark:building/crafting/slabs/netherrack_bricks_slab");
                //soul sandstone slabs
        slabBuilder(RecipeCategory.BUILDING_BLOCKS, Quark.ZETA.variantRegistry.slabs.get(SoulSandstoneModule.blocks.get(0)), Ingredient.of(SoulSandstoneModule.blocks.get(0)))
                .unlockedBy(getHasName(SoulSandstoneModule.blocks.get(0)), has(SoulSandstoneModule.blocks.get(0)))
                //chiseled soul sandstone has no slab variant.
                .save(recipeOutput.withConditions(zCond("soul_sandstone")), "quark:building/crafting/slabs/soul_sandstone_slab");
        slabBuilder(RecipeCategory.BUILDING_BLOCKS, Quark.ZETA.variantRegistry.slabs.get(SoulSandstoneModule.blocks.get(2)), Ingredient.of(SoulSandstoneModule.blocks.get(2)))
                .unlockedBy(getHasName(SoulSandstoneModule.blocks.get(2)), has(SoulSandstoneModule.blocks.get(2)))
                .save(recipeOutput.withConditions(zCond("soul_sandstone")), "quark:building/crafting/slabs/cut_soul_sandstone_slab");
        slabBuilder(RecipeCategory.BUILDING_BLOCKS, Quark.ZETA.variantRegistry.slabs.get(SoulSandstoneModule.blocks.get(3)), Ingredient.of(SoulSandstoneModule.blocks.get(3)))
                .unlockedBy(getHasName(SoulSandstoneModule.blocks.get(3)), has(SoulSandstoneModule.blocks.get(3)))
                .save(recipeOutput.withConditions(zCond("soul_sandstone")), "quark:building/crafting/slabs/smooth_soul_sandstone_slab");
                //raw ore bricks slabs
        slabBuilder(RecipeCategory.BUILDING_BLOCKS, Quark.ZETA.variantRegistry.slabs.get(RawMetalBricksModule.blocks.get(0)), Ingredient.of(MoreBrickTypesModule.blocks.get(0)))
                .unlockedBy(getHasName(MoreBrickTypesModule.blocks.get(0)), has(MoreBrickTypesModule.blocks.get(0)))
                .save(recipeOutput.withConditions(zCond("raw_metal_bricks")), "quark:building/crafting/slabs/raw_iron_bricks_slab");
        slabBuilder(RecipeCategory.BUILDING_BLOCKS, Quark.ZETA.variantRegistry.slabs.get(RawMetalBricksModule.blocks.get(1)), Ingredient.of(MoreBrickTypesModule.blocks.get(1)))
                .unlockedBy(getHasName(MoreBrickTypesModule.blocks.get(1)), has(MoreBrickTypesModule.blocks.get(1)))
                .save(recipeOutput.withConditions(zCond("raw_metal_bricks")), "quark:building/crafting/slabs/raw_gold_bricks_slab");
        slabBuilder(RecipeCategory.BUILDING_BLOCKS, Quark.ZETA.variantRegistry.slabs.get(RawMetalBricksModule.blocks.get(2)), Ingredient.of(MoreBrickTypesModule.blocks.get(2)))
                .unlockedBy(getHasName(MoreBrickTypesModule.blocks.get(2)), has(MoreBrickTypesModule.blocks.get(2)))
                .save(recipeOutput.withConditions(zCond("raw_metal_bricks")), "quark:building/crafting/slabs/raw_copper_bricks_slab");
                //TODO more stone variants slabs here

        //stairs
        stairBuilder(Quark.ZETA.variantRegistry.stairs.get(DuskboundBlocksModule.blocks.get(0)), Ingredient.of(DuskboundBlocksModule.blocks.get(0)))
                .unlockedBy(getHasName(DuskboundBlocksModule.blocks.get(0)), has(DuskboundBlocksModule.blocks.get(0)))
                .save(recipeOutput.withConditions(zCond("duskbound_blocks")), "quark:building/crafting/stairs/duskbound_stairs");
            //TODO the rest of the stairs
            //stonevariants (vanilla subdir removed 1.21, it was inconsistently used)
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MoreStoneVariantsModule.polishedCalcite)
                .pattern("##")
                .pattern("##")
                .define('#', Blocks.CALCITE)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput.withConditions(zCond("calcite")), "quark:building/crafting/stonevariants/polished_calcite");
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MoreStoneVariantsModule.polishedDripstone)
                .pattern("##")
                .pattern("##")
                .define('#', Blocks.DRIPSTONE_BLOCK)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput.withConditions(zCond("dripstone")), "quark:building/crafting/stonevariants/polished_dripstone");
        //no polished tuff/tuff bricks, they are vanilla now

            //vertplanks

            //vertslabs
        for(Block slab : VerticalSlabsModule.blocks.keySet()) {
            ICondition condition = zCond("vertical_slabs");

            if(slab.getDescriptionId().contains("ancient")){
                condition = and(zCond("vertical_slabs"), zCond("ancient_wood"));
            }
            else if(slab.getDescriptionId().contains("azalea")){
                condition = and(zCond("vertical_slabs"), zCond("azalea_wood"));
            }

            //probably more config-dependent vert slabs

            String slabName = BuiltInRegistries.BLOCK.getKey(slab).getPath().replace("_slab", "");

            vertslabRecipe(RecipeCategory.BUILDING_BLOCKS, VerticalSlabsModule.blocks.get(slab), Ingredient.of(slab))
                    .unlockedBy("obtained_slab", InventoryChangeTrigger.TriggerInstance.hasItems(slab.asItem()))
                    .save(recipeOutput.withConditions(condition), "quark:building/crafting/vertslabs/" + slabName + "_vertical_slab");
        }
            //walls (NOTE: has some from World Category)
        for(IZetaBlock baseBlock : Quark.ZETA.variantRegistry.walls.keySet()) {
            Block base = baseBlock.getBlock();
            ICondition condition = zCond("");
            Block wallBlock = Quark.ZETA.variantRegistry.walls.get(baseBlock);

            String dir;

            //detect World walls
            if(base.getDescriptionId().contains("limestone") || base.getDescriptionId().contains("jasper") || base.getDescriptionId().contains("shale") ||
                    base.getDescriptionId().contains("myalite") || base.getDescriptionId().contains("myalite") ||base.getDescriptionId().contains("permafrost")){

                dir = "quark:world/crafting/walls/";
                if(base.getDescriptionId().contains("jasper")){
                    condition = zCond("jasper");
                }
                else if(base.getDescriptionId().contains("limestone")){
                    condition = zCond("limestone");
                }
                else if(base.getDescriptionId().contains("shale")){
                    condition = zCond("shale");
                }
                else if(base.getDescriptionId().contains("myalite")){
                    condition = zCond("myalite");
                }
                else if(base.getDescriptionId().contains("permafrost")){
                    condition = zCond("permafrost");
                }
            }
            else {
                dir = "quark:building/crafting/walls/";

                if (base.getDescriptionId().contains("blackstone_bricks")) {
                    condition = zCond("blackstone_bricks");
                } else if (base.getDescriptionId().contains("blue_nether_bricks")) {
                    condition = zCond("blue_nether_bricks");
                } else if (base.getDescriptionId().contains("calcite_bricks")) {
                    condition = zCond("more_stone_variants");
                } else if (base.getDescriptionId().equals("block.minecraft.calcite")) {
                    condition = zCond("calcite");
                } else if (base.getDescriptionId().contains("cobblestone_bricks")) {
                    condition = zCond("cobblestone_bricks");
                } else if (base.getDescriptionId().contains("dirt_bricks")) {
                    condition = zCond("dirt_bricks");
                } else if (base.getDescriptionId().equals("block.minecraft.dripstone_block")) {
                    condition = zCond("dripstone");
                } else if (base.getDescriptionId().contains("dripstone_bricks")) {
                    condition = zCond("dripstone");
                } else if (base.getDescriptionId().contains("mossy_cobblestone_bricks")) {
                    condition = zCond("cobblestone_bricks");
                } else if (base.getDescriptionId().contains("netherrack_bricks")) {
                    condition = zCond("netherrack_bricks");
                } else if (base.getDescriptionId().contains("raw")) {
                    condition = zCond("raw_metal_bricks");
                } else if (base.getDescriptionId().contains("sandstone_bricks")) {
                    condition = zCond("sandstone_bricks");
                } else if (base.getDescriptionId().contains("soul_sandstone")) {
                    condition = and(zCond("soul_sandstone"), zCond("sandstone_bricks"));
                }
                else if (base.getDescriptionId().contains("tuff")) {
                    condition = zCond("tuff");
                } else if (base.getDescriptionId().contains("tuff")) {
                    condition = zCond("tuff");
                }
            }

            if(Objects.equals(condition, zCond(""))){
                System.out.println("Wall is missing a condition:" + wallBlock);
            }

            wallBuilder(RecipeCategory.BUILDING_BLOCKS, wallBlock, Ingredient.of(base))
                    .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                    .save(recipeOutput.withConditions(condition),  dir + BuiltInRegistries.BLOCK.getKey(base).getPath() + "_wall");
        }
            //bookshelves (new 1.21 folder) (NOTE: has some from World Category)
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Blocks.BOOKSHELF)
                .pattern("###")
                .pattern("XXX")
                .pattern("###")
                .define('#', Blocks.OAK_PLANKS)
                .define('X', Items.BOOK)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput.withConditions(zCond("variant_bookshelves")), "quark:building/crafting/bookshelves/oak_bookshelf");
        //vanilla wood variant bookshelves
        int i = 0;
        for(VanillaWoods.Wood type : VanillaWoods.NON_OAK){ //SPRUCE, BIRCH, JUNGLE, ACACIA, DARK_OAK, CRIMSON, WARPED, MANGROVE, BAMBOO, CHERRY
            String name = type.name();
            Block plank = type.planks();
            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, VariantBookshelvesModule.variantBookshelves.get(i))
                    .pattern("###")
                    .pattern("XXX")
                    .pattern("###")
                    .define('#', plank)
                    .define('X', Items.BOOK)
                    .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                    .save(recipeOutput.withConditions(zCond("variant_bookshelves")), "quark:building/crafting/bookshelves/" + name + "_bookshelf");
            i++;
        }
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, AncientWoodModule.woodSet.bookshelf)
                .pattern("###")
                .pattern("XXX")
                .pattern("###")
                .define('#', AncientWoodModule.woodSet.planks)
                .define('X', Items.BOOK)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput.withConditions(and(zCond("variant_bookshelves"), zCond("ancient_wood"))), "quark:world/crafting/woodsets/ancient/ancient_bookshelf");
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, AzaleaWoodModule.woodSet.bookshelf)
                .pattern("###")
                .pattern("XXX")
                .pattern("###")
                .define('#', AzaleaWoodModule.woodSet.planks)
                .define('X', Items.BOOK)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput.withConditions(and(zCond("variant_bookshelves"), zCond("azalea_wood"))), "quark:world/crafting/woodsets/ancient/azalea_bookshelf");
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, BlossomTreesModule.woodSet.bookshelf)
                .pattern("###")
                .pattern("XXX")
                .pattern("###")
                .define('#', BlossomTreesModule.woodSet.planks)
                .define('X', Items.BOOK)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput.withConditions(and(zCond("variant_bookshelves"), zCond("blossom_trees"))), "quark:building/crafting/bookshelves/blossom_bookshelf");

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Blocks.LECTERN)
                .pattern("SSS")
                .pattern(" B ")
                .pattern(" S ")
                .define('S', ItemTags.WOODEN_SLABS)
                .define('B', Tags.Items.BOOKSHELVES)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput.withConditions(zCond("variant_bookshelves")), "quark:building/crafting/bookshelves/lectern_with_variant_bookshelves");
            //hedges (new 1.21 folder) (NOTE: has some from World Category)
        i = 0;
        for(VanillaWoods.Wood wood : VanillaWoods.OVERWORLD_WITH_TREE) {
            Block hedge = HedgesModule.hedges.get(i);

            ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, hedge, 2)
                    .pattern("L")
                    .pattern("W")
                    .define('L', wood.leaf())
                    .define('W', DataUtil.getLogTagFromLog(wood.log()))
                    .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                    .save(recipeOutput.withConditions(zCond("hedges")), "quark:building/crafting/hedges/" + wood.name() + "_hedge");
            i++;
        }
        //azalea hedges
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, HedgesModule.hedges.get(8), 2)
                .pattern("L")
                .pattern("W")
                .define('L', Blocks.AZALEA_LEAVES)
                .define('W', DataUtil.getLogTagFromLog(AzaleaWoodModule.woodSet.log))
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput.withConditions(and(zCond("azalea_wood"), zCond("hedges"))), "quark:world/crafting/woodsets/azalea/azalea_hedge");
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, HedgesModule.hedges.get(9), 2)
                .pattern("L")
                .pattern("W")
                .define('L', Blocks.FLOWERING_AZALEA_LEAVES)
                .define('W', DataUtil.getLogTagFromLog(AzaleaWoodModule.woodSet.log))
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput.withConditions(and(zCond("azalea_wood"), zCond("hedges"))), "quark:world/crafting/woodsets/azalea/flowering_azalea_hedge");
        //azalea hedges, but azalea wood type is disabled
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, HedgesModule.hedges.get(8), 2)
                .pattern("L")
                .pattern("W")
                .define('L', Blocks.AZALEA_LEAVES)
                .define('W', ItemTags.OAK_LOGS)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput.withConditions(and(not(zCond("azalea_wood")), zCond("hedges"))), "quark:building/crafting/hedges/azalea_hedge_oak");
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, HedgesModule.hedges.get(9), 2)
                .pattern("L")
                .pattern("W")
                .define('L', Blocks.FLOWERING_AZALEA_LEAVES)
                .define('W', ItemTags.OAK_LOGS)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput.withConditions(and(not(zCond("azalea_wood")), zCond("hedges"))), "quark:building/crafting/hedges/flowering_azalea_hedge_oak");
        //ancient hedges
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, HedgesModule.hedges.get(15), 2)
                .pattern("L")
                .pattern("W")
                .define('L', AncientWoodModule.ancient_leaves)
                .define('W', DataUtil.getLogTagFromLog(AncientWoodModule.woodSet.log))
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput.withConditions(and(zCond("ancient_wood"), zCond("hedges"))), "quark:world/crafting/woodsets/ancient/ancient_hedge");
        //blossom hedges
        i = 10;
        for(BlossomTreesModule.BlossomTree tree : BlossomTreesModule.blossomTrees){
            ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, HedgesModule.hedges.get(i), 2)
                    .pattern("L")
                    .pattern("W")
                    .define('L', tree.leaves)
                    .define('W', DataUtil.getLogTagFromLog(BlossomTreesModule.woodSet.log))
                    .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                    .save(recipeOutput.withConditions(and(zCond("blossom_trees"), zCond("hedges"))), "quark:world/crafting/woodsets/blossom/" + tree.name + "_hedge");
            i++;
        }
            //leafcarpet (new 1.21 folder) (NOTE: has some from World Category)
        i = 0;
        for(VanillaWoods.Wood wood : VanillaWoods.OVERWORLD_WITH_TREE){ //0-7
            Block carpet = LeafCarpetModule.carpets.get(i);
            i++;
            ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, carpet, 3)
                    .pattern("##")
                    .define('#', wood.leaf())
                    .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                    .save(recipeOutput.withConditions(zCond("leaf_carpet")), "quark:building/crafting/leafcarpet/" + wood.name() + "_leaf_carpet");
        }
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, LeafCarpetModule.carpets.get(8), 3)
                .pattern("##")
                .define('#', Blocks.AZALEA_LEAVES)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput.withConditions(zCond("leaf_carpet")), "quark:building/crafting/leafcarpet/azalea_leaf_carpet");
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, LeafCarpetModule.carpets.get(8), 3)
                .pattern("##")
                .define('#', Blocks.FLOWERING_AZALEA_LEAVES)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput.withConditions(zCond("leaf_carpet")), "quark:building/crafting/leafcarpet/flowering_azalea_leaf_carpet");
        //ancient carpet
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, LeafCarpetModule.carpets.get(15), 3)
                .pattern("##")
                .define('#', AncientWoodModule.ancient_leaves)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput.withConditions(and(zCond("ancient_wood"), zCond("leaf_carpet"))), "quark:world/crafting/woodsets/ancient/ancient_leaf_carpet");
        //blossom carpet
        i = 10;
        for(BlossomTreesModule.BlossomTree tree : BlossomTreesModule.blossomTrees) {
            ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, LeafCarpetModule.carpets.get(i), 3)
                    .pattern("##")
                    .define('#', tree.leaves)
                    .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                    .save(recipeOutput.withConditions(and(zCond("blossom_trees"), zCond("leaf_carpet"))), "quark:world/crafting/woodsets/blossom/" + tree.name + "_leaf_carpet");
            i++;
        }


            //posts (new 1.21 folder) (NOTE: has some from World Category)
        i = 0;
        for(VanillaWoods.Wood wood : VanillaWoods.ALL) {
            if(wood.name().equals("bamboo")) { //bamboo has no wood block
                ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, WoodenPostsModule.blocks.get(i), 8)
                        .pattern("F")
                        .pattern("F")
                        .pattern("F")
                        .define('F', Blocks.BAMBOO_BLOCK)
                        .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                        .save(recipeOutput.withConditions(zCond("wooden_posts")), "quark:building/crafting/posts/" + wood.name() + "_post");
                i++;

                ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, WoodenPostsModule.blocks.get(i), 8)
                        .pattern("F")
                        .pattern("F")
                        .pattern("F")
                        .define('F', Blocks.STRIPPED_BAMBOO_BLOCK)
                        .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                        .save(recipeOutput.withConditions(zCond("wooden_posts")), "quark:building/crafting/posts/stripped_" + wood.name() + "_post");
                i++;
            }
            else
            {
                ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, WoodenPostsModule.blocks.get(i), 8)
                        .pattern("F")
                        .pattern("F")
                        .pattern("F")
                        .define('F', wood.wood().asItem())
                        .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                        .save(recipeOutput.withConditions(zCond("wooden_posts")), "quark:building/crafting/posts/" + wood.name() + "_post");
                i++;

                //zeta vanillawoods doesn't have stripped woods :(
                Block stripped = DataUtil.axeStrip(wood.wood());
                ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, WoodenPostsModule.blocks.get(i), 8)
                        .pattern("F")
                        .pattern("F")
                        .pattern("F")
                        .define('F', stripped.asItem())
                        .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                        .save(recipeOutput.withConditions(zCond("wooden_posts")), "quark:building/crafting/posts/stripped_" + wood.name() + "_post");
                i++;
            }
        }
        for(WoodSetHandler.WoodSet set : DataUtil.QuarkWorldWoodSets){
            ICondition cond = and(zCond("wooden_posts"), zCond(set.name + "_wood"));
            if(set == BlossomTreesModule.woodSet){
                cond = and(zCond("wooden_posts"), zCond("blossom_trees"));
            }

            ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, set.post, 8)
                    .pattern("F")
                    .pattern("F")
                    .pattern("F")
                    .define('F', set.wood.asItem())
                    .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                    .save(recipeOutput.withConditions(cond), "quark:world/crafting/woodsets/" + set.name + "/" + set.name + "_post");

            ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, set.strippedPost, 8)
                    .pattern("F")
                    .pattern("F")
                    .pattern("F")
                    .define('F', set.strippedWood.asItem())
                    .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                    .save(recipeOutput.withConditions(cond), "quark:world/crafting/woodsets/" + set.name + "/stripped_" + set.name + "_post");
        }
            //ladders (new 1.21 folder) (NOTE: has some from World Category)
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Blocks.LADDER, 4)
                .pattern("# #")
                .pattern("#W#")
                .pattern("# #")
                .define('#', Items.STICK)
                .define('W', Blocks.OAK_PLANKS)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput.withConditions(zCond("variant_ladders")), "quark:building/crafting/ladders/oak_ladder");
        i = 0;
        for(VanillaWoods.Wood wood : VanillaWoods.NON_OAK){
            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, DataUtil.getLadderFromPlank(wood.planks()), 4)
                    .pattern("# #")
                    .pattern("#W#")
                    .pattern("# #")
                    .define('#', Items.STICK)
                    .define('W', wood.planks())
                    .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                    .save(recipeOutput.withConditions(zCond("variant_ladders")), "quark:building/crafting/ladders/" + wood.name() + "_ladder");
            i++;
        }
        for(WoodSetHandler.WoodSet set : DataUtil.QuarkWorldWoodSets){
            ICondition cond = and(zCond("variant_ladders"), zCond(set.name + "_wood"));
            if(set == BlossomTreesModule.woodSet){
                cond = and(zCond("variant_ladders"), zCond("blossom_trees"));
            }

            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, set.ladder, 4)
                    .pattern("# #")
                    .pattern("#W#")
                    .pattern("# #")
                    .define('#', Items.STICK)
                    .define('W', set.planks)
                    .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                    .save(recipeOutput.withConditions(cond), "quark:world/crafting/woodsets/" + set.name + "/" + set.name + "_ladder");
        }
            //stools (new 1.21 folder)
        i = 0;
        for(DyeColor dye : MiscUtil.CREATIVE_COLOR_ORDER){
            colorStools(StoolsModule.stools.get(i), dye, recipeOutput);
            i++;
        }
            //misc building blocks, loose files in building/crafting (midori, raw metal bricks, rope, ironplate, paperwall/lantern, thatch)
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, IndustrialPaletteModule.blocks.get(3), 3)
                .pattern("N N")
                .pattern("NIN")
                .pattern("N N")
                .define('N', Tags.Items.NUGGETS_IRON)
                .define('I', Tags.Items.INGOTS_IRON)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput.withConditions(zCond("iron_ladder")), "quark:building/crafting/iron_ladder");
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, IndustrialPaletteModule.blocks.get(0), 24)
                .pattern("###")
                .pattern("# #")
                .pattern("###")
                .define('#', Tags.Items.INGOTS_IRON)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput.withConditions(zCond("iron_plates")), "quark:building/crafting/iron_plate");
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, IndustrialPaletteModule.blocks.get(1), 24)
                .pattern("###")
                .pattern("#W#")
                .pattern("###")
                .define('#', Tags.Items.INGOTS_IRON)
                .define('W', Tags.Items.BUCKETS_WATER)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput.withConditions(zCond("iron_plates")), "quark:building/crafting/rusty_iron_plate");
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, IndustrialPaletteModule.blocks.get(1), 8)
                .pattern("###")
                .pattern("#W#")
                .pattern("###")
                .define('#', IndustrialPaletteModule.blocks.get(0))
                .define('W', Tags.Items.BUCKETS_WATER)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput.withConditions(zCond("iron_plates")), "quark:building/crafting/rusty_iron_plate_from_iron_plate");
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, IndustrialPaletteModule.blocks.get(2), 1)
                .pattern("#")
                .pattern("#")
                .define('#', Quark.ZETA.variantRegistry.slabs.get(IndustrialPaletteModule.blocks.get(0)))
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput.withConditions(zCond("iron_plates")), "quark:building/crafting/iron_pillar");

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, MoreBrickTypesModule.blocks.get(6), 4)
                .pattern("C#")
                .pattern("##")
                .define('C', MoreBrickTypesModule.blocks.get(4))
                .define('#', Blocks.BLACKSTONE)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput.withConditions(zCond("blackstone_bricks")), "quark:building/crafting/blackstone_bricks");
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, CompressedBlocksModule.blaze_lantern)
                //blaze lantern is *in* compressed blocks, but it is not *a* compressed block.
                .pattern("BPB")
                .pattern("PPP")
                .pattern("BPB")
                .define('B', Items.BLAZE_ROD)
                .define('P', Items.BLAZE_POWDER)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput.withConditions(zCond("blaze_lantern")), "quark:building/crafting/blaze_lantern");
        ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, MoreBrickTypesModule.blocks.get(0), 2)
                .requires(Blocks.NETHER_BRICKS)
                .requires(Blocks.WARPED_WART_BLOCK)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput.withConditions(zCond("blue_nether_bricks")), "quark:building/crafting/blue_nether_bricks");
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, MoreMudBlocksModule.blocks.get(0), 2)
                .pattern("##")
                .pattern("##")
                .define('#', Blocks.MUD_BRICK_SLAB)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput.withConditions(zCond("more_mud_blocks")), "quark:building/crafting/carved_mud_bricks");

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, DuskboundBlocksModule.blocks.get(0), 16)
                .pattern("PPP")
                .pattern("POP")
                .pattern("PPP")
                .define('P', Blocks.PURPUR_BLOCK)
                .define('O', SpiralSpiresModule.dusky_myalite)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput.withConditions(and(zCond("duskbound_blocks"), zCond("spiral_spires"))), "quark:building/crafting/duskbound_block");
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, DuskboundBlocksModule.blocks.get(0), 16)
                .pattern("PPP")
                .pattern("POP")
                .pattern("PPP")
                .define('P', Blocks.PURPUR_BLOCK)
                .define('O', Blocks.OBSIDIAN)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput.withConditions(and(zCond("duskbound_blocks"), not(zCond("spiral_spires")))), "quark:building/crafting/duskbound_block_without_myalite");
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, DuskboundBlocksModule.blocks.get(1), 4)
                .pattern("DDD")
                .pattern("DED")
                .pattern("DDD")
                .define('D', DuskboundBlocksModule.blocks.get(0))
                .define('E', Items.ENDER_PEARL)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput.withConditions(zCond("duskbound_blocks")), "quark:building/crafting/duskbound_lantern");
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, SoulSandstoneModule.blocks.get(0))
                .pattern("##")
                .pattern("##")
                .define('#', Blocks.SOUL_SAND)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput.withConditions(zCond("soul_sandstone")), "quark:building/crafting/soul_sandstone");
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, SoulSandstoneModule.blocks.get(1))
                .pattern("#")
                .pattern("#")
                .define('#', SoulSandstoneModule.blocks.get(0))
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput.withConditions(zCond("soul_sandstone")), "quark:building/crafting/chiseled_soul_sandstone");
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, SoulSandstoneModule.blocks.get(3))
                .pattern("##")
                .pattern("##")
                .define('#', SoulSandstoneModule.blocks.get(0))
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput.withConditions(zCond("soul_sandstone")), "quark:building/crafting/cut_soul_sandstone");
        //smooth_soul_sandstone is furnace recipe
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, GrateModule.grate)
                .pattern("##")
                .pattern("##")
                .define('#', Blocks.IRON_BARS)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput.withConditions(zCond("grate")), "quark:building/crafting/grate");

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, SturdyStoneModule.sturdy_stone)
                .pattern("CCC")
                .pattern("CCC")
                .pattern("CCC")
                .define('C', Blocks.COBBLESTONE)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput.withConditions(and(not(modLoaded("cyclic")), zCond("sturdy_stone"))), "quark:building/crafting/sturdy_stone");
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, SturdyStoneModule.sturdy_stone)
                .pattern("SCS")
                .pattern("CCC")
                .pattern("SCS")
                .define('C', Blocks.COBBLESTONE)
                .define('S', Blocks.STONE)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput.withConditions(and(modLoaded("cyclic"), zCond("sturdy_stone"))), "quark:building/crafting/sturdy_stone_cyclic");
        ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, Blocks.COBBLESTONE, 9)
                .requires(SturdyStoneModule.sturdy_stone)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput.withConditions(and(not(modLoaded("cyclic")), zCond("sturdy_stone"))), "quark:building/crafting/sturdy_stone_uncompress");

        //CATEGORY: Experimental
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, VariantSelectorModule.hammer)
                .pattern("III")
                .pattern("ISI")
                .pattern(" S ")
                .define('I', Tags.Items.INGOTS_IRON)
                .define('S', Tags.Items.RODS_WOODEN)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput.withConditions(zCond("hammer")), "quark:experimental/crafting/hammer"); //this recipe is called "trowel" in 1.20
        //CATEGORY: Mobs
            //  RecipeProvider does not seem to have campfire recipes ??
        //CATEGORY: Oddities
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, BackpackModule.backpack)
                .pattern("LRL")
                .pattern("LCL")
                .pattern("LIL")
                .define('L', Tags.Items.LEATHERS)
                .define('R', BackpackModule.ravager_hide)
                .define('C', Tags.Items.CHESTS_WOODEN)
                .define('I', Tags.Items.INGOTS_IRON)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput.withConditions(and(zCond("backpack"), zCond("ravager_hide"))), "quark:oddities/crafting/backpack");
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, BackpackModule.backpack)
                .pattern("LIL")
                .pattern("LCL")
                .pattern("LIL")
                .define('L', Tags.Items.LEATHERS)
                .define('C', Tags.Items.CHESTS_WOODEN)
                .define('I', Tags.Items.INGOTS_IRON)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput.withConditions(and(zCond("backpack"), not(zCond("ravager_hide")))), "quark:oddities/crafting/backpack_no_hide");
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, BackpackModule.ravager_hide, 9)
                .requires(BackpackModule.bonded_ravager_hide)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput.withConditions(zCond("ravager_hide")), "quark:oddities/crafting/bonded_ravager_hide_uncompress");
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, BackpackModule.bonded_ravager_hide)
                .pattern("###")
                .pattern("###")
                .pattern("###")
                .define('#', BackpackModule.ravager_hide)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput.withConditions(zCond("ravager_hide")), "quark:oddities/crafting/bonded_ravager_hide");
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CrateModule.crate)
                .pattern("IWI")
                .pattern("WCW")
                .pattern("IWI")
                .define('W', ItemTags.PLANKS)
                .define('I', Tags.Items.INGOTS_IRON)
                .define('C', Tags.Items.CHESTS_WOODEN)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput.withConditions(zCond("crate")), "quark:oddities/crafting/crate");
        ShapelessRecipeBuilder.shapeless(RecipeCategory.REDSTONE, PipesModule.encasedPipe)
                .requires(PipesModule.pipe)
                .requires(Tags.Items.GLASS_BLOCKS_COLORLESS) //1.21 minecraft:glass -> c:glass_blocks/colorless
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput.withConditions(zCond("encased_pipes")), "quark:oddities/crafting/encased_pipe");
        ShapelessRecipeBuilder.shapeless(RecipeCategory.REDSTONE, PipesModule.pipe)
                .requires(PipesModule.encasedPipe)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput.withConditions(zCond("encased_pipes")), "quark:oddities/crafting/encased_pipe_revert");
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, MagnetsModule.magnet)
                .pattern("CIC")
                .pattern("BFR")
                .pattern("CIC")
                .define('C', Tags.Items.COBBLESTONES)
                .define('I', Tags.Items.INGOTS_IRON)
                .define('B', Tags.Items.DYES_BLUE)
                .define('R', Tags.Items.DUSTS_REDSTONE)
                .define('F', Items.CHORUS_FRUIT)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput.withConditions(and(zCond("magnet"), not(zCond("magnet_pre_end")))), "quark:oddities/crafting/magnet");
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, MagnetsModule.magnet)
                .pattern("CIC")
                .pattern("BPR")
                .pattern("CIC")
                .define('C', Tags.Items.COBBLESTONES)
                .define('I', Tags.Items.INGOTS_IRON)
                .define('B', Tags.Items.DYES_BLUE)
                .define('R', Tags.Items.DUSTS_REDSTONE)
                .define('P', Tags.Items.INGOTS_COPPER)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput.withConditions(and(zCond("magnet"), zCond("magnet_pre_end"))), "quark:oddities/crafting/magnet_pre_end");
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, PipesModule.pipe)
                .pattern("I")
                .pattern("G")
                .pattern("I")
                .define('I', Tags.Items.INGOTS_COPPER)
                .define('G', Tags.Items.GLASS_BLOCKS)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput.withConditions(zCond("pipes")), "quark:oddities/crafting/pipe");
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TinyPotatoModule.tiny_potato)
                .pattern("H")
                .pattern("P")
                .define('H', StonelingsModule.diamondHeart)
                .define('P', Items.POTATO)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput.withConditions(and(zCond("tiny_potato"), zCond("stonelings"), not(zCond("tiny_potato_never_uses_heart")))), "quark:oddities/crafting/tiny_potato_heart");
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TinyPotatoModule.tiny_potato)
                .pattern("D")
                .pattern("P")
                .define('D', Ingredient.of(Items.DIAMOND, Items.EMERALD))
                .define('P', Items.POTATO)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput.withConditions(and(zCond("tiny_potato"), or(zCond("tiny_potato_never_uses_heart"), not(zCond("stonelings"))))), "quark:oddities/crafting/tiny_potato_no_heart");
        //CATEGORY: Tools
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, AbacusModule.abacus)
                .pattern("WSW")
                .pattern("WIW")
                .pattern("WSW")
                .define('W', ItemTags.PLANKS)
                .define('S', Tags.Items.RODS_WOODEN)
                .define('I', Tags.Items.INGOTS_IRON)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput.withConditions(zCond("abacus")), "quark:tools/crafting/abacus");
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, PickarangModule.pickarang)
                .pattern("DWH")
                .pattern("  W")
                .pattern("  D")
                .define('W', ItemTags.PLANKS)
                .define('D', Tags.Items.GEMS_DIAMOND)
                .define('H', StonelingsModule.diamondHeart)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput.withConditions(and(zCond("pickarang"), zCond("stonelings"), zCond("pickarang_never_uses_heart"))), "quark:tools/crafting/pickarang_heart");
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, PickarangModule.pickarang)
                .pattern("DWD")
                .pattern("  W")
                .pattern("  D")
                .define('W', ItemTags.PLANKS)
                .define('D', Tags.Items.GEMS_DIAMOND)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput.withConditions(and(zCond("pickarang"), or(zCond("pickarang_never_uses_heart"), not(zCond("stonelings"))))), "quark:tools/crafting/pickarang_no_heart");
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ColorRunesModule.rune)
                .pattern("#S#")
                .pattern("#C#")
                .pattern("###")
                .define('#', TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("quark", "corundum")))
                .define('S', Tags.Items.COBBLESTONES)
                .define('C', ColorRunesModule.rune)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput.withConditions(zCond("color_runes")), "quark:tools/crafting/rune_duplication");
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, SeedPouchModule.seed_pouch, 1)
                .pattern(" S ")
                .pattern("HXH")
                .pattern(" H ")
                .define('S', Items.STRING) //there does not seem to be a convention string tag
                .define('H', TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("quark", "seed_pouch_holdable")))
                .define('X', ColorRunesModule.rune)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput.withConditions(zCond("seed_pouch")), "quark:tools/crafting/seed_pouch");
        ShapelessRecipeBuilder.shapeless(RecipeCategory.TOOLS, TorchArrowModule.torch_arrow)
                .requires(Items.TORCH)
                .requires(Items.ARROW)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput.withConditions(zCond("torch_arrow")), "quark:tools/crafting/torch_arrow");
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, TrowelModule.trowel)
                .pattern("S  ")
                .pattern(" II")
                .define('S', Tags.Items.RODS_WOODEN)
                .define('I', Tags.Items.INGOTS_IRON)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput.withConditions(zCond("trowel")), "quark:tools/crafting/trowel");
        //Tweaks
            //panes
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, GlassShardModule.dirtyGlassPane, 16)
                .pattern("###")
                .pattern("###")
                .define('#', GlassShardModule.dirtyGlass)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput.withConditions(zCond("glass_shard")), "quark:tweaks/crafting/panes/dirty_glass_pane");
            //utility/bent
        ShapedRecipeBuilder.shaped(RecipeCategory.FOOD, Items.BREAD)
                .pattern("##")
                .pattern("# ")
                .define('#', Items.WHEAT)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput.withConditions(zCond("bent_recipes")), "quark:tweaks/crafting/utility/bent/bread");
        ShapedRecipeBuilder.shaped(RecipeCategory.FOOD, Items.COOKIE)
                .pattern("X#")
                .pattern("# ")
                .define('#', Items.WHEAT)
                .define('X', Items.COCOA_BEANS)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput.withConditions(zCond("bent_recipes")), "quark:tweaks/crafting/utility/bent/cookie");
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Items.PAPER, 3)
                .pattern("##")
                .pattern("# ")
                .define('#', Items.SUGAR_CANE)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput.withConditions(zCond("bent_recipes")), "quark:tweaks/crafting/utility/bent/paper");
        //TODO direct chest boat, 8 logs to chest (including mixed exclusion recipe type)

            //utility/coral
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.PINK_DYE)
                .requires(Ingredient.of(Items.BRAIN_CORAL, Items.BRAIN_CORAL_FAN))
                .group("pink_dye")
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput.withConditions(zCond("coral_to_dye")), "quark:tweaks/crafting/utility/coral/brain_to_pink");
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.MAGENTA_DYE)
                .requires(Ingredient.of(Items.BUBBLE_CORAL, Items.BUBBLE_CORAL_FAN))
                .group("magenta_dye")
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput.withConditions(zCond("coral_to_dye")), "quark:tweaks/crafting/utility/coral/bubble_to_magenta");
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.RED_DYE)
                .requires(Ingredient.of(Items.FIRE_CORAL, Items.FIRE_CORAL_FAN))
                .group("red_dye")
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput.withConditions(zCond("coral_to_dye")), "quark:tweaks/crafting/utility/coral/fire_to_red");
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.YELLOW_DYE)
                .requires(Ingredient.of(Items.HORN_CORAL, Items.HORN_CORAL_FAN))
                .group("yellow_dye")
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput.withConditions(zCond("coral_to_dye")), "quark:tweaks/crafting/utility/coral/horn_to_yellow");
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.BLUE_DYE)
                .requires(Ingredient.of(Items.TUBE_CORAL, Items.TUBE_CORAL_FAN))
                .group("blue_dye")
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput.withConditions(zCond("coral_to_dye")), "quark:tweaks/crafting/utility/coral/tube_to_blue");
            //utility/better_stone_tools
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Items.STONE_SWORD)
                .pattern("#")
                .pattern("#")
                .pattern("S")
                .define('#', QuarkTags.Items.STONE_TOOL_MATERIALS)
                .define('S', Tags.Items.RODS_WOODEN)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput.withConditions(zCond("better_stone_tools")), "quark:tweaks/crafting/utility/better_stone_tools/sword");
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Items.STONE_SHOVEL)
                .pattern("#")
                .pattern("S")
                .pattern("S")
                .define('#', QuarkTags.Items.STONE_TOOL_MATERIALS)
                .define('S', Tags.Items.RODS_WOODEN)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput.withConditions(zCond("better_stone_tools")), "quark:tweaks/crafting/utility/better_stone_tools/shovel");
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Items.STONE_PICKAXE)
                .pattern("###")
                .pattern(" S ")
                .pattern(" S ")
                .define('#', QuarkTags.Items.STONE_TOOL_MATERIALS)
                .define('S', Tags.Items.RODS_WOODEN)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput.withConditions(zCond("better_stone_tools")), "quark:tweaks/crafting/utility/better_stone_tools/pickaxe");
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Items.STONE_AXE)
                .pattern("##")
                .pattern("#S")
                .pattern(" S")
                .define('#', QuarkTags.Items.STONE_TOOL_MATERIALS)
                .define('S', Tags.Items.RODS_WOODEN)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput.withConditions(zCond("better_stone_tools")), "quark:tweaks/crafting/utility/better_stone_tools/axe");
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Items.STONE_AXE)
                .pattern("##")
                .pattern(" S")
                .pattern(" S")
                .define('#', QuarkTags.Items.STONE_TOOL_MATERIALS)
                .define('S', Tags.Items.RODS_WOODEN)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput.withConditions(zCond("better_stone_tools")), "quark:tweaks/crafting/utility/better_stone_tools/hoe");
        //utility/misc
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.BLACK_DYE)
                .requires(Items.CHARCOAL)
                .group("black_dye")
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput.withConditions(zCond("charcoal_to_dye")), "quark:tweaks/crafting/utility/misc/charcoal_to_black_dye");
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Items.CHEST_MINECART)
                .pattern("#C#")
                .pattern("###")
                .define('#', Tags.Items.INGOTS_IRON)
                .define('C', Tags.Items.CHESTS_WOODEN)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput.withConditions(zCond("minecart_upgrade")), "quark:tweaks/crafting/utility/misc/chest_minecart");
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.DISPENSER)
                .requires(Items.BOW)
                .requires(Items.DROPPER)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput.withConditions(zCond("dropper_upgrade")), "quark:tweaks/crafting/utility/misc/dispenser_bow");
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Items.DISPENSER)
                .pattern(" #X")
                .pattern("#DX")
                .pattern(" #X")
                .define('#', Tags.Items.RODS_WOODEN)
                .define('X', Items.STRING)
                .define('D', Items.DROPPER)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput.withConditions(zCond("dropper_upgrade")), "quark:tweaks/crafting/utility/misc/dispenser_no_bow");
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Items.HOPPER)
                .pattern("IWI")
                .pattern("IWI")
                .pattern(" I ")
                .define('I', Tags.Items.INGOTS_IRON)
                .define('W', ItemTags.LOGS)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput.withConditions(zCond("easy_hopper")), "quark:tweaks/crafting/utility/misc/easy_hopper");
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Items.STICK, 16)
                .pattern("#")
                .pattern("#")
                .define('#', ItemTags.LOGS)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput.withConditions(zCond("easy_sticks")), "quark:tweaks/crafting/utility/misc/easy_sticks");
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Items.STICK, 8)
                .pattern("#")
                .pattern("#")
                .define('#', ItemTags.BAMBOO_BLOCKS)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput.withConditions(zCond("easy_sticks")), "quark:tweaks/crafting/utility/misc/easy_sticks_bamboo");
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Items.FURNACE_MINECART)
                .pattern("#X#")
                .pattern("###")
                .define('#', Tags.Items.INGOTS_IRON)
                .define('X', Items.FURNACE)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput.withConditions(zCond("minecart_upgrade")), "quark:tweaks/crafting/utility/misc/furnace_minecart");
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Items.HOPPER_MINECART)
                .pattern("#X#")
                .pattern("###")
                .define('#', Tags.Items.INGOTS_IRON)
                .define('X', Items.HOPPER)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput.withConditions(zCond("minecart_upgrade")), "quark:tweaks/crafting/utility/misc/hopper_minecart");
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Items.HOPPER_MINECART)
                .pattern("X X")
                .pattern("#X#")
                .pattern("III")
                .define('#', Tags.Items.RODS_WOODEN)
                .define('X', Tags.Items.DUSTS_REDSTONE)
                .define('I', Items.STONE)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput.withConditions(zCond("repeater_and_torches")), "quark:tweaks/crafting/utility/misc/repeater");
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Items.TNT_MINECART)
                .pattern("#X#")
                .pattern("###")
                .define('#', Tags.Items.INGOTS_IRON)
                .define('X', Items.TNT)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput.withConditions(zCond("minecart_upgrade")), "quark:tweaks/crafting/utility/misc/tnt_minecart");
            //utility/tools is in Quark VDO

            //utility/wool is no longer needed, these are now vanilla recipes

            //glass (new 1.21)
        shardGlassRecipe(Items.BLACK_STAINED_GLASS).unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput.withConditions(zCond("glass_shard")), "quark:tweaks/crafting/utility/glass/black_glass");
        shardGlassRecipe(Items.BLUE_STAINED_GLASS).unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput.withConditions(zCond("glass_shard")), "quark:tweaks/crafting/utility/glass/blue_glass");
        shardGlassRecipe(Items.BROWN_STAINED_GLASS).unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput.withConditions(zCond("glass_shard")), "quark:tweaks/crafting/utility/glass/brown_glass");
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Items.GLASS)
                .pattern("##")
                .pattern("##")
                .define('#', GlassShardModule.clearShard)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput.withConditions(zCond("glass_shard")), "quark:tweaks/crafting/utility/glass/clear_glass");
        shardGlassRecipe(Items.CYAN_STAINED_GLASS).unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput.withConditions(zCond("glass_shard")), "quark:tweaks/crafting/utility/glass/cyan_glass");
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, GlassShardModule.dirtyGlass)
                .pattern("##")
                .pattern("##")
                .define('#', GlassShardModule.dirtyShard)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput.withConditions(zCond("glass_shard")), "quark:tweaks/crafting/utility/glass/dirty_glass");
        shardGlassRecipe(Items.GRAY_STAINED_GLASS).unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput.withConditions(zCond("glass_shard")), "quark:tweaks/crafting/utility/glass/gray_glass");
        shardGlassRecipe(Items.GREEN_STAINED_GLASS).unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput.withConditions(zCond("glass_shard")), "quark:tweaks/crafting/utility/glass/green_glass");
        shardGlassRecipe(Items.LIGHT_BLUE_STAINED_GLASS).unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput.withConditions(zCond("glass_shard")), "quark:tweaks/crafting/utility/glass/light_blue_glass");
        shardGlassRecipe(Items.LIGHT_GRAY_STAINED_GLASS).unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput.withConditions(zCond("glass_shard")), "quark:tweaks/crafting/utility/glass/light_gray_glass");
        shardGlassRecipe(Items.LIME_STAINED_GLASS).unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput.withConditions(zCond("glass_shard")), "quark:tweaks/crafting/utility/glass/lime_glass");
        shardGlassRecipe(Items.MAGENTA_STAINED_GLASS).unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput.withConditions(zCond("glass_shard")), "quark:tweaks/crafting/utility/glass/magenta_glass");
        shardGlassRecipe(Items.ORANGE_STAINED_GLASS).unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput.withConditions(zCond("glass_shard")), "quark:tweaks/crafting/utility/glass/orange_glass");
        shardGlassRecipe(Items.PINK_STAINED_GLASS).unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput.withConditions(zCond("glass_shard")), "quark:tweaks/crafting/utility/glass/pink_glass");
        shardGlassRecipe(Items.PURPLE_STAINED_GLASS).unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput.withConditions(zCond("glass_shard")), "quark:tweaks/crafting/utility/glass/purple_glass");
        shardGlassRecipe(Items.RED_STAINED_GLASS).unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput.withConditions(zCond("glass_shard")), "quark:tweaks/crafting/utility/glass/red_glass");
        shardGlassRecipe(Items.WHITE_STAINED_GLASS).unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput.withConditions(zCond("glass_shard")), "quark:tweaks/crafting/utility/glass/white_glass");
        shardGlassRecipe(Items.YELLOW_STAINED_GLASS).unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput.withConditions(zCond("glass_shard")), "quark:tweaks/crafting/utility/glass/yellow_glass");


        //TODO elytra duplication recipetype
        //TODO slab to full block recipetype
        //CATEGORY: World
            //  use stonecutterResultFromBase for stonecutter recipes
    }

    public static ShapedRecipeBuilder chestRecipe(ItemLike output, ItemLike plank) {
        return ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, output)
                .pattern("###")
                .pattern("# #")
                .pattern("###")
                .define('#', plank);
    }

    public static ShapelessRecipeBuilder trappedChestRecipe(ItemLike output, ItemLike originalChest){
        return ShapelessRecipeBuilder.shapeless(RecipeCategory.REDSTONE, output)
                .requires(originalChest)
                .requires(Items.TRIPWIRE_HOOK);
    }

    public static ShapedRecipeBuilder shardGlassRecipe(Item output){
        StainedGlassBlock glass = (StainedGlassBlock) Block.byItem(output);
        DyeColor shardColor = glass.getColor();
        Item shard = GlassShardModule.shardColors.get(shardColor);
        return ShapedRecipeBuilder.shaped(RecipeCategory.MISC, output)
                .pattern("##")
                .pattern("##")
                .define('#', shard);
    }

    public static ShapelessRecipeBuilder dyedFramedGlassRecipe(ItemLike output, DyeColor dye){
        return ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, output, 8)
                .requires(FramedGlassModule.framed_glass)
                .requires(FramedGlassModule.framed_glass)
                .requires(FramedGlassModule.framed_glass)
                .requires(FramedGlassModule.framed_glass)
                .requires(FramedGlassModule.framed_glass)
                .requires(FramedGlassModule.framed_glass)
                .requires(FramedGlassModule.framed_glass)
                .requires(FramedGlassModule.framed_glass)
                .requires(DyeItem.byColor(dye));
    }

    public static ShapedRecipeBuilder paneRecipe(ItemLike output, ItemLike glass) {
        return ShapedRecipeBuilder.shaped(RecipeCategory.MISC, output, 16)
                .pattern("###")
                .pattern("###")
                .define('#', glass);
    }

    public static ShapedRecipeBuilder corundomLampRecipe(ItemLike output, CorundumColor corundumColor) {
        Block corundum = CorundumModule.getCrystal(corundumColor);

        return ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, output)
                .pattern(" R ")
                .pattern("RCR")
                .pattern(" R ")
                .define('R', Tags.Items.DUSTS_REDSTONE)
                .define('C', corundum);
    }

    public static ShapedRecipeBuilder crystalLampRecipe(ItemLike output, CorundumColor corundumColor) {
        Item dye = DataUtil.getDyeItemFromCorondumColor(corundumColor);

        return ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, output)
                .pattern(" D ")
                .pattern("RAR")
                .pattern(" G ")
                .define('D', dye)
                .define('R', Tags.Items.DUSTS_REDSTONE)
                .define('A', Blocks.AMETHYST_BLOCK)
                .define('G', Tags.Items.DUSTS_GLOWSTONE);
    }

    public static ShapedRecipeBuilder hollowLogRecipe(ItemLike output, ItemLike solidLog) {
        return ShapedRecipeBuilder.shaped(RecipeCategory.MISC, output, 4)
                .pattern(" L ")
                .pattern("L L")
                .pattern(" L ")
                .define('L', solidLog)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick());
    }

    public static RecipeBuilder vertslabRecipe(RecipeCategory category, ItemLike output, Ingredient input) {
        if(output == null || input == null){
            System.out.println("Missing vertical/horizontal slab pair!!!");
        }
        return ShapedRecipeBuilder.shaped(category, output, 3).define('#', input)
                .pattern("#")
                .pattern("#")
                .pattern("#");
    }

    //multi-recipe methods
    public static void compressUncompress(ItemLike item, ItemLike block, RecipeOutput recipeOutput, String configFlag, String blockName){
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, block)
                .pattern("###")
                .pattern("###")
                .pattern("###")
                .define('#', item)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput.withConditions(zCond(blockName)), "quark:building/crafting/compressed/" + blockName);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, item, 9)
                .requires(block)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput, "quark:building/crafting/compressed/" + blockName + "uncompress");
    }

    public static void variantFurnace(ItemLike baseBlock, Block furnaceBlock, RecipeOutput recipeOutput, String blockName){
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, furnaceBlock)
                .pattern("###")
                .pattern("# #")
                .pattern("###")
                .define('#', baseBlock)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput.withConditions(zCond("variant_furnaces")), "quark:building/crafting/furnaces/" + blockName + "_furnace");

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Blocks.SMOKER)
                .pattern(" # ")
                .pattern("#X#")
                .pattern(" # ")
                .define('#', ItemTags.LOGS)
                .define('X', furnaceBlock)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput.withConditions(zCond("variant_furnaces")), "quark:building/crafting/furnaces/" + blockName + "_smoker");

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, furnaceBlock)
                .pattern("III")
                .pattern("IXI")
                .pattern("###")
                .define('#', Blocks.SMOOTH_STONE)
                .define('X', furnaceBlock)
                .define('I', Tags.Items.INGOTS_IRON)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput.withConditions(zCond("variant_furnaces")), "quark:building/crafting/furnaces/" + blockName + "_blast_furnace");

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.FURNACE_MINECART)
                .requires(furnaceBlock)
                .requires(Items.MINECART)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput.withConditions(zCond("variant_furnaces")), "quark:building/crafting/furnaces/" + blockName + "minecart"); //these weren't shapeless in 1.20

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, furnaceBlock)
                .pattern("#X#")
                .pattern("###")
                .define('#', Tags.Items.INGOTS_IRON)
                .define('X', furnaceBlock)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput.withConditions(zCond("variant_furnaces"), zCond("minecart_upgrade")), "quark:building/crafting/furnaces/" + blockName + "_minecarft_tweaked");
    }

    public static void colorShingles(ItemLike output, DyeColor color, RecipeOutput recipeOutput) {
        Block terracotta = DataUtil.getTerrracottaFromDyeColor(color);

        //condition is passed in.
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, output, 2)
                .pattern("##")
                .define('#', terracotta)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput, "quark:building/crafting/shingles/" + color.getName() + "_shingles");

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, output, 8)
                .pattern("SSS")
                .pattern("SDS")
                .pattern("SSS")
                .define('S', ShinglesModule.blocks.getFirst())
                .define('D', DyeItem.byColor(color))
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput, "quark:building/crafting/shingles/" + color.getName() + "_shingles_dye");
    }

    public static void colorStools(ItemLike output, DyeColor color, RecipeOutput recipeOutput) {
        Block wool = DataUtil.getWoolFromDyeColor(color);

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, output, 4)
                .pattern("###")
                .pattern("WWW")
                .define('#', wool)
                .define('W', ItemTags.WOODEN_SLABS)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput.withConditions(zCond("stools")), "quark:building/crafting/stools/" + color.getName() + "_stool");
    }


}
