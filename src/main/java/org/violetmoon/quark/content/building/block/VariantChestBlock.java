package org.violetmoon.quark.content.building.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import org.violetmoon.quark.content.building.block.be.VariantChestBlockEntity;
import org.violetmoon.quark.content.building.module.VariantChestsModule.IVariantChest;
import org.violetmoon.zeta.block.IZetaBlock;
import org.violetmoon.zeta.module.ZetaModule;
import org.violetmoon.zeta.util.BooleanSuppliers;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public class VariantChestBlock extends ChestBlock implements IZetaBlock, IVariantChest {

	private final @Nullable ZetaModule module;
	private BooleanSupplier enabledSupplier = BooleanSuppliers.TRUE;

	protected final String type;

	public VariantChestBlock(String prefix, String type, ZetaModule module, Supplier<BlockEntityType<? extends ChestBlockEntity>> supplier, Properties props) {
		super(props, supplier);
		this.module = module;
		this.type = type;

		if(module == null) //auto registration below this line
			return;

		String resloc = (prefix != null ? prefix + "_" : "") + type + "_chest";
		module.zeta().registry.registerBlock(this, resloc, true);
	}

	public VariantChestBlock(String type, ZetaModule module, Supplier<BlockEntityType<? extends ChestBlockEntity>> supplier, Properties props) {
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
	public VariantChestBlock setCondition(BooleanSupplier enabledSupplier) {
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
		return new VariantChestBlockEntity(pos, state);
	}

	@Override
	public String getTexturePath() {
		return type;
	}

}
