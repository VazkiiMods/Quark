package org.violetmoon.quark.integration.obe;

import fr.madu59.obe.client.api.registry.RegistryApi;
import fr.madu59.obe.client.compat.lootr.LootrCompat;
import fr.madu59.obe.client.registry.Registry;
import fr.madu59.obe.client.registry.SpecialModelGetter;
import fr.madu59.obe.client.util.BackportUtil;
import fr.madu59.obe.client.util.blockentity.ChestUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.TrappedChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import noobanidus.mods.lootr.common.api.LootrAPI;
import noobanidus.mods.lootr.common.api.LootrTags;
import noobanidus.mods.lootr.common.block.entity.LootrChestBlockEntity;
import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.content.building.client.render.be.VariantChestRenderer;
import org.violetmoon.quark.content.building.module.VariantChestsModule;
import org.violetmoon.quark.integration.lootr.client.ClientLootrIntegration;

public class QuarkOBEIntegration {

    //OBE itself is adding the variant chest textures to the blocks atlas.
    public static void init(){
        RegistryApi.registerBlockEntityType(VariantChestsModule.chestTEType, "chest");
        RegistryApi.registerBlockEntityType(VariantChestsModule.trappedChestTEType, "chest");

        RegistryApi.registerMaterialProvider(VariantChestsModule.chestTEType, QuarkOBEIntegration::getVariantChestMaterial);
        RegistryApi.registerMaterialProvider(VariantChestsModule.trappedChestTEType, QuarkOBEIntegration::getVariantChestMaterial);

        if(Quark.ZETA.isModLoaded("lootr")){
            Registry.addBlockEntityTypeInGroup("chest", ClientLootrIntegration.LOOTR_INTEGRATION.chestTE(), ClientLootrIntegration.LOOTR_INTEGRATION.trappedChestTE());
            SpecialModelGetter.register(ClientLootrIntegration.LOOTR_INTEGRATION.chestTE(), new SpecialModelGetter.SpecialModelProvider(ChestUtil::getChestModelLayerLocation, QuarkOBEIntegration::getLootrChestMaterial, LootrCompat::transformChest, LootrCompat::getChestCacheKey));
            SpecialModelGetter.register(ClientLootrIntegration.LOOTR_INTEGRATION.trappedChestTE(), new SpecialModelGetter.SpecialModelProvider(ChestUtil::getChestModelLayerLocation, QuarkOBEIntegration::getLootrChestMaterial, LootrCompat::transformChest, LootrCompat::getChestCacheKey));
        }

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
        return Quark.asResource(tex.toString());
    }

    private static ResourceLocation getLootrChestMaterial(BlockState blockState, BlockEntity blockEntity) {
        if(blockEntity instanceof LootrChestBlockEntity lootrChestBe) {
            boolean isTrap = blockEntity.getBlockState().is(LootrTags.Blocks.TRAPPED_CHESTS);
            boolean isOpened = Minecraft.getInstance().player != null && lootrChestBe.hasClientOpened(Minecraft.getInstance().player.getUUID());
            Block block = blockState.getBlock();
            ChestType chestType = BackportUtil.getValueOrElse(blockState, ChestBlock.TYPE, ChestType.SINGLE);

            if (LootrAPI.isVanillaTextures()) {
                return getVariantChestMaterial(blockState);
            }

            if (!(block instanceof VariantChestsModule.IVariantChest v)) return null;
            StringBuilder tex = new StringBuilder(v.getTextureFolder())
                    .append('/')
                    .append(v.getTexturePath())
                    .append('/');
            if(isOpened){ //this block could be better
                if (isTrap) {
                    //Lootr does not do double chests, but its blocks (and ours) technically do support the blockstates;
                    //there's no textures for these
                    tex.append(VariantChestRenderer.choose(chestType, "lootr_trap_opened", "lootr_trap_left_opened", "lootr_trap_right_opened"));
                }
                else {
                    tex.append(VariantChestRenderer.choose(chestType, "lootr_opened", "lootr_left_opened", "lootr_right_opened"));
                }
            }
            else{
                if (isTrap) {
                    tex.append(VariantChestRenderer.choose(chestType, "lootr_trap", "lootr_trap_left", "lootr_trap_right"));
                }
                else {
                    tex.append(VariantChestRenderer.choose(chestType, "lootr_normal", "lootr_left", "lootr_right"));
                }
            }

            return Quark.asResource(tex.toString());
        }
        return getVariantChestMaterial(blockState);
    }

}
