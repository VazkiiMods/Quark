package org.violetmoon.quark.mixin;

import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.OverworldBiomeBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.violetmoon.quark.base.handler.UndergroundBiomeHandler;

import java.util.function.Consumer;

@Mixin(OverworldBiomeBuilder.class)
public class OverworldBiomeBuilderMixin {

	@Inject(method = "addUndergroundBiomes", at = @At("RETURN"))
	public void addUndergroundBiomes(Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> consumer, CallbackInfo info) {
		UndergroundBiomeHandler.addUndergroundBiomes((OverworldBiomeBuilder) (Object) this, consumer);
	}
	
}
