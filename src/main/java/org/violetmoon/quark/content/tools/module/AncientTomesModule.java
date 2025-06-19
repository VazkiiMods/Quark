package org.violetmoon.quark.content.tools.module;

import com.google.common.collect.Lists;
import com.mojang.serialization.*;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentPredicate;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades.ItemListing;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MerchantContainer;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.*;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.violetmoon.quark.api.QuarkCapabilities;
import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.base.util.ItemEnchantsUtil;
import org.violetmoon.quark.content.experimental.module.EnchantmentsBegoneModule;
import org.violetmoon.quark.content.tools.base.RuneColor;
import org.violetmoon.quark.content.tools.item.AncientTomeItem;
import org.violetmoon.quark.content.tools.loot.EnchantTome;
import org.violetmoon.quark.content.world.module.MonsterBoxModule;
import org.violetmoon.quark.mixin.mixins.accessor.AccessorMerchantMenu;
import org.violetmoon.zeta.advancement.ManualTrigger;
import org.violetmoon.zeta.config.Config;
import org.violetmoon.zeta.event.bus.LoadEvent;
import org.violetmoon.zeta.event.bus.PlayEvent;
import org.violetmoon.zeta.event.load.ZCommonSetup;
import org.violetmoon.zeta.event.load.ZConfigChanged;
import org.violetmoon.zeta.event.load.ZRegister;
import org.violetmoon.zeta.event.play.ZAnvilRepair;
import org.violetmoon.zeta.event.play.ZAnvilUpdate;
import org.violetmoon.zeta.event.play.entity.player.ZPlayer;
import org.violetmoon.zeta.event.play.loading.ZLootTableLoad;
import org.violetmoon.zeta.event.play.loading.ZVillagerTrades;
import org.violetmoon.zeta.module.ZetaLoadModule;
import org.violetmoon.zeta.module.ZetaModule;
import org.violetmoon.zeta.util.Hint;

import java.util.*;
import java.util.stream.Stream;

@ZetaLoadModule(category = "tools")
public class AncientTomesModule extends ZetaModule {

	private static final Object mutex = new Object();

	private static String loot(ResourceLocation lootLoc, int defaultWeight) {
		return lootLoc.toString() + "," + defaultWeight;
	}

	@Config(description = "Format is lootTable,weight. i.e. \"minecraft:chests/stronghold_library,30\"")
	public static List<String> lootTables = Lists.newArrayList(
			loot(BuiltInLootTables.STRONGHOLD_LIBRARY.location(), 20),
			loot(BuiltInLootTables.SIMPLE_DUNGEON.location(), 20),
			loot(BuiltInLootTables.BASTION_TREASURE.location(), 25),
			loot(BuiltInLootTables.WOODLAND_MANSION.location(), 15),
			loot(BuiltInLootTables.NETHER_BRIDGE.location(), 0),
			loot(BuiltInLootTables.UNDERWATER_RUIN_BIG.location(), 0),
			loot(BuiltInLootTables.UNDERWATER_RUIN_SMALL.location(), 0),
			loot(BuiltInLootTables.ANCIENT_CITY.location(), 4),
			loot(MonsterBoxModule.MONSTER_BOX_LOOT_TABLE, 5)
	);

	private static final Object2IntMap<ResourceLocation> lootTableWeights = new Object2IntArrayMap<>();

	@Config
	public static int itemQuality = 2;

	@Config
	public static int normalUpgradeCost = 10;
	@Config
	public static int limitBreakUpgradeCost = 30;

	public static LootItemFunctionType<EnchantTome> tomeEnchantType;

	@Config(name = "Valid Enchantments")
	public static List<String> enchantNames = generateDefaultEnchantmentList();

	@Config
	public static boolean overleveledBooksGlowRainbow = true;

	@Config(description = "When enabled, Efficiency VI Diamond and Netherite pickaxes can instamine Deepslate when under Haste 2", flag = "deepslate_tweak")
	public static boolean deepslateTweak = true;

	@Config
	public static boolean deepslateTweakNeedsHaste2 = true;

	@Config(description = "Master Librarians will offer to exchange Ancient Tomes, provided you give them a max-level Enchanted Book of the Tome's enchantment too.")
	public static boolean librariansExchangeAncientTomes = true;

