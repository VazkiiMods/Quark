package org.violetmoon.quark.content.tools.module;

import net.minecraft.Util;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.core.Position;
import net.minecraft.core.Registry;
import net.minecraft.core.dispenser.AbstractProjectileDispenseBehavior;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.animal.Parrot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.base.config.Config;
import org.violetmoon.quark.base.handler.QuarkSounds;
import org.violetmoon.quark.base.handler.advancement.QuarkAdvancementHandler;
import org.violetmoon.quark.base.handler.advancement.QuarkGenericTrigger;
import org.violetmoon.quark.content.tools.entity.ParrotEgg;
import org.violetmoon.quark.content.tools.item.ParrotEggItem;
import org.violetmoon.zeta.client.event.load.ZClientSetup;
import org.violetmoon.zeta.event.*;
import org.violetmoon.zeta.event.bus.LoadEvent;
import org.violetmoon.zeta.event.bus.PlayEvent;
import org.violetmoon.zeta.event.load.ZCommonSetup;
import org.violetmoon.zeta.event.load.ZConfigChanged;
import org.violetmoon.zeta.event.load.ZRegister;
import org.violetmoon.zeta.event.play.entity.living.ZLivingTick;
import org.violetmoon.zeta.event.play.entity.player.ZPlayerInteract;
import org.violetmoon.zeta.module.ZetaLoadModule;
import org.violetmoon.zeta.module.ZetaModule;
import org.violetmoon.zeta.util.Hint;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ZetaLoadModule(category = "tools")
public class ParrotEggsModule extends ZetaModule {

	private static final ResourceLocation KOTO = new ResourceLocation("quark", "textures/model/entity/variants/kotobirb.png");
	private static final String EGG_TIMER = "quark:parrot_egg_timer";

	private static final List<String> NAMES = List.of("red_blue", "blue", "green", "yellow_blue", "grey");

	public static EntityType<ParrotEgg> parrotEggType;

	public static TagKey<Item> feedTag;

	@Hint(key = "parrot_eggs")
	public static List<Item> parrotEggs;

	@Config(description = "The chance feeding a parrot will produce an egg")
	public static double chance = 0.05;
	@Config(description = "How long it takes to create an egg")
	public static int eggTime = 12000;
	@Config(name = "Enable Special Awesome Parrot")
	public static boolean enableKotobirb = true;

	private static boolean isEnabled;
	
	public static QuarkGenericTrigger throwParrotEggTrigger;

	@LoadEvent
	public final void register(ZRegister event) {
		parrotEggType = EntityType.Builder.<ParrotEgg>of(ParrotEgg::new, MobCategory.MISC)
				.sized(0.4F, 0.4F)
				.clientTrackingRange(64)
				.updateInterval(10) // update interval
				.setCustomClientFactory((spawnEntity, world) -> new ParrotEgg(parrotEggType, world))
				.build("parrot_egg");
		Quark.ZETA.registry.register(parrotEggType, "parrot_egg", Registry.ENTITY_TYPE_REGISTRY);

		parrotEggs = new ArrayList<>();
		for (int i = 0; i < ParrotEgg.VARIANTS; i++) {
			int variant = i;

			Item parrotEgg = new ParrotEggItem(NAMES.get(variant), variant, this);
			parrotEggs.add(parrotEgg);

			DispenserBlock.registerBehavior(parrotEgg, new AbstractProjectileDispenseBehavior() {
				@Nonnull
				@Override
				protected Projectile getProjectile(@Nonnull Level world, @Nonnull Position pos, @Nonnull ItemStack stack) {
					return Util.make(new ParrotEgg(world, pos.x(), pos.y(), pos.z()), (parrotEgg) -> {
						parrotEgg.setItem(stack);
						parrotEgg.setVariant(variant);
					});
				}
			});
		}
		
		throwParrotEggTrigger = QuarkAdvancementHandler.registerGenericTrigger("throw_parrot_egg");
	}

