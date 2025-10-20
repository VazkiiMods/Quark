package org.violetmoon.quark.catnip.animation;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.level.LevelEvent;

public class AnimationTickHolder {
	private static int ticks;
	private static int pausedTicks;
	
	public static void reset() {
		ticks = 0;
		pausedTicks = 0;
	}
	
	public static void tick() {
		if (!Minecraft.getInstance()
			.isPaused()) {
			ticks = (ticks + 1) % 1_728_000; // wrap around every 24 hours so we maintain enough floating point precision
		} else {
			pausedTicks = (pausedTicks + 1) % 1_728_000;
		}
	}

	public static int getTicks() {
		return getTicks(false);
	}

	public static int getTicks(boolean includePaused) {
		return includePaused ? ticks + pausedTicks : ticks;
	}

	public static float getRenderTime() {
		return getTicks() + getPartialTicks();
	}

	/**
	 * @return the fraction between the current tick to the next tick, frozen during game pause [0-1]
	 */
	public static float getPartialTicks() {
		Minecraft mc = Minecraft.getInstance();
		return mc.getTimer().getGameTimeDeltaPartialTick(false);
	}
	
	@EventBusSubscriber(Dist.CLIENT)
	public static class EventListener {
		@SubscribeEvent
		public static void onLoad(LevelEvent.Load event) {
			reset();
		}

		@SubscribeEvent
		public static void onUnload(LevelEvent.Unload event) {
			reset();
		}

		@SubscribeEvent
		public static void onTick(ClientTickEvent.Pre event) {
			tick();
		}
	}
}
