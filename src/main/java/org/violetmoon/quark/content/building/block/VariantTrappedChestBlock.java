package org.violetmoon.quark.content.building.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import org.violetmoon.quark.content.building.block.be.VariantTrappedChestBlockEntity;
import org.violetmoon.quark.content.building.module.VariantChestsModule.IVariantChest;
import org.violetmoon.zeta.block.IZetaBlock;
import org.violetmoon.zeta.module.ZetaModule;
import org.violetmoon.zeta.util.BooleanSuppliers;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public class VariantTrappedChestBlock extends ChestBlock implements IZetaBlock, IVariantChest {

	@Nullable
	private final ZetaModule module;
	private BooleanSupplier enabledSupplier = BooleanSuppliers.TRUE;

	protected final String type;

	public VariantTrappedChestBlock(String prefix, String type, @Nullable ZetaModule module, Supplier<BlockEntityType<? extends ChestBlockEntity>> supplier, Properties props) {
		super(props, supplier);
		this.module = module;
		this.type = type;

		if(module == null) //auto registration below this line
			return;

		String resloc = "trapped_" + (prefix != null ? prefix + "_" : "") + type + "_chest";
		module.zeta().registry.registerBlock(this, resloc, true);
	}

	public VariantTrappedChestBlock(String type, ZetaModule module, Supplier<BlockEntityType<? extends ChestBlockEntity>> supplier, Properties props) {
		this(null, type, module, supplier, props);
	}

	@Override
	public int getFlammabilityZeta(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
		return 0;
	}

	@Override
	public boolean isFlammableZeta(BlockState state, BlockGetter world, BlockPos pos, Direction face) {
		return false;
	}

	@Override
	public VariantTrappedChestBlock setCondition(BooleanSupplier enabledSupplier) {
		this.enabledSupplier = enabledSupplier;
		return this;
	}

	@Override
	public boolean doesConditionApply() {
		return enabledSupplier.getAsBoolean();
	}

	@Nullable
	@Override
	public ZetaModule getModule() {
		return module;
	}

	@Override
	public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
		return new VariantTrappedChestBlockEntity(pos, state);
	}

	@Override
	public String getTexturePath() {
		return type;
	}

	// VANILLA TrappedChestBlock copy

	@NotNull
	@Override
	protected Stat<ResourceLocation> getOpenChestStat() {
		return Stats.CUSTOM.get(Stats.TRIGGER_TRAPPED_CHEST);
	}

	@Override
	public boolean isSignalSource(@NotNull BlockState state) {
		return true;
	}

	@Override
	public int getSignal(@NotNull BlockState state, @NotNull BlockGetter world, @NotNull BlockPos pos, @NotNull Direction side) {
		return Mth.clamp(ChestBlockEntity.getOpenCount(world, pos), 0, 15);
	}

	@Override
	public int getDirectSignal(@NotNull BlockState state, @NotNull BlockGetter world, @NotNull BlockPos pos, @NotNull Direction side) {
		return side == Direction.UP ? state.getSignal(world, pos, side) : 0;
	}

}
