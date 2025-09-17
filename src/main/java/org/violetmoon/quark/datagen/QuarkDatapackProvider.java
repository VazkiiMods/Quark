package org.violetmoon.quark.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.datagen.groups.QuarkConfiguredFeatures;
import org.violetmoon.quark.datagen.groups.QuarkMusicDiscs;
import org.violetmoon.quark.datagen.groups.QuarkPlacedFeatures;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class QuarkDatapackProvider extends DatapackBuiltinEntriesProvider {

    private static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
            .add(Registries.CONFIGURED_FEATURE, QuarkConfiguredFeatures::bootstrap)
            //.add(Registries.PLACED_FEATURE, QuarkPlacedFeatures::bootstrap)
            .add(Registries.JUKEBOX_SONG, QuarkMusicDiscs::bootstrap);
            //.add(NeoForgeRegistries.Keys.BIOME_MODIFIERS, QuarkBiomeModifiers::bootstrap);

    public QuarkDatapackProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, BUILDER, Set.of(Quark.MOD_ID));
    }
}
