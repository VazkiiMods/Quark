package org.violetmoon.quark.datagen;

import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.PlayerTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StainedGlassBlock;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.conditions.IConditionBuilder;
import org.violetmoon.quark.addons.oddities.module.*;
import org.violetmoon.quark.base.util.CorundumColor;
import org.violetmoon.quark.content.automation.module.*;
import org.violetmoon.quark.content.building.block.RainbowLampBlock;
import org.violetmoon.quark.content.building.module.*;
import org.violetmoon.quark.content.experimental.module.VariantSelectorModule;
import org.violetmoon.quark.content.mobs.module.StonelingsModule;
import org.violetmoon.quark.content.tools.module.*;
import org.violetmoon.quark.content.tweaks.module.GlassShardModule;
import org.violetmoon.quark.content.world.module.CorundumModule;
import org.violetmoon.zeta.block.IZetaBlock;
import org.violetmoon.zeta.config.FlagCondition;

import javax.annotation.RegEx;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

public class QuarkRecipeProvider extends RecipeProvider implements IConditionBuilder {
    public QuarkRecipeProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> holderLookupProvider) {
        super(packOutput, holderLookupProvider);
    }

    //TODO define config flag requirement. None of these recipes respect config rn - Partonetrain
    @Override
    protected void buildRecipes(RecipeOutput recipeOutput){
        //Automation
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ChuteModule.chute)
                .pattern("WWW")
                .pattern("SWS")
                .pattern(" S ")
                .define('W', ItemTags.PLANKS)
                .define('S', Tags.Items.RODS_WOODEN)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput.withConditions(new FlagCondition("chute", Optional.empty())), "quark:automation/crafting/chute");
        //crafter is vanilla now :)
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, EnderWatcherModule.ender_watcher)
                .pattern("BRB")
                .pattern("RER")
                .pattern("BRB")
                .define('R', Tags.Items.DUSTS_REDSTONE)
                .define('B', Blocks.OBSIDIAN.asItem())
                .define('E', Items.ENDER_EYE)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput, "quark:automation/crafting/ender_watcher");
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, FeedingTroughModule.feeding_trough)
                .pattern("#W#")
                .pattern("###")
                .define('#', ItemTags.PLANKS)
                .define('W', Items.WHEAT)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.REDSTONE, MetalButtonsModule.gold_button)
                .requires(ItemTags.WOODEN_BUTTONS)
                .requires(Tags.Items.INGOTS_GOLD)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput, "quark:automation/crafting/gold_button");
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, GravisandModule.gravisand)
                .pattern("SSS")
                .pattern("SES")
                .pattern("SSS")
                .define('S', Tags.Items.SANDS_COLORLESS)
                .define('E', Tags.Items.ENDER_PEARLS)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput, "quark:automation/crafting/gravisand");
        ShapelessRecipeBuilder.shapeless(RecipeCategory.REDSTONE, MetalButtonsModule.iron_button)
                .requires(ItemTags.WOODEN_BUTTONS)
                .requires(Tags.Items.INGOTS_IRON)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput, "quark:automation/crafting/iron_button");
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, IronRodModule.iron_rod)
                .pattern("I")
                .pattern("I")
                .pattern("R")
                .define('I', Tags.Items.INGOTS_IRON)
                .define('R', Blocks.END_ROD)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput, "quark:automation/crafting/iron_rod");
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, IronRodModule.iron_rod)
                .pattern("I")
                .pattern("I")
                .pattern("I")
                .define('I', Tags.Items.INGOTS_IRON)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput, "quark:automation/crafting/iron_rod_pre_end");
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, NetherBrickFenceGateModule.netherBrickFenceGate)
                .pattern("#W#")
                .pattern("#W#")
                .define('#', Tags.Items.BRICKS_NETHER) //TODO check if this is block or singular brick item
                .define('W', Blocks.NETHER_BRICKS.asItem())
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput, "quark:automation/crafting/nether_brick_fence_gate");
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ObsidianPlateModule.obsidian_plate)
                .pattern("OO")
                .define('O', Tags.Items.OBSIDIANS)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput, "quark:automation/crafting/obsidian_late");
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, RedstoneRandomizerModule.redstone_randomizer)
                .pattern(" X ")
                .pattern("XBX")
                .pattern("III")
                .define('X', Items.REDSTONE_TORCH)
                .define('I', Items.STONE)
                .define('B', Items.PRISMARINE_CRYSTALS)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput, "quark:automation/crafting/redstone_randomizer");
        //etc
        //Building
            //chests
        for (Block chest : VariantChestsModule.regularChests){
            //TODO make a way to reference planks from VariantChestsModule
            //chestRecipe(chest.asItem(), chestPlanks).unlockedBy("test", PlayerTrigger.TriggerInstance.tick()).save(recipeOutput, "quark:building/chests/" + "");
        }
        for (Block chest : VariantChestsModule.trappedChests){
            //TODO make a way to reference regular chests from trapped chests
            //trappedChestRecipe(chest.asItem(), chest.originalChest).unlockedBy("test", PlayerTrigger.TriggerInstance.tick()).save(recipeOutput, "quark:building/chests/" + "");
        }
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
        //TODO vanilla nether wart block override?
        compressUncompress(Items.NETHER_WART, CompressedBlocksModule.wart, recipeOutput, null, "nether_wart_sack");
        compressUncompress(Items.POTATO, CompressedBlocksModule.potato, recipeOutput, null, "potato_crate");
        compressUncompress(Items.STICK, CompressedBlocksModule.stick_block, recipeOutput, null, "stick_block");
        compressUncompress(Items.SUGAR_CANE, CompressedBlocksModule.sugarCane, recipeOutput, null, "sugar_cane_block");
            //furnaces
        variantFurnace(Blocks.BLACKSTONE, VariantFurnacesModule.blackstoneFurnace, recipeOutput, "blackstone");
        variantFurnace(Blocks.BLACKSTONE, VariantFurnacesModule.deepslateFurnace, recipeOutput, "deepslate");
        //TODO mixed furnace recipe
            //glass
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, FramedGlassModule.framed_glass, 8)
                .pattern("IGI")
                .pattern("G G")
                .pattern("IGI")
                .define('G', Tags.Items.GLASS_BLOCKS_COLORLESS)
                .define('I', Tags.Items.INGOTS_IRON)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput, "quark:building/crafting/glass/framed_glass"); //1.21 moved from quark:building/crafting/framed_glass.json
        for(DyeColor dyeColor : FramedGlassModule.blockMap.keySet()){
            dyedFramedGlassRecipe(FramedGlassModule.blockMap.get(dyeColor).getBlock(), dyeColor)
                    .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput, "quark:building/glass/" + dyeColor.getName() + "_framed_glass");
        }
            //hollowlogs
        for(Block sourceLog : HollowLogsModule.logMap.keySet()){
            hollowLogRecipe(HollowLogsModule.logMap.get(sourceLog), sourceLog).unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput, "quark:building/crafting/hollowlogs/hollow_" + sourceLog.getDescriptionId().replaceAll("block..*.[.]", ""));
        }
            //lamps
        for(RainbowLampBlock rbl : RainbowLampsModule.lamps){
            CorundumColor color = RainbowLampsModule.lampMap.get(rbl);
            corundomLampRecipe(rbl, color).unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput, "quark:building/crafting/lamps/" + color.name + "_corundum_lamp");
            crystalLampRecipe(rbl, color).unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput, "quark:building/crafting/lamps/" + color.name + "_crsytal_lamp");
        }
            //panes
        for(DyeColor dyeColor : FramedGlassModule.paneMap.keySet()){
            paneRecipe(FramedGlassModule.blockMap.get(dyeColor).getBlock(), FramedGlassModule.blockMap.get(dyeColor).getBlock())
                    .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput, "quark:building/panes/" + dyeColor.getName() + "_framed_glass_pane");
        }
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, JapanesePaletteModule.paperWall, 6)
                .pattern("###")
                .pattern("PPP")
                .pattern("###")
                .define('#', Items.BAMBOO)
                .define('P', Items.PAPER)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput, "quark:building/crafting/panes/paper_wall");
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, JapanesePaletteModule.paperWallBig, 4)
                .pattern("##")
                .pattern("##")
                .define('#', JapanesePaletteModule.paperWall)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput, "quark:tweaks/crafting/panes/paper_wall_big");
            //shingles
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ShinglesModule.blocks.getFirst(), 2)
                .pattern("##")
                .define('#', Blocks.TERRACOTTA)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput, "quark:building/crafting/shingles/shingles");
        for(DyeColor dyeColor : ShinglesModule.blockMap.keySet()){
            colorShingles(ShinglesModule.blockMap.get(dyeColor).getBlock(), dyeColor, recipeOutput);
        }
            //slabs
        //need variant registry access
            //stairs

            //stonevariants (vanilla folder removed 1.21, it was inconsistently used)
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MoreStoneVariantsModule.polishedCalcite)
                .pattern("##")
                .pattern("##")
                .define('#', Blocks.CALCITE)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput, "quark:building/crafting/stonevariants/polished_calcite");
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MoreStoneVariantsModule.polishedDripstone)
                .pattern("##")
                .pattern("##")
                .define('#', Blocks.DRIPSTONE_BLOCK)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput, "quark:building/crafting/stonevariants/polished_dripstone");
        //no polished tuff/tuff bricks, they are vanilla now


            //vertplanks

            //vertslabs

            //walls

            //bookshelves (new 1.21 folder)

            //hedges (new 1.21 folder)

            //leafcarpet (new 1.21 folder)

            //posts (new 1.21 folder)

            //ladders (new 1.21 folder)

            //stools (new 1.21 folder)

            //misc building blocks (duskbound, soul sandstone, grate, midori, raw metal bricks, rope, ironplate, paperwall/lantern, thatch)

        //Experimental
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, VariantSelectorModule.hammer)
                .pattern("III")
                .pattern("ISI")
                .pattern(" S ")
                .define('I', Tags.Items.INGOTS_IRON)
                .define('S', Tags.Items.RODS_WOODEN)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput , "quark:experimental/crafting/hammer"); //this recipe is called "trowel" in 1.20
        //Mobs
            //  RecipeProvider does not seem to have campfire recipes ??
        //Oddities
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, BackpackModule.backpack)
                .pattern("LRL")
                .pattern("LCL")
                .pattern("LIL")
                .define('L', Tags.Items.LEATHERS)
                .define('R', BackpackModule.ravager_hide)
                .define('C', Tags.Items.CHESTS_WOODEN)
                .define('I', Tags.Items.INGOTS_IRON)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput, "quark:oddities/crafting/backpack");
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, BackpackModule.backpack)
                .pattern("LIL")
                .pattern("LCL")
                .pattern("LIL")
                .define('L', Tags.Items.LEATHERS)
                .define('C', Tags.Items.CHESTS_WOODEN)
                .define('I', Tags.Items.INGOTS_IRON)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput, "quark:oddities/crafting/backpack_no_hide");
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, BackpackModule.ravager_hide, 9)
                .requires(BackpackModule.bonded_ravager_hide)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput, "quark:oddities/crafting/bonded_ravager_hide_uncompress");
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, BackpackModule.bonded_ravager_hide)
                .pattern("###")
                .pattern("###")
                .pattern("###")
                .define('#', BackpackModule.ravager_hide)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput, "quark:oddities/crafting/bonded_ravager_hide");
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CrateModule.crate)
                .pattern("IWI")
                .pattern("WCW")
                .pattern("IWI")
                .define('W', ItemTags.PLANKS)
                .define('I', Tags.Items.INGOTS_IRON)
                .define('C', Tags.Items.CHESTS_WOODEN)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput, "quark:oddities/crafting/crate");
        ShapelessRecipeBuilder.shapeless(RecipeCategory.REDSTONE, PipesModule.encasedPipe)
                .requires(PipesModule.pipe)
                .requires(Tags.Items.GLASS_BLOCKS_COLORLESS) //1.21 minecraft:glass -> c:glass_blocks/colorless
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput, "quark:oddities/crafting/encased_pipe");
        ShapelessRecipeBuilder.shapeless(RecipeCategory.REDSTONE, PipesModule.pipe)
                .requires(PipesModule.encasedPipe)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput, "quark:oddities/crafting/encased_pipe_revert");
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
                .save(recipeOutput, "quark:oddities/crafting/crate2what");
        //TODO magnet_pre_end
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, PipesModule.pipe)
                .pattern("I")
                .pattern("G")
                .pattern("I")
                .define('I', Tags.Items.INGOTS_COPPER)
                .define('G', Tags.Items.GLASS_BLOCKS)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput, "quark:oddities/crafting/pipe");
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TinyPotatoModule.tiny_potato)
                .pattern("H")
                .pattern("P")
                .define('H', StonelingsModule.diamondHeart)
                .define('P', Items.POTATO)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput, "quark:oddities/crafting/tiny_potato_heart");
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TinyPotatoModule.tiny_potato)
                .pattern("D")
                .pattern("P")
                .define('D', Tags.Items.GEMS_DIAMOND) //TODO how do you include Tags.Items.GEMS_EMERALD
                .define('P', Items.POTATO)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput, "quark:oddities/crafting/tiny_potato_no_heart");
        //Tools
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, AbacusModule.abacus)
                .pattern("WSW")
                .pattern("WIW")
                .pattern("WSW")
                .define('W', ItemTags.PLANKS)
                .define('S', Tags.Items.RODS_WOODEN)
                .define('I', Tags.Items.INGOTS_IRON)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput, "quark:tools/crafting/abacus");
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, PickarangModule.pickarang)
                .pattern("DWH")
                .pattern("  W")
                .pattern("  D")
                .define('W', ItemTags.PLANKS)
                .define('D', Tags.Items.GEMS_DIAMOND)
                .define('H', StonelingsModule.diamondHeart)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput, "quark:tools/crafting/pickarang_heart");
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, PickarangModule.pickarang)
                .pattern("DWD")
                .pattern("  W")
                .pattern("  D")
                .define('W', ItemTags.PLANKS)
                .define('D', Tags.Items.GEMS_DIAMOND)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput, "quark:tools/crafting/pickarang_no_heart");
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ColorRunesModule.rune)
                .pattern("#S#")
                .pattern("#C#")
                .pattern("###")
                .define('#', TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("quark", "corundum")))
                .define('S', Tags.Items.COBBLESTONES)
                .define('C', ColorRunesModule.rune)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput, "quark:tools/crafting/rune_duplication");
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, SeedPouchModule.seed_pouch, 1)
                .pattern(" S ")
                .pattern("HXH")
                .pattern(" H ")
                .define('S', Items.STRING) //there does not seem to be a convention string tag
                .define('H', TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("quark", "seed_pouch_holdable")))
                .define('X', ColorRunesModule.rune)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput, "quark:tools/crafting/seed_pouch");
        ShapelessRecipeBuilder.shapeless(RecipeCategory.TOOLS, TorchArrowModule.torch_arrow)
                .requires(Items.TORCH)
                .requires(Items.ARROW)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput, "quark:tools/crafting/torch_arrow");
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, TrowelModule.trowel)
                .pattern("S  ")
                .pattern(" II")
                .define('S', Tags.Items.RODS_WOODEN)
                .define('I', Tags.Items.INGOTS_IRON)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput, "quark:tools/crafting/trowel");
        //Tweaks
            //panes
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, GlassShardModule.dirtyGlassPane, 16)
                .pattern("###")
                .pattern("###")
                .define('#', GlassShardModule.dirtyGlass)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput, "quark:tweaks/crafting/panes/dirty_glass_pane");
            //utility/bent
        ShapedRecipeBuilder.shaped(RecipeCategory.FOOD, Items.BREAD)
                .pattern("##")
                .pattern("# ")
                .define('#', Items.WHEAT)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput, "quark:tweaks/crafting/utility/bent/bread");
        ShapedRecipeBuilder.shaped(RecipeCategory.FOOD, Items.COOKIE)
                .pattern("X#")
                .pattern("# ")
                .define('#', Items.WHEAT)
                .define('X', Items.COCOA_BEANS)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput, "quark:tweaks/crafting/utility/bent/cookie");
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Items.PAPER, 3)
                .pattern("##")
                .pattern("# ")
                .define('#', Items.SUGAR_CANE)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput, "quark:tweaks/crafting/utility/bent/paper");
        //TODO direct chest boat, 8 logs to chest (including mixed exclusion recipe type)

            //utility/coral
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.PINK_DYE)
                .requires(Ingredient.of(Items.BRAIN_CORAL, Items.BRAIN_CORAL_FAN))
                .group("pink_dye")
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput, "quark:tweaks/crafting/utility/coral/brain_to_pink");
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.MAGENTA_DYE)
                .requires(Ingredient.of(Items.BUBBLE_CORAL, Items.BUBBLE_CORAL_FAN))
                .group("magenta_dye")
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput, "quark:tweaks/crafting/utility/coral/bubble_to_magenta");
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.RED_DYE)
                .requires(Ingredient.of(Items.FIRE_CORAL, Items.FIRE_CORAL_FAN))
                .group("red_dye")
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput, "quark:tweaks/crafting/utility/coral/fire_to_red");
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.YELLOW_DYE)
                .requires(Ingredient.of(Items.HORN_CORAL, Items.HORN_CORAL_FAN))
                .group("yellow_dye")
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput, "quark:tweaks/crafting/utility/coral/horn_to_yellow");
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.BLUE_DYE)
                .requires(Ingredient.of(Items.TUBE_CORAL, Items.TUBE_CORAL_FAN))
                .group("blue_dye")
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput, "quark:tweaks/crafting/utility/coral/tube_to_blue");
            //utility/misc
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.BLACK_DYE)
                .requires(Items.CHARCOAL)
                .group("black_dye")
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput, "quark:tweaks/crafting/utility/misc/charcoal_to_black_dye");
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Items.CHEST_MINECART)
                .pattern("#C#")
                .pattern("###")
                .define('#', Tags.Items.INGOTS_IRON)
                .define('C', Tags.Items.CHESTS_WOODEN)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput, "quark:tweaks/crafting/utility/misc/chest_minecart");
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.DISPENSER)
                .requires(Items.BOW)
                .requires(Items.DROPPER)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput, "quark:tweaks/crafting/utility/misc/dispenser_bow");
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Items.DISPENSER)
                .pattern(" #X")
                .pattern("#DX")
                .pattern(" #X")
                .define('#', Tags.Items.RODS_WOODEN)
                .define('X', Items.STRING)
                .define('D', Items.DROPPER)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput, "quark:tweaks/crafting/utility/misc/dispenser_no_bow");
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Items.HOPPER)
                .pattern("IWI")
                .pattern("IWI")
                .pattern(" I ")
                .define('I', Tags.Items.INGOTS_IRON)
                .define('W', ItemTags.LOGS)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput, "quark:tweaks/crafting/utility/misc/easy_hopper");
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Items.STICK, 16)
                .pattern("#")
                .pattern("#")
                .define('#', ItemTags.LOGS)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput, "quark:tweaks/crafting/utility/misc/easy_sticks");
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Items.STICK, 8)
                .pattern("#")
                .pattern("#")
                .define('#', ItemTags.BAMBOO_BLOCKS)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput, "quark:tweaks/crafting/utility/misc/easy_sticks_bamboo");
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Items.FURNACE_MINECART)
                .pattern("#X#")
                .pattern("###")
                .define('#', Tags.Items.INGOTS_IRON)
                .define('X', Items.FURNACE)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput, "quark:tweaks/crafting/utility/misc/furnace_minecart");
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Items.HOPPER_MINECART)
                .pattern("#X#")
                .pattern("###")
                .define('#', Tags.Items.INGOTS_IRON)
                .define('X', Items.HOPPER)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput, "quark:tweaks/crafting/utility/misc/hopper_minecart");
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Items.HOPPER_MINECART)
                .pattern("X X")
                .pattern("#X#")
                .pattern("III")
                .define('#', Tags.Items.RODS_WOODEN)
                .define('X', Tags.Items.DUSTS_REDSTONE)
                .define('I', Items.STONE)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput, "quark:tweaks/crafting/utility/misc/repeater");
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Items.TNT_MINECART)
                .pattern("#X#")
                .pattern("###")
                .define('#', Tags.Items.INGOTS_IRON)
                .define('X', Items.TNT)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput, "quark:tweaks/crafting/utility/misc/tnt_minecart");
            //utility/tools

            //utility/wool

            //glass (new 1.21)
        shardGlassRecipe(Items.BLACK_STAINED_GLASS).unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput, "quark:tweaks/crafting/utility/glass/black_glass");
        shardGlassRecipe(Items.BLUE_STAINED_GLASS).unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput, "quark:tweaks/crafting/utility/glass/blue_glass");
        shardGlassRecipe(Items.BROWN_STAINED_GLASS).unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput, "quark:tweaks/crafting/utility/glass/brown_glass");
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Items.GLASS)
                .pattern("##")
                .pattern("##")
                .define('#', GlassShardModule.clearShard)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput, "quark:tweaks/crafting/utility/glass/clear_glass");
        shardGlassRecipe(Items.CYAN_STAINED_GLASS).unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput, "quark:tweaks/crafting/utility/glass/cyan_glass");
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, GlassShardModule.dirtyGlass)
                .pattern("##")
                .pattern("##")
                .define('#', GlassShardModule.dirtyShard)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput, "quark:tweaks/crafting/utility/glass/dirty_glass");
        shardGlassRecipe(Items.GRAY_STAINED_GLASS).unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput, "quark:tweaks/crafting/utility/glass/gray_glass");
        shardGlassRecipe(Items.GREEN_STAINED_GLASS).unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput, "quark:tweaks/crafting/utility/glass/green_glass");
        shardGlassRecipe(Items.LIGHT_BLUE_STAINED_GLASS).unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput, "quark:tweaks/crafting/utility/glass/light_blue_glass");
        shardGlassRecipe(Items.LIGHT_GRAY_STAINED_GLASS).unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput, "quark:tweaks/crafting/utility/glass/light_gray_glass");
        shardGlassRecipe(Items.LIME_STAINED_GLASS).unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput, "quark:tweaks/crafting/utility/glass/lime_glass");
        shardGlassRecipe(Items.MAGENTA_STAINED_GLASS).unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput, "quark:tweaks/crafting/utility/glass/magenta_glass");
        shardGlassRecipe(Items.ORANGE_STAINED_GLASS).unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput, "quark:tweaks/crafting/utility/glass/orange_glass");
        shardGlassRecipe(Items.PINK_STAINED_GLASS).unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput, "quark:tweaks/crafting/utility/glass/pink_glass");
        shardGlassRecipe(Items.PURPLE_STAINED_GLASS).unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput, "quark:tweaks/crafting/utility/glass/purple_glass");
        shardGlassRecipe(Items.RED_STAINED_GLASS).unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput, "quark:tweaks/crafting/utility/glass/red_glass");
        shardGlassRecipe(Items.WHITE_STAINED_GLASS).unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput, "quark:tweaks/crafting/utility/glass/white_glass");
        shardGlassRecipe(Items.YELLOW_STAINED_GLASS).unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput, "quark:tweaks/crafting/utility/glass/yellow_glass");


        //TODO elytra duplication recipetype
        //TODO slab to full block recipetype
        //World
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

        //todo conditions: rainbow_lamps, corundum, rainbow_lamp_corundum
        return ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, output)
                .pattern(" R ")
                .pattern("RCR")
                .pattern(" R ")
                .define('R', Tags.Items.DUSTS_REDSTONE)
                .define('C', corundum);
    }

    public static ShapedRecipeBuilder crystalLampRecipe(ItemLike output, CorundumColor corundumColor) {
        Item dye = switch (corundumColor){
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

        //todo conditions: rainbow_lamps && ( ! corundum || ! rainbow_lamp_corundum)
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
        //TODO some logs, like ancient, need hollow_logs AND their respective wood type enabled config conditions
        return ShapedRecipeBuilder.shaped(RecipeCategory.MISC, output, 4)
                .pattern(" L ")
                .pattern("L L")
                .pattern(" L ")
                .define('L', solidLog);
    }

    //multi-recipe methods
    public static void compressUncompress(ItemLike item, ItemLike block, RecipeOutput recipeOutput, String configFlag, String blockName){
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, block)
                .pattern("###")
                .pattern("###")
                .pattern("###")
                .define('#', item)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput, "quark:building/crafting/compressed/" + blockName);

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
                .save(recipeOutput, "quark:building/crafting/furnaces/" + blockName + "_furnace");

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Blocks.SMOKER)
                .pattern(" # ")
                .pattern("#X#")
                .pattern(" # ")
                .define('#', ItemTags.LOGS)
                .define('X', furnaceBlock)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput, "quark:building/crafting/furnaces/" + blockName + "_smoker");

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, furnaceBlock)
                .pattern("III")
                .pattern("IXI")
                .pattern("###")
                .define('#', Blocks.SMOOTH_STONE)
                .define('X', furnaceBlock)
                .define('I', Tags.Items.INGOTS_IRON)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput, "quark:building/crafting/furnaces/" + blockName + "_blast_furnace");

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.FURNACE_MINECART)
                .requires(furnaceBlock)
                .requires(Items.MINECART)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput, "quark:building/crafting/furnaces/" + blockName + "minecart"); //these weren't shapeless in 1.20

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, furnaceBlock)
                .pattern("#X#")
                .pattern("###")
                .define('#', Tags.Items.INGOTS_IRON)
                .define('X', furnaceBlock)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput, "quark:building/crafting/furnaces/" + blockName + "_minecarft_tweaked");
    }

    public static void colorShingles(ItemLike output, DyeColor color, RecipeOutput recipeOutput) {
        //please excuse disgusting switch table
        ItemLike terracotta = switch (color) {
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

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, output, 2)
                .pattern("##")
                .define('#', terracotta)
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput, "quark:building/crafting/shingles/" + color.getName() + "shingles");

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, output, 8)
                .pattern("SSS")
                .pattern("SDS")
                .pattern("SSS")
                .define('S', ShinglesModule.blocks.getFirst())
                .define('D', DyeItem.byColor(color))
                .unlockedBy("test", PlayerTrigger.TriggerInstance.tick())
                .save(recipeOutput, "quark:building/crafting/shingles/" + color.getName() + "shingles_dye");
    }



}
