package org.violetmoon.quark.datagen;

import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.client.OptionInstance;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.storage.loot.IntRange;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.LimitCount;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.BonusLevelTableCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.neoforged.neoforge.common.Tags;
import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.content.automation.block.EnderWatcherBlock;
import org.violetmoon.quark.content.automation.block.RedstoneRandomizerBlock;
import org.violetmoon.quark.content.automation.module.*;
import org.violetmoon.quark.content.building.module.*;
import org.violetmoon.quark.content.tools.module.BottledCloudModule;
import org.violetmoon.quark.content.tweaks.module.GlassShardModule;
import org.violetmoon.quark.content.world.module.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class QuarkBlockLootTableProvider extends BlockLootSubProvider {

    private static final float[] LEAVES_STICK_CHANCES = new float[]{0.02F, 0.022222223F, 0.025F, 0.033333335F, 0.1F};
    private static final float[] LEAVES_BONUS_CHANCES = new float[]{0.005F, 0.0055555557F, 0.00625F, 0.008333334F, 0.025F};
    private static final float[] NORMAL_LEAVES_SAPLING_CHANCES = {0.05F, 0.0625F, 0.083333336F, 0.1F};
    protected static final LootItemCondition.Builder HAS_SHEARS = MatchTool.toolMatches(ItemPredicate.Builder.item().of(Tags.Items.TOOLS_SHEAR));

    protected QuarkBlockLootTableProvider(HolderLookup.Provider holderLookupProvider) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), holderLookupProvider);
    }

    @Override
    protected void generate() {
        System.out.println("QuarkBlockLootTableProvider#generate called");
        //Automation
        dropSelf(ChuteModule.chute);
        dropSelf(EnderWatcherModule.ender_watcher);
        dropSelf(FeedingTroughModule.feeding_trough); //todo: Needs to be aware of block-entity name when broken
        dropSelf(GravisandModule.gravisand);
        dropSelf(MetalButtonsModule.iron_button);
        dropSelf(MetalButtonsModule.gold_button);
        dropSelf(ObsidianPlateModule.obsidian_plate); //todo: Should this drop without a diamond/netherite pick?
        dropSelf(RedstoneRandomizerModule.redstone_randomizer);

        //Building
        dropSelf(CelebratoryLampsModule.stone_lamp);
        dropSelf(CelebratoryLampsModule.stone_brick_lamp);
        for(Block block : CompressedBlocksModule.blocks)
            dropSelf(block);
        for(Block block : FramedGlassModule.glassBlocks)
            dropSelf(block);
        dropSelf(GoldBarsModule.gold_bars);
        for(Block block : HedgesModule.hedges)
            dropSelf(block);
        for(Block block : HollowLogsModule.hollowLogs)
            dropSelf(block);
        dropSelf(GrateModule.grate);
        for(Block block : JapanesePaletteModule.blocks)
            dropSelfWithRespectToSlab(block);
        for(Block block : LeafCarpetModule.carpets)
            dropSelf(block);
        for(Block block : MidoriModule.blocks)
            dropSelfWithRespectToSlab(block);
        for(Block block : MoreMudBlocksModule.blocks)
            dropSelfWithRespectToSlab(block);
        for(Block block : MorePottedPlantsModule.pottedPlants)
            add(block, createPotFlowerItemTable(MorePottedPlantsModule.getItemLikeFromBlock(block))); //untested
        dropSelf(NetherBrickFenceGateModule.netherBrickFenceGate);
        for(Block block : RainbowLampsModule.lamps)
            dropSelf(block);
        for(Block block : RawMetalBricksModule.blocks)
            dropSelfWithRespectToSlab(block);
        dropSelf(RopeModule.rope);
        this.add(ShearVinesModule.cut_vine, noDrop());
        for(Block block : ShinglesModule.blocks)
            dropSelfWithRespectToSlab(block);
        for(Block block : StoolsModule.stools)
            dropSelf(block);
        dropSelf(SturdyStoneModule.sturdy_stone);
        dropSelf(ThatchModule.thatch);
        for(Block block : VariantBookshelvesModule.variantBookshelves)
            add(block, createSingleItemTableWithSilkTouch(block, Items.BOOK, ConstantValue.exactly(3.0F)));
        for(Block block : VariantChestsModule.regularChests)
            dropSelf(block);
        for(Block block : VariantChestsModule.trappedChests)
            dropSelf(block);
        dropSelf(VariantFurnacesModule.deepslateFurnace);
        dropSelf(VariantFurnacesModule.blackstoneFurnace);
        for(Block block : VariantLaddersModule.variantLadders)
            dropSelf(block);
        for(Block block : VerticalPlanksModule.blocks)
            dropSelf(block);
        for(Block block : VerticalSlabsModule.blocks)
            add(block, createSlabItemTable(block));
            //this is for vanilla double slabs, but it happens to work with verticals!
        for(Block block : WoodenPostsModule.blocks)
            dropSelf(block);
        //Tools
        this.add(BottledCloudModule.cloud, noDrop());
        //Tweaks
        this.add(GlassShardModule.dirtyGlass, dropDirtyShards(GlassShardModule.dirtyGlass));
        dropWhenSilkTouch(GlassShardModule.dirtyGlassPane);
        //World
        for(Block block : AncientWoodModule.woodSet.allBlocks())
            dropSelfWithRespectToSlab(block);
        add(AncientWoodModule.ancient_leaves, createLeavesDropWithBonusLikeHowOakLeavesDropApples(AncientWoodModule.ancient_leaves, AncientWoodModule.ancient_sapling, AncientWoodModule.ancient_fruit));
        dropSelf(AncientWoodModule.ancient_sapling);
        //Azalea leaves are vanilla
        add(ChorusVegetationModule.chorus_weeds, createShearsDrops(ChorusVegetationModule.chorus_weeds));
        add(ChorusVegetationModule.chorus_twist, createShearsDrops(ChorusVegetationModule.chorus_twist));
        for(Block block : CorundumModule.crystals)
            dropSelf(block);
        for(Block block : CorundumModule.waxedCrystals)
            dropSelf(block);
        for(Block block : CorundumModule.clusters)
            dropSelf(block);
        for(Block block : CorundumModule.panes)
            dropSelf(block);
        dropSelf(GlimmeringWealdModule.glow_shroom);
        dropSelf(GlimmeringWealdModule.glow_lichen_growth);
        //TODO GlimmeringWealdModule.glow_shroom_block. mushroom cap fullblock drops are weird
        dropWhenSilkTouch(GlimmeringWealdModule.glow_shroom_stem);
        dropSelf(GlimmeringWealdModule.glow_shroom_ring);
        this.add(MonsterBoxModule.monster_box, noDrop());
        dropSelf(NewStoneTypesModule.limestoneBlock);
        dropSelf(NewStoneTypesModule.jasperBlock);
        dropSelf(NewStoneTypesModule.shaleBlock);
        dropSelf(NewStoneTypesModule.myaliteBlock);
        for(Block block : NewStoneTypesModule.polishedBlocks.values())
            dropSelf(block);
        dropSelf(SpiralSpiresModule.myalite_crystal);
        dropSelf(SpiralSpiresModule.dusky_myalite);
        for(Block block : BlossomTreesModule.woodSet.allBlocks())
            dropSelfWithRespectToSlab(block);
        for(BlossomTreesModule.BlossomTree tree : BlossomTreesModule.blossomTrees){
            add(tree.leaves, createLeavesDrops(tree.leaves, tree.sapling));
            dropSelf(tree.sapling);
        }

//        //ALL variantregistry stuff
//        //preferably there'd be a better way to get to these registry objects
//        //so we can actually sort them by module
//        for(Block block : Quark.ZETA.variantRegistry.slabs){
//            add(block, createSlabItemTable(block));
//        }
//        for(Block block : Quark.ZETA.variantRegistry.stairs){
//            dropSelf(block);
//        }
//        for(Block block : Quark.ZETA.variantRegistry.walls){
//            dropSelf(block);
//        }
    }

    @Override
    protected Iterable<Block> getKnownBlocks(){
        System.out.println("QuarkBlockLootTableProvider#getKnownBlocks called");
        List<Block> ret = new ArrayList<>();
        //Automation
        ret.add(ChuteModule.chute);
        ret.add(EnderWatcherModule.ender_watcher);
        ret.add(FeedingTroughModule.feeding_trough);
        ret.add(GravisandModule.gravisand);
        ret.add(MetalButtonsModule.iron_button);
        ret.add(MetalButtonsModule.gold_button);
        ret.add(ObsidianPlateModule.obsidian_plate);
        ret.add(RedstoneRandomizerModule.redstone_randomizer);
        
        //Building
        ret.add(CelebratoryLampsModule.stone_lamp);
        ret.add(CelebratoryLampsModule.stone_brick_lamp);
        ret.addAll(CompressedBlocksModule.blocks);
        ret.addAll(FramedGlassModule.glassBlocks);
        ret.add(GoldBarsModule.gold_bars);
        ret.addAll(HedgesModule.hedges);
        ret.addAll(HollowLogsModule.hollowLogs);
        ret.add(GrateModule.grate);
        ret.addAll(JapanesePaletteModule.blocks);
        ret.addAll(LeafCarpetModule.carpets);
        ret.addAll(MidoriModule.blocks);
        ret.addAll(MoreMudBlocksModule.blocks);
        ret.addAll(MorePottedPlantsModule.pottedPlants);
        ret.add(NetherBrickFenceGateModule.netherBrickFenceGate);
        ret.addAll(RainbowLampsModule.lamps);
        ret.addAll(RawMetalBricksModule.blocks);
        ret.add(RopeModule.rope);
        ret.add(ShearVinesModule.cut_vine);
        ret.addAll(ShinglesModule.blocks);
        ret.addAll(StoolsModule.stools);
        ret.add(SturdyStoneModule.sturdy_stone);
        ret.add(ThatchModule.thatch);
        ret.addAll(VariantBookshelvesModule.variantBookshelves);
        ret.addAll(VariantChestsModule.regularChests);
        ret.addAll(VariantChestsModule.trappedChests);
        ret.add(VariantFurnacesModule.deepslateFurnace);
        ret.add(VariantFurnacesModule.blackstoneFurnace);
        ret.addAll(VariantLaddersModule.variantLadders);
        ret.addAll(VerticalPlanksModule.blocks);
        ret.addAll(VerticalSlabsModule.blocks);
        ret.addAll(WoodenPostsModule.blocks);
        //Tools
        ret.add(BottledCloudModule.cloud);
        //Tweaks
        ret.add(GlassShardModule.dirtyGlass);
        ret.add(GlassShardModule.dirtyGlassPane);
        //World
        ret.addAll(AncientWoodModule.woodSet.allBlocks());
        ret.add(AncientWoodModule.ancient_leaves);
        ret.add(AncientWoodModule.ancient_sapling);
        ret.add(ChorusVegetationModule.chorus_weeds);
        ret.add(ChorusVegetationModule.chorus_twist);
        ret.addAll(CorundumModule.crystals);
        ret.addAll(CorundumModule.waxedCrystals);
        ret.addAll(CorundumModule.clusters);
        ret.addAll(CorundumModule.panes);
        ret.add(GlimmeringWealdModule.glow_shroom);
        ret.add(GlimmeringWealdModule.glow_lichen_growth);
        ret.add(GlimmeringWealdModule.glow_shroom_stem);
        ret.add(GlimmeringWealdModule.glow_shroom_ring);
        ret.add(MonsterBoxModule.monster_box);
        ret.add(NewStoneTypesModule.limestoneBlock);
        ret.add(NewStoneTypesModule.jasperBlock);
        ret.add(NewStoneTypesModule.shaleBlock);
        ret.add(NewStoneTypesModule.myaliteBlock);
        ret.addAll(NewStoneTypesModule.polishedBlocks.values());
        ret.add(SpiralSpiresModule.myalite_crystal);
        ret.add(SpiralSpiresModule.dusky_myalite);
        ret.addAll(BlossomTreesModule.woodSet.allBlocks());
        for(BlossomTreesModule.BlossomTree tree : BlossomTreesModule.blossomTrees){
            ret.add(tree.leaves);
            ret.add(tree.sapling);
        }
        //Oddities
        //Experimental
        return ret;
    }

    public void dropSelfWithRespectToSlab(Block block) {
        if (block instanceof SlabBlock slabBlock) {
            createSlabItemTable(slabBlock);
        } else {
            dropSelf(block);
        }
    }


    //vanillacopies
    @Override
    protected LootTable.Builder createLeavesDrops(Block p_250088_, Block p_250731_, float... p_248949_) { //don't use last arg
        HolderLookup.RegistryLookup<Enchantment> registrylookup = this.registries.lookupOrThrow(Registries.ENCHANTMENT);
        return this.createSilkTouchOrShearsDispatchTable(p_250088_, ((LootPoolSingletonContainer.Builder<?>)this.applyExplosionCondition(p_250088_, LootItem.lootTableItem(p_250731_))).when(BonusLevelTableCondition.bonusLevelFlatChance(registrylookup.getOrThrow(Enchantments.FORTUNE), NORMAL_LEAVES_SAPLING_CHANCES))).withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).when(this.checkNotShearsOrSilk()).add(((LootPoolSingletonContainer.Builder)this.applyExplosionDecay(p_250088_, LootItem.lootTableItem(Items.STICK).apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 2.0F))))).when(BonusLevelTableCondition.bonusLevelFlatChance(registrylookup.getOrThrow(Enchantments.FORTUNE), LEAVES_STICK_CHANCES))));
    }

    //shears only, no silk touch
    @Override
    protected LootTable.Builder createShearsDispatchTable(Block p_252195_, LootPoolEntryContainer.Builder<?> p_250102_) {
        return createSelfDropDispatchTable(p_252195_, HAS_SHEARS, p_250102_);
    }

    //original table builders

    protected LootTable.Builder dropDirtyShards(Block block){ //TODO test output
        System.out.println("GENERATING DIRTY SHARDS");
        //TODO implement config condition
        // "conditions": [
        //                {
        //                  "condition": "quark:flag",
        //                  "flag": "glass_shard"
        //                }
        //              ]
        HolderLookup.RegistryLookup<Enchantment> registrylookup = this.registries.lookupOrThrow(Registries.ENCHANTMENT);
        return createSilkTouchDispatchTable(block, this.applyExplosionDecay(block, LootItem.lootTableItem(GlassShardModule.dirtyShard)
                .apply(SetItemCountFunction.setCount(UniformGenerator.between(2.0F, 4.0F)))
                .apply(ApplyBonusCount.addUniformBonusCount(registrylookup.getOrThrow(Enchantments.FORTUNE)))
                .apply(LimitCount.limitCount(IntRange.range(1, 4)))));
    }

    protected LootTable.Builder createShearsDrops(Block block) {
        return createShearsDispatchTable(block, LootItem.lootTableItem(block.asItem()));
    }

    protected LootTable.Builder createLeavesDropWithBonusLikeHowOakLeavesDropApples(Block p_249535_, Block p_251505_, Item bonus) {
        HolderLookup.RegistryLookup<Enchantment> registrylookup = this.registries.lookupOrThrow(Registries.ENCHANTMENT);
        return this.createLeavesDrops(p_249535_, p_251505_).withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).when(this.checkNotShearsOrSilk()).add(((LootPoolSingletonContainer.Builder)this.applyExplosionCondition(p_249535_, LootItem.lootTableItem(bonus))).when(BonusLevelTableCondition.bonusLevelFlatChance(registrylookup.getOrThrow(Enchantments.FORTUNE), LEAVES_BONUS_CHANCES))));
    }

    //original condition builders
    //(we want to use convention shears tag)
    private LootItemCondition.Builder checkNotShearsOrSilk() {
        return this.checkShearsOrSilk().invert();
    }

    private LootItemCondition.Builder checkShearsOrSilk() {
        return HAS_SHEARS.or(super.hasSilkTouch());
    }
}
