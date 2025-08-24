package org.violetmoon.quark.datagen.groups;

import net.minecraft.core.Holder;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.JukeboxSong;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import org.violetmoon.quark.base.handler.QuarkSounds;
import org.violetmoon.quark.content.tools.module.AmbientDiscsModule;
import org.violetmoon.quark.content.tools.module.EndermoshMusicDiscModule;

public class QuarkMusicDiscs {
    public static void bootstrap(BootstrapContext<JukeboxSong> context) {
        context.register(
                EndermoshMusicDiscModule.ENDERMOSH_DISC_SONG,
                new JukeboxSong(Holder.direct(QuarkSounds.MUSIC_ENDERMOSH), Component.translatable("item.quark.music_disc_endermosh.desc"), 189.40F, 14));
        context.register(
                AmbientDiscsModule.AMBIENT_DRIPS,
                new JukeboxSong(Holder.direct(QuarkSounds.AMBIENT_DRIPS), Component.translatable("item.quark.music_disc_drips.desc"), 1000000, 15));
        context.register(
                AmbientDiscsModule.AMBIENT_OCEAN,
                new JukeboxSong(Holder.direct(QuarkSounds.AMBIENT_OCEAN), Component.translatable("item.quark.music_disc_ocean.desc"), 1000000, 15));
        context.register(
                AmbientDiscsModule.AMBIENT_RAIN,
                new JukeboxSong(Holder.direct(QuarkSounds.AMBIENT_RAIN), Component.translatable("item.quark.music_disc_rain.desc"), 1000000, 15));
        context.register(
                AmbientDiscsModule.AMBIENT_WIND,
                new JukeboxSong(Holder.direct(QuarkSounds.AMBIENT_WIND), Component.translatable("item.quark.music_disc_wind.desc"), 1000000, 15));
        context.register(
                AmbientDiscsModule.AMBIENT_FIRE,
                new JukeboxSong(Holder.direct(QuarkSounds.AMBIENT_FIRE), Component.translatable("item.quark.music_disc_fire.desc"), 1000000, 15));
        context.register(
                AmbientDiscsModule.AMBIENT_CLOCK,
                new JukeboxSong(Holder.direct(QuarkSounds.AMBIENT_CLOCK), Component.translatable("item.quark.music_disc_clock.desc"), 1000000, 15));
        context.register(
                AmbientDiscsModule.AMBIENT_CRICKETS,
                new JukeboxSong(Holder.direct(QuarkSounds.AMBIENT_CRICKETS), Component.translatable("item.quark.music_disc_crickets.desc"), 1000000, 15));
        context.register(
                AmbientDiscsModule.AMBIENT_CHATTER,
                new JukeboxSong(Holder.direct(QuarkSounds.AMBIENT_CHATTER), Component.translatable("item.quark.music_disc_chatter.desc"), 1000000, 15));
    }
}
