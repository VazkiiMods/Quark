package org.violetmoon.quark.content.automation.client.render;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.piston.PistonMovingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.content.automation.module.PistonsMoveTileEntitiesModule;

import java.util.Objects;

public class QuarkPistonBlockEntityRenderer {

	public static boolean renderPistonBlock(PistonMovingBlockEntity piston, float partialTicks, PoseStack matrix, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
		if(!PistonsMoveTileEntitiesModule.staticEnabled || piston.getProgress(partialTicks) > 1.0F)
			return false;

		BlockState state = piston.getMovedState();
		BlockPos truePos = piston.getBlockPos();
		if(!(state.getBlock() instanceof EntityBlock eb))
			return false;
		BlockEntity tile = eb.newBlockEntity(truePos, state);
		if(tile == null)
			return false;
		CompoundTag tileTag = PistonsMoveTileEntitiesModule.getMovingBlockEntityData(piston.getLevel(), truePos);
		if(tileTag != null && tile.getType() == BuiltInRegistries.BLOCK_ENTITY_TYPE.get(ResourceLocation.parse(tileTag.getString("id"))))
			tile.loadWithComponents(tileTag, piston.getLevel().registryAccess());
		Vec3 offset = new Vec3(piston.getXOff(partialTicks), piston.getYOff(partialTicks), piston.getZOff(partialTicks));
		return renderTESafely(piston.getLevel(), truePos, state, tile, piston, partialTicks, offset, matrix, bufferIn, combinedLightIn, combinedOverlayIn);
	}

	public static boolean renderTESafely(Level world, BlockPos truePos, BlockState state, BlockEntity tile, BlockEntity sourceTE, float partialTicks, Vec3 offset, PoseStack matrix, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
		Block block = state.getBlock();
		String id = Objects.toString(BuiltInRegistries.BLOCK.getKey(block));

		PoseStack.Pose currEntry = matrix.last();
		render: try {
			if(tile == null || (block == Blocks.PISTON_HEAD) || PistonsMoveTileEntitiesModule.renderBlacklist.contains(id))
				break render;

			matrix.pushPose();
			Minecraft mc = Minecraft.getInstance();
			BlockEntityRenderer<BlockEntity> tileentityrenderer = mc.getBlockEntityRenderDispatcher().getRenderer(tile);
			if(tileentityrenderer != null) {
				tile.setLevel(sourceTE.getLevel());
				tile.clearRemoved();

				matrix.translate(offset.x, offset.y, offset.z);

				tile.setBlockState(state);
				tileentityrenderer.render(tile, partialTicks, matrix, bufferIn, combinedLightIn, combinedOverlayIn);
			}
		} catch (Exception e) {
			Quark.LOG.warn("{} can't be rendered for piston TE moving", id, e);
			PistonsMoveTileEntitiesModule.renderBlacklist.add(id);
			return false;
		} finally {
			while(matrix.last() != currEntry)
				matrix.popPose();
		}

		return state.getRenderShape() != RenderShape.MODEL;
	}

}
