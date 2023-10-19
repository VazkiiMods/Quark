package vazkii.quark.content.management.module;

import com.google.common.collect.Lists;
import net.minecraft.core.Registry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.PlayerInvWrapper;
import net.minecraftforge.registries.ForgeRegistries;
import vazkii.quark.base.util.InventoryIIH;
import vazkii.quark.addons.oddities.module.BackpackModule;
import vazkii.quark.api.event.GatherToolClassesEvent;
import vazkii.quark.base.handler.MiscUtil;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.ModuleLoader;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.base.module.config.Config;
import vazkii.quark.base.module.sync.SyncedFlagHandler;
import vazkii.zeta.event.ZConfigChanged;
import vazkii.zeta.event.bus.LoadEvent;

import java.util.*;
import java.util.function.Predicate;

@LoadModule(category = "management", hasSubscriptions = true, antiOverlap = "inventorytweaks")
public class AutomaticToolRestockModule extends QuarkModule {

	private static final Map<ToolAction, String> ACTION_TO_CLASS = new HashMap<>();

	static {
		ACTION_TO_CLASS.put(ToolActions.AXE_DIG, "axe");
		ACTION_TO_CLASS.put(ToolActions.HOE_DIG, "hoe");
		ACTION_TO_CLASS.put(ToolActions.SHOVEL_DIG, "shovel");
		ACTION_TO_CLASS.put(ToolActions.PICKAXE_DIG, "pickaxe");
		ACTION_TO_CLASS.put(ToolActions.SWORD_SWEEP, "sword");
		ACTION_TO_CLASS.put(ToolActions.SHEARS_HARVEST, "shears");
		ACTION_TO_CLASS.put(ToolActions.FISHING_ROD_CAST, "fishing_rod");
	}

	private static final WeakHashMap<Player, Stack<QueuedRestock>> replacements = new WeakHashMap<>();

	public List<Enchantment> importantEnchants = new ArrayList<>();
	public List<Item> itemsToIgnore = new ArrayList<>();

	@Config(name = "Important Enchantments",
			description = "Enchantments deemed important enough to have special priority when finding a replacement")
	private List<String> enchantNames = generateDefaultEnchantmentList();

	private static final String LOOSE_MATCHING = "automatic_restock_loose_matching";
	private static final String ENCHANT_MATCHING = "automatic_restock_enchant_matching";
	private static final String CHECK_HOTBAR = "automatic_restock_check_hotbar";
	private static final String UNSTACKABLES_ONLY = "automatic_restock_unstackables_only";

	@Config(description = "Enable replacing your tools with tools of the same type but not the same item", flag = LOOSE_MATCHING)
	private boolean enableLooseMatching = true;

	@Config(description = "Enable comparing enchantments to find a replacement", flag = ENCHANT_MATCHING)
	private boolean enableEnchantMatching = true;

	@Config(description = "Allow pulling items from one hotbar slot to another", flag = CHECK_HOTBAR)
	private boolean checkHotbar = false;

	@Config(flag = UNSTACKABLES_ONLY)
	private boolean unstackablesOnly = false;

	@Config(description = "Any items you place in this list will be ignored by the restock feature")
	private List<String> ignoredItems = Lists.newArrayList("botania:exchange_rod", "botania:dirt_rod", "botania:skydirt_rod", "botania:cobble_rod");

	private Object mutex = new Object();

	@LoadEvent
	public final void configChanged(ZConfigChanged event) {
		importantEnchants = MiscUtil.massRegistryGet(enchantNames, ForgeRegistries.ENCHANTMENTS);
		itemsToIgnore = MiscUtil.massRegistryGet(ignoredItems, ForgeRegistries.ITEMS);
	}

