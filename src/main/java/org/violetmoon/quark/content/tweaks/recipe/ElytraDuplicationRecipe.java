package org.violetmoon.quark.content.tweaks.recipe;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ElytraItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.NotNull;

import org.violetmoon.quark.content.tweaks.module.DragonScalesModule;

public class ElytraDuplicationRecipe extends CustomRecipe {

	public static final SimpleCraftingRecipeSerializer<?> SERIALIZER = new SimpleCraftingRecipeSerializer<>(ElytraDuplicationRecipe::new);

	public ElytraDuplicationRecipe(CraftingBookCategory category) {
		super(category);
	}

	@Override
	public boolean matches(CraftingInput input, Level level) {
		if (input.ingredientCount() != 2) return false;
		boolean hasElytra = false, hasScale = false;

		for (int i = 0; i < input.size(); ++i) {
			ItemStack stack = input.getItem(i);
			if (stack.getItem() instanceof ElytraItem) {
				hasElytra = true;
			} else if (stack.getItem() == DragonScalesModule.dragon_scale) {
				hasScale = true;
			}
		}
		return hasElytra && hasScale;
	}

	@Override
	public ItemStack assemble(CraftingInput input, HolderLookup.Provider provider) {
		return getResultItem(provider);
	}

	@NotNull
	@Override
	public ItemStack getResultItem(HolderLookup.Provider provider) {
		ItemStack stack = new ItemStack(Items.ELYTRA);

//		if(EnderdragonScales.dyeBlack && ModuleLoader.isFeatureEnabled(DyableElytra.class))
//			ItemNBTHelper.setInt(stack, DyableElytra.TAG_ELYTRA_DYE, 0);

		return stack;
	}

	@NotNull
	@Override
	public NonNullList<ItemStack> getRemainingItems(CraftingInput inv) {
		NonNullList<ItemStack> ret = NonNullList.withSize(inv.ingredientCount(), ItemStack.EMPTY);

		for(int i = 0; i < inv.size(); i++) {
			ItemStack stack = inv.getItem(i);
			if(stack.getItem() == Items.ELYTRA)
				ret.set(i, stack.copy());
		}
		return ret;
	}

	@Override
	public boolean isSpecial() {
		return true;
	}

	@Override
	public boolean canCraftInDimensions(int width, int height) {
		return (width * height) >= 2;
	}

	@Override
	@NotNull
	public NonNullList<Ingredient> getIngredients() {
		NonNullList<Ingredient> list = NonNullList.withSize(2, Ingredient.EMPTY);
		list.set(0, Ingredient.of(new ItemStack(Items.ELYTRA)));
		list.set(1, Ingredient.of(new ItemStack(DragonScalesModule.dragon_scale)));
		return list;
	}

	@NotNull
	@Override
	public RecipeSerializer<?> getSerializer() {
		return SERIALIZER;
	}

}
