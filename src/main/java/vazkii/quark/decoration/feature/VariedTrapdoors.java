/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Quark Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Quark
 *
 * Quark is Open Source and distributed under the
 * CC-BY-NC-SA 3.0 License: https://creativecommons.org/licenses/by-nc-sa/3.0/deed.en_GB
 *
 * File Created @ [24/03/2016, 17:13:26 (GMT)]
 */
package vazkii.quark.decoration.feature;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.oredict.OreDictionary;
import vazkii.arl.recipe.RecipeHandler;
import vazkii.arl.util.ProxyRegistry;
import vazkii.quark.base.block.BlockQuarkTrapdoor;
import vazkii.quark.base.module.Feature;

public class VariedTrapdoors extends Feature {

	public static Block spruce_trapdoor;
	public static Block birch_trapdoor;
	public static Block jungle_trapdoor;
	public static Block acacia_trapdoor;
	public static Block dark_oak_trapdoor;

	boolean renameVanillaTrapdoor;
	int recipeOutput;

	@Override
	public void setupConfig() {
		renameVanillaTrapdoor = loadPropBool("Rename vanilla trapdoor to Oak Trapdoor", "", true);
		recipeOutput = loadPropInt("Amount of trapdoors crafted (vanilla is 2)", "", 6);
	}

	@Override
	public void preInit(FMLPreInitializationEvent event) {
		spruce_trapdoor = new BlockQuarkTrapdoor("spruce_trapdoor");
		birch_trapdoor = new BlockQuarkTrapdoor("birch_trapdoor");
		jungle_trapdoor = new BlockQuarkTrapdoor("jungle_trapdoor");
		acacia_trapdoor = new BlockQuarkTrapdoor("acacia_trapdoor");
		dark_oak_trapdoor = new BlockQuarkTrapdoor("dark_oak_trapdoor");
	}

	@Override
	public void postPreInit(FMLPreInitializationEvent event) {		
		List<ResourceLocation> recipeList = new ArrayList(CraftingManager.REGISTRY.getKeys());
		for(ResourceLocation res : recipeList) {
			IRecipe recipe = CraftingManager.REGISTRY.getObject(res);
			ItemStack out = recipe.getRecipeOutput();
			if(recipe instanceof ShapedRecipes && !out.isEmpty() && (out.getItem() == Item.getItemFromBlock(Blocks.TRAPDOOR))) {
				ShapedRecipes shaped = (ShapedRecipes) recipe;
				NonNullList<Ingredient> ingredients = shaped.recipeItems;
				for(int i = 0; i < ingredients.size(); i++) {
					Ingredient ingr = ingredients.get(i);
					if(ingr.apply(ProxyRegistry.newStack(Blocks.PLANKS)))
						ingredients.set(i, Ingredient.fromStacks(ProxyRegistry.newStack(Blocks.PLANKS, 1, 0)));
				}
			}
		}

		RecipeHandler.addOreDictRecipe(ProxyRegistry.newStack(Blocks.TRAPDOOR, recipeOutput),
				"WWW", "WWW",
				'W', ProxyRegistry.newStack(Blocks.PLANKS));

		RecipeHandler.addOreDictRecipe(ProxyRegistry.newStack(spruce_trapdoor, recipeOutput),
				"WWW", "WWW",
				'W', ProxyRegistry.newStack(Blocks.PLANKS, 1, 1));
		RecipeHandler.addOreDictRecipe(ProxyRegistry.newStack(birch_trapdoor, recipeOutput),
				"WWW", "WWW",
				'W', ProxyRegistry.newStack(Blocks.PLANKS, 1, 2));
		RecipeHandler.addOreDictRecipe(ProxyRegistry.newStack(jungle_trapdoor, recipeOutput),
				"WWW", "WWW",
				'W', ProxyRegistry.newStack(Blocks.PLANKS, 1, 3));
		RecipeHandler.addOreDictRecipe(ProxyRegistry.newStack(acacia_trapdoor, recipeOutput),
				"WWW", "WWW",
				'W', ProxyRegistry.newStack(Blocks.PLANKS, 1, 4));
		RecipeHandler.addOreDictRecipe(ProxyRegistry.newStack(dark_oak_trapdoor, recipeOutput),
				"WWW", "WWW",
				'W', ProxyRegistry.newStack(Blocks.PLANKS, 1, 5));

		if(renameVanillaTrapdoor)
			Blocks.TRAPDOOR.setUnlocalizedName("oak_trapdoor");

		// Low priority ore dictionary recipe
		RecipeHandler.addOreDictRecipe(ProxyRegistry.newStack(Blocks.TRAPDOOR, recipeOutput),
				"WWW", "WWW",
				'W', "plankWood");
	}
	
	@Override
	public void init(FMLInitializationEvent event) {
		OreDictionary.registerOre("trapdoorWood", spruce_trapdoor);
		OreDictionary.registerOre("trapdoorWood", birch_trapdoor);
		OreDictionary.registerOre("trapdoorWood", jungle_trapdoor);
		OreDictionary.registerOre("trapdoorWood", acacia_trapdoor);
		OreDictionary.registerOre("trapdoorWood", dark_oak_trapdoor);
	}
	
	@Override
	public boolean requiresMinecraftRestartToEnable() {
		return true;
	}
}
