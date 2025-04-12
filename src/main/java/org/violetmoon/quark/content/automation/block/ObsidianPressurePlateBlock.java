package org.violetmoon.quark.content.automation.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.AABB;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import org.violetmoon.zeta.block.ZetaPressurePlateBlock;
import org.violetmoon.zeta.module.ZetaModule;

import java.util.List;

/**
 * @author WireSegal
 *         Created at 9:47 PM on 10/8/19.
 */
public class ObsidianPressurePlateBlock extends ZetaPressurePlateBlock {
	public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

	public ObsidianPressurePlateBlock(String regname, @Nullable ZetaModule module, Properties properties) {
		super(regname, module, BlockSetType.STONE, properties);
		this.registerDefaultState(defaultBlockState().setValue(POWERED, false));
	}

	@Override
	protected int getSignalStrength(@NotNull Level worldIn, @NotNull BlockPos pos) {
		AABB bounds = TOUCH_AABB.move(pos);
		List<? extends Entity> entities = worldIn.getEntitiesOfClass(Player.class, bounds);

		if(!entities.isEmpty()) {
			for(Entity entity : entities) {
				if(!entity.isIgnoringBlockTriggers()) {
					return 15;
				}
			}
		}

		return 0;
	}
}
