package org.violetmoon.quark.content.tweaks.module;

import net.minecraft.nbt.NbtOps;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.LevelEvent;

import net.neoforged.neoforge.event.EventHooks;
import org.violetmoon.zeta.event.bus.PlayEvent;
import org.violetmoon.zeta.event.play.entity.living.ZLivingConversion;
import org.violetmoon.zeta.module.ZetaLoadModule;
import org.violetmoon.zeta.module.ZetaModule;

@ZetaLoadModule(category = "tweaks")
public class ZombieVillagersOnNormalModule extends ZetaModule {
	@PlayEvent
	public void onConversion(ZLivingConversion.Pre event) {
		if(event.getEntity().getType() == EntityType.VILLAGER && event.getOutcome() == EntityType.ZOMBIE_VILLAGER) {
			Villager villager = (Villager) event.getEntity();
			Level level = villager.level();

			if(level instanceof ServerLevelAccessor serverLevel) {
				ZombieVillager zombievillager = villager.convertTo(EntityType.ZOMBIE_VILLAGER, false);

				if (zombievillager == null) return;

				zombievillager.finalizeSpawn(serverLevel, level.getCurrentDifficultyAt(zombievillager.blockPosition()), MobSpawnType.CONVERSION, new Zombie.ZombieGroupData(false, true));
				zombievillager.setVillagerData(villager.getVillagerData());
				zombievillager.setGossips(villager.getGossips().store(NbtOps.INSTANCE));
				zombievillager.setTradeOffers(villager.getOffers().copy());
				zombievillager.setVillagerXp(villager.getVillagerXp());

				EventHooks.onLivingConvert(villager, zombievillager);
				level.levelEvent(null, LevelEvent.SOUND_ZOMBIE_INFECTED, villager.blockPosition(), 0);

				event.setCanceled(true);
			}
		}
	}
}
