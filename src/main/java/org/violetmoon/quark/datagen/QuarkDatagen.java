package org.violetmoon.quark.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.tags.BiomeTagsProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.violetmoon.quark.base.Quark;
import org.violetmoon.zeta.config.ConfigFlagManager;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(modid = Quark.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class QuarkDatagen {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent gatherDataEvent){
        System.out.println("GENERATING QUARK DATA. PLEASE HOLD");
        DataGenerator generator = gatherDataEvent.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        ExistingFileHelper existingFileHelper = gatherDataEvent.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> holderLookupProvider = gatherDataEvent.getLookupProvider();

        //data
        if (gatherDataEvent.includeServer()) {
            System.out.println("It does server!");
        }

        if (gatherDataEvent.includeClient()) {
            System.out.println("It does client!");
        }

        generator.addProvider(gatherDataEvent.includeServer(), new QuarkRecipeProvider(packOutput, holderLookupProvider));
        generator.addProvider(gatherDataEvent.includeServer(), new LootTableProvider(packOutput, Collections.emptySet(),
                List.of(new LootTableProvider.SubProviderEntry(QuarkBlockLootTableProvider::new, LootContextParamSets.BLOCK),
                        new LootTableProvider.SubProviderEntry(QuarkEntityLootTableProvider::new, LootContextParamSets.ENTITY)),
                holderLookupProvider));

        //tags
        /*
        for 1.21.1 Siuol already manually converted the forge tags to c
        QuarkBlockTagProvider qbtp = new QuarkBlockTagProvider(packOutput, holderLookupProvider, null, existingFileHelper);
        generator.addProvider(gatherDataEvent.includeServer(), qbtp);
        generator.addProvider(gatherDataEvent.includeServer(), new QuarkItemTagProvider(packOutput, holderLookupProvider, qbtp.contentsGetter(), null, existingFileHelper));
         */

        //testing if biome tags can be done manually so they can have data load conditions (???)
        //generator.addProvider(gatherDataEvent.includeServer(), new QuarkBiomeTagProvider(packOutput, holderLookupProvider, null, existingFileHelper));

        //things like modded tags can be done manually

        //do we need datamaps?
        //generator.addProvider(gatherDataEvent.includeServer(), new QuarkDataMapProvider(packOutput, holderLookupProvider));
        //do we need advancements?
        //generator.addProvider(gatherDataEvent.includeServer(), new QuarkAdvancementProvider(packOutput, holderLookupProvider));

        //Built-In-Data generators
        generator.addProvider(true, new QuarkDatapackProvider(packOutput, holderLookupProvider));

        //assets
        //The existing models seem to work in 1.21.1 so these aren't high priority
        //generator.addProvider(gatherDataEvent.includeClient(), new QuarkItemModelProvider(packOutput, existingFileHelper));
        //generator.addProvider(gatherDataEvent.includeClient(), new QuarkBlockStateProvider(packOutput, existingFileHelper));


        System.out.println("QUARK DATA GATHERED. YIPPEE");
    }

}
