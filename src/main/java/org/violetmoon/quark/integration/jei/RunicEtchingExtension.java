package org.violetmoon.quark.integration.jei;

import mezz.jei.api.gui.builder.IIngredientAcceptor;
import mezz.jei.api.recipe.category.extensions.vanilla.smithing.ISmithingCategoryExtension;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;
import org.violetmoon.quark.content.tools.module.ColorRunesModule;
import org.violetmoon.quark.content.tools.recipe.SmithingRuneRecipe;

public class RunicEtchingExtension implements ISmithingCategoryExtension<SmithingRuneRecipe> {
    public RunicEtchingExtension() {}

	@Override
	public <T extends IIngredientAcceptor<T>> void setTemplate(@NotNull SmithingRuneRecipe smithingRuneRecipe, T t) {
		t.addItemStack(ColorRunesModule.rune.getDefaultInstance());
	}

	@Override
	public <T extends IIngredientAcceptor<T>> void setBase(@NotNull SmithingRuneRecipe smithingRuneRecipe, T t) {
		Ingredient ingredient = SmithingRuneRecipe.createBaseIngredient();
		for (ItemStack stack : ingredient.getItems()){
			t.addItemStack(stack);
		}

	}

	@Override
	public <T extends IIngredientAcceptor<T>> void setAddition(@NotNull SmithingRuneRecipe smithingRuneRecipe, T t) {
		Ingredient ingredient = smithingRuneRecipe.addition;
        if (ingredient != null && !ingredient.isEmpty()) {
            for (ItemStack stack : ingredient.getItems()) {
                t.addItemStack(stack);
            }
        }
	}

	@Override
	public <T extends IIngredientAcceptor<T>> void setOutput(@NotNull SmithingRuneRecipe smithingRuneRecipe, T t) {
        Ingredient ingredient = SmithingRuneRecipe.createBaseIngredient();
        for (ItemStack stack : ingredient.getItems()){
            ItemStack displayStack = SmithingRuneRecipe.makeEnchantedDisplayItem(stack);
            ColorRunesModule.withRune(displayStack, smithingRuneRecipe.runeColor);
            t.addItemStack(displayStack);
        }
	}
}
