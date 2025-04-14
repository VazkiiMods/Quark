package org.violetmoon.quark.content.automation.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.violetmoon.quark.content.automation.block.be.ChuteBlockEntity;
import org.violetmoon.zeta.block.ZetaBlock;
import org.violetmoon.zeta.module.ZetaModule;
import org.violetmoon.zeta.registry.RenderLayerRegistry;

public class ChuteBlock extends ZetaBlock implements EntityBlock {

	private static final VoxelShape INPUT_SHAPE = Block.box(0.0D, 10.0D, 0.0D, 16.0D, 16.0D, 16.0D);
	private static final VoxelShape MIDDLE_SHAPE = Block.box(4.0D, 4.0D, 4.0D, 12.0D, 10.0D, 12.0D);
	private static final VoxelShape CHUTE_TOP_SHAPE = Shapes.or(MIDDLE_SHAPE, INPUT_SHAPE);
	private static final VoxelShape DOWN_SHAPE = Shapes.or(CHUTE_TOP_SHAPE, Block.box(6.0D, 0.0D, 6.0D, 10.0D, 4.0D, 10.0D));

	public static final BooleanProperty ENABLED = BlockStateProperties.ENABLED;
	//public static final BooleanProperty HAS_SPACE_BELOW =  BooleanProperty.create("has_space_below");

	public ChuteBlock(String regname, @Nullable ZetaModule module, Properties properties) {
		super(regname, module, properties);
		registerDefaultState(defaultBlockState().setValue(ENABLED, true));

		if(module == null) //auto registration below this line
			return;

		module.zeta().renderLayerRegistry.put(this, RenderLayerRegistry.Layer.CUTOUT_MIPPED);
		setCreativeTab(CreativeModeTabs.REDSTONE_BLOCKS);
	}

	@Override
	public int getFlammabilityZeta(BlockState state, BlockGetter world, BlockPos pos, Direction face) {
		return 20;
	}

	@Override
	public int getFireSpreadSpeedZeta(BlockState state, BlockGetter world, BlockPos pos, Direction face) {
		return 5;
	}

	@Override
	public void neighborChanged(BlockState state, Level worldIn, @NotNull BlockPos pos, @NotNull Block blockIn, @NotNull BlockPos fromPos, boolean isMoving) {
		boolean flag = !worldIn.hasNeighborSignal(pos);

		if(flag != state.getValue(ENABLED))
			worldIn.setBlock(pos, state.setValue(ENABLED, flag), 2 | 4);
	}

	@NotNull
	@Override
	public VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter worldIn, @NotNull BlockPos pos, @NotNull CollisionContext context) {
		return DOWN_SHAPE;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(ENABLED);
	}

	@Override
	protected boolean isPathfindable(BlockState state, PathComputationType type) {
		return false;
	}

	@Override
	public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
		return new ChuteBlockEntity(pos, state);
	}
}
