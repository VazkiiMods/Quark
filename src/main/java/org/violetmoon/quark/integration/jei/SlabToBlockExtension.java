package org.violetmoon.quark.integration.jei;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.apache.commons.compress.utils.Lists;
import org.violetmoon.quark.content.tweaks.module.SlabsToBlocksModule;

import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;

public class SlabToBlockExtension<R extends CraftingRecipe> implements ICraftingCategoryExtension<R> {

	@Override
	public void setRecipe(RecipeHolder<R> recipeHolder, IRecipeLayoutBuilder builder, ICraftingGridHelper craftingGridHelper, IFocusGroup focuses) {
		List<ItemStack> input1 = Lists.newArrayList();
		List<ItemStack> input2 = Lists.newArrayList();
		List<ItemStack> outputs = Lists.newArrayList();

		for(Entry<Item, Item> recipe : SlabsToBlocksModule.recipes.entrySet()) {
			input1.add(new ItemStack(recipe.getKey()));
			input2.add(new ItemStack(recipe.getKey()));
			outputs.add(new ItemStack(recipe.getValue()));
		}

		List<IRecipeSlotBuilder> gridSlots = craftingGridHelper.createAndSetInputs(builder, Arrays.asList(input1, input2), 0, 0);
		IRecipeSlotBuilder outSlot = craftingGridHelper.createAndSetOutputs(builder, outputs);

		builder.createFocusLink(new IRecipeSlotBuilder[] {
				gridSlots.get(0), gridSlots.get(1), outSlot
		});
	}
}
