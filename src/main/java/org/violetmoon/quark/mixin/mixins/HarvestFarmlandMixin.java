package org.violetmoon.quark.mixin.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.behavior.HarvestFarmland;
import net.minecraft.world.entity.npc.Villager;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import org.violetmoon.quark.content.tweaks.module.SimpleHarvestModule;

@Mixin(HarvestFarmland.class)
public class HarvestFarmlandMixin {

	@WrapOperation(
		method = "tick(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/npc/Villager;J)V",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;destroyBlock(Lnet/minecraft/core/BlockPos;ZLnet/minecraft/world/entity/Entity;)Z")
	)
	private boolean harvestAndReplant(ServerLevel level, BlockPos pos, boolean b, Entity entity, Operation<Boolean> original) {
		if(!SimpleHarvestModule.staticEnabled && SimpleHarvestModule.villagersUseSimpleHarvest) {
			if(SimpleHarvestModule.tryHarvestOrClickCrop(level, pos, (Villager) entity, InteractionHand.MAIN_HAND, false)){
				return true;
			}
		}
		return original.call(level, pos, b, entity);
	}
}
