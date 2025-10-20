package org.violetmoon.quark.content.tools.item;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.JukeboxSong;
import org.jetbrains.annotations.Nullable;
import org.violetmoon.zeta.item.ZetaItem;
import org.violetmoon.zeta.module.ZetaModule;

public class AmbientDiscItem extends ZetaItem {
    public final ResourceKey<JukeboxSong> song;

    public AmbientDiscItem(String regname, @Nullable ZetaModule module, Properties properties, ResourceKey<JukeboxSong> song) {
        super(regname, module, properties.jukeboxPlayable(song));
        this.song = song;
    }
}
