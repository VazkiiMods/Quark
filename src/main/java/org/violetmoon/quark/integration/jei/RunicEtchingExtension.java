package org.violetmoon.quark.integration.jei;

import mezz.jei.api.gui.builder.IIngredientAcceptor;
import mezz.jei.api.recipe.category.extensions.vanilla.smithing.ISmithingCategoryExtension;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;
import org.violetmoon.quark.content.tools.module.ColorRunesModule;
import org.violetmoon.quark.content.tools.recipe.SmithingRuneRecipe;

@SuppressWarnings("NonExtendableApiUsage")
public class RunicEtchingExtension implements ISmithingCategoryExtension<SmithingRuneRecipe> {
    public RunicEtchingExtension() {}

	//wip!

	@Override
	public <T extends IIngredientAcceptor<T>> void setTemplate(@NotNull SmithingRuneRecipe smithingRuneRecipe, T t) {
		System.out.println("setTemplate");
		t.addItemStack(ColorRunesModule.rune.getDefaultInstance());
	}

	@Override
	public <T extends IIngredientAcceptor<T>> void setBase(@NotNull SmithingRuneRecipe smithingRuneRecipe, T t) {
		System.out.println("setBase");
		Ingredient ingredient = SmithingRuneRecipe.createBaseIngredient();
		for (ItemStack stack : ingredient.getItems()){
			t.addItemStack(stack);
		}

	}

	@Override
	public <T extends IIngredientAcceptor<T>> void setAddition(@NotNull SmithingRuneRecipe smithingRuneRecipe, T t) {
		System.out.println("setAddition");
		Ingredient ingredient = smithingRuneRecipe.addition;
		for (ItemStack stack : ingredient.getItems()){
			t.addItemStack(stack);
		}
	}

	@Override
	public <T extends IIngredientAcceptor<T>> void setOutput(@NotNull SmithingRuneRecipe smithingRuneRecipe, T t) {
		System.out.println("setOutput");
		SmithingRuneRecipe.makeEnchantedDisplayItem(smithingRuneRecipe.getIngredients().get(0).getItems()[0]);
	}
}
