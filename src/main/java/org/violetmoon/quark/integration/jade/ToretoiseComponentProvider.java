package org.violetmoon.quark.integration.jade;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.content.mobs.entity.Toretoise;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

import java.lang.reflect.Field;

public enum ToretoiseComponentProvider implements IEntityComponentProvider, IServerDataProvider<EntityAccessor> {
    INSTANCE;

    @Override
    public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
        int cooldown = accessor.getServerData().getInt("eatCooldown");

        if (cooldown != 0) {
            tooltip.add(Component.translatable("quark.jade.eating_cooldown", cooldown / 20));
        }
    }

    @Override
    public void appendServerData(CompoundTag data, EntityAccessor accessor) {
        Toretoise oretoise = (Toretoise) accessor.getEntity();
        data.putInt("eatCooldown", oretoise.getEatCooldown());
    }

    @Override
    public ResourceLocation getUid() {
        return Quark.asResource("oretoise_eating_cooldown");
    }
}
