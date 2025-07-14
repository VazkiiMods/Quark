package org.violetmoon.quark.datagen;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import org.violetmoon.quark.base.Quark;

public class QuarkPlacedFeatures {

    protected static final ResourceKey<PlacedFeature> BLUE_BLOSSOM_PLACED = ResourceKey.create(Registries.PLACED_FEATURE, Quark.asResource("blue_blossom_placed"));

    public static void bootstrap(BootstrapContext<PlacedFeature> context) {

    }
}
