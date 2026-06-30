package org.violetmoon.quark.integration.obe;

import fr.madu59.obe.client.api.registry.RegistryApi;
import fr.madu59.obe.client.util.BackportUtil;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.TrappedChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.content.building.client.render.be.VariantChestRenderer;
import org.violetmoon.quark.content.building.module.VariantChestsModule;

public class QuarkOBEIntegration {

    //OBE itself is adding the variant chest textures to the blocks atlas.
    public static void init(){
        RegistryApi.registerBlockEntityType(VariantChestsModule.chestTEType, "chest");
        RegistryApi.registerBlockEntityType(VariantChestsModule.trappedChestTEType, "chest");

        RegistryApi.registerMaterialProvider(VariantChestsModule.chestTEType, QuarkOBEIntegration::getVariantChestMaterial);
        RegistryApi.registerMaterialProvider(VariantChestsModule.trappedChestTEType, QuarkOBEIntegration::getVariantChestMaterial);

        //lootr chest integration does not seem to be possible - client needs to know about the block entity to know which texture to use (opened or unopened)
    }

    public static ResourceLocation getVariantChestMaterial(BlockState state) {
        Block block = state.getBlock();
        ChestType chestType = BackportUtil.getValueOrElse(state, ChestBlock.TYPE, ChestType.SINGLE);
        boolean isTrap = block instanceof TrappedChestBlock;

        if (!(block instanceof VariantChestsModule.IVariantChest v)) return null;
        //apply the texture naming convention
        StringBuilder tex = new StringBuilder(v.getTextureFolder())
                .append('/')
                .append(v.getTexturePath())
                .append('/');
        if (isTrap)
            tex.append(VariantChestRenderer.choose(chestType, "trap", "trap_left", "trap_right"));
        else
            tex.append(VariantChestRenderer.choose(chestType, "normal", "left", "right"));
        return new Material(Sheets.CHEST_SHEET, Quark.asResource(tex.toString())).texture();
        //this material constructor is probably not required but it works so eh
    }

}