	@SubscribeEvent
	public void onToolBreak(PlayerDestroyItemEvent event) {
		Player player = event.getEntity();
		ItemStack stack = event.getOriginal();
		Item item = stack.getItem();

		if (player instanceof ServerPlayer serverPlayer) {
			if (!SyncedFlagHandler.getFlagForPlayer(serverPlayer, "automatic_tool_restock"))
				return;

			boolean onlyUnstackables = SyncedFlagHandler.getFlagForPlayer(serverPlayer, UNSTACKABLES_ONLY);

			if (!stack.isEmpty() && !(item instanceof ArmorItem) && (!onlyUnstackables || !stack.isStackable())) {

				boolean hotbar = SyncedFlagHandler.getFlagForPlayer(serverPlayer, CHECK_HOTBAR);

				int currSlot = player.getInventory().selected;
				if (event.getHand() == InteractionHand.OFF_HAND)
					currSlot = player.getInventory().getContainerSize() - 1;

				List<Enchantment> enchantmentsOnStack = getImportantEnchantments(stack);
				Predicate<ItemStack> itemPredicate = (other) -> other.getItem() == item;
				if (!stack.isDamageableItem())
					itemPredicate = itemPredicate.and((other) -> other.getDamageValue() == stack.getDamageValue());

				Predicate<ItemStack> enchantmentPredicate = (other) -> !(new ArrayList<>(enchantmentsOnStack)).retainAll(getImportantEnchantments(other));

				Set<String> classes = getItemClasses(stack);
				Optional<Predicate<ItemStack>> toolPredicate = Optional.empty();

				if (!classes.isEmpty())
					toolPredicate = Optional.of((other) -> {
						Set<String> otherClasses = getItemClasses(other);
						return !otherClasses.isEmpty() && !otherClasses.retainAll(classes);
					});

				RestockContext ctx = new RestockContext(serverPlayer, currSlot, enchantmentsOnStack, itemPredicate, enchantmentPredicate, toolPredicate);

				int lower = hotbar ? 0 : 9;
				int upper = player.getInventory().items.size();
				boolean foundInInv = crawlInventory(new PlayerInvWrapper(player.getInventory()), lower, upper, ctx);

				if (!foundInInv && ModuleLoader.INSTANCE.isModuleEnabled(BackpackModule.class)) {
					ItemStack backpack = player.getInventory().armor.get(2);

					if (backpack.getItem() == BackpackModule.backpack) {
						InventoryIIH inv = new InventoryIIH(backpack);
						crawlInventory(inv, 0, inv.getSlots(), ctx);
					}
				}
			}
		}
	}

	private boolean crawlInventory(IItemHandler inv, int lowerBound, int upperBound, RestockContext ctx) {
		ServerPlayer player = ctx.player;
		int currSlot = ctx.currSlot;
		List<Enchantment> enchantmentsOnStack = ctx.enchantmentsOnStack;
		Predicate<ItemStack> itemPredicate = ctx.itemPredicate;
		Predicate<ItemStack> enchantmentPredicate = ctx.enchantmentPredicate;
		Optional<Predicate<ItemStack>> toolPredicateOpt = ctx.toolPredicate;

		boolean enchantMatching = SyncedFlagHandler.getFlagForPlayer(player, ENCHANT_MATCHING);
		boolean looseMatching = SyncedFlagHandler.getFlagForPlayer(player, LOOSE_MATCHING);

		if(enchantMatching && findReplacement(inv, player, lowerBound, upperBound, currSlot, itemPredicate.and(enchantmentPredicate)))
			return true;

		if(findReplacement(inv, player, lowerBound, upperBound, currSlot, itemPredicate))
			return true;

		if(looseMatching && toolPredicateOpt.isPresent()) {
			Predicate<ItemStack> toolPredicate = toolPredicateOpt.get();
			if(enchantMatching && !enchantmentsOnStack.isEmpty() && findReplacement(inv, player, lowerBound, upperBound, currSlot, toolPredicate.and(enchantmentPredicate)))
				return true;

			return findReplacement(inv, player, lowerBound, upperBound, currSlot, toolPredicate);
		}

		return false;
	}

	@SubscribeEvent
	public void onPlayerTick(PlayerTickEvent event) {
		if(event.phase == Phase.END && !event.player.level.isClientSide && replacements.containsKey(event.player)) {
			Stack<QueuedRestock> replacementStack = replacements.get(event.player);
			synchronized(mutex) {
				while(!replacementStack.isEmpty()) {
					QueuedRestock restock = replacementStack.pop();
					switchItems(event.player, restock);
				}
			}
		}
	}

