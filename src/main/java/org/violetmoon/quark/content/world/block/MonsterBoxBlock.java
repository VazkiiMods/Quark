package org.violetmoon.quark.content.world.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.pathfinder.PathComputationType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.violetmoon.quark.content.world.block.be.MonsterBoxBlockEntity;
import org.violetmoon.quark.content.world.module.MonsterBoxModule;
import org.violetmoon.zeta.block.ZetaBlock;
import org.violetmoon.zeta.module.ZetaModule;
import org.violetmoon.zeta.registry.RenderLayerRegistry;

public class MonsterBoxBlock extends ZetaBlock implements EntityBlock {

	public MonsterBoxBlock(@Nullable ZetaModule module) {
		super("monster_box", module,
				Block.Properties.of()
						.mapColor(MapColor.METAL)
						.instrument(NoteBlockInstrument.IRON_XYLOPHONE)
						.strength(25F)
						.sound(SoundType.METAL)
						.noOcclusion());

		if(module == null) //auto registration below this line
			return;
		setCreativeTab(CreativeModeTabs.SPAWN_EGGS, Blocks.SPAWNER, false);
		module.zeta.renderLayerRegistry.put(this, RenderLayerRegistry.Layer.CUTOUT);
	}

	@Override
	protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
		return false;
	}

	@Override
	public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
		return new MonsterBoxBlockEntity(pos, state);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level world, @NotNull BlockState state, @NotNull BlockEntityType<T> type) {
		return createTickerHelper(type, MonsterBoxModule.blockEntityType, MonsterBoxBlockEntity::tick);
	}

}
