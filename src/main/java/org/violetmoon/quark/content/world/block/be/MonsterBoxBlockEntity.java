package org.violetmoon.quark.content.world.block.be;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

import org.violetmoon.quark.base.handler.QuarkSounds;
import org.violetmoon.quark.content.world.module.MonsterBoxModule;
import org.violetmoon.zeta.block.be.ZetaBlockEntity;

import java.util.List;

public class MonsterBoxBlockEntity extends ZetaBlockEntity {

	private int breakProgress;

	public MonsterBoxBlockEntity(BlockPos pos, BlockState state) {
		super(MonsterBoxModule.blockEntityType, pos, state);
	}

	public static void tick(Level level, BlockPos pos, BlockState state, MonsterBoxBlockEntity be) {
		if(level.getDifficulty() == Difficulty.PEACEFUL)
			return;
		int x = pos.getX();
		int y = pos.getY();
		int z = pos.getZ();

		if(level.isClientSide)
			level.addParticle(be.breakProgress == 0 ? ParticleTypes.FLAME : ParticleTypes.LARGE_SMOKE, x + Math.random(), y + Math.random(), z + Math.random(), 0, 0, 0);

		boolean doBreak = be.breakProgress > 0;
		if(!doBreak) {
			List<? extends Player> players = level.players();
			for(Player p : players)
				if(p.distanceToSqr(x + 0.5, y + 0.5, z + 0.5) < MonsterBoxModule.activationRange * MonsterBoxModule.activationRange && !p.isSpectator()) {
					doBreak = true;
					break;
				}
		}

		if(doBreak) {
			if(be.breakProgress == 0)
				level.playSound(null, pos, QuarkSounds.BLOCK_MONSTER_BOX_GROWL, SoundSource.BLOCKS, 0.5F, 1F);

			be.breakProgress++;
			if(be.breakProgress > 40) {
				be.spawnMobs();

				level.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, pos, Block.getId(level.getBlockState(pos)));
				level.removeBlock(pos, false);
			}
		}
	}

	private void spawnMobs() {
		if(level instanceof ServerLevel serverLevel) {
			BlockPos pos = getBlockPos();

			LootTable loot = serverLevel.getServer().reloadableRegistries().getLootTable(ResourceKey.create(Registries.LOOT_TABLE, MonsterBoxModule.MONSTER_BOX_SPAWNS_LOOT_TABLE));
			LootParams.Builder builder = (new LootParams.Builder(serverLevel))
					.withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
					.withParameter(LootContextParams.BLOCK_STATE, getBlockState())
					.withParameter(LootContextParams.TOOL, ItemStack.EMPTY)
					.withParameter(LootContextParams.BLOCK_ENTITY, this);

			LootParams ctx = builder.create(LootContextParamSets.BLOCK);

			int mobCount = MonsterBoxModule.minMobCount + level.random.nextInt(Math.max(MonsterBoxModule.maxMobCount - MonsterBoxModule.minMobCount + 1, 1));

			for(int i = 0; i < mobCount; i++) {
				loot.getRandomItemsRaw(ctx, stack -> {
					Entity e = null;

					if(stack.getItem() instanceof SpawnEggItem egg) {
						EntityType<?> entitytype = egg.getType(stack);
						e = entitytype.spawn(serverLevel, stack, null, pos, MobSpawnType.SPAWNER, true, true);

						if(e != null) {
							double motionMultiplier = 0.4;
							e.setPos(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
							double mx = (level.random.nextFloat() - 0.5) * motionMultiplier;
							double my = (level.random.nextFloat() - 0.5) * motionMultiplier;
							double mz = (level.random.nextFloat() - 0.5) * motionMultiplier;
							e.setDeltaMovement(mx, my, mz);
							e.getPersistentData().putBoolean(MonsterBoxModule.TAG_MONSTER_BOX_SPAWNED, true);
						}
					}
				});
			}

			serverLevel.getLevel().gameEvent(null, GameEvent.ENTITY_PLACE, pos);
		}
	}

}
