package org.violetmoon.quark.datagen;

import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.BiomeModifiers;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.violetmoon.quark.base.Quark;

public class QuarkBiomeModifiers {

    protected static final ResourceKey<BiomeModifier> ADD_FROSTY_BLOSSOM_TREES = ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS,
            Quark.asResource("add_frosty_blossom_trees"));

    //TODO make biomemodifiers config dependent
    public static void bootstrap(BootstrapContext<BiomeModifier> context) {
        HolderGetter<Biome> biomeGetter = context.lookup(Registries.BIOME);
        HolderGetter<PlacedFeature> placedGetter = context.lookup(Registries.PLACED_FEATURE);

        HolderSet.Named<Biome> hasFrosty = biomeGetter.getOrThrow(QuarkTags.Biomes.HAS_FROSTY_BLOSSOM_TREES);

        context.register(ADD_FROSTY_BLOSSOM_TREES, new BiomeModifiers.AddFeaturesBiomeModifier(
                hasFrosty,
                HolderSet.direct(placedGetter.getOrThrow(QuarkPlacedFeatures.BLUE_BLOSSOM_PLACED)),
                GenerationStep.Decoration.VEGETAL_DECORATION
        ));

    }

}
