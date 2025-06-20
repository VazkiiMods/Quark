/**
 * This class was created by <WireSegal>. It's distributed as
 * part of the Quark Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Quark
 * <p>
 * Quark is Open Source and distributed under the
 * CC-BY-NC-SA 3.0 License: https://creativecommons.org/licenses/by-nc-sa/3.0/deed.en_GB
 * <p>
 * File Created @ [Jul 05, 2019, 16:56 AM (EST)]
 */
package org.violetmoon.quark.content.tweaks.module;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.GrowingPlantBlock;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.ItemAbility;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jetbrains.annotations.Nullable;
import org.violetmoon.quark.api.event.SimpleHarvestEvent;
import org.violetmoon.quark.api.event.SimpleHarvestEvent.ActionType;
import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.base.network.message.HarvestMessage;
import org.violetmoon.zeta.config.Config;
import org.violetmoon.zeta.event.bus.LoadEvent;
import org.violetmoon.zeta.event.bus.PlayEvent;
import org.violetmoon.zeta.event.load.ZCommonSetup;
import org.violetmoon.zeta.event.load.ZConfigChanged;
import org.violetmoon.zeta.event.play.entity.player.ZRightClickBlock;
import org.violetmoon.zeta.module.ZetaLoadModule;
import org.violetmoon.zeta.module.ZetaModule;
import org.violetmoon.zeta.util.MiscUtil;

import java.util.List;
import java.util.Map;
import java.util.Set;

@ZetaLoadModule(category = "tweaks")
public class SimpleHarvestModule extends ZetaModule {

	@Config(description = "Can players harvest crops with empty hand clicks?")
	public static boolean emptyHandHarvest = true;
	@Config(description = "Does harvesting crops with a hoe cost durability?")
	public static boolean harvestingCostsDurability = false;
	@Config(description = "Should Quark look for(nonvanilla) crops, and handle them?")
	public static boolean doHarvestingSearch = true;
	@Config(description = "Should villagers use simple harvest instead of breaking crops?")
	public static boolean villagersUseSimpleHarvest = true;

	@Config(
		description = "Which crops can be harvested?\n" +
				"Format is: \"harvestState[,afterHarvest]\", i.e. \"minecraft:wheat[age=7]\" or \"minecraft:cocoa[age=2,facing=north],minecraft:cocoa[age=0,facing=north]\""
	)
	public static List<String> harvestableBlocks = Lists.newArrayList(
			"minecraft:wheat[age=7]",
			"minecraft:carrots[age=7]",
			"minecraft:potatoes[age=7]",
			"minecraft:beetroots[age=3]",
			"minecraft:nether_wart[age=3]",
			"minecraft:cocoa[age=2,facing=north],minecraft:cocoa[age=0,facing=north]",
			"minecraft:cocoa[age=2,facing=south],minecraft:cocoa[age=0,facing=south]",
			"minecraft:cocoa[age=2,facing=east],minecraft:cocoa[age=0,facing=east]",
			"minecraft:cocoa[age=2,facing=west],minecraft:cocoa[age=0,facing=west]");

	@Config(
		description = "Which blocks should right click harvesting simulate a click on instead of breaking?\n" +
				"This is for blocks like sweet berry bushes, which have right click harvesting built in."
	)
	public static List<String> rightClickableBlocks = Lists.newArrayList(
			"minecraft:sweet_berry_bush",
			"minecraft:cave_vines");

	public static final Map<BlockState, BlockState> crops = Maps.newHashMap(); //max age to base state
	private static final Set<Block> cropBlocks = Sets.newHashSet(); //used for the event
	public static final Set<Block> rightClickCrops = Sets.newHashSet();

	public static TagKey<Block> simpleHarvestBlacklistedTag;

	public static boolean staticEnabled;

	@LoadEvent
	public void setup(ZCommonSetup event) {
		simpleHarvestBlacklistedTag = Quark.asTagKey(Registries.BLOCK,"simple_harvest_blacklisted");
	}

