package org.violetmoon.quark.content.tweaks.client.item;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemPropertyFunction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.violetmoon.quark.content.tweaks.module.CompassesWorkEverywhereModule;

@OnlyIn(Dist.CLIENT)
public class ClockTimePropertyFunction implements ItemPropertyFunction {

	private double rotation;
	private double rota;
	private long lastUpdateTick;

	@Override
	@OnlyIn(Dist.CLIENT)
	public float call(@NotNull ItemStack stack, @Nullable ClientLevel worldIn, @Nullable LivingEntity entityIn, int id) {
		if(!CompassesWorkEverywhereModule.isClockCalculated(stack))
			return 0F;

		boolean carried = entityIn != null;
		Entity entity = carried ? entityIn : stack.getFrame();

		if(worldIn == null && entity != null && entity.level() instanceof ClientLevel)
			worldIn = (ClientLevel) entity.level();

		if(worldIn == null)
			return 0F;
		else {
			double angle;

			if(worldIn.dimensionType().natural())
				angle = worldIn.getTimeOfDay(1F); // getCelestrialAngleByTime
			else
				angle = Math.random();

			angle = wobble(worldIn, angle);
			return (float) angle;
		}
	}

	private double wobble(Level world, double time) {
		long gameTime = world.getGameTime();
		if(gameTime != lastUpdateTick) {
			lastUpdateTick = gameTime;
			double d0 = time - rotation;
			d0 = Mth.positiveModulo(d0 + 0.5D, 1.0D) - 0.5D;
			rota += d0 * 0.1D;
			rota *= 0.9D;
			rotation = Mth.positiveModulo(rotation + rota, 1.0D);
		}

		return rotation;
	}

}
