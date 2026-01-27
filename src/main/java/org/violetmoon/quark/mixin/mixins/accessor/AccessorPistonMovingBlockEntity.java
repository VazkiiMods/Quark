package org.violetmoon.quark.mixin.mixins.accessor;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.piston.PistonMovingBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(PistonMovingBlockEntity.class)
public interface AccessorPistonMovingBlockEntity {
    @Invoker("moveEntityByPiston")
    static void getMoveEntityByPiston(Direction noClipDirection, Entity entity, double progress, Direction direction) {
        throw new AssertionError();
    }
}