	@LoadEvent
	public void configChanged(ZConfigChanged event) {
		crops.clear();
		cropBlocks.clear();
		rightClickCrops.clear();
		staticEnabled = isEnabled();

		if(doHarvestingSearch) {
			for (var b : BuiltInRegistries.BLOCK) {
				if (!isVanilla(b)) {
					if (b instanceof CropBlock c) {
						//only grabbing blocks whose max age is acceptable
						crops.put(c.getStateForAge(c.getMaxAge()), c.defaultBlockState());
					} else if ((b instanceof BushBlock || b instanceof GrowingPlantBlock) && b instanceof BonemealableBlock) {
						rightClickCrops.add(b);
					}
				}
			}
		}

		for(String harvestKey : harvestableBlocks) {
			BlockState initial, result;
			String[] split = tokenize(harvestKey);
			initial = MiscUtil.fromString(split[0]);
			if(split.length > 1)
				result = MiscUtil.fromString(split[1]);
			else
				result = initial.getBlock().defaultBlockState();

			if(initial.getBlock() != Blocks.AIR)
				crops.put(initial, result);
		}

		for(String blockName : rightClickableBlocks) {
			Block block = BuiltInRegistries.BLOCK.get(ResourceLocation.parse(blockName));
			if(block != Blocks.AIR)
				rightClickCrops.add(block);
		}

		crops.values().forEach(bl -> cropBlocks.add(bl.getBlock()));
	}

	private String[] tokenize(String harvestKey) {
		boolean inBracket = false;
		for(int i = 0; i < harvestKey.length(); i++) {
			char charAt = harvestKey.charAt(i);
			if(charAt == '[')
				inBracket = true;
			else if(charAt == ']')
				inBracket = false;
			else if(charAt == ',' && !inBracket)
				return new String[] { harvestKey.substring(0, i), harvestKey.substring(i + 1) };
		}
		return new String[] { harvestKey };
	}

	private boolean isVanilla(Block entry) {
		ResourceLocation loc = BuiltInRegistries.BLOCK.getKey(entry);

		return loc.getNamespace().equals("minecraft");
	}

