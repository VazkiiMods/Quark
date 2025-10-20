package org.violetmoon.quark.content.tweaks.recipe;

import net.minecraft.core.HolderLookup;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.violetmoon.quark.content.tweaks.module.SlabsToBlocksModule;

import java.util.Optional;

public class SlabToBlockRecipe extends CustomRecipe {

	public static final SimpleCraftingRecipeSerializer<?> SERIALIZER = new SimpleCraftingRecipeSerializer<>(SlabToBlockRecipe::new);
	private boolean locked = false;

	public SlabToBlockRecipe(CraftingBookCategory category) {
		super(category);
	}

	@Override
	public boolean matches(CraftingInput input, Level level) {
		if(locked) return false;

		Item target = null;

		boolean checked = false;
		boolean result = false;

		for(int i = 0; i < input.size(); i++) {
			ItemStack stack = input.getItem(i);
			if(!stack.isEmpty()) {
				Item item = stack.getItem();

				if(target != null) {
					if(checked)
						return false;

					result = item == target && checkForOtherRecipes(input, level);
					checked = true;
				} else {
					if(SlabsToBlocksModule.recipes.containsKey(item)) {
						target = item;
					} else
						return false;
				}
			}
		}

		return result;
	}

	// very much doubt multiple threads would ever touch this but JUST IN CASE
	private synchronized boolean checkForOtherRecipes(CraftingInput input, Level level) {
		locked = true;
		boolean ret = false;
		MinecraftServer server = level.getServer();
		if(server != null) {
			Optional<RecipeHolder<CraftingRecipe>> optional = server.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, level);
			ret = optional.isEmpty();
		}
		locked = false;
		return ret;
	}

	@Override
	public ItemStack assemble(CraftingInput inv, HolderLookup.Provider provider) {
		for(int i = 0; i < inv.size(); i++) {
			ItemStack stack = inv.getItem(i);
			if(!stack.isEmpty()) {
				Item item = stack.getItem();

				if(SlabsToBlocksModule.recipes.containsKey(item))
					return new ItemStack(SlabsToBlocksModule.recipes.get(item));
			}
		}
		return ItemStack.EMPTY;
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
	public RecipeSerializer<?> getSerializer() {
		return SERIALIZER;
	}

}
