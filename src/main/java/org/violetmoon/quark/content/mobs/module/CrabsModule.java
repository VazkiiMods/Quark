package org.violetmoon.quark.content.mobs.module;

import com.google.common.collect.ImmutableSet;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.entity.npc.VillagerTrades.ItemListing;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.material.Fluids;
import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.base.handler.QuarkSounds;
import org.violetmoon.quark.content.mobs.client.render.entity.CrabRenderer;
import org.violetmoon.quark.content.mobs.entity.Crab;
import org.violetmoon.zeta.advancement.modifier.BalancedDietModifier;
import org.violetmoon.zeta.advancement.modifier.FuriousCocktailModifier;
import org.violetmoon.zeta.advancement.modifier.TwoByTwoModifier;
import org.violetmoon.zeta.client.event.load.ZClientSetup;
import org.violetmoon.zeta.config.Config;
import org.violetmoon.zeta.config.type.CompoundBiomeConfig;
import org.violetmoon.zeta.config.type.EntitySpawnConfig;
import org.violetmoon.zeta.event.bus.LoadEvent;
import org.violetmoon.zeta.event.bus.PlayEvent;
import org.violetmoon.zeta.event.load.ZCommonSetup;
import org.violetmoon.zeta.event.load.ZEntityAttributeCreation;
import org.violetmoon.zeta.event.load.ZRegister;
import org.violetmoon.zeta.event.play.loading.ZVillagerTrades;
import org.violetmoon.zeta.item.ZetaItem;
import org.violetmoon.zeta.item.ZetaMobBucketItem;
import org.violetmoon.zeta.module.ZetaLoadModule;
import org.violetmoon.zeta.module.ZetaModule;
import org.violetmoon.zeta.registry.CreativeTabManager;
import org.violetmoon.zeta.util.Hint;
import org.violetmoon.zeta.util.ZetaEffect;

import java.util.List;
import java.util.Set;

/**
 * @author WireSegal
 *         Created at 7:28 PM on 9/22/19.
 */
@ZetaLoadModule(category = "mobs")
public class CrabsModule extends ZetaModule {

	public static EntityType<Crab> crabType;

	@Config
	public static EntitySpawnConfig spawnConfig = new EntitySpawnConfig(5, 1, 3, CompoundBiomeConfig.fromBiomeTags(false, BiomeTags.IS_BEACH));

	public static TagKey<Block> crabSpawnableTag;
	public static MobEffect resilience;

	@Config(flag = "crab_brewing")
	public static boolean enableBrewing = true;

	@Config(
		description = "Whether Resilience should be required for 'How Did We Get Here?' and (if brewing is enabled) 'A Furious Cocktail'.\n" +
				"Keep this on when brewing is disabled if your pack adds an alternative source for the effect."
	)
	public static boolean resilienceRequiredForAllEffects = true;
	
	@Config
	public static boolean addCrabLegToFishermanTrades = true;

	@Hint(key = "crab_info")
	public static Item crab_leg;
	@Hint(key = "crab_info")
	public static Item crab_shell;
	@Hint(key = "crab_info")
	public static Item crab_bucket;

	@LoadEvent
	public final void register(ZRegister event) {
		CreativeTabManager.daisyChain();
		crab_leg = new ZetaItem("crab_leg", this, new Item.Properties()
				.food(new FoodProperties.Builder()
						.nutrition(1)
						.saturationModifier(0.3F)
						.build()))
				.setCreativeTab(CreativeModeTabs.FOOD_AND_DRINKS, Items.PUFFERFISH, false);

		Item cookedCrabLeg = new ZetaItem("cooked_crab_leg", this, new Item.Properties()
				.food(new FoodProperties.Builder()
						.nutrition(8)
						.saturationModifier(0.8F)
						.build()))
				.setCreativeTab(CreativeModeTabs.FOOD_AND_DRINKS);
		CreativeTabManager.endDaisyChain();

		crab_shell = new ZetaItem("crab_shell", this, new Item.Properties())
				.setCondition(() -> enableBrewing).setCreativeTab(CreativeModeTabs.INGREDIENTS, Items.RABBIT_FOOT, false);

		crab_bucket = new ZetaMobBucketItem(crabType, Fluids.WATER, QuarkSounds.BUCKET_EMPTY_CRAB, "crab_bucket", this);

		resilience = new ZetaEffect(Quark.ZETA, "resilience", MobEffectCategory.BENEFICIAL, 0x5b1a04);
		resilience.addAttributeModifier(Attributes.KNOCKBACK_RESISTANCE, Quark.asResource("resilience_knockback_resistance"), 0.5, AttributeModifier.Operation.ADD_VALUE);

		event.getBrewingRegistry().addPotionMix("crab_brewing", crab_shell, resilience);

		crabType = EntityType.Builder.<Crab>of(Crab::new, MobCategory.CREATURE)
				.sized(0.9F, 0.5F)
				.clientTrackingRange(8)
				.build("crab");
		Quark.ZETA.registry.register(crabType, "crab", Registries.ENTITY_TYPE);

		Quark.ZETA.entitySpawn.registerSpawn(crabType, MobCategory.CREATURE, SpawnPlacementTypes.ON_GROUND, Types.MOTION_BLOCKING_NO_LEAVES, Crab::spawnPredicate, spawnConfig);
		Quark.ZETA.entitySpawn.addEgg(this, crabType, 0x893c22, 0x916548, spawnConfig);

		event.getAdvancementModifierRegistry().addModifier(new FuriousCocktailModifier(this, () -> enableBrewing, Set.of(Holder.direct(resilience)))
				.setCondition(() -> resilienceRequiredForAllEffects));
		event.getAdvancementModifierRegistry().addModifier(new TwoByTwoModifier(this, ImmutableSet.of(crabType)));
		event.getAdvancementModifierRegistry().addModifier(new BalancedDietModifier(this, ImmutableSet.of(crab_leg, cookedCrabLeg)));
	}
	
	@PlayEvent
	public void onTradesLoaded(ZVillagerTrades event) {
		if(event.getType() == VillagerProfession.FISHERMAN && addCrabLegToFishermanTrades) {
			List<ItemListing> journeymanListing = event.getTrades().get(4);
			journeymanListing.add(new VillagerTrades.EmeraldForItems(crab_leg, 4, 12, 30));
		}
	}

	@LoadEvent
	public final void entityAttrs(ZEntityAttributeCreation e) {
		e.put(crabType, Crab.prepareAttributes().build());
	}

	@LoadEvent
	public final void setup(ZCommonSetup event) {
		crabSpawnableTag = Quark.asTagKey(Registries.BLOCK,"crab_spawnable");
	}

	@ZetaLoadModule(clientReplacement = true)
	public static class Client extends CrabsModule {

		@LoadEvent
		public final void clientSetup(ZClientSetup event) {
			EntityRenderers.register(crabType, CrabRenderer::new);
		}
	}
}
