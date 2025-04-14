package org.violetmoon.quark.content.client.module;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.HitResult.Type;

import org.violetmoon.quark.base.Quark;
import org.violetmoon.zeta.module.ZetaLoadModule;
import org.violetmoon.zeta.module.ZetaModule;

@ZetaLoadModule(category = "client")
public class LongRangePickBlockModule extends ZetaModule {

	public HitResult transformHitResult(HitResult orig) {
		return orig;
	}

	@ZetaLoadModule(clientReplacement = true)
	public static class Client extends LongRangePickBlockModule {

		@Override
		public HitResult transformHitResult(HitResult hitResult) {
			if(!isEnabled())
				return hitResult;

			Minecraft mc = Minecraft.getInstance();
			Player player = mc.player;
			Level level = mc.level;

			if(hitResult != null) {
				if(hitResult instanceof BlockHitResult bhr && !level.getBlockState(bhr.getBlockPos()).isAir())
					return hitResult;

				if(hitResult instanceof EntityHitResult)
					return hitResult;
			}

			HitResult result = Quark.ZETA.raytracingUtil.rayTrace(player, level, player, Block.OUTLINE, Fluid.NONE, 200);
			if(result != null && result.getType() == Type.BLOCK)
				return result;

			return hitResult;
		}

	}

}
