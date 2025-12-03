package org.violetmoon.quark.addons.oddities.client.render.be;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.violetmoon.quark.addons.oddities.block.be.TinyPotatoBlockEntity;

public class TaterWithoutLevelRenderer extends BlockEntityWithoutLevelRenderer {
    protected final TinyPotatoBlockEntity be;

    public TaterWithoutLevelRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet ems, BlockEntityType<TinyPotatoBlockEntity> beType, BlockState state) {
        super(dispatcher, ems);
        this.be = beType.create(BlockPos.ZERO, state);
    }

    public TaterWithoutLevelRenderer(BlockEntityType<TinyPotatoBlockEntity> beType, BlockState state) {
        this(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels(), beType, state);
    }

    @Override
    public void renderByItem(@NotNull ItemStack stack, @NotNull ItemDisplayContext itemDisplayContext, @NotNull PoseStack pose, @NotNull MultiBufferSource buffer, int x, int y) {
        be.name = Component.empty().append(stack.getDisplayName().plainCopy().getString().replace("[","").replace("]",""));
        be.evilCodeIHate = true; // Lmfao
        Minecraft.getInstance().getBlockEntityRenderDispatcher().renderItem(be, pose, buffer, x, y);
    }
}
