package org.violetmoon.quark.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.conditions.IConditionBuilder;
import org.violetmoon.quark.content.automation.module.*;
import org.violetmoon.quark.content.building.module.NetherBrickFenceGateModule;
import org.violetmoon.quark.content.building.module.VariantChestsModule;
import org.violetmoon.quark.content.tools.module.AbacusModule;
import org.violetmoon.quark.content.tools.module.TrowelModule;

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
                .save(recipeOutput);
        //crafter is vanilla now :)
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, EnderWatcherModule.ender_watcher)
                .pattern("BRB")
                .pattern("RER")
                .pattern("BRB")
                .define('R', Tags.Items.DUSTS_REDSTONE)
                .define('B', Blocks.OBSIDIAN.asItem())
                .define('E', Items.ENDER_EYE)
                .save(recipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, FeedingTroughModule.feeding_trough)
                .pattern("#W#")
                .pattern("###")
                .define('#', ItemTags.PLANKS)
                .define('#', Items.WHEAT)
                .save(recipeOutput);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.REDSTONE, MetalButtonsModule.gold_button)
                .requires(ItemTags.WOODEN_BUTTONS)
                .requires(Tags.Items.INGOTS_GOLD)
                .save(recipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, GravisandModule.gravisand)
                .pattern("SSS")
                .pattern("SES")
                .pattern("SSS")
                .define('S', Tags.Items.SANDS_COLORLESS)
                .define('E', Tags.Items.ENDER_PEARLS)
                .save(recipeOutput);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.REDSTONE, MetalButtonsModule.iron_button)
                .requires(ItemTags.WOODEN_BUTTONS)
                .requires(Tags.Items.INGOTS_IRON)
                .save(recipeOutput);
        //TODO 2 iron rod recipes depending on config: pre-end
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, NetherBrickFenceGateModule.netherBrickFenceGate)
                .pattern("#W#")
                .pattern("#W#")
                .define('#', Tags.Items.BRICKS_NETHER) //TODO check if this is block or singular brick item
                .define('W', Blocks.NETHER_BRICKS.asItem())
                .save(recipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ObsidianPlateModule.obsidian_plate)
                .pattern("OO")
                .define('W', Tags.Items.OBSIDIANS)
                .save(recipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, RedstoneRandomizerModule.redstone_randomizer)
                .pattern(" X ")
                .pattern("XBX")
                .pattern("III")
                .define('X', Items.REDSTONE_TORCH)
                .define('X', Items.STONE)
                .define('X', Items.PRISMARINE_CRYSTALS)
                .save(recipeOutput);
        //etc
        //Building
        for (Block chest : VariantChestsModule.regularChests){
            //TODO make a way to reference planks from VariantChestsModule
            //chestRecipe(chest.asItem(), chestPlanks).save(recipeOutput, "quark:building/chests/" + "");
        }
        for (Block chest : VariantChestsModule.trappedChests){
            //TODO make a way to reference regular chests from trapped chests
            //trappedChestRecipe(chest.asItem(), chest.originalChest).save(recipeOutput, "quark:building/chests/" + "");
        }
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, AbacusModule.abacus)
                .pattern("WSW")
                .pattern("WIW")
                .pattern("WSW")
                .define('W', ItemTags.PLANKS)
                .define('S', Tags.Items.RODS_WOODEN)
                .define('I', Tags.Items.INGOTS_IRON)
                .save(recipeOutput);
        //Experimental
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, TrowelModule.trowel)
                .pattern("III")
                .pattern("ISI")
                .pattern(" S ")
                .define('I', Tags.Items.INGOTS_IRON)
                .define('S', Tags.Items.RODS_WOODEN)
                .save(recipeOutput);
        //Mobs
            //  RecipeProvider does not seem to have campfire recipes ??
        //Oddities
        //Tools
        //Tweaks
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



}
