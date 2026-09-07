package org.violetmoon.quark.content.tweaks.module;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import org.violetmoon.quark.content.building.module.GrateModule;
import org.violetmoon.zeta.config.Config;
import org.violetmoon.zeta.event.bus.PlayEvent;
import org.violetmoon.zeta.event.play.entity.player.ZPlayerTick;
import org.violetmoon.zeta.module.ZetaLoadModule;
import org.violetmoon.zeta.module.ZetaModule;
import org.violetmoon.zeta.util.Hint;

@ZetaLoadModule(category = "tweaks")
public class CampfiresBoostElytraModule extends ZetaModule {

	@Config
	public double boostStrength = 0.5;
	@Config
	public double maxSpeed = 1;

	@Hint
	Item campfire = Items.CAMPFIRE;
	@Hint
	Item soul_campfire = Items.SOUL_CAMPFIRE;

	@PlayEvent
	public void onPlayerTick(ZPlayerTick.Start event) {
		Player player = event.getPlayer();

		if(player.isFallFlying()) {
			Vec3 motion = player.getDeltaMovement();
			if(motion.y() < maxSpeed) {
				BlockPos pos = player.blockPosition();
				Level world = player.level();

				int moves = 0;
				while((world.isEmptyBlock(pos) || world.getBlockState(pos).getBlock().equals(GrateModule.grate)) && world.isInWorldBounds(pos) && moves < 20) {
					pos = pos.below();
					moves++;
				}

				BlockState state = world.getBlockState(pos);
				Block block = state.getBlock();
				boolean isCampfire = state.is(BlockTags.CAMPFIRES);
				if(isCampfire && block instanceof CampfireBlock && state.getValue(CampfireBlock.LIT) && state.getValue(CampfireBlock.SIGNAL_FIRE)) {
					double force = boostStrength;
					if(moves > 16)
						force -= (force * (1.0 - ((double) moves - 16.0) / 4.0));

					if(block == Blocks.SOUL_CAMPFIRE)
						force *= -1.5; // Hey, maybe we consider a less violent multiplier?

					player.setDeltaMovement(motion.x(), Math.min(maxSpeed, motion.y() + force), motion.z());
				}
			}
		}
	}

}
