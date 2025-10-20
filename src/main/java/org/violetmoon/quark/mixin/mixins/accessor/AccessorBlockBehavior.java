package org.violetmoon.quark.mixin.mixins.accessor;

import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(BlockBehaviour.class)
public interface AccessorBlockBehavior {
    @Invoker("getSoundType")
    public SoundType invokeGetSoundType(BlockState state);
}
