package org.violetmoon.quark.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import org.violetmoon.quark.base.Quark;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(modid = Quark.MOD_ID)
public class QuarkDatagen {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        System.out.println("GENERATING QUARK DATA. PLEASE HOLD");
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        //data
        if (event.includeServer()) {
            System.out.println("It does server!");
        }

        if (event.includeClient()) {
            System.out.println("It does client!");
        }

		//Built-In-Data generators
		QuarkDatapackProvider quarkDatapackProvider = new QuarkDatapackProvider(packOutput, lookupProvider);
		lookupProvider = quarkDatapackProvider.getRegistryProvider();
		generator.addProvider(event.includeServer(), quarkDatapackProvider);

        //generator.addProvider(event.includeServer(), new QuarkRecipeProvider(packOutput, lookupProvider)); //enabling and disabling this as-needed to test datagen
        generator.addProvider(event.includeServer(), new LootTableProvider(packOutput, Collections.emptySet(),
                List.of(
                        new LootTableProvider.SubProviderEntry(QuarkBlockLootTableProvider::new, LootContextParamSets.BLOCK),
                        new LootTableProvider.SubProviderEntry(QuarkEntityLootTableProvider::new, LootContextParamSets.ENTITY) //Temporarily disabled due to crash regarding Glimmering Weald
                ),
                lookupProvider));

        //tags
        /*
        for 1.21.1 Siuol already manually converted the forge tags to c
        QuarkBlockTagProvider qbtp = new QuarkBlockTagProvider(packOutput, lookupProvider, null, existingFileHelper);
        generator.addProvider(event.includeServer(), qbtp);
        generator.addProvider(event.includeServer(), new QuarkItemTagProvider(packOutput, lookupProvider, qbtp.contentsGetter(), null, existingFileHelper));
         */

        //testing if biome tags can be done manually so they can have data load conditions (???)
        //generator.addProvider(event.includeServer(), new QuarkBiomeTagProvider(packOutput, lookupProvider, null, existingFileHelper));

        //things like modded tags can be done manually

        //do we need datamaps?
        //generator.addProvider(event.includeServer(), new QuarkDataMapProvider(packOutput, lookupProvider));
        //do we need advancements?
        //generator.addProvider(event.includeServer(), new QuarkAdvancementProvider(packOutput, lookupProvider));

        //assets
        //The existing models seem to work in 1.21.1 so these aren't high priority
        //generator.addProvider(event.includeClient(), new QuarkItemModelProvider(packOutput, existingFileHelper));
        //generator.addProvider(event.includeClient(), new QuarkBlockStateProvider(packOutput, existingFileHelper));


        System.out.println("QUARK DATA GATHERED. YIPPEE");
    }
}
