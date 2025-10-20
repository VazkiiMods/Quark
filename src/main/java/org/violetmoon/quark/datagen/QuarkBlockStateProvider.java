package org.violetmoon.quark.datagen;

import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.content.automation.module.ChuteModule;

public class QuarkBlockStateProvider extends BlockStateProvider {
    public QuarkBlockStateProvider(PackOutput packOutput, ExistingFileHelper existingFileHelper){
        super(packOutput, Quark.MOD_ID, existingFileHelper);
    }
    @Override
    protected void registerStatesAndModels() {
        //Automation
        simpleBlockWithItem(ChuteModule.chute, cubeAll(ChuteModule.chute)); //TODO this is NOT a simple block model
    }
}
