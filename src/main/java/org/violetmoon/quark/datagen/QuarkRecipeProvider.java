package org.violetmoon.quark.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
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
import org.violetmoon.quark.content.automation.module.*;
import org.violetmoon.quark.content.building.module.NetherBrickFenceGateModule;
import org.violetmoon.quark.content.building.module.VariantChestsModule;
import org.violetmoon.quark.content.experimental.item.HammerItem;
import org.violetmoon.quark.content.experimental.module.VariantSelectorModule;
import org.violetmoon.quark.content.mobs.module.StonelingsModule;
import org.violetmoon.quark.content.tools.module.*;
import org.violetmoon.quark.content.tweaks.module.GlassShardModule;

import java.util.concurrent.CompletableFuture;

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
                .save(recipeOutput, "quark:automation/crafting/chute");
        //crafter is vanilla now :)
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, EnderWatcherModule.ender_watcher)
                .pattern("BRB")
                .pattern("RER")
                .pattern("BRB")
                .define('R', Tags.Items.DUSTS_REDSTONE)
                .define('B', Blocks.OBSIDIAN.asItem())
                .define('E', Items.ENDER_EYE)
                .save(recipeOutput, "quark:automation/crafting/ender_watcher");
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, FeedingTroughModule.feeding_trough)
                .pattern("#W#")
                .pattern("###")
                .define('#', ItemTags.PLANKS)
                .define('#', Items.WHEAT)
                .save(recipeOutput);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.REDSTONE, MetalButtonsModule.gold_button)
                .requires(ItemTags.WOODEN_BUTTONS)
                .requires(Tags.Items.INGOTS_GOLD)
                .save(recipeOutput, "quark:automation/crafting/gold_button");
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, GravisandModule.gravisand)
                .pattern("SSS")
                .pattern("SES")
                .pattern("SSS")
                .define('S', Tags.Items.SANDS_COLORLESS)
                .define('E', Tags.Items.ENDER_PEARLS)
                .save(recipeOutput, "quark:automation/crafting/gravisand");
        ShapelessRecipeBuilder.shapeless(RecipeCategory.REDSTONE, MetalButtonsModule.iron_button)
                .requires(ItemTags.WOODEN_BUTTONS)
                .requires(Tags.Items.INGOTS_IRON)
                .save(recipeOutput, "quark:automation/crafting/iron_button");
        //TODO 2 iron rod recipes depending on config: pre-end
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, NetherBrickFenceGateModule.netherBrickFenceGate)
                .pattern("#W#")
                .pattern("#W#")
                .define('#', Tags.Items.BRICKS_NETHER) //TODO check if this is block or singular brick item
                .define('W', Blocks.NETHER_BRICKS.asItem())
                .save(recipeOutput, "quark:automation/crafting/nether_brick_fence_gate");
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ObsidianPlateModule.obsidian_plate)
                .pattern("OO")
                .define('W', Tags.Items.OBSIDIANS)
                .save(recipeOutput, "quark:automation/crafting/obsidian_late");
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, RedstoneRandomizerModule.redstone_randomizer)
                .pattern(" X ")
                .pattern("XBX")
                .pattern("III")
                .define('X', Items.REDSTONE_TORCH)
                .define('X', Items.STONE)
                .define('X', Items.PRISMARINE_CRYSTALS)
                .save(recipeOutput, "quark:automation/crafting/redstone_randomizer");
        //etc
        //Building
            //chests
        for (Block chest : VariantChestsModule.regularChests){
            //TODO make a way to reference planks from VariantChestsModule
            //chestRecipe(chest.asItem(), chestPlanks).save(recipeOutput, "quark:building/chests/" + "");
        }
        for (Block chest : VariantChestsModule.trappedChests){
            //TODO make a way to reference regular chests from trapped chests
            //trappedChestRecipe(chest.asItem(), chest.originalChest).save(recipeOutput, "quark:building/chests/" + "");
        }
            //compressed

            //furnaces

            //glass

            //hollowlogs

            //lamps

            //panes

            //shingles

            //slabs

            //stairs

            //stonevariants

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
                .save(recipeOutput , "quark:experimental/crafting/hammer");
        //Mobs
            //  RecipeProvider does not seem to have campfire recipes ??
        //Oddities
        //TODO 2 backpack recipes dependant on config
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, BackpackModule.ravager_hide, 9)
                .requires(BackpackModule.bonded_ravager_hide)
                .save(recipeOutput, "quark:oddities/crafting/bonded_ravager_hide_uncompress");
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, BackpackModule.bonded_ravager_hide)
                .pattern("###")
                .pattern("###")
                .pattern("###")
                .define('#', BackpackModule.ravager_hide)
                .save(recipeOutput, "quark:oddities/crafting/bonded_ravager_hide");
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CrateModule.crate)
                .pattern("IWI")
                .pattern("WCW")
                .pattern("IWI")
                .define('W', ItemTags.PLANKS)
                .define('I', Tags.Items.INGOTS_IRON)
                .define('C', Tags.Items.CHESTS_WOODEN)
                .save(recipeOutput, "quark:oddities/crafting/crate");
        ShapelessRecipeBuilder.shapeless(RecipeCategory.REDSTONE, PipesModule.encasedPipe)
                .requires(PipesModule.pipe)
                .requires(Tags.Items.GLASS_BLOCKS_COLORLESS) //1.21 minecraft:glass -> c:glass_blocks/colorless
                .save(recipeOutput, "quark:oddities/crafting/encased_pipe");
        ShapelessRecipeBuilder.shapeless(RecipeCategory.REDSTONE, PipesModule.pipe)
                .requires(PipesModule.encasedPipe)
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
                .save(recipeOutput, "quark:oddities/crafting/crate");
        //TODO magnet_pre_end
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, PipesModule.pipe)
                .pattern("I")
                .pattern("G")
                .pattern("I")
                .define('I', Tags.Items.INGOTS_COPPER)
                .define('G', Tags.Items.GLASS_BLOCKS)
                .save(recipeOutput, "quark:oddities/crafting/pipe");
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TinyPotatoModule.tiny_potato)
                .pattern("H")
                .pattern("P")
                .define('H', StonelingsModule.diamondHeart)
                .define('P', Items.POTATO)
                .save(recipeOutput, "quark:oddities/crafting/tiny_potato_heart");
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TinyPotatoModule.tiny_potato)
                .pattern("D")
                .pattern("P")
                .define('D', Tags.Items.GEMS_DIAMOND) //TODO how do you include Tags.Items.GEMS_EMERALD
                .define('P', Items.POTATO)
                .save(recipeOutput, "quark:oddities/crafting/tiny_potato_no_heart");
        //Tools
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, AbacusModule.abacus)
                .pattern("WSW")
                .pattern("WIW")
                .pattern("WSW")
                .define('W', ItemTags.PLANKS)
                .define('S', Tags.Items.RODS_WOODEN)
                .define('I', Tags.Items.INGOTS_IRON)
                .save(recipeOutput, "quark:tools/crafting/abacus");
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, PickarangModule.pickarang)
                .pattern("DWH")
                .pattern("  W")
                .pattern("  D")
                .define('W', ItemTags.PLANKS)
                .define('D', Tags.Items.GEMS_DIAMOND)
                .define('H', StonelingsModule.diamondHeart)
                .save(recipeOutput, "quark:tools/crafting/pickarang_heart");
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, PickarangModule.pickarang)
                .pattern("DWD")
                .pattern("  W")
                .pattern("  D")
                .define('W', ItemTags.PLANKS)
                .define('D', Tags.Items.GEMS_DIAMOND)
                .save(recipeOutput, "quark:tools/crafting/pickarang_no_heart");
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ColorRunesModule.rune)
                .pattern("#S#")
                .pattern("#C#")
                .pattern("###")
                .define('#', TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("quark", "corundum")))
                .define('S', Tags.Items.COBBLESTONES)
                .define('I', ColorRunesModule.rune)
                .save(recipeOutput, "quark:tools/crafting/rune_duplication");
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, SeedPouchModule.seed_pouch, 2)
                .pattern(" S ")
                .pattern("HXH")
                .pattern(" H")
                .define('S', Items.STRING) //there does not seem to be a convention string tag
                .define('H', TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("quark", "seed_pouch_holdable")))
                .define('X', ColorRunesModule.rune)
                .save(recipeOutput, "quark:tools/crafting/seed_pouch");
        ShapelessRecipeBuilder.shapeless(RecipeCategory.TOOLS, TorchArrowModule.torch_arrow)
                .requires(Items.TORCH)
                .requires(Items.ARROW)
                .save(recipeOutput, "quark:tools/crafting/torch_arrow");
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, TrowelModule.trowel)
                .pattern("S  ")
                .pattern(" II")
                .define('S', Tags.Items.RODS_WOODEN)
                .define('I', Tags.Items.INGOTS_IRON)
                .save(recipeOutput, "quark:tools/crafting/trowel");
        //Tweaks
            //panes
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, GlassShardModule.dirtyGlassPane, 16)
                .pattern("###")
                .pattern("###")
                .define('#', GlassShardModule.dirtyGlass)
                .save(recipeOutput, "quark:tweaks/crafting/panes/dirty_glass_pane");
            //utility/bent
        ShapedRecipeBuilder.shaped(RecipeCategory.FOOD, Items.BREAD)
                .pattern("##")
                .pattern("# ")
                .define('#', Items.WHEAT)
                .save(recipeOutput, "quark:tweaks/crafting/utility/bent/bread");
        ShapedRecipeBuilder.shaped(RecipeCategory.FOOD, Items.COOKIE)
                .pattern("X#")
                .pattern("# ")
                .define('#', Items.WHEAT)
                .define('X', Items.COCOA_BEANS)
                .save(recipeOutput, "quark:tweaks/crafting/utility/bent/cookie");
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Items.PAPER, 3)
                .pattern("##")
                .pattern("# ")
                .define('#', Items.SUGAR_CANE)
                .save(recipeOutput, "quark:tweaks/crafting/utility/bent/paper");
        //TODO direct chest boat, 8 logs to chest (including mixed exclusion recipe type), coral
            //utility/misc
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.BLACK_DYE)
                .requires(Items.CHARCOAL)
                .group("black_dye")
                .save(recipeOutput, "quark:tweaks/crafting/utility/misc/charcoal_to_black_dye");
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Items.CHEST_MINECART)
                .pattern("#C#")
                .pattern("### ")
                .define('#', Tags.Items.INGOTS_IRON)
                .define('C', Tags.Items.CHESTS_WOODEN)
                .save(recipeOutput, "quark:tweaks/crafting/utility/misc/chest_minecart");
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.DISPENSER)
                .requires(Items.BOW)
                .requires(Items.DROPPER)
                .save(recipeOutput, "quark:tweaks/crafting/utility/misc/dispenser_bow");
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Items.DISPENSER)
                .pattern(" #X")
                .pattern("#DX")
                .pattern(" #X")
                .define('#', Tags.Items.RODS_WOODEN)
                .define('X', Items.STRING)
                .define('D', Items.DROPPER)
                .save(recipeOutput, "quark:tweaks/crafting/utility/misc/dispenser_no_bow");
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Items.HOPPER)
                .pattern("IWI")
                .pattern("IWI")
                .pattern(" I ")
                .define('I', Tags.Items.INGOTS_IRON)
                .define('X', ItemTags.LOGS)
                .save(recipeOutput, "quark:tweaks/crafting/utility/misc/easy_hopper");
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Items.STICK, 16)
                .pattern("#")
                .pattern("#")
                .define('#', ItemTags.LOGS)
                .save(recipeOutput, "quark:tweaks/crafting/utility/misc/easy_sticks");
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Items.STICK, 8)
                .pattern("#")
                .pattern("#")
                .define('#', ItemTags.BAMBOO_BLOCKS)
                .save(recipeOutput, "quark:tweaks/crafting/utility/misc/easy_sticks_bamboo");
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Items.FURNACE_MINECART)
                .pattern("#X#")
                .pattern("###")
                .define('#', Tags.Items.INGOTS_IRON)
                .define('#', Items.FURNACE)
                .save(recipeOutput, "quark:tweaks/crafting/utility/misc/furnace_minecart");
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Items.HOPPER_MINECART)
                .pattern("#X#")
                .pattern("###")
                .define('#', Tags.Items.INGOTS_IRON)
                .define('#', Items.HOPPER)
                .save(recipeOutput, "quark:tweaks/crafting/utility/misc/hopper_minecart");
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Items.HOPPER_MINECART)
                .pattern("X X")
                .pattern("#X#")
                .pattern("III")
                .define('#', Tags.Items.RODS_WOODEN)
                .define('X', Tags.Items.DUSTS_REDSTONE)
                .define('I', Items.STONE)
                .save(recipeOutput, "quark:tweaks/crafting/utility/misc/repeater");
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Items.TNT_MINECART)
                .pattern("#X#")
                .pattern("###")
                .define('#', Tags.Items.INGOTS_IRON)
                .define('#', Items.TNT)
                .save(recipeOutput, "quark:tweaks/crafting/utility/misc/tnt_minecart");
            //utility/tools

            //utility/wool

            //glass (new 1.21)

        shardGlassRecipe(Items.BLACK_STAINED_GLASS).save(recipeOutput, "quark:tweaks/crafting/utility/glass/black_glass");
        shardGlassRecipe(Items.BLUE_STAINED_GLASS).save(recipeOutput, "quark:tweaks/crafting/utility/glass/blue_glass");
        shardGlassRecipe(Items.BROWN_STAINED_GLASS).save(recipeOutput, "quark:tweaks/crafting/utility/glass/brown_glass");
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Items.GLASS)
                .pattern("##")
                .pattern("##")
                .define('#', GlassShardModule.clearShard)
                .save(recipeOutput, "quark:tweaks/crafting/utility/glass/clear_glass");
        shardGlassRecipe(Items.CYAN_STAINED_GLASS).save(recipeOutput, "quark:tweaks/crafting/utility/glass/cyan_glass");
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, GlassShardModule.dirtyGlass)
                .pattern("##")
                .pattern("##")
                .define('#', GlassShardModule.dirtyShard)
                .save(recipeOutput, "quark:tweaks/crafting/utility/glass/dirty_glass");
        shardGlassRecipe(Items.GRAY_STAINED_GLASS).save(recipeOutput, "quark:tweaks/crafting/utility/glass/gray_glass");
        shardGlassRecipe(Items.GREEN_STAINED_GLASS).save(recipeOutput, "quark:tweaks/crafting/utility/glass/green_glass");
        shardGlassRecipe(Items.LIGHT_BLUE_STAINED_GLASS).save(recipeOutput, "quark:tweaks/crafting/utility/glass/light_blue_glass");
        shardGlassRecipe(Items.LIGHT_GRAY_STAINED_GLASS).save(recipeOutput, "quark:tweaks/crafting/utility/glass/light_gray_glass");
        shardGlassRecipe(Items.LIME_STAINED_GLASS).save(recipeOutput, "quark:tweaks/crafting/utility/glass/lime_glass");
        shardGlassRecipe(Items.MAGENTA_STAINED_GLASS).save(recipeOutput, "quark:tweaks/crafting/utility/glass/magenta_glass");
        shardGlassRecipe(Items.ORANGE_STAINED_GLASS).save(recipeOutput, "quark:tweaks/crafting/utility/glass/orange_glass");
        shardGlassRecipe(Items.PINK_STAINED_GLASS).save(recipeOutput, "quark:tweaks/crafting/utility/glass/pink_glass");
        shardGlassRecipe(Items.PURPLE_STAINED_GLASS).save(recipeOutput, "quark:tweaks/crafting/utility/glass/purple_glass");
        shardGlassRecipe(Items.RED_STAINED_GLASS).save(recipeOutput, "quark:tweaks/crafting/utility/glass/red_glass");
        shardGlassRecipe(Items.WHITE_STAINED_GLASS).save(recipeOutput, "quark:tweaks/crafting/utility/glass/white_glass");
        shardGlassRecipe(Items.YELLOW_STAINED_GLASS).save(recipeOutput, "quark:tweaks/crafting/utility/glass/yellow_glass");


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



}
