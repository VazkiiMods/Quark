package org.violetmoon.quark.content.building.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.ItemAbility;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.violetmoon.quark.base.Quark;
import org.violetmoon.zeta.module.ZetaModule;
import org.violetmoon.zeta.registry.RenderLayerRegistry;
import org.violetmoon.zeta.util.MiscUtil;

/**
 * Unfortunately, due to Ladder Weirdness (tm) this block is NYI
 */
public class HollowWoodBlock extends HollowFrameBlock {

	protected static final BooleanProperty DOWN = BlockStateProperties.DOWN;
	protected static final BooleanProperty UP = BlockStateProperties.UP;
	protected static final BooleanProperty NORTH = BlockStateProperties.NORTH;
	protected static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
	protected static final BooleanProperty WEST = BlockStateProperties.WEST;
	protected static final BooleanProperty EAST = BlockStateProperties.EAST;

	private final boolean flammable;

	public HollowWoodBlock(Block sourceLog, @Nullable ZetaModule module, boolean flammable) {
		this(Quark.ZETA.registryUtil.inherit(sourceLog, "hollow_%s"), sourceLog, module, flammable);
	}

	public HollowWoodBlock(String name, Block sourceLog, @Nullable ZetaModule module, boolean flammable) {
		super(name, module,
				MiscUtil.copyPropertySafe(sourceLog)
						.isSuffocating((s, g, p) -> false));

		this.flammable = flammable;
		registerDefaultState(defaultBlockState()
				.setValue(DOWN, true)
				.setValue(UP, true)
				.setValue(NORTH, true)
				.setValue(SOUTH, true)
				.setValue(WEST, true)
				.setValue(EAST, true));

		if(module == null) //auto registration below this line
			return;
		module.zeta().renderLayerRegistry.put(this, RenderLayerRegistry.Layer.CUTOUT_MIPPED);
	}

	@Override
	public byte getShapeCode(BlockState state) {
		return shapeCode(state, DOWN, UP, NORTH, SOUTH, WEST, EAST);
	}

	@SuppressWarnings("deprecation") //Don't need the Forge extension
	@Override
	public BlockState rotate(BlockState state, Rotation direction) {
		BlockState newState = state;
		for(Direction dir : Direction.values())
			newState = newState.setValue(MiscUtil.directionProperty(dir), state.getValue(MiscUtil.directionProperty(direction.rotate(dir))));
		return newState;
	}

	@NotNull
	@Override
	public BlockState mirror(@NotNull BlockState state, @NotNull Mirror mirror) {
		BlockState newState = state;
		for(Direction dir : Direction.values())
			newState = newState.setValue(MiscUtil.directionProperty(dir), state.getValue(MiscUtil.directionProperty(mirror.mirror(dir))));
		return newState;
	}

	@Override
	public @Nullable BlockState getToolModifiedState(BlockState state, UseOnContext context, ItemAbility itemAbility, boolean simulate) {
		if("axe_strip".equals(String.valueOf(itemAbility))) {
			Vec3 exactPos = context.getClickLocation();
			BlockPos centerPos = context.getClickedPos();
			Direction face = Direction.getNearest(exactPos.x - (centerPos.getX() + 0.5), exactPos.y - (centerPos.getY() + 0.5), exactPos.z - (centerPos.getZ() + 0.5));
			return state.cycle(MiscUtil.directionProperty(face));
		}
		return super.getToolModifiedStateZeta(state, context, ItemAbility.get(String.valueOf(itemAbility)), simulate);
	}

	@Override
	protected void createBlockStateDefinition(@NotNull StateDefinition.Builder<Block, BlockState> def) {
		super.createBlockStateDefinition(def);
		def.add(UP, DOWN, NORTH, SOUTH, WEST, EAST);
	}

	@Override
	public boolean isFlammable(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
		return flammable;
	}
}
