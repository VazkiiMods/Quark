package org.violetmoon.quark.content.tweaks.module;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.Criterion;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.components.toasts.RecipeToast;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.GameRules;
import org.violetmoon.quark.base.Quark;
import org.violetmoon.zeta.client.event.play.ZClientTick;
import org.violetmoon.zeta.client.event.play.ZScreen;
import org.violetmoon.zeta.config.Config;
import org.violetmoon.zeta.event.bus.LoadEvent;
import org.violetmoon.zeta.event.bus.PlayEvent;
import org.violetmoon.zeta.event.load.ZConfigChanged;
import org.violetmoon.zeta.event.play.entity.player.ZPlayer;
import org.violetmoon.zeta.module.ZetaLoadModule;
import org.violetmoon.zeta.module.ZetaModule;

import java.util.*;

@ZetaLoadModule(category = "tweaks", antiOverlap = "nerb")
public class AutomaticRecipeUnlockModule extends ZetaModule {

	@Config(description = "A list of recipe names that should NOT be added in by default")
	public static List<String> ignoredRecipes = Lists.newArrayList();

	@Config
	public static boolean forceLimitedCrafting = false;
	@Config
	public static boolean disableRecipeBook = false;
	@Config(
		description = "If enabled, advancements granting recipes will be stopped from loading, " +
				"potentially reducing the lagspike on first world join."
	)
	public static boolean filterRecipeAdvancements = true;

	private static boolean staticEnabled;

	@LoadEvent
	public final void configChanged(ZConfigChanged event) {
		staticEnabled = isEnabled();
	}

	@PlayEvent
	public void onPlayerLoggedIn(ZPlayer.LoggedIn event) {
		Player player = event.getPlayer();

		if(player instanceof ServerPlayer spe) {
			MinecraftServer server = spe.getServer();
			if(server != null) {
				List<RecipeHolder<?>> recipes = new ArrayList<>(server.getRecipeManager().getRecipes());

				recipes.removeIf((recipe) -> {
                    if (recipe == null) return true;
                    recipe.value().getResultItem(event.getPlayer().level().registryAccess());
                    return ignoredRecipes.contains(Objects.toString(recipe.id())) || recipe.value().getResultItem(event.getPlayer().level().registryAccess()).isEmpty();
                });

				int idx = 0;
				int maxShift = 1000;
				int shift;
				int size = recipes.size();

				do {
					shift = size - idx;
					int effShift = Math.min(maxShift, shift);
					List<RecipeHolder<?>> sectionedRecipes = recipes.subList(idx, idx + effShift);

					player.awardRecipes(sectionedRecipes);
					idx += effShift;
				} while(shift > maxShift);

				if(forceLimitedCrafting)
					player.level().getGameRules().getRule(GameRules.RULE_LIMITED_CRAFTING).set(true, server);
			}
		}
	}

	//todo: Stupid idiot Mojang made stupid idiot Siuol uglyify this please make it better
	public static Map<ResourceLocation, AdvancementHolder> removeRecipeAdvancements(Map<ResourceLocation, AdvancementHolder> advancements) {
		if(!staticEnabled || !filterRecipeAdvancements) return advancements;

		Map<ResourceLocation, AdvancementHolder> advancementReal = new HashMap<>();
		advancementReal.putAll(advancements);
		int removeCount = 0;

		for (Map.Entry<ResourceLocation, AdvancementHolder> advancement : advancementReal.entrySet()) {
			if (advancement.getKey().getPath().startsWith("recipes/") && advancement.getValue().value().criteria().containsKey("has_the_recipe")) {
				Advancement oldAdv = advancement.getValue().value();

				Map<String, Criterion<?>> criteriaReal = new HashMap<>();
				for (Map.Entry<String, Criterion<?>> oldCriteria : oldAdv.criteria().entrySet()) {
					if (!oldCriteria.getKey().equals("has_the_recipe")) {
						criteriaReal.put(oldCriteria.getKey(), oldCriteria.getValue());
					}
				}


				Advancement replacementAdv  = new Advancement(oldAdv.parent(), oldAdv.display(), oldAdv.rewards(), criteriaReal, oldAdv.requirements(), oldAdv.sendsTelemetryEvent(), oldAdv.name());
				AdvancementHolder realAdvancementHolder = new AdvancementHolder(advancement.getValue().id(), replacementAdv);
				advancementReal.replace(advancement.getKey(), realAdvancementHolder);
				removeCount++;
			}
		}
		Quark.LOG.info("[Automatic Recipe Unlock] Removed {} recipe advancements", removeCount);
		return advancementReal;
	}

	@ZetaLoadModule(clientReplacement = true)
	public static class Client extends AutomaticRecipeUnlockModule {

		@PlayEvent
		public void onInitGui(ZScreen.Init.Post event) {
			LocalPlayer player = Minecraft.getInstance().player;
			Screen gui = event.getScreen();
			if (disableRecipeBook && player != null && gui instanceof RecipeUpdateListener) {
				player.getRecipeBook().getBookSettings().setOpen(RecipeBookType.CRAFTING, false);

				List<GuiEventListener> widgets = event.getListenersList();
				for(GuiEventListener w : widgets)
					if(w instanceof ImageButton) {
						event.removeListener(w);
						return;
					}
			}
		}

		@PlayEvent
		public void clientTick(ZClientTick.End event) {
			Minecraft mc = Minecraft.getInstance();
			if(mc.player != null && mc.player.tickCount < 20) {
				ToastComponent toasts = mc.getToasts();
				Queue<Toast> toastQueue = toasts.queued;
				for (Toast toast : toastQueue) {
					if (toast instanceof RecipeToast recipeToast) {
						List<RecipeHolder<?>> stacks = recipeToast.recipes;
						if (stacks.size() > 100) {
							toastQueue.remove(toast);
							return;
						}
					}
				}
			}
		}
	}
}
