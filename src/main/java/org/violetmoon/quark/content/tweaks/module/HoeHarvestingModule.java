package org.violetmoon.quark.content.tweaks.module;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.ItemAbilities;
import org.violetmoon.quark.base.Quark;
import org.violetmoon.zeta.config.Config;
import org.violetmoon.zeta.event.bus.LoadEvent;
import org.violetmoon.zeta.event.bus.PlayEvent;
import org.violetmoon.zeta.event.load.ZCommonSetup;
import org.violetmoon.zeta.event.play.ZBlock;
import org.violetmoon.zeta.module.ZetaLoadModule;
import org.violetmoon.zeta.module.ZetaModule;
import org.violetmoon.zeta.util.Hint;

@ZetaLoadModule(category = "tweaks")
public class HoeHarvestingModule extends ZetaModule {

	@Config
	@Config.Min(1)
	@Config.Max(5)
	public static int regularHoeRadius = 2;

	@Config
	@Config.Min(1)
	@Config.Max(5)
	public static int highTierHoeRadius = 3;

	@Hint(key = "hoe_harvesting")
	public static TagKey<Block> hoe_harvestable = Quark.asTagKey(Registries.BLOCK, "hoe_harvestable");

	public static TagKey<Item> bigHarvestingHoesTag;

	public static int getRange(ItemStack hoe) {
		if(!Quark.ZETA.modules.isEnabled(HoeHarvestingModule.class))
			return 1;

		if(!isHoe(hoe))
			return 1;
		else if(hoe.is(bigHarvestingHoesTag))
			return highTierHoeRadius;
		else
			return regularHoeRadius;
	}

	public static boolean isHoe(ItemStack itemStack) {
		return !itemStack.isEmpty() &&
				(itemStack.getItem() instanceof HoeItem
						|| itemStack.is(ItemTags.HOES)
						|| itemStack.getItem().canPerformAction(itemStack, ItemAbilities.HOE_DIG)); //TODO: IForgeItem
	}

	@LoadEvent
	public final void setup(ZCommonSetup event) {
		bigHarvestingHoesTag = Quark.asTagKey(Registries.ITEM, "big_harvesting_hoes");
	}

	@PlayEvent
	public void onBlockBroken(ZBlock.Break event) {
		LevelAccessor world = event.getLevel();
		if(!(world instanceof Level level))
			return;

		Player player = event.getPlayer();
		BlockPos basePos = event.getPos();
		ItemStack stack = player.getMainHandItem();
		if(isHoe(stack) && canHarvest(player, world, basePos, event.getState())) {
			boolean brokeNonInstant = false;
			int range = getRange(stack);

			for(int i = 1 - range; i < range; i++)
				for(int k = 1 - range; k < range; k++) {
					if(i == 0 && k == 0)
						continue;

					BlockPos pos = basePos.offset(i, 0, k);
					BlockState state = world.getBlockState(pos);
					if(canHarvest(player, world, pos, state)) {
						Block block = state.getBlock();

						if(state.getDestroySpeed(world, pos) != 0.0F)
							brokeNonInstant = true;
						
						block.playerWillDestroy(level, pos, state, player);
						if(block.canHarvestBlock(state, world, pos, player))
							block.playerDestroy(level, player, pos, state, world.getBlockEntity(pos), stack);
						
						world.destroyBlock(pos, false);
						world.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, pos, Block.getId(state));
					}
				}

			if(brokeNonInstant)
				stack.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
		}
	}

	private boolean canHarvest(Player player, LevelAccessor world, BlockPos pos, BlockState state) {
		return isHarvestableBlock(state);
		//old logic:
		//state.canBeReplaced(new BlockPlaceContext(new UseOnContext(player, InteractionHand.MAIN_HAND, new BlockHitResult(new Vec3(0.5, 0.5, 0.5), Direction.DOWN, pos, false))));
	}

	public static boolean isHarvestableBlock(BlockState state) {
		boolean isHarvestable = state.is(hoe_harvestable);

		//extra logic for certain kinds of blocks.
		if(state.getBlock().equals(Blocks.WATER)){
			isHarvestable = false;
		}
		else if(state.getBlock() instanceof CropBlock crop){
			isHarvestable = state.equals(crop.getStateForAge(crop.getMaxAge())); //only harvest full crops
		}
		else if(state.getBlock() instanceof CocoaBlock){
			isHarvestable = state.getValue(CocoaBlock.AGE) == 2; //only harvest full cocoa
		}
		//TODO more crop types?
		else if(state.getBlock() instanceof LeavesBlock){
			if(state.getValue(LeavesBlock.PERSISTENT)){
				isHarvestable = false; //don't harvest placed leaves
			}
		}
		return isHarvestable;
	}
}