	@Config(description = "Applying a tome will also randomly curse your item")
	public static boolean curseGear = false;

	@Config(description = "Allows combining tomes with normal books")
	public static boolean combineWithBooks = true;

	@Config(description = "Whether a sanity check is performed on the valid enchantments. If this is turned off, enchantments such as Silk Touch will be allowed to generate Ancient Tomes, if explicitly added to the Valid Enchantments.")
	public static boolean sanityCheck = true;

	@Hint
	public static Item ancient_tome;
	public static final List<Holder<Enchantment>> validEnchants = new ArrayList<>();
	private static boolean initialized = false;

	public static ManualTrigger overlevelTrigger;
	public static ManualTrigger instamineDeepslateTrigger;

	@LoadEvent
	public void register(ZRegister event) {
		ancient_tome = new AncientTomeItem(this);

		tomeEnchantType = new LootItemFunctionType<>(EnchantTome.CODEC);
		Registry.register(BuiltInRegistries.LOOT_FUNCTION_TYPE, Quark.asResource("tome_enchant"), tomeEnchantType);

		overlevelTrigger = event.getAdvancementModifierRegistry().registerManualTrigger("overlevel");
		instamineDeepslateTrigger = event.getAdvancementModifierRegistry().registerManualTrigger("instamine_deepslate");
	}

	@PlayEvent
	public void onTradesLoaded(ZVillagerTrades event) {
		if(event.getType() == VillagerProfession.LIBRARIAN && librariansExchangeAncientTomes) {
			synchronized (mutex) {
				Int2ObjectMap<List<ItemListing>> trades = event.getTrades();
				trades.get(5).add(new ExchangeAncientTomesTrade());
			}
		}
	}

	@LoadEvent
	public void configChanged(ZConfigChanged event) {
		lootTableWeights.clear();
		for(String table : lootTables) {
			String[] split = table.split(",");
			if(split.length == 2) {
				int weight;
				ResourceLocation loc = ResourceLocation.parse(split[0]);
				try {
					weight = Integer.parseInt(split[1]);
				} catch (NumberFormatException e) {
					continue;
				}
				if(weight > 0)
					lootTableWeights.put(loc, weight);
			}
		}

		if(initialized)
			setupEnchantList();
	}

	@LoadEvent
	public void setup(ZCommonSetup event) {
		setupEnchantList();
		setupCursesList();
		initialized = true;
	}

	@PlayEvent
	public void onLootTableLoad(ZLootTableLoad event) {
		ResourceLocation res = event.getName();
		int weight = lootTableWeights.getOrDefault(res, 0);

		if(weight > 0) {
			LootPoolEntryContainer entry = LootItem.lootTableItem(ancient_tome)
					.setWeight(weight)
					.setQuality(itemQuality)
					.apply(() -> new EnchantTome(List.of(new LootItemCondition[0])))
					.build();

			event.add(entry);
		}
	}

	public static boolean isInitialized() {
		return initialized;
	}

