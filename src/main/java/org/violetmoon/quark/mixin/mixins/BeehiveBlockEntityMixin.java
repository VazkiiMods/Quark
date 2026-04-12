package org.violetmoon.quark.mixin.mixins;

import net.minecraft.world.level.block.entity.BeehiveBlockEntity;

import org.spongepowered.asm.mixin.Mixin;

@Mixin(BeehiveBlockEntity.class)
public class BeehiveBlockEntityMixin {

	//todo: We have to remove the "remove UUID" tag from the list.
	/*@WrapWithCondition(method = { "writeBees", "removeIgnoredBeeTags" }, at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/CompoundTag;remove(Ljava/lang/String;)V"))
	private static boolean doNotRemoveUUIDOfBees(CompoundTag instance, String key) {
		return !key.equals("UUID");
	}

	@Inject(method = "setBeeReleaseData", at = @At("HEAD"))
	private static void rerollUUIDIfNeeded(int ticksInHive, Bee bee, CallbackInfo ci) {
		if(bee.level() instanceof ServerLevel level) {
			if(level.getEntities().get(bee.getUUID()) != null)
				bee.setUUID(Mth.createInsecureUUID(level.random));
		}
	}*/

}
