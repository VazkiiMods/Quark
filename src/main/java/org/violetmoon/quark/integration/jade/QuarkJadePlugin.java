package org.violetmoon.quark.integration.jade;

import org.violetmoon.quark.content.mobs.entity.Toretoise;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public class QuarkJadePlugin implements IWailaPlugin {
    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerEntityDataProvider(ToretoiseComponentProvider.INSTANCE, Toretoise.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerEntityComponent(ToretoiseComponentProvider.INSTANCE, Toretoise.class);
    }
}