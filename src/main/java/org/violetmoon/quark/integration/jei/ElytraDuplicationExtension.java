package org.violetmoon.quark.integration.jei;

import com.google.common.collect.Lists;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.violetmoon.quark.base.QuarkClient;
import org.violetmoon.quark.content.tweaks.recipe.ElytraDuplicationRecipe;

import java.util.ArrayList;
import java.util.List;

public class ElytraDuplicationExtension<R extends CraftingRecipe> implements ICraftingCategoryExtension<R> {
    public ElytraDuplicationExtension() {}

	@Override
	public void setRecipe(RecipeHolder<R> recipeHolder, IRecipeLayoutBuilder builder, ICraftingGridHelper craftingGridHelper, IFocusGroup focuses) {
		List<List<ItemStack>> inputLists = new ArrayList<>();
		for(Ingredient input : recipeHolder.value().getIngredients()) {
			ItemStack[] stacks = input.getItems();
			List<ItemStack> expandedInput = List.of(stacks);
			inputLists.add(expandedInput);
		}
		craftingGridHelper.createAndSetInputs(builder, VanillaTypes.ITEM_STACK, inputLists, 0, 0);
		craftingGridHelper.createAndSetOutputs(builder, VanillaTypes.ITEM_STACK, Lists.newArrayList(recipeHolder.value().getResultItem(QuarkClient.ZETA_CLIENT.hackilyGetCurrentClientLevelRegistryAccess())));
	}

	@Override
	public void drawInfo(RecipeHolder<R> recipe, int recipeWidth, int recipeHeight, GuiGraphics guiGraphics, double mouseX, double mouseY) {
		guiGraphics.drawString(Minecraft.getInstance().font, I18n.get("quark.jei.makes_copy"), 60, 46, 0x555555);
	}
}
