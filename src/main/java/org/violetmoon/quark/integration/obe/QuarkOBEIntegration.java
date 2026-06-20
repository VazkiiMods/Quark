package org.violetmoon.quark.integration.obe;

import fr.madu59.obe.api.registry.RegistryApi;
import fr.madu59.obe.util.BackportUtil;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.TrappedChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import org.apache.commons.lang3.tuple.Pair;
import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.content.building.client.render.be.VariantChestRenderer;
import org.violetmoon.quark.content.building.module.VariantChestsModule;

public class QuarkOBEIntegration {

    public static void init(){
        RegistryApi.registerBlockEntityType(VariantChestsModule.chestTEType, "chest");
        RegistryApi.registerBlockEntityType(VariantChestsModule.trappedChestTEType, "chest");

        RegistryApi.registerMaterialProvider(VariantChestsModule.chestTEType, QuarkOBEIntegration::getVariantChestMaterial);
        RegistryApi.registerMaterialProvider(VariantChestsModule.trappedChestTEType, QuarkOBEIntegration::getVariantChestMaterial);

        //TODO lootr
        if(Quark.ZETA.isModLoaded("lootr")){

        }
        //LootrIntegration.chestTEType
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
        Quark.LOG.info("OBE integration returned " + Quark.asResource(tex.toString()));
        return new Material(Sheets.CHEST_SHEET, Quark.asResource(tex.toString())).texture();
    }
}
