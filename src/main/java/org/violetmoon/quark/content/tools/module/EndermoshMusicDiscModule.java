package org.violetmoon.quark.content.tools.module;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.JukeboxSongs;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import org.violetmoon.quark.base.handler.QuarkSounds;
import org.violetmoon.zeta.client.event.play.ZClientTick;
import org.violetmoon.zeta.config.Config;
import org.violetmoon.zeta.event.bus.LoadEvent;
import org.violetmoon.zeta.event.bus.PlayEvent;
import org.violetmoon.zeta.event.bus.ZPhase;
import org.violetmoon.zeta.event.load.ZRegister;
import org.violetmoon.zeta.event.play.loading.ZLootTableLoad;
import org.violetmoon.zeta.item.ZetaItem;
import org.violetmoon.zeta.module.ZetaLoadModule;
import org.violetmoon.zeta.module.ZetaModule;
import org.violetmoon.zeta.util.Hint;

@ZetaLoadModule(category = "tools")
public class EndermoshMusicDiscModule extends ZetaModule {

	@Config
	protected boolean playEndermoshDuringEnderdragonFight = false;

	@Config
	protected boolean addToEndCityLoot = true;
	@Config
	protected int lootWeight = 5;
	@Config
	protected int lootQuality = 1;

	@Hint
	public Item endermosh;

	@LoadEvent
	public final void register(ZRegister event) {
		// endermosh = new QuarkMusicDiscItem(14, () -> QuarkSounds.MUSIC_ENDERMOSH, "endermosh", this, 3783); // Tick length calculated from endermosh.ogg - 3:09.150
		// TODO: JukeboxSongs.XXXXXX requires datagen with above parameters
		endermosh = new ZetaItem("music_disc_endermosh", this, new Item.Properties().rarity(Rarity.RARE).jukeboxPlayable(JukeboxSongs.THIRTEEN));
	}

	@PlayEvent
	public void onLootTableLoad(ZLootTableLoad event) {
		if(addToEndCityLoot) {
			ResourceLocation res = event.getName();
			if(res.equals(BuiltInLootTables.END_CITY_TREASURE.location())) {
				LootPoolEntryContainer entry = LootItem.lootTableItem(endermosh)
						.setWeight(lootWeight)
						.setQuality(lootQuality)
						.build();
				event.add(entry);
			}
		}
	}

	@ZetaLoadModule(clientReplacement = true)
	public static class Client extends EndermoshMusicDiscModule {

		private boolean isFightingDragon;
		private int delay;
		private SimpleSoundInstance sound;

		@PlayEvent
		public void clientTick(ZClientTick event) {
			if (event.getPhase() != ZPhase.END) return;

			if(playEndermoshDuringEnderdragonFight) {
				boolean wasFightingDragon = isFightingDragon;

				Minecraft mc = Minecraft.getInstance();
				isFightingDragon = mc.level != null
						&& mc.level.dimension().location().equals(LevelStem.END.location())
						&& mc.gui.getBossOverlay().shouldPlayMusic();

				final int targetDelay = 50;

				if(isFightingDragon) {
					if(delay == targetDelay) {
						sound = SimpleSoundInstance.forMusic(QuarkSounds.MUSIC_ENDERMOSH);
						mc.getSoundManager().playDelayed(sound, 0);
						mc.gui.setNowPlaying(endermosh.getDescription());
					}

					double x = mc.player.getX();
					double z = mc.player.getZ();

					if(mc.screen == null && ((x * x) + (z * z)) < 3000) // is not in screen and within island
						delay++;

				} else if(wasFightingDragon && sound != null) {
					mc.getSoundManager().stop(sound);
					delay = 0;
					sound = null;
				}
			}
		}
	}
}
