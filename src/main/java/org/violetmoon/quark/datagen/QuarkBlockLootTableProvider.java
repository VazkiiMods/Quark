package org.violetmoon.quark.datagen;

import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.*;
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
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.neoforged.neoforge.common.Tags;
import org.violetmoon.quark.addons.oddities.module.*;
import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.content.automation.module.*;
import org.violetmoon.quark.content.building.module.*;
import org.violetmoon.quark.content.tools.module.BottledCloudModule;
import org.violetmoon.quark.content.tweaks.module.GlassShardModule;
import org.violetmoon.quark.content.tweaks.module.PetalsOnWaterModule;
import org.violetmoon.quark.content.world.module.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

public class QuarkBlockLootTableProvider extends BlockLootSubProvider {

    private static final float[] LEAVES_STICK_CHANCES = new float[]{0.02F, 0.022222223F, 0.025F, 0.033333335F, 0.1F};
    private static final float[] LEAVES_BONUS_CHANCES = new float[]{0.005F, 0.0055555557F, 0.00625F, 0.008333334F, 0.025F};
    private static final float[] NORMAL_LEAVES_SAPLING_CHANCES = {0.05F, 0.0625F, 0.083333336F, 0.1F};
    private static final float[] TRUMPET_LEAVES_SAPLING_CHANCES = {0.0046875F, 0.00520833337F, 0.005859375F, 0.00781250025F, 0.01875F};
    protected static final LootItemCondition.Builder HAS_SHEARS = MatchTool.toolMatches(ItemPredicate.Builder.item().of(Tags.Items.TOOLS_SHEAR));

