package org.violetmoon.quark.content.tweaks.module;

import java.util.List;

import org.violetmoon.quark.base.handler.QuarkSounds;
import org.violetmoon.quark.content.mobs.entity.Foxhound;
import org.violetmoon.quark.content.tweaks.ai.NuzzleGoal;
import org.violetmoon.quark.content.tweaks.ai.WantLoveGoal;
import org.violetmoon.zeta.config.Config;
import org.violetmoon.zeta.event.bus.PlayEvent;
import org.violetmoon.zeta.event.play.entity.ZEntityJoinLevel;
import org.violetmoon.zeta.event.play.entity.living.ZAnimalTame;
import org.violetmoon.zeta.event.play.entity.player.ZPlayerInteract;
import org.violetmoon.zeta.module.ZetaLoadModule;
import org.violetmoon.zeta.module.ZetaModule;
import org.violetmoon.zeta.util.MiscUtil;

import com.google.common.collect.Lists;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.GlowSquid;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.AbstractFish;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.animal.Ocelot;
import net.minecraft.world.entity.animal.Parrot;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.animal.Squid;
import net.minecraft.world.entity.animal.Turtle;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.monster.Strider;
import net.minecraft.world.entity.player.Player;

/**
 * @author WireSegal
 *         Created at 11:25 AM on 9/2/19.
 */
@ZetaLoadModule(category = "tweaks")
public class PatTheDogsModule extends ZetaModule {
	@Config(description = "How many ticks it takes for a dog to want affection after being pet/tamed; leave -1 to disable")
	public static int dogsWantLove = -1;
	@Config(description = "Whether you can pet all mobs")
	public static boolean petAllMobs = false;
	@Config(description = "If `petAllMobs` is set, these mobs still can't be pet")
	public static List<String> pettableDenylist = Lists.newArrayList("minecraft:ender_dragon", "minecraft:wither", "minecraft:armor_stand");
	@Config(description = "Even if `petAllMobs` is not set, these mobs can be pet")
	public static List<String> pettableAllowlist = Lists.newArrayList();

	@PlayEvent
	public void onWolfAppear(ZEntityJoinLevel event) {
		if(dogsWantLove > 0 && event.getEntity() instanceof Wolf wolf) {
			boolean alreadySetUp = wolf.goalSelector.getAvailableGoals().stream().anyMatch((goal) -> goal.getGoal() instanceof WantLoveGoal);

			if(!alreadySetUp) {
				MiscUtil.addGoalJustAfterLatestWithPriority(wolf.goalSelector, 4, new NuzzleGoal(wolf, 0.5F, 16, 2, SoundEvents.WOLF_WHINE));
				MiscUtil.addGoalJustAfterLatestWithPriority(wolf.goalSelector, 5, new WantLoveGoal(wolf, 0.2F));
			}
		}
	}

	@PlayEvent
	public void onInteract(ZPlayerInteract.EntityInteract event) {
		var player = event.getEntity();

		if(player.isDiscrete() && player.getMainHandItem().isEmpty()) {
			if(event.getTarget() instanceof Wolf wolf) {
				if(event.getHand() == InteractionHand.MAIN_HAND && WantLoveGoal.canPet(wolf)) {
					if(player.level() instanceof ServerLevel serverLevel) {
						var pos = wolf.position();
						serverLevel.sendParticles(ParticleTypes.HEART, pos.x, pos.y + 0.5, pos.z, 1, 0, 0, 0, 0.1);
						wolf.playSound(SoundEvents.WOLF_WHINE, 1F, 0.5F + (float) Math.random() * 0.5F);
					} else
						player.swing(InteractionHand.MAIN_HAND);

					WantLoveGoal.setPetTime(wolf);

					if(wolf instanceof Foxhound && !player.isInWater() && !player.hasEffect(MobEffects.FIRE_RESISTANCE)
							&& !player.getAbilities().invulnerable)
						player.setRemainingFireTicks(5);
				}

				event.setCanceled(true);
			} else if(event.getTarget() instanceof LivingEntity living &&
					(petAllMobs || living instanceof Player || pettableAllowlist.contains(living.getEncodeId())) &&
					!pettableDenylist.contains(living.getEncodeId())) {
				SoundEvent sound = null;
				float pitchCenter = 1f;
				if(living instanceof Axolotl) {
					sound = SoundEvents.AXOLOTL_SPLASH;
				} else if(living instanceof Cat || living instanceof Ocelot) {
					sound = SoundEvents.CAT_PURREOW;
				} else if(living instanceof Chicken) {
					sound = SoundEvents.CHICKEN_AMBIENT;
				} else if(living instanceof Cow) {
					sound = SoundEvents.COW_AMBIENT;
					pitchCenter = 1.2f;
				} else if(living instanceof AbstractHorse) {
					sound = SoundEvents.HORSE_AMBIENT;
				} else if(living instanceof AbstractFish) {
					sound = SoundEvents.FISH_SWIM;
				} else if(living instanceof Fox) {
					sound = SoundEvents.FOX_SLEEP;
				} else if(living instanceof Squid) {
					sound = (living instanceof GlowSquid) ? SoundEvents.GLOW_SQUID_SQUIRT : SoundEvents.SQUID_SQUIRT;
					pitchCenter = 1.2f;
				} else if(living instanceof Parrot) {
					sound = SoundEvents.PARROT_AMBIENT;
				} else if(living instanceof Pig) {
					sound = SoundEvents.PIG_AMBIENT;
				} else if(living instanceof Rabbit) {
					sound = SoundEvents.RABBIT_AMBIENT;
				} else if(living instanceof Sheep) {
					sound = SoundEvents.SHEEP_AMBIENT;
				} else if(living instanceof Strider) {
					sound = SoundEvents.STRIDER_HAPPY;
				} else if(living instanceof Turtle) {
					sound = SoundEvents.TURTLE_AMBIENT_LAND;
				} else if(living instanceof Player pettee) {
					var uuid = pettee.getStringUUID();
					sound = switch(uuid) {
					case "a2ce9382-2518-4752-87b2-c6a5c97f173e" -> // petra_the_kat
						QuarkSounds.PET_DEVICE;
					case "29a10dc6-a201-4993-80d8-c847212bc92b", // MacyMacerator
							"d30d8e38-6f93-4d96-968d-dd6ec5596941" -> // Falkory220
						QuarkSounds.PET_NEKO;
					case "d475af59-d73c-42be-90ed-f1a78f10d452" -> // DaniCherryJam
						QuarkSounds.PET_SLIME;
					case "458391f5-6303-4649-b416-e4c0d18f837a" -> // yrsegal
						QuarkSounds.PET_WIRE;
					default -> null;
					};
				}
				if(sound != null) {
					if(event.getHand() == InteractionHand.MAIN_HAND) {
						if(player.level() instanceof ServerLevel serverLevel) {
							var pos = living.getEyePosition();
							serverLevel.sendParticles(ParticleTypes.HEART, pos.x, pos.y + 0.5, pos.z, 1, 0, 0, 0, 0.1);

							living.playSound(sound, 1F, pitchCenter + (float) (Math.random() - 0.5) * 0.5F);
						} else
							player.swing(InteractionHand.MAIN_HAND);
					}

					event.setCanceled(true);
				}
			}
		}
	}

	@PlayEvent
	public void onTame(ZAnimalTame event) {
		if(event.getAnimal() instanceof Wolf wolf) {
			WantLoveGoal.setPetTime(wolf);
		}
	}

}
