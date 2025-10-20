package org.violetmoon.quark.datagen;

import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.content.automation.module.ChuteModule;
import org.violetmoon.quark.content.building.module.*;
import org.violetmoon.quark.content.tools.module.AbacusModule;

public class QuarkItemModelProvider extends ItemModelProvider {
    //1.21: we don't need these, the 1.20 models work
    public QuarkItemModelProvider(PackOutput packOutput, ExistingFileHelper existingFileHelper){
        super(packOutput, Quark.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        //Automation
        simpleBlockItem(ChuteModule.chute);
        //Building
        simpleBlockItem(CelebratoryLampsModule.stone_lamp);
        simpleBlockItem(CelebratoryLampsModule.stone_brick_lamp);
        for(Block block : CompressedBlocksModule.blocks)
            simpleBlockItem(block);
        for(Block block : FramedGlassModule.glassBlocks)
            simpleBlockItem(block);
        simpleBlockItem(GoldBarsModule.gold_bars);
        for(Block block : HedgesModule.hedges)
            simpleBlockItem(block);
        for(Block block : HollowLogsModule.hollowLogs)
            simpleBlockItem(block);
        simpleBlockItem(GrateModule.grate);
        for(Block block : JapanesePaletteModule.blocks)
            simpleBlockItem(block);
        for(Block block : LeafCarpetModule.carpets)
            simpleBlockItem(block);
        for(Block block : MidoriModule.blocks)
            simpleBlockItem(block);
        for(Block block : MoreMudBlocksModule.blocks)
            simpleBlockItem(block);
        //MorePottedPlants shouldn't need item models
        simpleBlockItem(NetherBrickFenceGateModule.netherBrickFenceGate);
        for(Block block : RainbowLampsModule.lamps)
            simpleBlockItem(block);
        for(Block block : RawMetalBricksModule.blocks)
            simpleBlockItem(block);
        simpleBlockItem(RopeModule.rope);
        //ShearVines shouldn't need item models
        for(Block block : ShinglesModule.blocks)
            simpleBlockItem(block);
        for(Block block : StoolsModule.stools)
            simpleBlockItem(block);
        simpleBlockItem(SturdyStoneModule.sturdy_stone);
        simpleBlockItem(ThatchModule.thatch);
        for(Block block : VariantBookshelvesModule.variantBookshelves)
            simpleBlockItem(block);
        for(Block block : VariantChestsModule.regularChests.values())
            simpleBlockItem(block);
        for(Block block : VariantChestsModule.trappedChests.values())
            simpleBlockItem(block);
        simpleBlockItem(VariantFurnacesModule.deepslateFurnace);
        simpleBlockItem(VariantFurnacesModule.blackstoneFurnace);
        for(Block block : VariantLaddersModule.variantLadders)
            simpleBlockItem(block);
        for(Block block : VerticalPlanksModule.blocks)
            simpleBlockItem(block);
        for(Block block : VerticalSlabsModule.blocks.values())
            simpleBlockItem(block); //untested
        for(Block block : WoodenPostsModule.blocks)
            simpleBlockItem(block);
        //etc
        //Tools
        basicItem(AbacusModule.abacus); //todo all 48 models + unset
    }
}