	@LoadEvent
	public final void setup(ZCommonSetup event) {
		feedTag = ItemTags.create(new ResourceLocation(Quark.MOD_ID, "parrot_feed"));
	}

	@LoadEvent
	public final void configChanged(ZConfigChanged event) {
		// Pass over to a static reference for easier computing the coremod hook
		isEnabled = this.enabled;
	}

	@LoadEvent
	public final void clientSetup(ZClientSetup event) {
		EntityRenderers.register(parrotEggType, ThrownItemRenderer::new);
	}

	@PlayEvent
	public void entityInteract(ZPlayerInteract.EntityInteract event) {
		Entity e = event.getTarget();
		Player player = event.getEntity();
		if (e instanceof Parrot parrot) {
			ItemStack stack = player.getMainHandItem();
			if (stack.isEmpty() || !stack.is(feedTag)) {
				stack = player.getOffhandItem();
			}

			if (!stack.isEmpty() && stack.is(feedTag)) {
				if (e.getPersistentData().getInt(EGG_TIMER) <= 0) {
					if (!parrot.isTame())
						return;

					event.setCanceled(true);
					if (parrot.level.isClientSide || event.getHand() == InteractionHand.OFF_HAND)
						return;

					if (!player.getAbilities().instabuild)
						stack.shrink(1);

					if (parrot.level instanceof ServerLevel ws) {
						ws.playSound(null, parrot.getX(), parrot.getY(), parrot.getZ(), SoundEvents.PARROT_EAT, SoundSource.NEUTRAL, 1.0F, 1.0F + (ws.random.nextFloat() - ws.random.nextFloat()) * 0.2F);

						if (ws.random.nextDouble() < chance) {
							parrot.getPersistentData().putInt(EGG_TIMER, eggTime);
							ws.sendParticles(ParticleTypes.HAPPY_VILLAGER, parrot.getX(), parrot.getY(), parrot.getZ(), 10, parrot.getBbWidth(), parrot.getBbHeight(), parrot.getBbWidth(), 0);
						} else
							ws.sendParticles(ParticleTypes.SMOKE, parrot.getX(), parrot.getY(), parrot.getZ(), 10, parrot.getBbWidth(), parrot.getBbHeight(), parrot.getBbWidth(), 0);
					}
				} else if (parrot.level instanceof ServerLevel ws) {
					ws.sendParticles(ParticleTypes.HEART, parrot.getX(), parrot.getY(), parrot.getZ(), 1, parrot.getBbWidth(), parrot.getBbHeight(), parrot.getBbWidth(), 0);
				}
			}
		}
	}

	@PlayEvent
	public void entityUpdate(ZLivingTick event) {
		Entity e = event.getEntity();
		if(e instanceof Parrot parrot) {
			int time = parrot.getPersistentData().getInt(EGG_TIMER);
			if(time > 0) {
				if(time == 1) {
					e.playSound(QuarkSounds.ENTITY_PARROT_EGG, 1.0F, (parrot.level.random.nextFloat() - parrot.level.random.nextFloat()) * 0.2F + 1.0F);
					e.spawnAtLocation(new ItemStack(parrotEggs.get(getResultingEggColor(parrot))), 0);
				}
				e.getPersistentData().putInt(EGG_TIMER, time - 1);
			}
		}
	}

	private int getResultingEggColor(Parrot parrot) {
		int color = parrot.getVariant();
		RandomSource rand = parrot.level.random;
		if(rand.nextBoolean())
			return color;
		return rand.nextInt(ParrotEgg.VARIANTS);
	}

	@ZetaLoadModule(clientReplacement = true)
	public static class Client extends ParrotEggsModule {
		@Nullable
		public static ResourceLocation getTextureForParrot(Parrot parrot) {
			if (!isEnabled || !enableKotobirb)
				return null;

			UUID uuid = parrot.getUUID();
			if (parrot.getVariant() == 4 && uuid.getLeastSignificantBits() % 20 == 0)
				return KOTO;

			return null;
		}
	}
}
