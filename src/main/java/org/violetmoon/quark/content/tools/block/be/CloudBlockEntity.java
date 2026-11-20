package org.violetmoon.quark.content.tools.block.be;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.violetmoon.quark.content.tools.module.BottledCloudModule;
import org.violetmoon.zeta.block.be.ZetaBlockEntity;

public class CloudBlockEntity extends ZetaBlockEntity {

	private static final String TAG_LIVE_TIME = "liveTime";
	public static final int LIFETIME = 200;

	public int liveTime = LIFETIME;

	public CloudBlockEntity(BlockPos pos, BlockState state) {
		super(BottledCloudModule.blockEntityType, pos, state);
	}

	public static void tick(Level level, BlockPos pos, BlockState state, CloudBlockEntity be) {
		if(be.liveTime > 0) {
			be.liveTime--;

			if(level.isClientSide && be.liveTime % 20 == 0)
				for(int i = 0; i < (7 - (LIFETIME - be.liveTime) / 30); i++)
					level.addParticle(BottledCloudModule.cloudParticle, be.worldPosition.getX() + level.random.nextDouble(), be.worldPosition.getY() + level.random.nextDouble(), be.worldPosition.getZ() + level.random.nextDouble(), 0, 0, 0);

			if (level.isClientSide && level.isRaining() && level.random.nextInt(4) == 0) {
				double size = (double) be.liveTime / LIFETIME * 10 / 16;
				double x = pos.getX() + 0.5 + (level.random.nextDouble() - 0.5) * size;
				double z = pos.getZ() + 0.5 + (level.random.nextDouble() - 0.5) * size;
				level.addParticle(ParticleTypes.FALLING_WATER, x,
				  pos.getY() + 0.5 - size / 2, z, 0, 0, 0);
			}
		} else {
			if(!level.isClientSide)
				level.removeBlock(pos, false);
		}
	}

	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public void writeSharedNBT(CompoundTag cmp, HolderLookup.Provider provider) {
		cmp.putInt(TAG_LIVE_TIME, liveTime);
	}

	@Override
	public void readSharedNBT(CompoundTag cmp, HolderLookup.Provider provider) {
		liveTime = cmp.getInt(TAG_LIVE_TIME);
	}
}
