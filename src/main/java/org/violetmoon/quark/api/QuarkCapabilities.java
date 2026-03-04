package org.violetmoon.quark.api;

import net.neoforged.neoforge.capabilities.ItemCapability;
import org.jetbrains.annotations.Nullable;
import org.violetmoon.quark.base.Quark;

// Nuked for now.
public class QuarkCapabilities {
    public static final ItemCapability<ICustomSorting, @Nullable Void> CUSTOM_SORTING_CAPABILITY = ItemCapability.createVoid(Quark.asResource("custom_sorting"), ICustomSorting.class);
}
