package org.violetmoon.quark.content.mobs.module;

import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.content.mobs.client.render.entity.BudgieRenderer;
import org.violetmoon.quark.content.mobs.entity.Budgie;
import org.violetmoon.zeta.advancement.modifier.TwoByTwoModifier;
import org.violetmoon.zeta.client.event.load.ZClientSetup;
import org.violetmoon.zeta.config.Config;
import org.violetmoon.zeta.config.type.CompoundBiomeConfig;
import org.violetmoon.zeta.config.type.EntitySpawnConfig;
import org.violetmoon.zeta.event.bus.LoadEvent;
import org.violetmoon.zeta.event.load.ZEntityAttributeCreation;
import org.violetmoon.zeta.event.load.ZRegister;
import org.violetmoon.zeta.item.ZetaItem;
import org.violetmoon.zeta.module.ZetaLoadModule;
import org.violetmoon.zeta.module.ZetaModule;
import org.violetmoon.zeta.registry.CreativeTabManager;
import org.violetmoon.zeta.util.Hint;

import java.util.Collections;
import java.util.List;

/**
 * @author sunbatheproductions28
 * Created for Quark 1.21.1
 */
@ZetaLoadModule(category = "mobs")
public class BudgieModule extends ZetaModule {

    public static EntityType<Budgie> budgieType;

    @Config
    public static EntitySpawnConfig spawnConfig = new EntitySpawnConfig(10, 2, 4,
            CompoundBiomeConfig.fromBiomeReslocs(false,
                    "minecraft:flower_forest",
                    "minecraft:meadow",
                    "minecraft:sunflower_plains"
            )
    );

    @Config(description = "The chance (1 in X) for a budgie to successfully groom a flower and trigger a growth effect.")
    public static int flowerGroomingChance = 20;

    @Hint(key = "budgie_info")
    public static Item budgie_feather;

    @LoadEvent
    public final void register(ZRegister event) {
        budgie_feather = new ZetaItem("budgie_feather", this, new Item.Properties())
                .setCreativeTab(CreativeModeTabs.INGREDIENTS, Items.FEATHER, false);

        budgieType = EntityType.Builder.of(Budgie::new, MobCategory.CREATURE)
                .sized(0.4F, 0.8F)
                .clientTrackingRange(8)
                .build("budgie");

        Quark.ZETA.registry.register(budgieType, "budgie", Registries.ENTITY_TYPE);

        Quark.ZETA.entitySpawn.registerSpawn(budgieType, MobCategory.CREATURE,
                SpawnPlacementTypes.ON_GROUND, Types.MOTION_BLOCKING_NO_LEAVES,
                Budgie::spawnPredicate, spawnConfig);

        Quark.ZETA.entitySpawn.addEgg(this, budgieType, 0x4CAF50, 0xFFFF00, spawnConfig);

        event.getAdvancementModifierRegistry().addModifier(new TwoByTwoModifier(this, Collections.singleton(budgieType)));
    }

    @LoadEvent
    public final void entityAttrs(ZEntityAttributeCreation e) {
        e.put(budgieType, Budgie.prepareAttributes().build());
    }

    @ZetaLoadModule(clientReplacement = true)
    public static class Client extends BudgieModule {

        @LoadEvent
        public final void clientSetup(ZClientSetup event) {
            EntityRenderers.register(budgieType, BudgieRenderer::new);
        }
    }
}