    protected QuarkBlockLootTableProvider(HolderLookup.Provider holderLookupProvider) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), holderLookupProvider);
    }


    /*
     * IMPORTANT
     * IMPORTANT
     * IMPORTANT
     * IMPORTANT
     * IMPORTANT
     *
     * REMEMBER TO ADD ENTRIES TO BOTH GENERATE AND GET KNOWN BLOCKS
     */
    @Override
    protected void generate() {
        System.out.println("QuarkBlockLootTableProvider#generate called");
        //Automation
        dropSelf(ChuteModule.chute);
        dropSelf(EnderWatcherModule.ender_watcher);
        dropSelf(FeedingTroughModule.feeding_trough); //todo: Needs to be aware of block-entity name when broken
        dropSelf(GravisandModule.gravisand);
        dropSelf(IronRodModule.iron_rod);
        dropSelf(MetalButtonsModule.iron_button);
        dropSelf(MetalButtonsModule.gold_button);
        dropSelf(ObsidianPlateModule.obsidian_plate); //todo: Should this drop without a diamond/netherite pick?
        dropSelf(RedstoneRandomizerModule.redstone_randomizer);

        //Building
        dropSelf(CelebratoryLampsModule.stone_lamp);
        dropSelf(CelebratoryLampsModule.stone_brick_lamp);
        for(Block block : CompressedBlocksModule.blocks)
            dropSelf(block);
        for(Block block : DuskboundBlocksModule.blocks)
            dropSelf(block);
        for(Block block : FramedGlassModule.glassBlocks)
            dropSelf(block);
        dropSelf(GoldBarsModule.gold_bars);
        for(Block block : HedgesModule.hedges)
            dropSelf(block);
        for(Block block : HollowLogsModule.hollowLogs)
            dropSelf(block);
        for(Block block : IndustrialPaletteModule.blocks)
            dropSelf(block);
        dropSelf(GrateModule.grate);
        for(Block block : JapanesePaletteModule.blocks)
            dropSelfWithRespectToAlternates(block);
        for(Block block : LeafCarpetModule.carpets)
            dropSelf(block);
        for(Block block : MidoriModule.blocks)
            dropSelfWithRespectToAlternates(block);
        for(Block block : MoreBrickTypesModule.blocks)
            dropSelfWithRespectToAlternates(block);
        for(Block block : MoreMudBlocksModule.blocks)
            dropSelfWithRespectToAlternates(block);
        for(Block block : MorePottedPlantsModule.pottedPlants)
            add(block, createPotFlowerItemTable(MorePottedPlantsModule.getItemLikeFromBlock(block))); //untested
        for(Block block : MoreStoneVariantsModule.blocks)
            dropSelf(block);
        dropSelf(NetherBrickFenceGateModule.netherBrickFenceGate);
        for(Block block : RainbowLampsModule.lamps)
            dropSelf(block);
        for(Block block : RawMetalBricksModule.blocks)
            dropSelfWithRespectToAlternates(block);
        dropSelf(RopeModule.rope);
        this.add(ShearVinesModule.cut_vine, noDrop());
        for(Block block : ShinglesModule.blocks)
            dropSelfWithRespectToAlternates(block);
        for(Block block : SoulSandstoneModule.blocks)
            dropSelf(block);
        for(Block block : StoolsModule.stools)
            dropSelf(block);
        dropSelf(SturdyStoneModule.sturdy_stone);
        dropSelf(ThatchModule.thatch);
        for(Block block : VariantBookshelvesModule.variantBookshelves)
            add(block, createSingleItemTableWithSilkTouch(block, Items.BOOK, ConstantValue.exactly(3.0F)));
        for(Block block : VariantChestsModule.regularChests.values())
            dropSelf(block);
        for(Block block : VariantChestsModule.trappedChests.values())
            dropSelf(block);
        dropSelf(VariantFurnacesModule.deepslateFurnace);
        dropSelf(VariantFurnacesModule.blackstoneFurnace);
        for(Block block : VariantLaddersModule.variantLadders)
            dropSelf(block);
        for(Block block : VerticalPlanksModule.blocks)
            dropSelf(block);
        for(Block block : VerticalSlabsModule.blocks.values())
            add(block, createSlabItemTable(block));
            //this is for vanilla double slabs, but it happens to work with verticals!
        for(Block block : WoodenPostsModule.blocks)
            dropSelf(block);


        //Tools
        this.add(BottledCloudModule.cloud, noDrop());

        //Tweaks
        this.add(GlassShardModule.dirtyGlass, dropDirtyShards(GlassShardModule.dirtyGlass));
        dropWhenSilkTouch(GlassShardModule.dirtyGlassPane);
        this.add(PetalsOnWaterModule.water_pink_petals, createWaterPetalsDrops(PetalsOnWaterModule.water_pink_petals));

        //World
        for(Block block : AncientWoodModule.woodSet.allBlocks())
            dropSelfWithRespectToAlternates(block);
        add(AncientWoodModule.ancient_leaves, createLeavesDropWithBonusLikeHowOakLeavesDropApples(AncientWoodModule.ancient_leaves, AncientWoodModule.ancient_sapling, AncientWoodModule.ancient_fruit));
        dropSelf(AncientWoodModule.ancient_sapling);
        for(Block block : AzaleaWoodModule.woodSet.allBlocks())
            dropSelfWithRespectToAlternates(block);
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
            dropWhenSilkTouch(block);
        dropSelf(GlimmeringWealdModule.glow_shroom);
        dropSelf(GlimmeringWealdModule.glow_lichen_growth);
        //TODO GlimmeringWealdModule.glow_shroom_block. mushroom cap fullblock drops are weird
        this.add(GlimmeringWealdModule.glow_shroom_block,createMushroomBlockDrop(GlimmeringWealdModule.glow_shroom_block, GlimmeringWealdModule.glow_shroom));
        dropWhenSilkTouch(GlimmeringWealdModule.glow_shroom_stem);
        dropSelf(GlimmeringWealdModule.glow_shroom_ring);
        this.add(MonsterBoxModule.monster_box, noDrop());
        dropSelf(NewStoneTypesModule.limestoneBlock);
        dropSelf(NewStoneTypesModule.jasperBlock);
        dropSelf(NewStoneTypesModule.shaleBlock);
        dropSelf(NewStoneTypesModule.myaliteBlock);
        for(Block block : NewStoneTypesModule.polishedBlocks.values())
            dropSelf(block);
        for(Block block : PermafrostModule.blocks)
            dropSelfWithRespectToAlternates(block);
        dropSelf(SpiralSpiresModule.myalite_crystal);
        dropSelf(SpiralSpiresModule.dusky_myalite);
        for(Block block : BlossomTreesModule.woodSet.allBlocks())
            dropSelfWithRespectToAlternates(block);
        for(BlossomTreesModule.BlossomTree tree : BlossomTreesModule.blossomTrees){
            add(tree.leaves, createLeavesDrops(tree.leaves, tree.sapling, TRUMPET_LEAVES_SAPLING_CHANCES));
            dropSelf(tree.sapling);
        }

        //ALL variantregistry stuff
        //preferably there'd be a better way to get to these registry objects
        //so we can actually sort them by module
        for(Block block : Quark.ZETA.variantRegistry.slabs.values()){
            add(block, createSlabItemTable(block));
        }

        for(Block block : Quark.ZETA.variantRegistry.stairs.values()){
            dropSelf(block);
        }

        for(Block block : Quark.ZETA.variantRegistry.walls.values()){
            dropSelf(block);
        }

        //Oddities
        dropSelf(PipesModule.pipe);
        dropSelf(PipesModule.encasedPipe);
        dropSelf(TinyPotatoModule.tiny_potato);
        dropSelf(CrateModule.crate);
        dropSelf(MagnetsModule.magnet);
        dropSelf(BackpackModule.bonded_ravager_hide);
        dropOther(MatrixEnchantingModule.matrixEnchanter, Items.ENCHANTING_TABLE);
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
        ret.add(IronRodModule.iron_rod);
        
        //Building
        ret.add(CelebratoryLampsModule.stone_lamp);
        ret.add(CelebratoryLampsModule.stone_brick_lamp);
        ret.addAll(CompressedBlocksModule.blocks);
        ret.addAll(DuskboundBlocksModule.blocks);
        ret.addAll(FramedGlassModule.glassBlocks);
        ret.add(GoldBarsModule.gold_bars);
        ret.addAll(HedgesModule.hedges);
        ret.addAll(HollowLogsModule.hollowLogs);
        ret.addAll(IndustrialPaletteModule.blocks);
        ret.add(GrateModule.grate);
        ret.addAll(JapanesePaletteModule.blocks);
        ret.addAll(LeafCarpetModule.carpets);
        ret.addAll(MidoriModule.blocks);
        ret.addAll(MoreBrickTypesModule.blocks);
        ret.addAll(MoreMudBlocksModule.blocks);
        ret.addAll(MorePottedPlantsModule.pottedPlants);
        ret.addAll(MoreStoneVariantsModule.blocks);
        ret.add(NetherBrickFenceGateModule.netherBrickFenceGate);
        ret.addAll(RainbowLampsModule.lamps);
        ret.addAll(RawMetalBricksModule.blocks);
        ret.add(RopeModule.rope);
        ret.add(ShearVinesModule.cut_vine);
        ret.addAll(ShinglesModule.blocks);
        ret.addAll(SoulSandstoneModule.blocks);
        ret.addAll(StoolsModule.stools);
        ret.add(SturdyStoneModule.sturdy_stone);
        ret.add(ThatchModule.thatch);
        ret.addAll(VariantBookshelvesModule.variantBookshelves);
        ret.addAll(VariantChestsModule.regularChests.values());
        ret.addAll(VariantChestsModule.trappedChests.values());
        ret.add(VariantFurnacesModule.deepslateFurnace);
        ret.add(VariantFurnacesModule.blackstoneFurnace);
        ret.addAll(VariantLaddersModule.variantLadders);
        ret.addAll(VerticalPlanksModule.blocks);
        ret.addAll(VerticalSlabsModule.blocks.values());
        ret.addAll(WoodenPostsModule.blocks);

        //Tools
        ret.add(BottledCloudModule.cloud);

        //Tweaks
        ret.add(GlassShardModule.dirtyGlass);
        ret.add(GlassShardModule.dirtyGlassPane);
        ret.add(PetalsOnWaterModule.water_pink_petals);

        //World
        ret.addAll(AncientWoodModule.woodSet.allBlocks());
        ret.add(AncientWoodModule.ancient_leaves);
        ret.add(AncientWoodModule.ancient_sapling);
        ret.addAll(AzaleaWoodModule.woodSet.allBlocks());
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
        ret.add(GlimmeringWealdModule.glow_shroom_block);
        ret.add(MonsterBoxModule.monster_box);
        ret.add(NewStoneTypesModule.limestoneBlock);
        ret.addAll(PermafrostModule.blocks);
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

        ret.addAll(Quark.ZETA.variantRegistry.slabs.values());
        ret.addAll(Quark.ZETA.variantRegistry.stairs.values());
        ret.addAll(Quark.ZETA.variantRegistry.walls.values());

        //Oddities

        ret.add(PipesModule.pipe);
        ret.add(PipesModule.encasedPipe);
        ret.add(TinyPotatoModule.tiny_potato);
        ret.add(CrateModule.crate);
        ret.add(MagnetsModule.magnet);
        ret.add(BackpackModule.bonded_ravager_hide);
        ret.add(MatrixEnchantingModule.matrixEnchanter);
        //Experimental
        return ret;
    }

    public void dropSelfWithRespectToAlternates(Block block) {
        if (block instanceof SlabBlock slabBlock) {
            this.add(slabBlock, createSlabItemTable(slabBlock));
        } else if (block instanceof DoorBlock doorBlock) {
            this.add(doorBlock, createDoorTable(doorBlock));
        } else {
            dropSelf(block);
        }
    }


    //vanillacopies
    @Override
    protected LootTable.Builder createLeavesDrops(Block leafBlock, Block saplingBlock, float... saplingChances) {
        HolderLookup.RegistryLookup<Enchantment> registrylookup = this.registries.lookupOrThrow(Registries.ENCHANTMENT);
        return this.createSilkTouchOrShearsDispatchTable(leafBlock, ((LootPoolSingletonContainer.Builder<?>)this.applyExplosionCondition(leafBlock, LootItem.lootTableItem(saplingBlock))).when(BonusLevelTableCondition.bonusLevelFlatChance(registrylookup.getOrThrow(Enchantments.FORTUNE), saplingChances))).withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).when(this.checkNotShearsOrSilk()).add(((LootPoolSingletonContainer.Builder)this.applyExplosionDecay(leafBlock, LootItem.lootTableItem(Items.STICK).apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 2.0F))))).when(BonusLevelTableCondition.bonusLevelFlatChance(registrylookup.getOrThrow(Enchantments.FORTUNE), LEAVES_STICK_CHANCES))));
    }

    //shears only, no silk touch
    @Override
    protected LootTable.Builder createShearsDispatchTable(Block p_252195_, LootPoolEntryContainer.Builder<?> p_250102_) {
        return createSelfDropDispatchTable(p_252195_, HAS_SHEARS, p_250102_);
    }

    //original table builders

    protected LootTable.Builder dropDirtyShards(Block block){ //TODO test output
        System.out.println("GENERATING DIRTY SHARDS");
        HolderLookup.RegistryLookup<Enchantment> registrylookup = this.registries.lookupOrThrow(Registries.ENCHANTMENT);
        return createSilkTouchDispatchTable(block, this.applyExplosionDecay(block, LootItem.lootTableItem(GlassShardModule.dirtyShard)
                .apply(SetItemCountFunction.setCount(UniformGenerator.between(2.0F, 4.0F)))
                .apply(ApplyBonusCount.addUniformBonusCount(registrylookup.getOrThrow(Enchantments.FORTUNE)))
                .apply(LimitCount.limitCount(IntRange.range(1, 4)))));
    }

    protected LootTable.Builder createShearsDrops(Block block) {
        return LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).when(HAS_SHEARS).add(LootItem.lootTableItem(block)));
    }

    protected LootTable.Builder createWaterPetalsDrops(Block petalBlock) {
        return LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).add(this.applyExplosionDecay(petalBlock, LootItem.lootTableItem(Items.PINK_PETALS).apply(IntStream.rangeClosed(1, 4).boxed().toList(), (p_272348_) -> SetItemCountFunction.setCount(ConstantValue.exactly((float)p_272348_)).when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(petalBlock).setProperties(net.minecraft.advancements.critereon.StatePropertiesPredicate.Builder.properties().hasProperty(PinkPetalsBlock.AMOUNT, p_272348_)))))));
    }

    protected LootTable.Builder createLeavesDropWithBonusLikeHowOakLeavesDropApples(Block p_249535_, Block p_251505_, Item bonus) {
        HolderLookup.RegistryLookup<Enchantment> registrylookup = this.registries.lookupOrThrow(Registries.ENCHANTMENT);
        return this.createLeavesDrops(p_249535_, p_251505_, NORMAL_LEAVES_SAPLING_CHANCES).withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).when(this.checkNotShearsOrSilk()).add(((LootPoolSingletonContainer.Builder)this.applyExplosionCondition(p_249535_, LootItem.lootTableItem(bonus))).when(BonusLevelTableCondition.bonusLevelFlatChance(registrylookup.getOrThrow(Enchantments.FORTUNE), LEAVES_BONUS_CHANCES))));
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
