package org.violetmoon.quark.content.tools.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;
import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.base.QuarkClient;
import org.violetmoon.quark.base.components.QuarkDataComponents;
import org.violetmoon.quark.content.mobs.module.StonelingsModule;
import org.violetmoon.quark.content.tools.module.PathfinderMapsModule;
import org.violetmoon.quark.content.tools.module.PathfinderMapsModule.TradeInfo;
import org.violetmoon.quark.content.world.module.GlimmeringWealdModule;
import org.violetmoon.zeta.item.ZetaItem;
import org.violetmoon.zeta.module.ZetaModule;
import org.violetmoon.zeta.registry.CreativeTabManager;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PathfindersQuillItem extends ZetaItem implements CreativeTabManager.AppendsUniquely {

	private static final Direction[] DIRECTIONS = new Direction[] { Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.NORTH };

	protected static final String TAG_POS_X = "searchPosX";
	protected static final String TAG_POS_Z = "searchPosZ";
	protected static final String TAG_POS_LEG = "searchPosLeg";
	protected static final String TAG_POS_LEG_INDEX = "searchPosLegIndex";

	public PathfindersQuillItem(ZetaModule module, Item.Properties properties) {
		super("pathfinders_quill", module, properties);
		CreativeTabManager.addToCreativeTabNextTo(CreativeModeTabs.TOOLS_AND_UTILITIES, this, Items.MAP, false);
	}

	public PathfindersQuillItem(ZetaModule module) {
		this(module, new Item.Properties().stacksTo(1));
	}

	public static ResourceLocation getTargetBiome(ItemStack stack) {
		String str = Optional.ofNullable(stack.get(QuarkDataComponents.TAG_BIOME)).orElse("");
		return str.isEmpty() ? null : ResourceLocation.parse(str);
	}

	public static int getOverlayColor(ItemStack stack) {
		return Optional.ofNullable(stack.get(QuarkDataComponents.TAG_COLOR)).orElse(0xFFFFFF);
	}

	public static ItemStack forBiome(String biome, int color) {
		ItemStack stack = new ItemStack(PathfinderMapsModule.pathfinders_quill);
		setBiome(stack, biome, color, false);
		return stack;
	}

	public static void setBiome(ItemStack stack, String biome, int color, boolean underground) {
		stack.set(QuarkDataComponents.TAG_BIOME, biome);
		stack.set(QuarkDataComponents.TAG_COLOR, color);
		stack.set(QuarkDataComponents.IS_UNDERGROUND, underground);
	}

	public static @Nullable ItemStack getActiveQuill(Player player) {
		for(ItemStack stack : player.getInventory().items)
			if(stack.getItem() instanceof PathfindersQuillItem) {
				boolean searching = Optional.ofNullable(stack.get(QuarkDataComponents.IS_SEARCHING)).orElse(false);

				if(searching)
					return stack;
			}

		for(ItemStack stack : player.getInventory().offhand)
			if(stack.getItem() instanceof PathfindersQuillItem) {
				boolean searching = Optional.ofNullable(stack.get(QuarkDataComponents.IS_SEARCHING)).orElse(false);

				if(searching)
					return stack;
			}

		for(ItemStack stack : player.getInventory().armor)
			if(stack.getItem() instanceof PathfindersQuillItem) {
				boolean searching = Optional.ofNullable(stack.get(QuarkDataComponents.IS_SEARCHING)).orElse(false);

				if(searching)
					return stack;
			}

		return null;
	}

	@Override
	public boolean shouldCauseReequipAnimationZeta(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
		return slotChanged || (oldStack.getItem() != newStack.getItem());
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);

		if(this.getTarget(stack) == null)
			return InteractionResultHolder.pass(stack);

		ItemStack active = getActiveQuill(player);
		if(active != null) {
			player.displayClientMessage(Component.translatable("quark.misc.only_one_quill"), true);
			return InteractionResultHolder.fail(stack);
		}

		Vec3 pos = player.getPosition(1F);
		level.playSound(null, pos.x, pos.y, pos.z, SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 0.5F, 1F);

		stack.set(QuarkDataComponents.IS_SEARCHING, true);
		stack.set(QuarkDataComponents.TAG_SOURCE_X, player.getBlockX());
		stack.set(QuarkDataComponents.TAG_SOURCE_Z, player.getBlockZ());
		return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
	}

	public ResourceLocation getTarget(ItemStack stack) {
		return getTargetBiome(stack);
	}

	protected int getIterations() {
		return PathfinderMapsModule.pathfindersQuillSpeed;
	}

	protected boolean isMultiThreaded() {
		return PathfinderMapsModule.multiThreaded;
	}

	@Override
	public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean held) {
		if(!level.isClientSide
				&& level instanceof ServerLevel sl
				&& Optional.ofNullable(stack.get(QuarkDataComponents.IS_SEARCHING)).orElse(false)
				&& entity instanceof Player player
				&& getActiveQuill(player) == stack) {

			ItemStack runningStack = search(stack, sl, player, slot);

			if(runningStack != stack) {
				String msg;

				if(runningStack.isEmpty()) {
					if(PathfinderMapsModule.allowRetrying) {
						runningStack = this.resetSearchingTags(stack);
						msg = getRetryMessage();
					} else
						msg = getFailMessage();
				} else
					msg = getSuccessMessage();

				player.displayClientMessage(Component.translatable(msg), true);

				Vec3 pos = player.getPosition(1F);
				level.playSound(null, pos.x, pos.y, pos.z, SoundEvents.NOTE_BLOCK_CHIME, SoundSource.PLAYERS, 0.5F, 1F);

				//we have to check for off hand manually as game uses same slot index....
				if(player.getOffhandItem() == stack){
					player.setItemInHand(InteractionHand.OFF_HAND, runningStack);
				} else {
					player.getInventory().setItem(slot, runningStack);
				}
			}
		}
	}

	protected ItemStack resetSearchingTags(ItemStack stack) {
		stack.remove(QuarkDataComponents.TAG_SOURCE_X);
		stack.remove(QuarkDataComponents.TAG_SOURCE_Z);
		stack.remove(QuarkDataComponents.IS_SEARCHING);
		stack.remove(QuarkDataComponents.TAG_POS_X);
		stack.remove(QuarkDataComponents.TAG_POS_Z);
		stack.remove(QuarkDataComponents.TAG_POS_LEG);
		stack.remove(QuarkDataComponents.TAG_POS_LEG_INDEX);
		return stack;
	}

	protected String getRetryMessage() {
		return "quark.misc.quill_retry";
	}

	protected String getSuccessMessage() {
		return "quark.misc.quill_finished";
	}

	protected String getFailMessage() {
		return "quark.misc.quill_failed";
	}

	protected ItemStack search(ItemStack stack, ServerLevel level, Player player, int slot) {
		ResourceLocation searchKey = this.getTarget(stack);
		if(searchKey == null)
			return ItemStack.EMPTY;

		InteractionResultHolder<BlockPos> result;
		if(isMultiThreaded()) {
			result = this.searchConcurrent(searchKey, stack, level, player);
		} else {
			result = this.searchIterative(searchKey, stack, level, player, getIterations());
		}

		if(result.getResult() == InteractionResult.FAIL) {
			return ItemStack.EMPTY;
		} else if(result.getResult() == InteractionResult.PASS) {
			return stack;
		} else {
			BlockPos found = result.getObject();
			return this.createMap(level, found, searchKey, stack);
		}
	}

	protected InteractionResultHolder<BlockPos> searchConcurrent(ResourceLocation searchKey, ItemStack stack, ServerLevel level, Player player) {
		int sourceX = Optional.ofNullable(stack.get(QuarkDataComponents.TAG_SOURCE_X)).orElse(0);
		int sourceZ = Optional.ofNullable(stack.get(QuarkDataComponents.TAG_SOURCE_Z)).orElse(0);
		BlockPos centerPos = new BlockPos(sourceX, 64, sourceZ);
		Key key = new Key(GlobalPos.of(level.dimension(), centerPos), searchKey);
		if(COMPUTING.contains(key)) {
			return InteractionResultHolder.pass(BlockPos.ZERO);
		} else if(RESULTS.containsKey(key)) {
			//we could use remove here instead but this serves as a cache since the result is always the same
			var ret = RESULTS.get(key);
			//EXECUTORS.submit(() -> RESULTS.remove(key)); //lmao. no lag spikes allowed. write is slow
			if(ret.getResult() == InteractionResult.PASS) {
				//this should never happen
				return InteractionResultHolder.fail(BlockPos.ZERO);
			}
			return ret;
		} else {
			//we don't want to alter the original stack here
			ItemStack dummy = stack.copy();
			EXECUTORS.submit(() -> {
				COMPUTING.add(key);
				RESULTS.put(key, searchIterative(searchKey, dummy, level, player, Integer.MAX_VALUE));
				COMPUTING.remove(key);
			});
			return InteractionResultHolder.pass(BlockPos.ZERO);
		}
	}

	//basically the old search method
	//pass = not done
	//fail = failed
	//present = success
	protected InteractionResultHolder<BlockPos> searchIterative(
			ResourceLocation searchKey, ItemStack stack, ServerLevel level, Player player, int maxIter) {
		int y = player.getBlockY();
		for(int i = 0; i < maxIter; i++) {

			final int height = 64;

			BlockPos nextPos = nextPos(stack);
			if(nextPos == null) {
				return InteractionResultHolder.fail(BlockPos.ZERO);
			}
			int[] searchedHeights = Mth.outFromOrigin(y, level.getMinBuildHeight() + 1, level.getMaxBuildHeight(), height).toArray();

			int testX = nextPos.getX();
			int testZ = nextPos.getZ();
			int quartX = QuartPos.fromBlock(testX);
			int quartZ = QuartPos.fromBlock(testZ);

			for(int testY : searchedHeights) {
				int quartY = QuartPos.fromBlock(testY);

				ServerChunkCache cache = level.getChunkSource();
				BiomeSource source = cache.getGenerator().getBiomeSource();
				Climate.Sampler sampler = cache.randomState().sampler();

				Holder<Biome> holder = source.getNoiseBiome(quartX, quartY, quartZ, sampler);

				if(holder.is(searchKey)) {
					BlockPos mapPos = new BlockPos(testX, testY, testZ);
					return InteractionResultHolder.sidedSuccess(mapPos, level.isClientSide);
				}
			}
		}
		return InteractionResultHolder.pass(BlockPos.ZERO);
	}

	protected static BlockPos nextPos(ItemStack stack) {
		final int step = 32;

		int sourceX = Optional.ofNullable(stack.get(QuarkDataComponents.TAG_SOURCE_X)).orElse(0);
		int sourceZ = Optional.ofNullable(stack.get(QuarkDataComponents.TAG_SOURCE_Z)).orElse(0);

		int x = Optional.ofNullable(stack.get(QuarkDataComponents.TAG_POS_X)).orElse(0);
		int z = Optional.ofNullable(stack.get(QuarkDataComponents.TAG_POS_Z)).orElse(0);
		int leg = Optional.ofNullable(stack.get(QuarkDataComponents.TAG_POS_LEG)).orElse(0);
		int legIndex = Optional.ofNullable(stack.get(QuarkDataComponents.TAG_POS_LEG_INDEX)).orElse(0);

		BlockPos cursor = new BlockPos(x, 0, z).relative(DIRECTIONS[(leg + 4) % 4]);

		int newX = cursor.getX();
		int newZ = cursor.getZ();

		int legSize = leg / 2 + 1;
		int maxLegs = 4 * Math.floorDiv(PathfinderMapsModule.searchRadius, step);

		if(legIndex >= legSize) {
			if(leg > maxLegs)
				return null;

			leg++;
			legIndex = 0;
		}

		legIndex++;

		stack.set(QuarkDataComponents.TAG_POS_X, newX);
		stack.set(QuarkDataComponents.TAG_POS_Z, newZ);
		stack.set(QuarkDataComponents.TAG_POS_LEG, leg);
		stack.set(QuarkDataComponents.TAG_POS_LEG_INDEX, legIndex);

		int retX = sourceX + newX * step;
		int retZ = sourceZ + newZ * step;

		return new BlockPos(retX, 0, retZ);
	}

	public ItemStack createMap(ServerLevel level, BlockPos targetPos, ResourceLocation target, ItemStack original) {
		int color = getOverlayColor(original);
		Component biomeComponent = Component.translatable("biome." + target.getNamespace() + "." + target.getPath());

		ItemStack stack = MapItem.create(level, targetPos.getX(), targetPos.getZ(), (byte) 2, true, true);

		MapItem.renderBiomePreviewMap(level, stack);
		MapItemSavedData.addTargetDecoration(stack, targetPos, "+", Type.RED_X);
		stack.set(DataComponents.CUSTOM_NAME, Component.translatable("item.quark.biome_map", biomeComponent));

		stack.getOrCreateTagElement("display").putInt("MapColor", color);
		stack.set(QuarkDataComponents.IS_PATHFINDER, true);
		return stack;
	}

	@OnlyIn(Dist.CLIENT)
	public static MutableComponent getSearchingComponent() {
		MutableComponent comp = Component.translatable("quark.misc.quill_searching");

		int dots = ((QuarkClient.ticker.ticksInGame / 10) % 4);
		for(int i = 0; i < dots; i++)
			comp.append(".");

		return comp;
	}

	@Override
	public List<ItemStack> appendItemsToCreativeTab() {
		List<ItemStack> items = new ArrayList<>();
		boolean generatedWeald = false;

		for(TradeInfo trade : PathfinderMapsModule.tradeList) {
			if(trade.biome.equals(GlimmeringWealdModule.BIOME_NAME))
				generatedWeald = true;

			items.add(forBiome(trade.biome.toString(), trade.color));
		}

		if(!generatedWeald &&
				Quark.ZETA.modules.isEnabled(StonelingsModule.class) &&
				Quark.ZETA.modules.isEnabled(GlimmeringWealdModule.class) &&
				StonelingsModule.wealdPathfinderMaps) {

			items.add(forBiome(GlimmeringWealdModule.BIOME_NAME.toString(), 0x317546));
		}

		return items;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, TooltipContext p_339594_, List<Component> comps, TooltipFlag flags) {
		ResourceLocation biome = this.getTarget(stack);
		if(biome != null) {
			if(Optional.ofNullable(stack.get(QuarkDataComponents.IS_SEARCHING)).orElse(false))
				comps.add(getSearchingComponent().withStyle(ChatFormatting.BLUE));

			comps.add(Component.translatable("biome." + biome.getNamespace() + "." + biome.getPath()).withStyle(ChatFormatting.GRAY));
		} else
			comps.add(Component.translatable("quark.misc.quill_blank").withStyle(ChatFormatting.GRAY));
	}

	//new concurrent search stuff. Experimental
	private record Key(GlobalPos pos, ResourceLocation structure) {}

	private static final Map<Key, InteractionResultHolder<BlockPos>> RESULTS = new ConcurrentHashMap<>();
	private static final Set<Key> COMPUTING = ConcurrentHashMap.newKeySet();
	protected static final ExecutorService EXECUTORS = Executors.newCachedThreadPool();

}
