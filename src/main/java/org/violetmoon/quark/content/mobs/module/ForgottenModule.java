package org.violetmoon.quark.content.mobs.module;

import com.google.common.collect.ImmutableSet;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.base.QuarkClient;
import org.violetmoon.quark.base.client.handler.ModelHandler;
import org.violetmoon.quark.content.mobs.client.render.entity.ForgottenRenderer;
import org.violetmoon.quark.content.mobs.entity.Forgotten;
import org.violetmoon.quark.content.mobs.item.ForgottenHatItem;
import org.violetmoon.zeta.advancement.modifier.MonsterHunterModifier;
import org.violetmoon.zeta.client.HumanoidArmorModelGetter;
import org.violetmoon.zeta.client.event.load.ZClientSetup;
import org.violetmoon.zeta.client.event.load.ZRegisterClientExtension;
import org.violetmoon.zeta.client.extensions.IZetaClientItemExtensions;
import org.violetmoon.zeta.config.Config;
import org.violetmoon.zeta.event.bus.LoadEvent;
import org.violetmoon.zeta.event.bus.PlayEvent;
import org.violetmoon.zeta.event.bus.ZResult;
import org.violetmoon.zeta.event.load.ZEntityAttributeCreation;
import org.violetmoon.zeta.event.load.ZRegister;
import org.violetmoon.zeta.event.play.entity.living.ZMobSpawnEvent;
import org.violetmoon.zeta.module.ZetaLoadModule;
import org.violetmoon.zeta.module.ZetaModule;
import org.violetmoon.zeta.util.BooleanSuppliers;
import org.violetmoon.zeta.util.Hint;

@ZetaLoadModule(category = "mobs")
public class ForgottenModule extends ZetaModule {

	public static EntityType<Forgotten> forgottenType;

	@Hint
	public static Item forgotten_hat;

	@Config(description = "This is the probability of a Skeleton that spawns under the height threshold being replaced with a Forgotten.")
	public double forgottenSpawnRate = 0.05;

	@Config
	public int maxHeightForSpawn = 0;

	@LoadEvent
	public final void register(ZRegister event) {
		forgotten_hat = new ForgottenHatItem(this);

		forgottenType = EntityType.Builder.of(Forgotten::new, MobCategory.MONSTER)
				.sized(0.7F, 2.4F)
				.clientTrackingRange(8)
				.build("forgotten");

		Quark.ZETA.registry.register(forgottenType, "forgotten", Registries.ENTITY_TYPE);
		Quark.ZETA.entitySpawn.addEgg(forgottenType, 0x969487, 0x3a3330, this, BooleanSuppliers.TRUE);

		event.getAdvancementModifierRegistry().addModifier(new MonsterHunterModifier(this, ImmutableSet.of(forgottenType)));
	}

	@LoadEvent
	public final void entityAttrs(ZEntityAttributeCreation e) {
		e.put(forgottenType, Forgotten.registerAttributes().build());
	}

	@PlayEvent
	public void onSkeletonSpawn(ZMobSpawnEvent.CheckSpawn.Lowest event) {
		if (event.getSpawnType() == MobSpawnType.SPAWNER) return;

		LivingEntity entity = event.getEntity();
		ZResult result = event.getResult() ? ZResult.ALLOW : ZResult.DENY;
		ServerLevelAccessor world = event.getLevel();

		if(entity.getType() == EntityType.SKELETON && entity instanceof Mob mob && result != ZResult.DENY && entity.getY() < maxHeightForSpawn && world.getRandom().nextDouble() < forgottenSpawnRate) {
			if(result == ZResult.ALLOW || (mob.checkSpawnRules(world, event.getSpawnType()) && mob.checkSpawnObstruction(world))) {
				Forgotten forgotten = new Forgotten(forgottenType, entity.level());
				Vec3 epos = entity.position();

				forgotten.absMoveTo(epos.x, epos.y, epos.z, entity.getYRot(), entity.getXRot());
				forgotten.prepareEquipment(event.getLevel().registryAccess(), event.getLevel().getCurrentDifficultyAt(entity.getOnPos()));

				BlockPos pos = BlockPos.containing(event.getX(), event.getY(), event.getZ());

				FinalizeSpawnEvent newEvent = new FinalizeSpawnEvent(forgotten, world, event.getX(), event.getY(), event.getZ(), world.getCurrentDifficultyAt(pos), event.getSpawnType(), null, event.getSpawner());
				NeoForge.EVENT_BUS.post(newEvent);

				if(!newEvent.isCanceled()) {
					world.addFreshEntity(forgotten);
					event.setResult(ZResult.DENY);
				}
			}
		}
	}

	@ZetaLoadModule(clientReplacement = true)
	public static class Client extends ForgottenModule {

		@LoadEvent
		public final void clientSetup(ZClientSetup event) {
			event.enqueueWork(() -> {
				EntityRenderers.register(forgottenType, ForgottenRenderer::new);
			});
		}

		@LoadEvent
		public void setItemExtensions(ZRegisterClientExtension event) {
			event.registerItem(new IZetaClientItemExtensions() {
				@Override
				public HumanoidArmorModelGetter getHumanoidArmorModel() {
					return (living, stack, slot, original) -> ModelHandler.armorModel(ModelHandler.forgotten_hat, slot);
				}
			}, forgotten_hat);
		}
	}
}