	@PlayEvent
	public void onAnvilUpdate(ZAnvilUpdate.Highest event) {
		ItemStack left = event.getLeft();
		ItemStack right = event.getRight();
		String name = event.getName();

		if(!left.isEmpty() && !right.isEmpty() && left.getCount() == 1 && right.getCount() == 1) {

			// Apply tome to book or item
			if(right.is(ancient_tome)) {
				if(!combineWithBooks && left.is(Items.ENCHANTED_BOOK))
					return;

				Holder<Enchantment> ench = getTomeEnchantment(right);
				ItemEnchantments enchants = left.get(DataComponents.ENCHANTMENTS);

				if(ench != null && enchants.keySet().contains(ench) && enchants.getLevel(ench) <= ench.value().getMaxLevel()) {
					int lvl = enchants.getLevel(ench) + 1;
					enchants = ItemEnchantsUtil.addEnchantmentToList(enchants, ench, lvl);

					ItemStack out = left.copy();
					ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
					for (Map.Entry<Holder<Enchantment>, Integer> enchEntry : enchants.entrySet()) {
						mutable.set(enchEntry.getKey(), enchEntry.getValue());
					}
					EnchantmentHelper.setEnchantments(out, mutable.toImmutable());
					int cost = lvl > ench.value().getMaxLevel() ? limitBreakUpgradeCost : normalUpgradeCost;

					if(name != null && !name.isEmpty() && (!out.has(DataComponents.CUSTOM_NAME) || !out.getHoverName().getString().equals(name))) {
						out.set(DataComponents.CUSTOM_NAME, Component.literal(name));
						cost++;
					}

					event.setOutput(out);
					event.setCost(cost);
				}
			}

			// Apply overleveled book to item
			else if(combineWithBooks && right.is(Items.ENCHANTED_BOOK)) {
				ItemEnchantments enchants = right.get(DataComponents.ENCHANTMENTS);
				ItemEnchantments currentEnchants = left.get(DataComponents.ENCHANTMENTS);
				boolean hasOverLevel = false;
				boolean hasMatching = false;
				for(Object2IntMap.Entry<Holder<Enchantment>> entry : enchants.entrySet()) {
					Holder<Enchantment> enchantment = entry.getKey();
					if(enchantment == null)
						continue;

					int level = entry.getIntValue();
					if(level > enchantment.value().getMaxLevel()) {
						hasOverLevel = true;
						if(enchantment.value().canEnchant(left) || left.is(Items.ENCHANTED_BOOK)) {
							hasMatching = true;
							//remove incompatible enchantments
							for(Iterator<Holder<Enchantment>> iterator = currentEnchants.keySet().iterator(); iterator.hasNext();) {
								Holder<Enchantment> comparingEnchantment = iterator.next();
								if(comparingEnchantment == enchantment)
									continue;

								if(!comparingEnchantment.value().exclusiveSet().contains(enchantment)) {
									iterator.remove();
								}
							}
							currentEnchants = ItemEnchantsUtil.addEnchantmentToList(currentEnchants, enchantment, level);
						}
					} else if(enchantment.value().canEnchant(left)) {
						boolean compatible = true;
						//don't apply incompatible enchantments
						for(Holder<Enchantment> comparingEnchantment : currentEnchants.keySet()) {
							if(comparingEnchantment == enchantment)
								continue;

							if(comparingEnchantment != null && !comparingEnchantment.value().exclusiveSet().contains(enchantment)) {
								compatible = false;
								break;
							}
						}
						if(compatible) {
							currentEnchants = ItemEnchantsUtil.addEnchantmentToList(currentEnchants, enchantment, level);
						}
					}
				}

				if(hasOverLevel) {
					if(hasMatching) {
						ItemStack out = left.copy();
						out.set(DataComponents.ENCHANTMENTS, currentEnchants);
						int cost = normalUpgradeCost;

						if(name != null && !name.isEmpty() && (!out.has(DataComponents.CUSTOM_NAME) || !out.getHoverName().getString().equals(name))) {
							out.set(DataComponents.CUSTOM_NAME, Component.literal(name));
							cost++;
						}

						event.setOutput(out);
						event.setCost(cost);
					}
				}
			}
		}
	}

	@PlayEvent
	public void onAnvilUse(ZAnvilRepair event) {
		ItemStack output = event.getOutput();
		ItemStack right = event.getRight();

		if(curseGear && (right.is(ancient_tome) || event.getLeft().is(ancient_tome))) {
			event.getOutput().enchant(curses.get(event.getEntity().level().random.nextInt(curses.size())), 1);
		}

		if(isOverlevel(output) && (right.getItem() == Items.ENCHANTED_BOOK || right.getItem() == ancient_tome) && event.getEntity() instanceof ServerPlayer sp)
			overlevelTrigger.trigger(sp);
	}

