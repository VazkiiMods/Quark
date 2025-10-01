package org.violetmoon.quark.mixin.mixins.accessor;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.armortrim.ArmorTrim;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ArmorTrim.class)
public interface AccessorArmorTrim {

    @Accessor
    static Component getUPGRADE_TITLE() {
        throw new UnsupportedOperationException();
    }

    @Accessor("showInTooltip")
    boolean showInTooltip();
}
