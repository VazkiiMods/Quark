package org.violetmoon.quark.content.tweaks.module;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.SpecialPlantable;
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
	TagKey<Item> hoes = ItemTags.HOES;

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
		Block block = state.getBlock();
		//todo: Needs to check if plant is a water/desert plant.
		/*if(block instanceof SpecialPlantable plant) {
			PlantType type = plant.getPlantType(world, pos);
			return type != PlantType.WATER && type != PlantType.DESERT;
		}*/

		return isHarvestableMaterial(state) &&
				state.canBeReplaced(new BlockPlaceContext(new UseOnContext(player, InteractionHand.MAIN_HAND, new BlockHitResult(new Vec3(0.5, 0.5, 0.5), Direction.DOWN, pos, false))));
	}

	public static boolean isHarvestableMaterial(BlockState state) {
		NoteBlockInstrument instrument = state.instrument();

		boolean PLANT = state.mapColor == MapColor.PLANT && state.getPistonPushReaction() == PushReaction.DESTROY;
		boolean WATER_PLANT = state.mapColor == MapColor.WATER && instrument == NoteBlockInstrument.BASEDRUM;
		boolean REPLACEABLE_FIREPROOF_PLANT = state.mapColor == MapColor.PLANT && state.canBeReplaced() && state.getPistonPushReaction() == PushReaction.DESTROY;
		boolean REPLACEABLE_WATER_PLANT = state.mapColor == MapColor.WATER && state.canBeReplaced() && state.getPistonPushReaction() == PushReaction.DESTROY;

		return PLANT || WATER_PLANT || REPLACEABLE_FIREPROOF_PLANT || REPLACEABLE_WATER_PLANT;
	}
}
