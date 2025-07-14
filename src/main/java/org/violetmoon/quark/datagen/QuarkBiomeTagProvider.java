package org.violetmoon.quark.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.BiomeTagsProvider;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.violetmoon.quark.base.Quark;

import java.util.concurrent.CompletableFuture;

public class QuarkBiomeTagProvider extends BiomeTagsProvider {
    public QuarkBiomeTagProvider(PackOutput p_255800_, CompletableFuture<HolderLookup.Provider> p_256205_, String modId, @Nullable ExistingFileHelper existingFileHelper) {
        super(p_255800_, p_256205_, Quark.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.@NotNull Provider p_256485_) {
        this.tag(QuarkTags.Biomes.HAS_FROSTY_BLOSSOM_TREES).addTags(Tags.Biomes.IS_COLD);
        this.tag(QuarkTags.Biomes.HAS_SERENE_BLOSSOM_TREES).addTags(Tags.Biomes.IS_SWAMP);
        this.tag(QuarkTags.Biomes.HAS_WARM_BLOSSOM_TREES).addTags(Tags.Biomes.IS_SAVANNA);
        this.tag(QuarkTags.Biomes.HAS_SUNNY_BLOSSOM_TREES).addTags(Tags.Biomes.IS_PLAINS);
        this.tag(QuarkTags.Biomes.HAS_FIERY_BLOSSOM_TREES).addTags(Tags.Biomes.IS_BADLANDS);
    }
}
