package vazkii.quark.content.tweaks.module;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import vazkii.quark.base.handler.MiscUtil;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.base.module.config.Config;
import vazkii.zeta.event.ZConfigChanged;
import vazkii.zeta.event.ZGatherHints;
import vazkii.zeta.event.bus.LoadEvent;
import vazkii.zeta.event.bus.PlayEvent;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@LoadModule(category = "tweaks")
public class DiamondRepairModule extends QuarkModule {

	@Config(name = "Repair Item Changes",
			description = "List of changes to apply to repair items, format is \"<item>=<repair_item>\" as seen in the defualt.\n"
					+ "Multiple repair items can be applied for the same base item, and as long as at least one is provided, any vanilla option will be removed.\n"
					+ "To use multiple items, comma separate them (e.g. \"minecraft:diamond_sword=minecraft:diamond,minecraft:emerald\")"
					+ "If you want the vanilla option back, you must add it again manually.")
	public static List<String> repairChangesList = Arrays.asList(
			"minecraft:netherite_sword=minecraft:diamond",
			"minecraft:netherite_pickaxe=minecraft:diamond",
			"minecraft:netherite_axe=minecraft:diamond",
			"minecraft:netherite_shovel=minecraft:diamond",
			"minecraft:netherite_hoe=minecraft:diamond",
			"minecraft:netherite_helmet=minecraft:diamond",
			"minecraft:netherite_chestplate=minecraft:diamond",
			"minecraft:netherite_leggings=minecraft:diamond",
			"minecraft:netherite_boots=minecraft:diamond"
			);

	@Config(name = "Unrepairable Items")
	public static List<String> unrepairableItemsList = Arrays.asList();

	@Config private static boolean enableJeiHints = true;

	private static boolean staticEnabled;
	public static Multimap<Item, Item> repairChanges = HashMultimap.create();
	public static List<Item> unrepairableItems;

	@LoadEvent
	public final void configChanged(ZConfigChanged event) {
		staticEnabled = enabled;

		repairChanges.clear();
		for(String s : repairChangesList) {
			String[] toks = s.split("=");
			if(toks.length == 2) {
				ResourceLocation itemRes = new ResourceLocation(toks[0]);

				if(ForgeRegistries.ITEMS.containsKey(itemRes)) {
					String repairItems = toks[1];
					String[] repairToks = repairItems.split(",");
					for(String repairTok : repairToks) {
						ResourceLocation repairItemRes = new ResourceLocation(repairTok);

						if(ForgeRegistries.ITEMS.containsKey(repairItemRes)) {
							Item item = ForgeRegistries.ITEMS.getValue(itemRes);
							Item repairItem = ForgeRegistries.ITEMS.getValue(repairItemRes);
							repairChanges.put(item, repairItem);
						}
					}
				}
			}
		}

		unrepairableItems = MiscUtil.massRegistryGet(unrepairableItemsList, ForgeRegistries.ITEMS);
	}

	@PlayEvent
	public void addAdditionalHints(ZGatherHints consumer) {
		if(!enableJeiHints)
			return;

		Component removed = Component.translatable("quark.jei.hint.repair_item_removed");
		for(Item item : unrepairableItems)
			consumer.accept(item, removed);

		for(Item item : repairChanges.keySet()) {
			Collection<Item> options = repairChanges.get(item);

			int len = options.size();
			String key = "quark.jei.hint.repair_item_changed" + (len == 1 ? "" : "_multiple");

			MutableComponent formatParams = Component.empty();
			int i = 1;
			for(Item repair : options) {
				formatParams = formatParams.append(repair.getName(new ItemStack(repair)));
				if(i < len)
					formatParams = formatParams.append(Component.literal(", "));
				i++;
			}

			consumer.accept(item, Component.translatable(key, formatParams));
		}
	}

	public static boolean isValidRepairItem(boolean prev, Item item, ItemStack repairItem) {
		if(staticEnabled) {
			if(unrepairableItems.contains(item))
				return false;

			if(repairChanges.containsKey(item))
				return repairChanges.get(item).contains(repairItem.getItem());
		}

		return prev;
	}

}
