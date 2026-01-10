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
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
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

	@Config(description = "If enabled, advancements granting recipes will be stopped from loading, potentially reducing the lagspike on first world join.")
	public static boolean filterRecipeAdvancements = true;

	private static boolean staticEnabled;

	@LoadEvent
	public final void configChanged(ZConfigChanged event) {
		staticEnabled = isEnabled();
	}

	@PlayEvent
	public void onPlayerLoggedIn(ZPlayer.LoggedIn event) {
		if (!(event.getPlayer() instanceof ServerPlayer player && player.getServer() instanceof MinecraftServer server)) return;

		Level level = player.level();
		List<RecipeHolder<?>> recipes = new ArrayList<>(server.getRecipeManager().getRecipes());

		recipes.removeIf((recipe) -> recipe == null || ignoredRecipes.contains(Objects.toString(recipe.id())) || recipe.value().getResultItem(level.registryAccess()).isEmpty());

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
		} while (shift > maxShift);

		if (forceLimitedCrafting) {
			level.getGameRules().getRule(GameRules.RULE_LIMITED_CRAFTING).set(true, server);
		}
	}

	public static ImmutableMap.Builder<ResourceLocation, AdvancementHolder> removeRecipeAdvancements(ImmutableMap.Builder<ResourceLocation, AdvancementHolder> advancements) {
		if (!staticEnabled || !filterRecipeAdvancements) return advancements;

		Map<ResourceLocation, AdvancementHolder> copy = new HashMap<>(Map.copyOf(advancements.build()));
		ImmutableMap.Builder<ResourceLocation, AdvancementHolder> replacements = ImmutableMap.builder();
		int removeCount = 0;

		for (Map.Entry<ResourceLocation, AdvancementHolder> entry : copy.entrySet()) {
			Advancement advancement = entry.getValue().value();
			if (entry.getKey().getPath().startsWith("recipes/") && advancement.criteria().containsKey("has_the_recipe")) {
				Map<String, Criterion<?>> replacementCriteria = new HashMap<>(advancement.criteria());
				replacementCriteria.remove("has_the_recipe");

				Advancement replacement = new Advancement(advancement.parent(), advancement.display(), advancement.rewards(), replacementCriteria, advancement.requirements(), advancement.sendsTelemetryEvent(), advancement.name());
				AdvancementHolder replacementHolder = new AdvancementHolder(entry.getValue().id(), replacement);
				replacements.put(entry.getKey(), replacementHolder);
				removeCount++;
			}
		}

		Quark.LOG.info("[Automatic Recipe Unlock] Removed {} recipe advancements", removeCount);
		return replacements;
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
				for (GuiEventListener eventListener : widgets) {
					if (eventListener instanceof ImageButton) {
						event.removeListener(eventListener);
						return;
					}
				}
			}
		}

		@PlayEvent
		public void clientTick(ZClientTick.End event) {
			Minecraft client = Minecraft.getInstance();

			if (client.player != null && client.player.tickCount < 20) {
				Queue<Toast> toastQueue = client.getToasts().queued;
				for (Toast toast : toastQueue) {
					if (toast instanceof RecipeToast recipeToast && recipeToast.recipes.size() > 100) {
                        toastQueue.remove(toast);
                        return;
                    }
				}
			}
		}
	}
}
