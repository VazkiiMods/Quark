package org.violetmoon.quark.content.client.module;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CraftingScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.gui.screens.recipebook.GhostRecipe;
import net.minecraft.client.gui.screens.recipebook.GhostRecipe.GhostIngredient;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeBookPage;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import org.apache.commons.lang3.tuple.Pair;
import org.joml.Vector2i;
import org.joml.Vector2ic;
import org.violetmoon.quark.base.client.handler.ClientUtil;
import org.violetmoon.zeta.client.event.play.ZClientTick;
import org.violetmoon.zeta.client.event.play.ZRenderContainerScreen;
import org.violetmoon.zeta.client.event.play.ZScreen;
import org.violetmoon.zeta.event.bus.PlayEvent;
import org.violetmoon.zeta.module.ZetaLoadModule;
import org.violetmoon.zeta.module.ZetaModule;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Stack;
import java.util.function.BooleanSupplier;

@ZetaLoadModule(category = "client")
public class MicrocraftingHelperModule extends ZetaModule {

	@ZetaLoadModule(clientReplacement = true)
	public static class Client extends MicrocraftingHelperModule {

		private static Screen currentScreen;
		private static Recipe<?> currentRecipe;

		private static final Stack<StackedRecipe> recipes = new Stack<>();
		private static int compoundCount = 1;

		@PlayEvent
		public void onClick(ZScreen.MouseButtonPressed.Pre event) {
			Screen screen = event.getScreen();
			if(screen instanceof CraftingScreen cscreen && event.getButton() == 1) {

				RecipeBookComponent recipeBook = cscreen.getRecipeBookComponent();

				Pair<GhostRecipe, GhostIngredient> pair = getHoveredGhost(cscreen, recipeBook);
				if(pair != null) {
					GhostRecipe ghost = pair.getLeft();
					GhostIngredient ghostIngr = pair.getRight();
					Ingredient ingr = ghostIngr.ingredient;

					Minecraft mc = screen.getMinecraft();
					RegistryAccess registryAccess = mc.level.registryAccess();
					Recipe<?> recipeToSet = getRecipeToSet(recipeBook, ingr, true, registryAccess);
					if(recipeToSet == null)
						recipeToSet = getRecipeToSet(recipeBook, ingr, false, registryAccess);

					if(recipeToSet != null) {
						int ourCount = 0;


						ItemStack testStack = recipeToSet.getResultItem(registryAccess);
						for(int j = 1; j < ghost.size(); j++) { // start at 1 to skip output
							GhostIngredient testGhostIngr = ghost.get(j);
							Ingredient testIngr = testGhostIngr.ingredient;

							if(testIngr.test(testStack))
								ourCount++;
						}

						if (ourCount > 0) {
							int prevCount = compoundCount;
							int reqCount = ourCount * prevCount;

							int mult = (int) (Math.ceil((double) ourCount / (double) testStack.getCount()));
							compoundCount *= mult;

							Recipe<?> ghostRecipe = ghost.getRecipe();
							StackedRecipe stackedRecipe = new StackedRecipe(ghostRecipe, testStack, compoundCount, getClearCondition(ingr, reqCount));
							boolean stackIt = true;

							if(recipes.isEmpty()) {
								ItemStack rootDisplayStack = ghostRecipe.getResultItem(registryAccess);
								StackedRecipe rootRecipe = new StackedRecipe(null, rootDisplayStack, rootDisplayStack.getCount(), () -> recipes.size() == 1);
								recipes.add(rootRecipe);
							} else
								for(int i = 0; i < recipes.size(); i++) { // check dupes
									StackedRecipe currRecipe = recipes.get(recipes.size() - i - 1);
									if(currRecipe.recipe == recipeToSet) {
										for(int j = 0; j <= i; j++)
											recipes.pop();

										stackIt = false;
										compoundCount = currRecipe.count;
										break;
									}
								}

							if(stackIt)
								recipes.add(stackedRecipe);
						}

						ghost.clear();
						mc.gameMode.handlePlaceRecipe(mc.player.containerMenu.containerId, recipeToSet, true);
						currentRecipe = recipeToSet;
					}

					event.setCanceled(true);
				}
			}
		}

