package org.violetmoon.quark.content.building.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import org.violetmoon.zeta.block.ZetaBlock;
import org.violetmoon.zeta.module.ZetaModule;
import org.violetmoon.zeta.registry.IZetaBlockColorProvider;
import org.violetmoon.zeta.registry.RenderLayerRegistry;

public class LeafCarpetBlock extends ZetaBlock implements IZetaBlockColorProvider {

	private static final VoxelShape SHAPE = box(0, 0, 0, 16, 1, 16);

	public final BlockState baseState;

	public LeafCarpetBlock(String name, Block base, @Nullable ZetaModule module) {
		super(name, module,
				Block.Properties.of()
						.mapColor(base.defaultBlockState().mapColor)
						.noCollission()
						.strength(0F)
						.sound(SoundType.GRASS)
						.noOcclusion()
						.ignitedByLava());

		baseState = base.defaultBlockState();

		if(module == null) //auto registration below this line
			return;
		module.zeta().renderLayerRegistry.put(this, RenderLayerRegistry.Layer.CUTOUT_MIPPED);
		setCreativeTab(CreativeModeTabs.NATURAL_BLOCKS, base, false);
	}

	public BlockState getBaseState() {
		return baseState;
	}

	@Override
	public boolean canBeReplaced(@NotNull BlockState state, @NotNull BlockPlaceContext useContext) {
		return useContext.getItemInHand().isEmpty() || useContext.getItemInHand().getItem() != this.asItem();
	}

	@NotNull
	@Override
	public VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter world, @NotNull BlockPos pos, @NotNull CollisionContext context) {
		return SHAPE;
	}

	@NotNull
	@Override
	public VoxelShape getCollisionShape(@NotNull BlockState state, @NotNull BlockGetter world, @NotNull BlockPos pos, @NotNull CollisionContext context) {
		return Shapes.empty();
	}

	@NotNull
	@Override

	public BlockState updateShape(BlockState state, @NotNull Direction facing, @NotNull BlockState facingState, @NotNull LevelAccessor world, @NotNull BlockPos pos, @NotNull BlockPos facingPos) {
		return !state.canSurvive(world, pos) ? Blocks.AIR.defaultBlockState() : super.updateShape(state, facing, facingState, world, pos, facingPos);
	}

	@Override
	public boolean canSurvive(@NotNull BlockState state, LevelReader world, BlockPos pos) {
		return !world.isEmptyBlock(pos.below());
	}

	@Override
	public @Nullable String getBlockColorProviderName() {
		return "leaf_carpet";
	}

	@Override
	public @Nullable String getItemColorProviderName() {
		return "leaf_carpet";
	}

}
