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
        generator.addProvider(gatherDataEvent.includeServer(), new QuarkRecipeProvider(packOutput, holderLookupProvider));
        generator.addProvider(gatherDataEvent.includeServer(), new LootTableProvider(packOutput, Collections.emptySet(),
                List.of(new LootTableProvider.SubProviderEntry(QuarkBlockLootTableProvider::new, LootContextParamSets.BLOCK),
                        new LootTableProvider.SubProviderEntry(QuarkEntityLootTableProvider::new, LootContextParamSets.ENTITY)),
                holderLookupProvider));
        //tags
        //generator.addProvider(gatherDataEvent.includeServer(), new QuarkBlockTagProvider(packOutput, holderLookupProvider, existingFileHelper));
        //generator.addProvider(gatherDataEvent.includeServer(), new QuarkItemTagProvider(packOutput, holderLookupProvider, existingFileHelper));

        //do we need datamaps?
        //generator.addProvider(gatherDataEvent.includeServer(), new QuarkDataMapProvider(packOutput, holderLookupProvider));

        //assets
        generator.addProvider(gatherDataEvent.includeClient(), new QuarkItemModelProvider(packOutput, existingFileHelper));
        generator.addProvider(gatherDataEvent.includeClient(), new QuarkBlockStateProvider(packOutput, existingFileHelper));
        System.out.println("QUARK DATA GENERATED. YIPPEE");
    }
}
