package org.violetmoon.quark.content.automation.block.be;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import org.joml.Vector3f;

import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.content.automation.block.EnderWatcherBlock;
import org.violetmoon.quark.content.automation.module.EnderWatcherModule;
import org.violetmoon.zeta.block.be.ZetaBlockEntity;

import java.util.List;

public class EnderWatcherBlockEntity extends ZetaBlockEntity {

	public EnderWatcherBlockEntity(BlockPos pos, BlockState state) {
		super(EnderWatcherModule.blockEntityType, pos, state);
	}

	public static void tick(Level level, BlockPos pos, BlockState state, EnderWatcherBlockEntity be) {
		boolean wasLooking = state.getValue(EnderWatcherBlock.WATCHED);
		int currWatch = state.getValue(EnderWatcherBlock.POWER);
		int range = 80;

		int newWatch = 0;
		List<Player> players = level.getEntitiesOfClass(Player.class, new AABB(be.worldPosition.offset(-range, -range, -range).getCenter(), be.worldPosition.offset(range, range, range).getCenter()));

		EnderMan fakeEnderman = new EnderMan(EntityType.ENDERMAN, level);
		fakeEnderman.setPos(pos.getX() + 0.5, pos.getY() + 0.5 - fakeEnderman.getEyeHeight(), pos.getZ() + 0.5);

		boolean looking = false;
		for(Player player : players) {
			ItemStack helm = player.getItemBySlot(EquipmentSlot.HEAD);
			fakeEnderman.lookAt(player, 180, 180);
			if(!helm.isEmpty() && Quark.ZETA.itemExtensions.get(helm).isEnderMaskZeta(helm, player, fakeEnderman))
				continue;

			HitResult result = Quark.ZETA.raytracingUtil.rayTrace(player, level, player, Block.OUTLINE, Fluid.NONE, 64);
			if(result != null && result.getType() == HitResult.Type.BLOCK && ((BlockHitResult) result).getBlockPos().equals(be.worldPosition)) {
				looking = true;

				Vec3 vec = result.getLocation();
				Direction dir = ((BlockHitResult) result).getDirection();
				double x = Math.abs(vec.x - be.worldPosition.getX() - 0.5) * (1 - Math.abs(dir.getStepX()));
				double y = Math.abs(vec.y - be.worldPosition.getY() - 0.5) * (1 - Math.abs(dir.getStepY()));
				double z = Math.abs(vec.z - be.worldPosition.getZ() - 0.5) * (1 - Math.abs(dir.getStepZ()));

				// 0.7071067811865476 being the hypotenuse of an isosceles triangle with cathetus of length 0.5
				double fract = 1 - (Math.sqrt(x * x + y * y + z * z) / 0.7071067811865476);
				int playerWatch = (int) Math.ceil(fract * 15);

				if(playerWatch == 15 && player instanceof ServerPlayer sp)
					EnderWatcherModule.watcherCenterTrigger.trigger(sp);

				newWatch = Math.max(newWatch, playerWatch);
			}
		}

		if(!level.isClientSide && (looking != wasLooking || currWatch != newWatch))
			level.setBlock(be.worldPosition, level.getBlockState(be.worldPosition).setValue(EnderWatcherBlock.WATCHED, looking).setValue(EnderWatcherBlock.POWER, newWatch), 1 | 2);

		if(looking) {
			double x = be.worldPosition.getX() - 0.1 + Math.random() * 1.2;
			double y = be.worldPosition.getY() - 0.1 + Math.random() * 1.2;
			double z = be.worldPosition.getZ() - 0.1 + Math.random() * 1.2;

			level.addParticle(new DustParticleOptions(new Vector3f(1.0F, 0.0F, 0.0F), 1.0F), x, y, z, 0.0D, 0.0D, 0.0D);
		}
	}

}