	@PlayEvent
	public void onGetSpeed(ZPlayer.BreakSpeed event) {
		if(deepslateTweak) {
			Player player = event.getPlayer();
			ItemStack stack = player.getMainHandItem();
			BlockState state = event.getState();
			Holder<Enchantment> efficiency = event.getPlayer().level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.EFFICIENCY);

			if(state.is(Blocks.DEEPSLATE)
					&& EnchantmentHelper.getTagEnchantmentLevel(efficiency, stack) >= 6
					&& event.getOriginalSpeed() >= 45F
					&& (!deepslateTweakNeedsHaste2 || playerHasHaste2(player))) {

				event.setNewSpeed(100F);

				if(player instanceof ServerPlayer sp)
					instamineDeepslateTrigger.trigger(sp);
			}
		}
	}

	private boolean playerHasHaste2(Player player) {
		MobEffectInstance inst = player.getEffect(MobEffects.DIG_SPEED);
		return inst != null && inst.getAmplifier() > 0;
	}

	private static boolean isOverlevel(ItemStack stack) {
		ItemEnchantments enchantments = stack.get(DataComponents.ENCHANTMENTS);
		for(Holder<Enchantment> enchant : enchantments.keySet()) {
			if(enchant == null)
				continue;

			int level = enchantments.getLevel(enchant);
			if(level > enchant.value().getMaxLevel()) {
				return true;
			}
		}

		return false;
	}

	private static final ResourceLocation OVERLEVEL_COLOR_HANDLER = Quark.asResource("overlevel_rune");

	/*@PlayEvent
	public void attachRuneCapability(ZAttachCapabilities.ItemStackCaps event) {
		if(event.getObject().getItem() == Items.ENCHANTED_BOOK) {
			event.addCapability(OVERLEVEL_COLOR_HANDLER, QuarkCapabilities.RUNE_COLOR, stack -> {
				if(overleveledBooksGlowRainbow && isOverlevel(stack))
					return RuneColor.RAINBOW;
				else
					return null;
			});
		}
	}*/

	public static Rarity shiftRarity(ItemStack itemStack, Rarity returnValue) {
		return Quark.ZETA.modules.isEnabled(AncientTomesModule.class) && overleveledBooksGlowRainbow &&
				itemStack.getItem() == Items.ENCHANTED_BOOK && isOverlevel(itemStack) ? Rarity.EPIC : returnValue;

	}

	private static List<String> generateDefaultEnchantmentList() {
		ResourceKey<Enchantment>[] enchants = new ResourceKey[] {
				Enchantments.FEATHER_FALLING,
				Enchantments.THORNS,
				Enchantments.SHARPNESS,
				Enchantments.SMITE,
				Enchantments.BANE_OF_ARTHROPODS,
				Enchantments.KNOCKBACK,
				Enchantments.FIRE_ASPECT,
				Enchantments.LOOTING,
				Enchantments.SWEEPING_EDGE,
				Enchantments.EFFICIENCY,
				Enchantments.UNBREAKING,
				Enchantments.FORTUNE,
				Enchantments.POWER,
				Enchantments.PUNCH,
				Enchantments.LUCK_OF_THE_SEA,
				Enchantments.LURE,
				Enchantments.LOYALTY,
				Enchantments.RIPTIDE,
				Enchantments.IMPALING,
				Enchantments.PIERCING
		};

		List<String> strings = new ArrayList<>();
		for(ResourceKey<Enchantment> e : enchants) {
			ResourceLocation regname = e.location();
			if(e != null && regname != null)
				strings.add(regname.toString());
		}

		return strings;
	}

	private void setupEnchantList() {
		initializeEnchantmentList(enchantNames, validEnchants);
		if(sanityCheck)
			validEnchants.removeIf((ench) -> ench.value().getMaxLevel() == 1);
	}
	
	public static void initializeEnchantmentList(Iterable<String> enchantNames, List<Holder<Enchantment>> enchants) {
		enchants.clear();
		for(String s : enchantNames) {
			Holder<Enchantment> enchant = BuiltInRegistries.ENCHANTMENT.get(ResourceLocation.parse(s));
			if(enchant != null && !EnchantmentsBegoneModule.shouldBegone(enchant))
				enchants.add(enchant);
		}
	}

	private final List<Holder<Enchantment>> curses = new ArrayList<>();

	public void setupCursesList() {

		for(var e : BuiltInRegistries.ENCHANTMENT) {
			if(e.isCurse())
				curses.add(e);
		}
	}

	public static Holder<Enchantment> getTomeEnchantment(ItemStack stack) {
		if(stack.getItem() != ancient_tome)
			return null;

		ItemEnchantments enchantments = stack.get(DataComponents.ENCHANTMENTS);
		List<Holder<Enchantment>> enchantList = enchantments.keySet().stream().toList();

        for (Holder<Enchantment> enchantment : enchantList) {
			if (enchantment != null) {
				return enchantment;
			}
        }

		return null;
	}

	private static boolean isAncientTomeOffer(MerchantOffer offer) {
		return offer.getCostA().is(ancient_tome) && offer.getCostB().is(Items.ENCHANTED_BOOK) && offer.getResult().is(ancient_tome);
	}

	public static void moveVillagerItems(MerchantMenu menu, MerchantContainer container, MerchantOffer offer) {
		// Doesn't check if enabled, since this should apply to the trades that have already been generated regardless
		if(isAncientTomeOffer(offer)) {
			if(container.getItem(0).isEmpty() && container.getItem(1).isEmpty()) {
				ItemStack costA = offer.getCostA();
				moveFromInventoryToPaymentSlot(menu, container, offer, 0, costA);
				ItemStack costB = offer.getCostB();
				moveFromInventoryToPaymentSlot(menu, container, offer, 1, costB);
			}
		}
	}

	private static void moveFromInventoryToPaymentSlot(MerchantMenu menu, MerchantContainer container, MerchantOffer offer, int tradeSlot, ItemStack targetStack) {
		((AccessorMerchantMenu)menu).invokeMoveFromInventoryToPaymentSlot(tradeSlot, new ItemCost(targetStack.getItemHolder(), targetStack.getCount(), DataComponentPredicate.allOf(targetStack.getComponents())));
		// Do a second pass with a softer match severity, but don't put in books that are the same as the output
		if(container.getItem(tradeSlot).isEmpty() && !targetStack.isEmpty()) {
			for(int slot = 3; slot < 39; ++slot) {
				ItemStack inSlot = menu.slots.get(slot).getItem();
				ItemStack currentStack = container.getItem(tradeSlot);

				if(!ItemStack.isSameItemSameComponents(inSlot, offer.getResult()) &&
						!inSlot.isEmpty() && (currentStack.isEmpty() ? offer.satisfiedBy(inSlot, targetStack) : ItemStack.isSameItemSameComponents(targetStack, inSlot))) {
					int currentCount = currentStack.isEmpty() ? 0 : currentStack.getCount();
					int amountToTake = Math.min(targetStack.getMaxStackSize() - currentCount, inSlot.getCount());
					ItemStack newStack = inSlot.copy();
					int newCount = currentCount + amountToTake;
					inSlot.shrink(amountToTake);
					newStack.setCount(newCount);
					container.setItem(tradeSlot, newStack);
					if(newCount >= targetStack.getMaxStackSize()) {
						break;
					}
				}
			}
		}
	}

	public static boolean matchWildcardEnchantedBook(MerchantOffer offer, ItemStack comparing, ItemStack reference) {
		// Doesn't check if enabled, since this should apply to the trades that have already been generated regardless
		if(isAncientTomeOffer(offer) && comparing.is(Items.ENCHANTED_BOOK) && reference.is(Items.ENCHANTED_BOOK)) {
			ItemEnchantments referenceEnchants = reference.get(DataComponents.ENCHANTMENTS);
			if(referenceEnchants.size() == 1) {
				Holder<Enchantment> enchantment = referenceEnchants.keySet().iterator().next();
				int level = referenceEnchants.getLevel(enchantment);

				ItemEnchantments comparingEnchants = comparing.get(DataComponents.ENCHANTMENTS);
				for(var entry : comparingEnchants.entrySet()) {
					if(entry.getKey() == enchantment && entry.getValue() >= level) {
						return true;
					}
				}
			}
		}

		return false;
	}

	private class ExchangeAncientTomesTrade implements ItemListing {
		@Nullable
		@Override
		public MerchantOffer getOffer(@NotNull Entity trader, @NotNull RandomSource random) {
			if(validEnchants.isEmpty() || !isEnabled())
				return null;
			Holder<Enchantment> target = validEnchants.get(random.nextInt(validEnchants.size()));

			ItemStack enchantedBook = EnchantedBookItem.createForEnchantment(new EnchantmentInstance(target, target.value().getMaxLevel()));
			ItemStack outputTome = AncientTomeItem.getEnchantedItemStack(target);
			return new MerchantOffer(
					new ItemCost(ancient_tome),
					Optional.of(
							new ItemCost(
									enchantedBook.getItemHolder(),
									1,
									DataComponentPredicate.allOf(enchantedBook.getComponents()
									))),
					outputTome,
					3,
					3,
					0.2F
			);
		}
	}
}