	private HashSet<String> getItemClasses(ItemStack stack) {
		Item item = stack.getItem();

		HashSet<String> classes = new HashSet<>();
		if(item instanceof BowItem)
			classes.add("bow");

		else if(item instanceof CrossbowItem)
			classes.add("crossbow");

		for(ToolAction action : ACTION_TO_CLASS.keySet()) {
			if(item.canPerformAction(stack, action))
				classes.add(ACTION_TO_CLASS.get(action));
		}

		GatherToolClassesEvent event = new GatherToolClassesEvent(stack, classes);
		MinecraftForge.EVENT_BUS.post(event);

		return classes;
	}

	private boolean findReplacement(IItemHandler inv, Player player, int lowerBound, int upperBound, int currSlot, Predicate<ItemStack> match) {
		synchronized(mutex) {
			for(int i = lowerBound; i < upperBound; i++) {
				if(i == currSlot)
					continue;

				ItemStack stackAt = inv.getStackInSlot(i);
				if(!stackAt.isEmpty() && match.test(stackAt)) {
					pushReplace(player, inv, i, currSlot);
					return true;
				}
			}

			return false;
		}
	}

	private void pushReplace(Player player, IItemHandler inv, int slot1, int slot2) {
		if(!replacements.containsKey(player))
			replacements.put(player, new Stack<>());
		replacements.get(player).push(new QueuedRestock(inv, slot1, slot2));
	}

	private void switchItems(Player player, QueuedRestock restock) {
		Inventory playerInv = player.getInventory();
		IItemHandler providingInv = restock.providingInv;

		int providingSlot = restock.providingSlot;
		int playerSlot = restock.playerSlot;

		if(providingSlot >= providingInv.getSlots() || playerSlot >= playerInv.items.size())
			return;

		ItemStack stackAtPlayerSlot = playerInv.getItem(playerSlot).copy();
		ItemStack stackProvidingSlot = providingInv.getStackInSlot(providingSlot).copy();

		//Botania rods are only detected in the stackAtPlayerSlot but other tools are only detected in stackProvidingSlot so we check em both
		if (itemIgnored(stackAtPlayerSlot) || itemIgnored(stackProvidingSlot))
			return;

		providingInv.extractItem(providingSlot, stackProvidingSlot.getCount(), false);
		providingInv.insertItem(providingSlot, stackAtPlayerSlot, false);

		playerInv.setItem(playerSlot, stackProvidingSlot);
	}

	private boolean itemIgnored(ItemStack stack) {
		return stack != null && !stack.is(Items.AIR) && itemsToIgnore.contains(stack.getItem());
	}

	private List<Enchantment> getImportantEnchantments(ItemStack stack) {
		List<Enchantment> enchantsOnStack = new ArrayList<>();
		for(Enchantment ench : importantEnchants)
			if(EnchantmentHelper.getItemEnchantmentLevel(ench, stack) > 0)
				enchantsOnStack.add(ench);

		return enchantsOnStack;
	}

	private static List<String> generateDefaultEnchantmentList() {
		Enchantment[] enchants = new Enchantment[] {
				Enchantments.SILK_TOUCH,
				Enchantments.BLOCK_FORTUNE,
				Enchantments.INFINITY_ARROWS,
				Enchantments.FISHING_LUCK,
				Enchantments.MOB_LOOTING
		};

		List<String> strings = new ArrayList<>();
		for(Enchantment e : enchants)
			strings.add(Registry.ENCHANTMENT.getKey(e).toString());

		return strings;
	}

	private record RestockContext(ServerPlayer player, int currSlot,
			List<Enchantment> enchantmentsOnStack,
			Predicate<ItemStack> itemPredicate,
			Predicate<ItemStack> enchantmentPredicate,
			Optional<Predicate<ItemStack>> toolPredicate) {}

	private record QueuedRestock(IItemHandler providingInv, int providingSlot, int playerSlot) {}

}
