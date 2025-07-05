package org.violetmoon.quark.mixin.mixins;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.violetmoon.quark.addons.oddities.capability.MagnetTracker;
import org.violetmoon.quark.addons.oddities.magnetsystem.MagnetWorldInterface;
import org.violetmoon.quark.api.IMagnetTracker;

@Mixin(ServerLevel.class)
public class ServerLevelMixin implements MagnetWorldInterface {
    @Unique
    private final IMagnetTracker tracker = new MagnetTracker((((ServerLevel)(Object)this)));

    @Override
    public IMagnetTracker getTracker() {
        return tracker;
    }
}