	private static boolean harvestAndReplant(Level level, BlockPos pos, BlockState inWorld,
											 @Nullable LivingEntity entity, @Nullable InteractionHand hand) {

		// This could be modified and called by the event, so we don't actually know if we know how to replant a block. As such we first check that
		BlockState newBlock = crops.get(inWorld);
		if(newBlock == null) return false;

		if(level instanceof ServerLevel serverLevel) {

			ItemStack copy;
			if(entity == null || hand == null){
				copy = new ItemStack(Items.STICK);
			}else{
				// is this needed? isnt enchantent level handled intenrally?
				copy = entity.getItemInHand(hand).copy();
			}

			MutableBoolean hasTaken = new MutableBoolean(false);
			Item blockItem = inWorld.getBlock().asItem();
			Block.getDrops(inWorld, serverLevel, pos, level.getBlockEntity(pos), entity, copy)
					.forEach((stack) -> {
						if (stack.getItem() == blockItem && !hasTaken.getValue()) {
							stack.shrink(1);
							hasTaken.setValue(true);
						}

						if (!stack.isEmpty())
							Block.popResource(level, pos, stack);
					});
			boolean dropXp = entity instanceof Player;
			inWorld.spawnAfterBreak(serverLevel, pos, copy, dropXp);

			level.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, pos, Block.getId(newBlock));
			level.setBlockAndUpdate(pos, newBlock);
			level.gameEvent(GameEvent.BLOCK_DESTROY, pos, GameEvent.Context.of(entity, inWorld));
		}
		return true;
	}

	private boolean isHarvesting = false;

	@PlayEvent
	public void onClick(ZRightClickBlock event) {
		if(isHarvesting)
			return;
		isHarvesting = true;
		if(click(event.getPlayer(), event.getHand(), event.getPos(), event.getHitVec())) {
			event.setCanceled(true);
			event.setCancellationResult(InteractionResult.sidedSuccess(event.getLevel().isClientSide));
		}
		isHarvesting = false;
	}

	public static boolean tryHarvestOrClickCrop(Level level, BlockPos pos, @Nullable LivingEntity entity, @Nullable InteractionHand hand,
												 boolean canRightClick) {
		if(entity instanceof Player p && (!level.mayInteract(p, pos)))
			return false;

		BlockState worldBlock = level.getBlockState(pos);
		ActionType action = getActionForBlock(worldBlock, canRightClick);
		//prevents firing event for non-crop blocks
		if(action != ActionType.NONE) {
			// event stuff
			SimpleHarvestEvent event = new SimpleHarvestEvent(worldBlock, pos, level, hand, entity, action);
			NeoForge.EVENT_BUS.post(event);
			if(event.isCanceled()) return false;

			BlockPos newPos = event.getTargetPos();
			if(newPos != pos) worldBlock = level.getBlockState(newPos);
			action = event.getAction();
			// end event stuff

			if(action == ActionType.HARVEST) {
				if(entity instanceof Player p /*&& !Quark.FLAN_INTEGRATION.canBreak(p, pos)*/) return false; // TODO: Wait for Neoforge Flan or remove
				return harvestAndReplant(level, pos, worldBlock, entity, hand);
			} else if(action == ActionType.CLICK && entity instanceof Player p) { //Only players can click!
				// if(!Quark.FLAN_INTEGRATION.canInteract(p, pos)) return false;

				var hitResult = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, true);
				if(hand == null) hand = InteractionHand.MAIN_HAND;
				//TODO: add fake player here for villagers
				if(entity instanceof ServerPlayer player) {
					return player.gameMode.useItemOn(player, player.level(), player.getItemInHand(hand), hand,
							hitResult).consumesAction();
				} else {
					return Quark.proxy.clientUseItem(p, level, hand, hitResult).consumesAction();
				}
			}
		}

		return false;
	}

	private static ActionType getActionForBlock(BlockState state, boolean closeEnoughToRightClick) {
		if(state.is(simpleHarvestBlacklistedTag)) return ActionType.NONE;
		else if(closeEnoughToRightClick && rightClickCrops.contains(state.getBlock()))
			return ActionType.CLICK;
		else if(crops.containsKey(state))
			return ActionType.HARVEST;
		return ActionType.NONE;
	}

	public static boolean click(Player player, InteractionHand hand, BlockPos pos, BlockHitResult pick) {
		if(player == null || hand == null || player.isSpectator())
			return false;

		if(pick.getType() != HitResult.Type.BLOCK || !pick.getBlockPos().equals(pos))
			return false;

		Level level = player.level();
		BlockState stateAt = level.getBlockState(pos);
		//can you till this block?
		BlockState modifiedState = Quark.ZETA.blockExtensions.get(stateAt).getToolModifiedStateZeta(stateAt, new UseOnContext(player, hand, pick), ItemAbility.get("hoe_till"), true);
		if(modifiedState != null)
			return false;

		ItemStack inHand = player.getItemInHand(hand);
		boolean isHoe = HoeHarvestingModule.isHoe(inHand);

		if(!emptyHandHarvest && !isHoe)
			return false;

		BlockState stateAbove = level.getBlockState(pos.above());

		//why do we only check this if using hoe?
		if(isHoe) {
			//Early check. Check action at the target block
			if(getActionForBlock(stateAt, true) == ActionType.NONE &&
					getActionForBlock(stateAbove, true) == ActionType.NONE)
				return false;
		}

		int range = HoeHarvestingModule.getRange(inHand);

		boolean hasHarvested = false;

		for(int x = 1 - range; x < range; x++)
			for(int z = 1 - range; z < range; z++) {
				BlockPos shiftPos = pos.offset(x, 0, z);

				if(!tryHarvestOrClickCrop(level, shiftPos, player, hand, range > 1)) {
					shiftPos = shiftPos.above();

					if(tryHarvestOrClickCrop(level, shiftPos, player, hand, range > 1))
						hasHarvested = true;
				} else {
					hasHarvested = true;
				}
			}

		if(!hasHarvested)
			return false;

		if(level.isClientSide) {
			if(inHand.isEmpty())
				PacketDistributor.sendToServer(new HarvestMessage(pos, hand));
		} else {
			if(harvestingCostsDurability && isHoe)
				inHand.hurtAndBreak(1, player, Player.getSlotForHand(InteractionHand.MAIN_HAND));
		}

		return true;
	}
}
