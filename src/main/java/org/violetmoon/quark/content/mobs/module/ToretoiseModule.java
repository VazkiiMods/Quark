package org.violetmoon.quark.content.mobs.module;

import java.util.List;

import net.minecraft.tags.TagKey;
import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.content.mobs.client.render.entity.ToretoiseRenderer;
import org.violetmoon.quark.content.mobs.entity.Toretoise;
import org.violetmoon.zeta.advancement.ManualTrigger;
import org.violetmoon.zeta.client.event.load.ZClientSetup;
import org.violetmoon.zeta.config.Config;
import org.violetmoon.zeta.config.type.CompoundBiomeConfig;
import org.violetmoon.zeta.config.type.DimensionConfig;
import org.violetmoon.zeta.config.type.EntitySpawnConfig;
import org.violetmoon.zeta.event.bus.LoadEvent;
import org.violetmoon.zeta.event.load.ZEntityAttributeCreation;
import org.violetmoon.zeta.event.load.ZRegister;
import org.violetmoon.zeta.module.ZetaLoadModule;
import org.violetmoon.zeta.module.ZetaModule;
import org.violetmoon.zeta.world.EntitySpawnHandler;

import com.google.common.collect.Lists;

import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacements.Type;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraftforge.common.Tags;

@ZetaLoadModule(category = "mobs")
public class ToretoiseModule extends ZetaModule {

	public static EntityType<Toretoise> toretoiseType;

	@Config
	public static int maxYLevel = 0;

	@Config(description = "The number of ticks from mining a tortoise until feeding it could cause it to regrow.")
	public static int cooldownTicks = 20 * 60;

	@Config(description = "The items that can be fed to toretoises to make them regrow ores.")
	public static List<String> foods = Lists.newArrayList("minecraft:glow_berries");

	@Config(flag = "toretoise_regrow")
	public static boolean allowToretoiseToRegrow = true;

	@Config(
		description = "Feeding a toretoise after cooldown will regrow them with a one-in-this-number chance. "
				+ "Set to 1 to always regrow, higher = lower chance."
	)
	public static int regrowChance = 3;

	@Config
	public static DimensionConfig dimensions = DimensionConfig.overworld(false);

	@Config
	public static EntitySpawnConfig spawnConfig = new EntitySpawnConfig(120, 2, 4,
			CompoundBiomeConfig.fromBiomeTags(true, Tags.Biomes.IS_VOID, BiomeTags.IS_NETHER, BiomeTags.IS_END));

	public static ManualTrigger mineToretoiseTrigger;
	public static ManualTrigger mineFedToretoiseTrigger;

	public static final TagKey<EntityType<?>> toretoiseWhitelist = TagKey.create( Registries.ENTITY_TYPE, Quark.asResource("toretoise_whitelist"));

	@LoadEvent
	public final void register(ZRegister event) {
		toretoiseType = EntityType.Builder.of(Toretoise::new, MobCategory.CREATURE)
				.sized(2F, 1F)
				.clientTrackingRange(8)
				.fireImmune()
				.setCustomClientFactory((spawnEntity, world) -> new Toretoise(toretoiseType, world))
				.build("toretoise");

		Quark.ZETA.registry.register(toretoiseType, "toretoise", Registries.ENTITY_TYPE);

		Quark.ZETA.entitySpawn.registerSpawn(toretoiseType, MobCategory.MONSTER, Type.ON_GROUND, Types.MOTION_BLOCKING_NO_LEAVES, Toretoise::spawnPredicate, spawnConfig);
		Quark.ZETA.entitySpawn.addEgg(this, toretoiseType, 0x55413b, 0x383237, spawnConfig);

		mineToretoiseTrigger = event.getAdvancementModifierRegistry().registerManualTrigger("mine_toretoise");
		mineFedToretoiseTrigger = event.getAdvancementModifierRegistry().registerManualTrigger("mine_fed_toretoise");
	}

	@LoadEvent
	public final void entityAttrs(ZEntityAttributeCreation e) {
		e.put(toretoiseType, Toretoise.prepareAttributes().build());
	}

	@ZetaLoadModule(clientReplacement = true)
	public static class Client extends ToretoiseModule {

		@LoadEvent
		public final void clientSetup(ZClientSetup event) {
			EntityRenderers.register(toretoiseType, ToretoiseRenderer::new);
		}

	}
}
