package org.violetmoon.quark.integration.jei;

import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;

import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.crafting.CraftingRecipe;

import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.Nullable;

import org.violetmoon.quark.addons.oddities.inventory.BackpackMenu;

import java.util.List;
import java.util.Optional;

/**
 * Essentially just a copy of JEI's PlayerRecipeTransferHandler, but for BackpackMenu.
 * For all intents and purposes I could've just used the JEI one, but I'd rather not touch internals.
 * 
 * @author mezz, tbh
 *
 */
public class BackpackRecipeTransferHandler implements IRecipeTransferHandler<BackpackMenu, RecipeHolder<CraftingRecipe>> {

	private static final IntSet PLAYER_INV_INDEXES = IntArraySet.of(0, 1, 3, 4);

	private final IRecipeTransferHandlerHelper handlerHelper;
	private final IRecipeTransferHandler<BackpackMenu, RecipeHolder<CraftingRecipe>> handler;

	public BackpackRecipeTransferHandler(IRecipeTransferHandlerHelper handlerHelper) {
		this.handlerHelper = handlerHelper;
		var basicRecipeTransferInfo = handlerHelper.createBasicRecipeTransferInfo(BackpackMenu.class, null, RecipeTypes.CRAFTING, 1, 4, 9, 36);
		this.handler = handlerHelper.createUnregisteredRecipeTransferHandler(basicRecipeTransferInfo);
	}

	@Override
	public Class<? extends BackpackMenu> getContainerClass() {
		return handler.getContainerClass();
	}

	@Override
	public Optional<MenuType<BackpackMenu>> getMenuType() {
		return handler.getMenuType();
	}

	@Override
	public RecipeType<RecipeHolder<CraftingRecipe>> getRecipeType() {
		return RecipeTypes.CRAFTING;
	}

	@Nullable
	@Override
	public IRecipeTransferError transferRecipe(BackpackMenu container, RecipeHolder<CraftingRecipe> recipe, IRecipeSlotsView recipeSlotsView, Player player, boolean maxTransfer, boolean doTransfer) {
		if(!handlerHelper.recipeTransferHasServerSupport()) {
			Component tooltipMessage = Component.translatable("jei.tooltip.error.recipe.transfer.no.server");
			return this.handlerHelper.createUserErrorWithTooltip(tooltipMessage);
		}

		List<IRecipeSlotView> slotViews = recipeSlotsView.getSlotViews(RecipeIngredientRole.INPUT);
		if(!validateIngredientsOutsidePlayerGridAreEmpty(slotViews)) {
			Component tooltipMessage = Component.translatable(
					"jei.tooltip.error.recipe.transfer.too.large.player.inventory"
			);
			return this.handlerHelper.createUserErrorWithTooltip(tooltipMessage);
		}

		// filter the crafting table input slots to player inventory input slots
		List<IRecipeSlotView> filteredSlotViews = filterSlots(slotViews);
		IRecipeSlotsView filteredRecipeSlots = this.handlerHelper.createRecipeSlotsView(filteredSlotViews);
		return this.handler.transferRecipe(container, recipe, filteredRecipeSlots, player, maxTransfer, doTransfer);
	}

	private static boolean validateIngredientsOutsidePlayerGridAreEmpty(List<IRecipeSlotView> slotViews) {
		int bound = slotViews.size();
		for(int i = 0; i < bound; i++) {
			if(!PLAYER_INV_INDEXES.contains(i)) {
				IRecipeSlotView slotView = slotViews.get(i);
				if(!slotView.isEmpty()) {
					return false;
				}
			}
		}
		return true;
	}

	private static List<IRecipeSlotView> filterSlots(List<IRecipeSlotView> slotViews) {
		return PLAYER_INV_INDEXES.intStream()
				.mapToObj(slotViews::get)
				.toList();
	}
}
