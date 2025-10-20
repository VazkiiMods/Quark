package org.violetmoon.quark.mixin.mixins;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import org.violetmoon.quark.content.automation.module.FeedingTroughModule;

@Mixin(TemptGoal.class)
public class TemptGoalMixin {

	@Shadow
	protected Player player;

	@Shadow
	@Final
    protected PathfinderMob mob;

	@Inject(method = "canUse", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/ai/goal/TemptGoal;player:Lnet/minecraft/world/entity/player/Player;", ordinal = 0, shift = At.Shift.AFTER))
	private void findTroughs(CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
		if (player == null && mob.level() instanceof ServerLevel level && mob instanceof Animal animal) {
			// here valid players with food have already been selected
			player = FeedingTroughModule.modifyTemptGoal((TemptGoal) (Object) this, animal, level);
		}
	}

}
