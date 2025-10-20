package org.violetmoon.quark.content.building.block;

import com.google.common.collect.ImmutableList;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import net.neoforged.neoforge.common.util.TriState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.base.util.BlockPropertyUtil;
import org.violetmoon.quark.content.building.module.HedgesModule;
import org.violetmoon.quark.content.world.block.BlossomLeavesBlock;
import org.violetmoon.zeta.block.IZetaBlock;
import org.violetmoon.zeta.block.ZetaFenceBlock;
import org.violetmoon.zeta.module.ZetaModule;
import org.violetmoon.zeta.registry.CreativeTabManager;
import org.violetmoon.zeta.registry.IZetaBlockColorProvider;
import org.violetmoon.zeta.registry.RenderLayerRegistry;

public class HedgeBlock extends ZetaFenceBlock implements IZetaBlock, IZetaBlockColorProvider {

	private static final VoxelShape WOOD_SHAPE = box(6F, 0F, 6F, 10F, 15F, 10F);
	private static final VoxelShape HEDGE_CENTER_SHAPE = box(2F, 1F, 2F, 14F, 16F, 14F);
	private static final VoxelShape NORTH_SHAPE = box(2F, 1F, 0F, 14F, 16F, 2F);
	private static final VoxelShape SOUTH_SHAPE = box(2F, 1F, 14F, 14F, 16F, 15F);
	private static final VoxelShape EAST_SHAPE = box(14F, 1F, 2F, 16F, 16F, 14F);
	private static final VoxelShape WEST_SHAPE = box(0F, 1F, 2F, 2F, 16F, 14F);
	private static final VoxelShape EXTEND_SHAPE = box(2F, 0F, 2F, 14F, 1F, 14F);

	private final Object2IntMap<BlockState> hedgeStateToIndex = new Object2IntOpenHashMap<>();
	private final VoxelShape[] hedgeShapes;
	public final BlockState leafState;

	public static final BooleanProperty EXTEND = BooleanProperty.create("extend");

	public HedgeBlock(String regname, @Nullable ZetaModule module, Block fence, Block leaf) {
		super(regname, module, BlockPropertyUtil.copyPropertySafe(fence));

		this.leafState = leaf.defaultBlockState();

		registerDefaultState(defaultBlockState().setValue(EXTEND, false));
		this.hedgeShapes = cacheHedgeShapes(stateDefinition.getPossibleStates());

		if(module == null) //auto registration below this line
			return;

		CreativeTabManager.addNextToItem(CreativeModeTabs.NATURAL_BLOCKS, this, leaf, false);
		module.zeta().renderLayerRegistry.put(this, RenderLayerRegistry.Layer.CUTOUT);
	}

	@Deprecated(forRemoval = true) //for bin compat; requires early registry-name-retrieval of blocks, which isn't composable with other mods etc.
	public HedgeBlock(@Nullable ZetaModule module, Block fence, Block leaf) {
		this(legacyComputeRegistryName(leaf), module, fence, leaf);
	}

	@Deprecated(forRemoval = true)
	private static String legacyComputeRegistryName(Block leaf) {
		ResourceLocation leafRes = Quark.ZETA.registry.getRegistryName(leaf, BuiltInRegistries.BLOCK);
		if (leaf instanceof BlossomLeavesBlock)
			return leafRes.getPath().replaceAll("_blossom_leaves", "") + "_blossom_hedge";
		else
			return leafRes.getPath().replaceAll("_leaves", "_hedge");
	}

	public BlockState getLeaf() {
		return leafState;
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
		return hedgeShapes[getHedgeAABBIndex(state)];
	}

	private VoxelShape[] cacheHedgeShapes(ImmutableList<BlockState> possibleStates) {
		VoxelShape[] shapes = new VoxelShape[possibleStates.size()];

		for(int i = 0; i < shapes.length; i++) {
			BlockState state = possibleStates.get(i);
			int realIndex = getHedgeAABBIndex(state);

			VoxelShape finishedShape = Shapes.or(state.getValue(HedgeBlock.EXTEND) ? EXTEND_SHAPE : WOOD_SHAPE, HEDGE_CENTER_SHAPE);
			if(state.getValue(FenceBlock.NORTH))
				finishedShape = Shapes.or(finishedShape, NORTH_SHAPE);
			if(state.getValue(FenceBlock.SOUTH))
				finishedShape = Shapes.or(finishedShape, SOUTH_SHAPE);
			if(state.getValue(FenceBlock.EAST))
				finishedShape = Shapes.or(finishedShape, EAST_SHAPE);
			if(state.getValue(FenceBlock.WEST))
				finishedShape = Shapes.or(finishedShape, WEST_SHAPE);

			shapes[realIndex] = finishedShape;
		}

		return shapes;
	}

	protected int getHedgeAABBIndex(BlockState curr) {
		return hedgeStateToIndex.computeIntIfAbsent(curr, (state) -> {
			int i = 0;

			if(state.getValue(FenceBlock.NORTH))
				i |= 0b00001;
			if(state.getValue(FenceBlock.SOUTH))
				i |= 0b00010;
			if(state.getValue(FenceBlock.EAST))
				i |= 0b00100;
			if(state.getValue(FenceBlock.WEST))
				i |= 0b01000;
			if(state.getValue(EXTEND))
				i |= 0b10000;

			return i;
		});
	}

	@Override
	public boolean connectsTo(BlockState state, boolean isSideSolid, @NotNull Direction direction) {
		return state.is(HedgesModule.hedgesTag);
	}

	@Override
	public TriState canSustainPlantZeta(BlockState state, BlockGetter level, BlockPos soilPosition, Direction facing, BlockState plant) {
		return (facing == Direction.UP && !state.getValue(WATERLOGGED)) ? TriState.TRUE : TriState.FALSE;
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		BlockGetter iblockreader = context.getLevel();
		BlockPos blockpos = context.getClickedPos();
		BlockPos down = blockpos.below();
		BlockState downState = iblockreader.getBlockState(down);

		return super.getStateForPlacement(context)
				.setValue(EXTEND, downState.getBlock() instanceof HedgeBlock);
	}

	@NotNull
	@Override
	public BlockState updateShape(BlockState stateIn, @NotNull Direction facing, @NotNull BlockState facingState, @NotNull LevelAccessor worldIn, @NotNull BlockPos currentPos, @NotNull BlockPos facingPos) {
		if(stateIn.getValue(WATERLOGGED)) {
			worldIn.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(worldIn));
		}

		if(facing == Direction.DOWN)
			return stateIn.setValue(EXTEND, facingState.getBlock() instanceof HedgeBlock);

		return super.updateShape(stateIn, facing, facingState, worldIn, currentPos, facingPos);
	}

	@Override
	protected void createBlockStateDefinition(@NotNull StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(EXTEND);
	}

	@Override
	public @Nullable String getBlockColorProviderName() {
		return "hedge";
	}

	@Override
	public @Nullable String getItemColorProviderName() {
		return "hedge";
	}

}