		@PlayEvent
		public void onDrawGui(ZRenderContainerScreen.Background event) {
			Minecraft mc = Minecraft.getInstance();
			if(mc.screen instanceof CraftingScreen cscreen) {
				GuiGraphics guiGraphics = event.getGuiGraphics();
				PoseStack mstack = guiGraphics.pose();
				int left = cscreen.getGuiLeft() + 95;
				int top = cscreen.getGuiTop() + 6;

				if(!recipes.isEmpty()) {
					RenderSystem.setShader(GameRenderer::getPositionTexShader);
					RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

					mstack.pushPose();
					guiGraphics.blit(ClientUtil.GENERAL_ICONS, left, top, 0, 0, 108, 80, 20, 256, 256);
					mstack.popPose();

					int start = Math.max(0, recipes.size() - 3);
					for(int i = start; i < recipes.size(); i++) {
						int index = i - start;
						StackedRecipe recipe = recipes.get(i);
						int x = left + index * 24 + 2;
						int y = top + 2;

						ItemStack drawStack = recipe.displayItem;
						guiGraphics.renderItem(drawStack, x, y);
						guiGraphics.renderItemDecorations(mc.font, drawStack, x, y);

						if(index > 0)
							guiGraphics.drawString(mc.font, "<", x - 6, y + 4, 0x3f3f3f, false);
					}
				}

				Pair<GhostRecipe, GhostIngredient> pair = getHoveredGhost(cscreen, cscreen.getRecipeBookComponent());
				if(pair != null) {
					GhostIngredient ingr = pair.getRight();
					if(ingr != null)
						currentScreen.setTooltipForNextRenderPass(
								Tooltip.create(Component.translatable("quark.misc.rightclick_to_craft")),
										AboveCursorPositioner.INSTANCE , false);
				}
			}
		}

		private static class AboveCursorPositioner implements ClientTooltipPositioner{

			private static final AboveCursorPositioner INSTANCE = new AboveCursorPositioner();

			@Override
			public Vector2ic positionTooltip(int pScreenWidth, int pScreenHeight, int pMouseX, int pMouseY, int pTooltipWidth, int pTooltipHeight) {
				Vector2i vector2i = (new Vector2i(pMouseX, pMouseY)).add(12, -12 - 17);
				this.positionTooltip(pScreenWidth, pScreenHeight, vector2i, pTooltipWidth, pTooltipHeight);
				return vector2i;
			}

			private void positionTooltip(int pScreenWidth, int pScreenHeight, Vector2i pTooltipPos, int pTooltipWidth, int pTooltipHeight) {
				if (pTooltipPos.x + pTooltipWidth > pScreenWidth) {
					pTooltipPos.x = Math.max(pTooltipPos.x - 24 - pTooltipWidth, 4);
				}

				int i = pTooltipHeight + 3;
				if (pTooltipPos.y + i > pScreenHeight) {
					pTooltipPos.y = pScreenHeight - i;
				}

			}
		}


		@PlayEvent
		public void onTick(ZClientTick.Start event) {

			Minecraft mc = Minecraft.getInstance();
			Screen prevScreen = currentScreen;
			currentScreen = mc.screen;

			boolean clearCompound = true;
			if(prevScreen != currentScreen) {
				recipes.clear();
				currentRecipe = null;
			}

			if(!recipes.isEmpty()) {
				if(currentScreen instanceof CraftingScreen crafting) {
					RecipeBookComponent book = crafting.getRecipeBookComponent();
                    GhostRecipe ghost = book.ghostRecipe;
                    if(currentRecipe != null && ghost.getRecipe() != null && ghost.getRecipe() != currentRecipe) {
                        recipes.clear();
                        currentRecipe = null;
                    }
                }

				if(!recipes.isEmpty()) {
					StackedRecipe top = recipes.peek();

					if(top.clearCondition.getAsBoolean()) {
						if(top.recipe != null) {
							mc.gameMode.handlePlaceRecipe(mc.player.containerMenu.containerId, top.recipe, true);
							currentRecipe = top.recipe;
							compoundCount = top.count;
						}

						recipes.pop();
					}

					clearCompound = false;
				}
			}

			if(clearCompound)
				compoundCount = 1;
		}

