package org.violetmoon.quark.content.tweaks.module;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Vex;

import org.violetmoon.zeta.event.bus.PlayEvent;
import org.violetmoon.zeta.event.play.entity.living.ZLivingTick;
import org.violetmoon.zeta.module.ZetaLoadModule;
import org.violetmoon.zeta.module.ZetaModule;

@ZetaLoadModule(category = "tweaks")
public class VexesDieWithTheirMastersModule extends ZetaModule {

	@PlayEvent // omae wa mou shindeiru
	public void checkWhetherAlreadyDead(ZLivingTick event) {
		if(event.getEntity() instanceof Vex vex) {
			Mob owner = vex.getOwner();
			if(owner != null && owner.isDeadOrDying() && !vex.isDeadOrDying())
				vex.hurt(vex.level().damageSources().genericKill(), Float.MAX_VALUE); // Should stop any problems from happening. Probably
		}
	}
}
