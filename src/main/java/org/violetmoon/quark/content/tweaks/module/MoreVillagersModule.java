package org.violetmoon.quark.content.tweaks.module;

import com.google.common.collect.ImmutableMap;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.biome.Biomes;
import org.violetmoon.zeta.config.Config;
import org.violetmoon.zeta.event.bus.LoadEvent;
import org.violetmoon.zeta.event.load.ZCommonSetup;
import org.violetmoon.zeta.event.load.ZRegister;
import org.violetmoon.zeta.module.ZetaLoadModule;
import org.violetmoon.zeta.module.ZetaModule;

import java.util.HashMap;

@ZetaLoadModule(category = "tweaks")
public class MoreVillagersModule extends ZetaModule {

    @Config
    public boolean oceanVillager = true;
    @Config
    public boolean beachVillager = true;

    // need it here so I can reference it early in the trade mixin
    public static final VillagerType beach = new VillagerType("beach");
    public static final VillagerType ocean = new VillagerType("ocean");


    @LoadEvent
    public final void register(ZRegister event) {
        event.getRegistry().register(beach, "beach", Registries.VILLAGER_TYPE);
        event.getRegistry().register(ocean, "ocean", Registries.VILLAGER_TYPE);
    }

    @LoadEvent
    public final void onCommonSetup(ZCommonSetup event) {
        if (this.enabled) {
            var map = new HashMap<>(VillagerType.BY_BIOME);
            if (oceanVillager) {
                map.put(Biomes.WARM_OCEAN, ocean);
                map.put(Biomes.LUKEWARM_OCEAN, ocean);
                map.put(Biomes.DEEP_LUKEWARM_OCEAN, ocean);
                map.put(Biomes.OCEAN, ocean);
                map.put(Biomes.DEEP_OCEAN, ocean);
                map.put(Biomes.COLD_OCEAN, ocean);
                map.put(Biomes.DEEP_COLD_OCEAN, ocean);
                map.put(Biomes.FROZEN_OCEAN, ocean);
                map.put(Biomes.DEEP_FROZEN_OCEAN, ocean);
            }
            if (beachVillager){
                map.put(Biomes.BEACH, beach);
            }
            VillagerType.BY_BIOME = map;
        }
    }

    public static void addExtraTrades(ImmutableMap.Builder<VillagerType, Item> original) {
        original.put(MoreVillagersModule.beach, Items.JUNGLE_BOAT);
        original.put(MoreVillagersModule.ocean, Items.OAK_BOAT);
    }
}
