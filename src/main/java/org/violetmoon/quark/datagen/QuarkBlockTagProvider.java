package org.violetmoon.quark.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;
import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.content.building.module.FramedGlassModule;
import org.violetmoon.quark.content.building.module.RainbowLampsModule;
import org.violetmoon.quark.content.building.module.VariantBookshelvesModule;
import org.violetmoon.quark.content.world.module.AncientWoodModule;
import org.violetmoon.quark.content.world.module.AzaleaWoodModule;
import org.violetmoon.quark.content.world.module.BlossomTreesModule;
import org.violetmoon.quark.content.world.module.CorundumModule;

import java.util.concurrent.CompletableFuture;

public class QuarkBlockTagProvider extends BlockTagsProvider {
    public QuarkBlockTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, String modId, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, Quark.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        addToQuarkTags(provider);
        addToVanillaTags(provider);
        addToConventionTags(provider);
        addToOtherModTags(provider);
    }

    private void addToQuarkTags(HolderLookup.Provider provider) {
        // Logs
        for (Block log : AncientWoodModule.woodSet.allLogs()) {
            this.tag(QuarkTags.Blocks.ANCIENT_LOGS).add(log);
        }
        for (Block log : AzaleaWoodModule.woodSet.allLogs()) {
            this.tag(QuarkTags.Blocks.AZALEA_LOGS).add(log);
        }
        for (Block log : BlossomTreesModule.woodSet.allLogs()) {
            this.tag(QuarkTags.Blocks.BLOSSOM_LOGS).add(log);
        }

        // Corundum
        for (Block corundumCrystal : CorundumModule.crystals) {
            this.tag(QuarkTags.Blocks.CORUNDUM).add(corundumCrystal);
        }
        for (Block lamp : RainbowLampsModule.lamps) {
            this.tag(QuarkTags.Blocks.CRYSTAL_LAMP).add(lamp);
        }

        // Framed Glass
        this.tag(QuarkTags.Blocks.FRAMED_GLASS_PANES).add(FramedGlassModule.framed_glass_pane).addTag(QuarkTags.Blocks.STAINED_FRAMED_GLASS_PANES);
        this.tag(QuarkTags.Blocks.FRAMED_GLASSES).add(FramedGlassModule.framed_glass).addTag(QuarkTags.Blocks.STAINED_FRAMED_GLASSES);
        for (Block stainedFramedGlass : FramedGlassModule.stainedFramedGlass) {
            this.tag(QuarkTags.Blocks.STAINED_FRAMED_GLASSES).add(stainedFramedGlass);
        }
        for (Block stainedFramedGlass : FramedGlassModule.stainedFramedGlass) {
            this.tag(QuarkTags.Blocks.STAINED_FRAMED_GLASSES).add(stainedFramedGlass);
        }

        // Spawning
        this.tag(QuarkTags.Blocks.CRAB_SPAWNABLE).add(Blocks.SAND).add(Blocks.RED_SAND);
        this.tag(QuarkTags.Blocks.FALLEN_LOG_CAN_SPAWN_ON).add(Blocks.DIRT);
        this.tag(QuarkTags.Blocks.FOXHOUND_SPAWNABLE).add(Blocks.NETHERRACK).add(Blocks.SOUL_SAND).add(Blocks.SOUL_SOIL).add(Blocks.BASALT); //todo: Should Blackstone be added to this?

        // Misc
        this.tag(QuarkTags.Blocks.BEACON_TRANSPARENT).add(Blocks.BEDROCK);
    }

    private void addToVanillaTags(HolderLookup.Provider provider) {
        for(Block log : AncientWoodModule.woodSet.allLogs()){
            this.tag(BlockTags.LOGS_THAT_BURN).add(log);
        }
    }

    private void addToConventionTags(HolderLookup.Provider provider) {
        for (Block bookshelf : VariantBookshelvesModule.variantBookshelves){
            this.tag(Tags.Blocks.BOOKSHELVES).add(bookshelf);
        }
    }

    private void addToOtherModTags(HolderLookup.Provider provider) {
        //lootr
    }
}
