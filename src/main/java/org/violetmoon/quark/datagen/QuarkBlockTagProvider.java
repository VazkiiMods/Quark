package org.violetmoon.quark.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;
import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.content.building.module.VariantBookshelvesModule;
import org.violetmoon.quark.content.world.module.AncientWoodModule;

import java.util.concurrent.CompletableFuture;

public class QuarkBlockTagProvider extends BlockTagsProvider {
    public QuarkBlockTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, String modId, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, Quark.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        //Our tags
        for(Block log : AncientWoodModule.woodSet.allLogs()){
            this.tag(QuarkTags.Blocks.ANCIENT_LOGS).add(log);
        }

        //Vanilla tags
        for(Block log : AncientWoodModule.woodSet.allLogs()){
            this.tag(BlockTags.LOGS_THAT_BURN).add(log);
        }

        //Convention tags
        for (Block bookshelf : VariantBookshelvesModule.variantBookshelves){
            this.tag(Tags.Blocks.BOOKSHELVES).add(bookshelf);
        }

    }
}
