package org.violetmoon.quark.content.mobs.module;

import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.neoforged.neoforge.common.Tags;
import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.content.mobs.client.render.entity.StonelingRenderer;
import org.violetmoon.quark.content.mobs.entity.Stoneling;
import org.violetmoon.quark.content.mobs.item.DiamondHeartItem;
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
import org.violetmoon.zeta.util.Hint;

@ZetaLoadModule(category = "mobs")
public class StonelingsModule extends ZetaModule {

	public static EntityType<Stoneling> stonelingType;

	@Config
	public static int maxYLevel = 0;
	@Config
	public static DimensionConfig dimensions = DimensionConfig.overworld(false);
	@Config
	public static EntitySpawnConfig spawnConfig = new EntitySpawnConfig(80, 1, 1, CompoundBiomeConfig.fromBiomeTags(true, Tags.Biomes.IS_VOID, BiomeTags.IS_NETHER, BiomeTags.IS_END));
	@Config(flag = "stoneling_drop_diamond_heart")
	public static boolean enableDiamondHeart = true;
	@Config(description = "When enabled, stonelings are much more aggressive in checking for players")
	public static boolean cautiousStonelings = false;
	@Config
	public static boolean tamableStonelings = true;

	@Config(description = "Disabled if if Pathfinder Maps are disabled.", flag = "stoneling_weald_pathfinder")
	public static boolean wealdPathfinderMaps = true;

	public static ManualTrigger makeStonelingTrigger;

	@Hint("stoneling_drop_diamond_heart")
	public static Item diamondHeart;

	public boolean registered = false;

	@LoadEvent
	public final void register(ZRegister event) {
		this.registered = true;
		diamondHeart = new DiamondHeartItem("diamond_heart", this, new Item.Properties().rarity(Rarity.UNCOMMON));

		stonelingType = EntityType.Builder.of(Stoneling::new, MobCategory.CREATURE)
				.sized(0.5F, 0.9F)
				.clientTrackingRange(8)
				.build("stoneling");
		Quark.ZETA.registry.register(stonelingType, "stoneling", Registries.ENTITY_TYPE);

		makeStonelingTrigger = event.getAdvancementModifierRegistry().registerManualTrigger("make_stoneling");

        Quark.ZETA.entitySpawn.registerSpawn(stonelingType, MobCategory.MONSTER, SpawnPlacementTypes.ON_GROUND, Types.MOTION_BLOCKING_NO_LEAVES, Stoneling::spawnPredicate, spawnConfig);
		//secondary placement is done in GW biome definition json.
		Quark.ZETA.entitySpawn.addEgg(this, stonelingType, 0xA1A1A1, 0x505050, spawnConfig);
	}

	@LoadEvent
	public final void entityAttrs(ZEntityAttributeCreation e) {
		e.put(stonelingType, Stoneling.prepareAttributes().build());
	}

	@ZetaLoadModule(clientReplacement = true)
	public static class Client extends StonelingsModule {
		@LoadEvent
		public final void clientSetup(ZClientSetup event) {
			EntityRenderers.register(stonelingType, StonelingRenderer::new);
		}

	}

}
