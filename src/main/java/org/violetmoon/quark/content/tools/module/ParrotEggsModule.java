package org.violetmoon.quark.content.tools.module;

import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.core.dispenser.ProjectileDispenseBehavior;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.animal.Parrot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.DispenserBlock;
import org.jetbrains.annotations.Nullable;
import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.base.handler.QuarkSounds;
import org.violetmoon.quark.content.tools.entity.ParrotEgg;
import org.violetmoon.quark.content.tools.item.ParrotEggItem;
import org.violetmoon.zeta.advancement.ManualTrigger;
import org.violetmoon.zeta.client.event.load.ZClientSetup;
import org.violetmoon.zeta.config.Config;
import org.violetmoon.zeta.event.bus.LoadEvent;
import org.violetmoon.zeta.event.bus.PlayEvent;
import org.violetmoon.zeta.event.load.ZCommonSetup;
import org.violetmoon.zeta.event.load.ZConfigChanged;
import org.violetmoon.zeta.event.load.ZRegister;
import org.violetmoon.zeta.event.play.entity.living.ZLivingTick;
import org.violetmoon.zeta.event.play.entity.player.ZPlayerInteract;
import org.violetmoon.zeta.module.ZetaLoadModule;
import org.violetmoon.zeta.module.ZetaModule;
import org.violetmoon.zeta.registry.CreativeTabManager;
import org.violetmoon.zeta.util.Hint;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ZetaLoadModule(category = "tools")
public class ParrotEggsModule extends ZetaModule {

	private static final ResourceLocation KOTO = Quark.asResource("textures/model/entity/variants/kotobirb.png");
	private static final String EGG_TIMER = "quark:parrot_egg_timer";

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

	public static ManualTrigger throwParrotEggTrigger;

	@LoadEvent
	public final void register(ZRegister event) {
		parrotEggType = EntityType.Builder.<ParrotEgg>of(ParrotEgg::new, MobCategory.MISC)
				.sized(0.4F, 0.4F)
				.clientTrackingRange(64)
				.updateInterval(10) // update interval
				.build("parrot_egg");
		Quark.ZETA.registry.register(parrotEggType, "parrot_egg", Registries.ENTITY_TYPE);

		CreativeTabManager.daisyChain();
		parrotEggs = new ArrayList<>();
		for(Parrot.Variant variant : Parrot.Variant.values()) {
			Item parrotEgg = new ParrotEggItem(variant, this).setCreativeTab(CreativeModeTabs.INGREDIENTS, Items.EGG, false);
			parrotEggs.add(parrotEgg);
			DispenserBlock.registerBehavior(parrotEgg, new ProjectileDispenseBehavior(parrotEgg)); // TODO: Fix variant logic
		}
		CreativeTabManager.endDaisyChain();

		throwParrotEggTrigger = event.getAdvancementModifierRegistry().registerManualTrigger("throw_parrot_egg");
	}

	@LoadEvent
	public final void setup(ZCommonSetup event) {
		feedTag = Quark.asTagKey(Registries.ITEM,"parrot_feed");
	}

	@LoadEvent
	public final void configChanged(ZConfigChanged event) {
		// Pass over to a static reference for easier computing the coremod hook
		isEnabled = isEnabled();
	}

	@PlayEvent
	public void entityInteract(ZPlayerInteract.EntityInteract event) {
		Entity e = event.getTarget();
		Player player = event.getEntity();
		if(e instanceof Parrot parrot) {
			ItemStack stack = player.getMainHandItem();
			if(stack.isEmpty() || !stack.is(feedTag)) {
				stack = player.getOffhandItem();
			}

			if(!stack.isEmpty() && stack.is(feedTag)) {
				if(e.getPersistentData().getInt(EGG_TIMER) <= 0) {
					if(!parrot.isTame())
						return;

					event.setCanceled(true);
					if(parrot.level().isClientSide || event.getHand() == InteractionHand.OFF_HAND)
						return;

					if(!player.getAbilities().instabuild)
						stack.shrink(1);

					if(parrot.level() instanceof ServerLevel ws) {
						ws.playSound(null, parrot.getX(), parrot.getY(), parrot.getZ(), SoundEvents.PARROT_EAT, SoundSource.NEUTRAL, 1.0F, 1.0F + (ws.random.nextFloat() - ws.random.nextFloat()) * 0.2F);

						if(ws.random.nextDouble() < chance) {
							parrot.getPersistentData().putInt(EGG_TIMER, eggTime);
							ws.sendParticles(ParticleTypes.HAPPY_VILLAGER, parrot.getX(), parrot.getY(), parrot.getZ(), 10, parrot.getBbWidth(), parrot.getBbHeight(), parrot.getBbWidth(), 0);
						} else
							ws.sendParticles(ParticleTypes.SMOKE, parrot.getX(), parrot.getY(), parrot.getZ(), 10, parrot.getBbWidth(), parrot.getBbHeight(), parrot.getBbWidth(), 0);
					}
				} else if(parrot.level() instanceof ServerLevel ws) {
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
					e.playSound(QuarkSounds.ENTITY_PARROT_EGG, 1.0F, (parrot.level().random.nextFloat() - parrot.level().random.nextFloat()) * 0.2F + 1.0F);
					e.spawnAtLocation(new ItemStack(parrotEggs.get(getResultingEggColor(parrot).getId())), 0);
				}
				e.getPersistentData().putInt(EGG_TIMER, time - 1);
			}
		}
	}

	private Parrot.Variant getResultingEggColor(Parrot parrot) {
		Parrot.Variant originalVariant = parrot.getVariant();
		RandomSource rand = parrot.level().random;
		if(rand.nextBoolean())
			return originalVariant;
		// mutation?
		return Parrot.Variant.byId(rand.nextInt(Parrot.Variant.values().length));
	}

	@ZetaLoadModule(clientReplacement = true)
	public static class Client extends ParrotEggsModule {

		@LoadEvent
		public final void clientSetup(ZClientSetup event) {
			EntityRenderers.register(parrotEggType, ThrownItemRenderer::new);
		}

		@Nullable
		public static ResourceLocation getTextureForParrot(Parrot parrot) {
			if(!isEnabled || !enableKotobirb)
				return null;

			UUID uuid = parrot.getUUID();
			if(parrot.getVariant().getId() == 4 && uuid.getLeastSignificantBits() % 20 == 0)
				return KOTO;

			return null;
		}
	}
}
