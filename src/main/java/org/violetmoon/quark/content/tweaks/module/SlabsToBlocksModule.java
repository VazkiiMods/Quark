package org.violetmoon.quark.content.tweaks.module;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.SlabBlock;

import org.violetmoon.quark.content.tweaks.recipe.SlabToBlockRecipe;
import org.violetmoon.zeta.event.bus.LoadEvent;
import org.violetmoon.zeta.event.bus.PlayEvent;
import org.violetmoon.zeta.event.load.ZRegister;
import org.violetmoon.zeta.event.play.ZRecipeCrawl;
import org.violetmoon.zeta.module.ZetaLoadModule;
import org.violetmoon.zeta.module.ZetaModule;

import java.util.HashMap;
import java.util.Map;

@ZetaLoadModule(category = "tweaks")
public class SlabsToBlocksModule extends ZetaModule {

	public static Map<Item, Item> recipes = new HashMap<>();

	@LoadEvent
	public final void register(ZRegister event) {
		event.getRegistry().register(SlabToBlockRecipe.SERIALIZER, "slab_to_block", Registries.RECIPE_SERIALIZER);
	}

	@PlayEvent
	public void onReset(ZRecipeCrawl.Reset event) {
		recipes.clear();
	}

	private ItemStack extract(ItemStack[] array) {
		if(array.length == 0)
			return ItemStack.EMPTY;
		return array[0];
	}

	@PlayEvent
	public void onVisitShaped(ZRecipeCrawl.Visit.Shaped visit) {
		if(visit.ingredients.size() == 3
				&& visit.recipe.canCraftInDimensions(3, 1)
                /* Merging from main, is this the better one?
                && visit.recipe.getHeight() == 1
				&& visit.recipe.getWidth() == 3
				&& visit.output.getCount() == 6
                 */
				&& visit.output.getItem() instanceof BlockItem bi
				&& bi.getBlock() instanceof SlabBlock) {

			Item a = extract(visit.ingredients.get(0).getItems()).getItem();
			Item b = extract(visit.ingredients.get(1).getItems()).getItem();
			Item c = extract(visit.ingredients.get(2).getItems()).getItem();

			if(a == b && b == c && a != Items.AIR)
				recipes.put(bi, a);
		}
	}

}