		private Recipe<?> getRecipeToSet(RecipeBookComponent recipeBook, Ingredient ingr, boolean craftableOnly, RegistryAccess registryAccess) {
			EditBox text = recipeBook.searchBox;

			for(ItemStack stack : ingr.getItems()) {
				String itemName = stack.getHoverName().copy().getString().toLowerCase(Locale.ROOT).trim();
				text.setValue(itemName);

				recipeBook.checkSearchStringUpdate();

				RecipeBookPage page = recipeBook.recipeBookPage;
                List<RecipeCollection> recipeLists = page.recipeCollections;
                recipeLists = new ArrayList<>(recipeLists); // ensure we're not messing with the original

                if(!recipeLists.isEmpty()) {
                    recipeLists.removeIf(rl -> {
                        List<Recipe<?>> list = rl.getDisplayRecipes(craftableOnly);
                        return list.isEmpty();
                    });

                    if(recipeLists.isEmpty())
                        return null;

                    recipeLists.sort((rl1, rl2) -> {
                        if (rl1 == rl2)
                            return 0;

                        Recipe<?> r1 = rl1.getDisplayRecipes(craftableOnly).get(0);
                        Recipe<?> r2 = rl2.getDisplayRecipes(craftableOnly).get(0);
                        return compareRecipes(r1, r2);
                    });

                    for(RecipeCollection list : recipeLists) {
                        List<Recipe<?>> recipeList = list.getDisplayRecipes(craftableOnly);
                        recipeList.sort(this::compareRecipes);

                        for(Recipe<?> recipe : recipeList)
                            if(ingr.test(recipe.getResultItem(registryAccess)))
                                return recipe;
                    }
                }
            }

			return null;
		}

		private int compareRecipes(Recipe<?> r1, Recipe<?> r2) {
			if(r1 == r2)
				return 0;

			String id1 = r1.getId().toString();
			String id2 = r2.getId().toString();

			boolean id1Mc = id1.startsWith("minecraft");
			boolean id2Mc = id2.startsWith("minecraft");

			if(id1Mc != id2Mc)
				return id1Mc ? -1 : 1;

			return id1.compareTo(id2);
		}

		private BooleanSupplier getClearCondition(final Ingredient ingr, final int req) {
			Minecraft mc = Minecraft.getInstance();
			return () -> {
				int missing = req;
				for(ItemStack invStack : mc.player.getInventory().items) {
					if(ingr.test(invStack)) {
						missing -= invStack.getCount();

						if(missing <= 0)
							return true;
					}
				}

				return false;
			};
		}

		private Pair<GhostRecipe, GhostIngredient> getHoveredGhost(AbstractContainerScreen<?> cscreen, RecipeBookComponent recipeBook) {
			Slot slot = cscreen.getSlotUnderMouse();

			if(recipeBook != null && slot != null) {
				GhostRecipe ghost = recipeBook.ghostRecipe;
				if(ghost.getRecipe() != null) {
					for(int i = 1; i < ghost.size(); i++) { // start at 1 to skip output
						GhostIngredient ghostIngr = ghost.get(i);

						if(ghostIngr.getX() == slot.x && ghostIngr.getY() == slot.y)
							return Pair.of(ghost, ghostIngr);
					}
				}
			}

			return null;
		}

		private record StackedRecipe(Recipe<?> recipe,
				ItemStack displayItem, int count,
				BooleanSupplier clearCondition) {

			private StackedRecipe(Recipe<?> recipe, ItemStack displayItem, int count, BooleanSupplier clearCondition) {
				this.recipe = recipe;
				this.count = count;
				this.clearCondition = clearCondition;

				this.displayItem = displayItem.copy();
				this.displayItem.setCount(count);
			}

		}

	}
}
