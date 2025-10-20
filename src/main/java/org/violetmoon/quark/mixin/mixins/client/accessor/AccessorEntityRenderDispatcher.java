package org.violetmoon.quark.mixin.mixins.client.accessor;

import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(EntityRenderDispatcher.class)
public interface AccessorEntityRenderDispatcher {
    @Accessor("renderers")
    Map<EntityType<?>, EntityRenderer<?>> getRenderers();
}
