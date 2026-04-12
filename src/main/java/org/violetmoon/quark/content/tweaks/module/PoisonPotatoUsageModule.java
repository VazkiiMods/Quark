package org.violetmoon.quark.content.tweaks.module;

import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.frog.Tadpole;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

import org.violetmoon.zeta.advancement.ManualTrigger;
import org.violetmoon.zeta.config.Config;
import org.violetmoon.zeta.event.bus.LoadEvent;
import org.violetmoon.zeta.event.bus.PlayEvent;
import org.violetmoon.zeta.event.load.ZRegister;
import org.violetmoon.zeta.event.play.entity.ZEntityInteract;
import org.violetmoon.zeta.event.play.entity.living.ZLivingTick;
import org.violetmoon.zeta.module.ZetaLoadModule;
import org.violetmoon.zeta.module.ZetaModule;
import org.violetmoon.zeta.util.Hint;

@ZetaLoadModule(category = "tweaks")
public class PoisonPotatoUsageModule extends ZetaModule {

	private static final String TAG_POISONED = "quark:poison_potato_applied";

	@Config
	public static double chance = 0.1;
	@Config
	public static boolean poisonEffect = true;

	@Hint
	Item poison_potato = Items.POISONOUS_POTATO;

	public static ManualTrigger poisonBabyTrigger;

	@LoadEvent
	public final void register(ZRegister event) {
		poisonBabyTrigger = event.getAdvancementModifierRegistry().registerManualTrigger("poison_baby");
	}

	@PlayEvent
	public void onInteract(ZEntityInteract event) {
		if(event.getItemStack().getItem() == Items.POISONOUS_POTATO && canPoison(event.getTarget())) {
			LivingEntity entity = (LivingEntity) event.getTarget();

			if(!event.getLevel().isClientSide) {
				Vec3 pos = entity.position();
				if(event.getEntity().isCreative() || entity.level().random.nextDouble() < chance) {
					entity.playSound(SoundEvents.GENERIC_EAT, 0.5f, 0.25f);
					entity.level().addParticle(ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, 0xffffff), pos.x, pos.y, pos.z, 0.2, 0.8, 0);
					poisonEntity(entity);

					if(event.getEntity() instanceof ServerPlayer sp)
						poisonBabyTrigger.trigger(sp);

					if(poisonEffect)
						entity.addEffect(new MobEffectInstance(MobEffects.POISON, 80));
				} else {
					entity.playSound(SoundEvents.GENERIC_EAT, 0.5f, 0.5f + entity.level().random.nextFloat() / 2);
					entity.level().addParticle(ParticleTypes.SMOKE, pos.x, pos.y, pos.z, 0, 0.1, 0);
				}

				if(!event.getEntity().getAbilities().instabuild)
					event.getItemStack().shrink(1);

			} else
				event.getEntity().swing(event.getHand());

		}
	}

	private boolean canPoison(Entity entity) {
		return !isEntityPoisoned(entity) &&
				(entity instanceof AgeableMob ageable && ageable.isBaby()
						|| entity instanceof Tadpole);
	}

	@PlayEvent
	public void onEntityUpdate(ZLivingTick event) {
		if(event.getEntity() instanceof Animal animal) {
			if(animal.isBaby() && isEntityPoisoned(animal))
				animal.setAge(-24000);
		}

		else if(event.getEntity() instanceof Tadpole tadpole) {
			if(isEntityPoisoned(tadpole))
				tadpole.setAge(0);
		}
	}

	private boolean isEntityPoisoned(Entity e) {
		return e.getPersistentData().getBoolean(TAG_POISONED);
	}

	private void poisonEntity(Entity e) {
		e.getPersistentData().putBoolean(TAG_POISONED, true);
	}

}
