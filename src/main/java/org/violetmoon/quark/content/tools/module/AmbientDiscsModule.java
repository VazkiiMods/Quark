package org.violetmoon.quark.content.tools.module;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.content.tools.item.AmbientDiscItem;
import org.violetmoon.quark.mixin.mixins.client.accessor.AccessorLevelRenderer;
import org.violetmoon.zeta.config.Config;
import org.violetmoon.zeta.event.bus.LoadEvent;
import org.violetmoon.zeta.event.bus.PlayEvent;
import org.violetmoon.zeta.event.load.ZRegister;
import org.violetmoon.zeta.event.play.entity.living.ZLivingDeath;
import org.violetmoon.zeta.module.ZetaLoadModule;
import org.violetmoon.zeta.module.ZetaModule;
import org.violetmoon.zeta.util.Hint;

import java.util.ArrayList;
import java.util.List;

@ZetaLoadModule(category = "tools")
public class AmbientDiscsModule extends ZetaModule {

	@Config
	public static boolean dropOnSpiderKill = true;
	@Config
	public static double volume = 3;

	@Hint(key = "ambience_discs")
	private final List<Item> discs = new ArrayList<>();
    
    public static final ResourceKey<JukeboxSong> AMBIENT_DRIPS = Quark.asResourceKey(Registries.JUKEBOX_SONG,"ambient/drips");
    public static final ResourceKey<JukeboxSong> AMBIENT_OCEAN = Quark.asResourceKey(Registries.JUKEBOX_SONG,"ambient/ocean");
    public static final ResourceKey<JukeboxSong> AMBIENT_RAIN = Quark.asResourceKey(Registries.JUKEBOX_SONG,"ambient/rain");
    public static final ResourceKey<JukeboxSong> AMBIENT_WIND = Quark.asResourceKey(Registries.JUKEBOX_SONG,"ambient/wind");
    public static final ResourceKey<JukeboxSong> AMBIENT_FIRE = Quark.asResourceKey(Registries.JUKEBOX_SONG,"ambient/fire");
    public static final ResourceKey<JukeboxSong> AMBIENT_CLOCK = Quark.asResourceKey(Registries.JUKEBOX_SONG,"ambient/clock");
    public static final ResourceKey<JukeboxSong> AMBIENT_CRICKETS = Quark.asResourceKey(Registries.JUKEBOX_SONG,"ambient/crickets");
    public static final ResourceKey<JukeboxSong> AMBIENT_CHATTER = Quark.asResourceKey(Registries.JUKEBOX_SONG,"ambient/chatter");
    
    @LoadEvent
	public void register(ZRegister event) {
		disc(AMBIENT_DRIPS, "drips");
		disc(AMBIENT_OCEAN, "ocean");
		disc(AMBIENT_RAIN, "rain");
		disc(AMBIENT_WIND, "wind");
		disc(AMBIENT_FIRE, "fire");
		disc(AMBIENT_CLOCK, "clock");
		disc(AMBIENT_CRICKETS, "crickets");
		disc(AMBIENT_CHATTER, "chatter");
	}

	private void disc(ResourceKey<JukeboxSong> song, String name) {
		discs.add(new AmbientDiscItem("music_disc_" + name, this, new Item.Properties().rarity(Rarity.RARE).stacksTo(1), song).setCreativeTab(CreativeModeTabs.TOOLS_AND_UTILITIES));
	}

	@PlayEvent
	public void onMobDeath(ZLivingDeath event) {
		if(dropOnSpiderKill && event.getEntity() instanceof Spider && event.getSource().getEntity() instanceof Skeleton) {
			Item item = discs.get(event.getEntity().level().random.nextInt(discs.size()));
			event.getEntity().spawnAtLocation(item, 0);
		}
	}

	@ZetaLoadModule(clientReplacement = true)
	public static class Client extends AmbientDiscsModule {

		public static void onJukeboxLoad(JukeboxBlockEntity tile) {
			Minecraft mc = Minecraft.getInstance();
			LevelRenderer render = mc.levelRenderer;
			BlockPos pos = tile.getBlockPos();

			SoundInstance sound = ((AccessorLevelRenderer)render).getPlayingJukeboxSongs().get(pos);
			SoundManager soundEngine = mc.getSoundManager();
			if(sound == null || !soundEngine.isActive(sound)) {
				if(sound != null) {
					soundEngine.play(sound);
				} else {
					ItemStack stack = tile.getTheItem();
					if (stack.getItem() instanceof AmbientDiscItem disc)
						playAmbientSound(disc, pos);
				}
			}
		}

		public static boolean playAmbientSound(AmbientDiscItem disc, BlockPos pos) {
            Minecraft mc = Minecraft.getInstance();
            SoundManager soundEngine = mc.getSoundManager();
            LevelRenderer render = mc.levelRenderer;

            SimpleSoundInstance simplesound = new SimpleSoundInstance(disc.song.location(), SoundSource.RECORDS, (float) AmbientDiscsModule.volume, 1.0F, SoundInstance.createUnseededRandom(), true, 0, SoundInstance.Attenuation.LINEAR, pos.getX(), pos.getY(), pos.getZ(), false);

            ((AccessorLevelRenderer)render).getPlayingJukeboxSongs().put(pos, simplesound);
            soundEngine.play(simplesound);

            if(mc.level != null)
                mc.level.addParticle(ParticleTypes.NOTE, pos.getX() + Math.random(), pos.getY() + 1.1, pos.getZ() + Math.random(), Math.random(), 0, 0);

            return true;
        }
	}
}
