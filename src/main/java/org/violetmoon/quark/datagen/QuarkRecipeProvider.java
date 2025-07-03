package org.violetmoon.quark.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.conditions.IConditionBuilder;
import org.violetmoon.quark.content.automation.module.*;
import org.violetmoon.quark.content.building.module.NetherBrickFenceGateModule;
import org.violetmoon.quark.content.building.module.VariantChestsModule;
import org.violetmoon.quark.content.experimental.item.HammerItem;
import org.violetmoon.quark.content.experimental.module.VariantSelectorModule;
import org.violetmoon.quark.content.tools.module.*;

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
        for (Block chest : VariantChestsModule.regularChests){
            //TODO make a way to reference planks from VariantChestsModule
            //chestRecipe(chest.asItem(), chestPlanks).save(recipeOutput, "quark:building/chests/" + "");
        }
        for (Block chest : VariantChestsModule.trappedChests){
            //TODO make a way to reference regular chests from trapped chests
            //trappedChestRecipe(chest.asItem(), chest.originalChest).save(recipeOutput, "quark:building/chests/" + "");
        }

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
        //Tools
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, AbacusModule.abacus)
                .pattern("WSW")
                .pattern("WIW")
                .pattern("WSW")
                .define('W', ItemTags.PLANKS)
                .define('S', Tags.Items.RODS_WOODEN)
                .define('I', Tags.Items.INGOTS_IRON)
                .save(recipeOutput, "quark:tools/crafting/abacus");
        //TODO 2 pickarang recipes dependent on stoneling heart
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
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, TrowelModule.trowel)
                .pattern("S  ")
                .pattern(" II")
                .define('S', Tags.Items.RODS_WOODEN)
                .define('I', Tags.Items.INGOTS_IRON)
                .save(recipeOutput, "quark:tools/crafting/trowel");
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
