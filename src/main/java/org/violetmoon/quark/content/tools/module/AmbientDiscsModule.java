package org.violetmoon.quark.content.tools.module;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.JukeboxSongs;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import org.violetmoon.quark.base.handler.QuarkSounds;
import org.violetmoon.quark.base.item.QuarkMusicDiscItem;
import org.violetmoon.zeta.config.Config;
import org.violetmoon.zeta.event.bus.LoadEvent;
import org.violetmoon.zeta.event.bus.PlayEvent;
import org.violetmoon.zeta.event.load.ZRegister;
import org.violetmoon.zeta.event.play.entity.living.ZLivingDeath;
import org.violetmoon.zeta.item.ZetaItem;
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

	@LoadEvent
	public void register(ZRegister event) {
		disc(QuarkSounds.AMBIENT_DRIPS);
		disc(QuarkSounds.AMBIENT_OCEAN);
		disc(QuarkSounds.AMBIENT_RAIN);
		disc(QuarkSounds.AMBIENT_WIND);
		disc(QuarkSounds.AMBIENT_FIRE);
		disc(QuarkSounds.AMBIENT_CLOCK);
		disc(QuarkSounds.AMBIENT_CRICKETS);
		disc(QuarkSounds.AMBIENT_CHATTER);
	}

	private void disc(SoundEvent sound) {
		String name = sound.getLocation().getPath().replaceAll(".+\\.", "");
		// discs.add(new QuarkMusicDiscItem(15, () -> sound, name, this, Integer.MAX_VALUE));
		// TODO: JukeboxSongs.XXXXXX requires datagen with above parameters
		discs.add(new ZetaItem(name, this, new Item.Properties().rarity(Rarity.RARE).jukeboxPlayable(JukeboxSongs.THIRTEEN)));
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

			SoundInstance sound = render.playingJukeboxSongs.get(pos);
			SoundManager soundEngine = mc.getSoundManager();
			if(sound == null || !soundEngine.isActive(sound)) {
				if(sound != null) {
					soundEngine.play(sound);
				} else {
					ItemStack stack = tile.getTheItem();
					if (stack.getItem() instanceof QuarkMusicDiscItem disc)
						playAmbientSound(disc, pos);
				}
			}
		}

		public static boolean playAmbientSound(QuarkMusicDiscItem disc, BlockPos pos) {
			if(disc.isAmbient) {
				Minecraft mc = Minecraft.getInstance();
				SoundManager soundEngine = mc.getSoundManager();
				LevelRenderer render = mc.levelRenderer;

				SimpleSoundInstance simplesound = new SimpleSoundInstance(disc.soundSupplier.get().getLocation(), SoundSource.RECORDS, (float) AmbientDiscsModule.volume, 1.0F, SoundInstance.createUnseededRandom(), true, 0, SoundInstance.Attenuation.LINEAR, pos.getX(), pos.getY(), pos.getZ(), false);

				render.playingJukeboxSongs.put(pos, simplesound);
				soundEngine.play(simplesound);

				if(mc.level != null)
					mc.level.addParticle(ParticleTypes.NOTE, pos.getX() + Math.random(), pos.getY() + 1.1, pos.getZ() + Math.random(), Math.random(), 0, 0);

				return true;
			}

			return false;
		}

	}
}
