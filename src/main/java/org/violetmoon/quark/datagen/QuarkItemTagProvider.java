package org.violetmoon.quark.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;
import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.content.building.module.VariantBookshelvesModule;
import org.violetmoon.quark.content.world.module.AncientWoodModule;

import java.util.concurrent.CompletableFuture;

public class QuarkItemTagProvider extends ItemTagsProvider {


    public QuarkItemTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, CompletableFuture<TagLookup<Block>> blockTags, String modId, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, blockTags, Quark.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        addToQuarkTags(provider);
        addToVanillaTags(provider);
        addToConventionTags(provider);
        addToOtherModTags(provider);

    }

    private void addToQuarkTags(HolderLookup.Provider provider) {
        for(Block log : AncientWoodModule.woodSet.allLogs()){
            this.tag(QuarkTags.Items.ANCIENT_LOGS).add(log.asItem());
        }
    }

    private void addToVanillaTags(HolderLookup.Provider provider) {
        for(Block log : AncientWoodModule.woodSet.allLogs()){
            this.tag(ItemTags.LOGS_THAT_BURN).add(log.asItem());
        }
    }

    private void addToConventionTags(HolderLookup.Provider provider) {
        for (Block bookshelf : VariantBookshelvesModule.variantBookshelves){
            this.tag(Tags.Items.BOOKSHELVES).add(bookshelf.asItem());
        }
    }

    private void addToOtherModTags(HolderLookup.Provider provider) {
        //lootr
    }
}
