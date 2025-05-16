package org.violetmoon.quark.content.tweaks.module;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import org.violetmoon.zeta.event.bus.LoadEvent;
import org.violetmoon.zeta.event.bus.PlayEvent;
import org.violetmoon.zeta.event.load.ZConfigChanged;
import org.violetmoon.zeta.event.play.entity.living.ZLivingDeath;
import org.violetmoon.zeta.module.ZetaLoadModule;
import org.violetmoon.zeta.module.ZetaModule;
import org.violetmoon.zeta.util.Hint;

@ZetaLoadModule(category = "tweaks")
public class SlimesToMagmaCubesModule extends ZetaModule {

	private static final String TAG_MAGMAED = "quark:damaged_by_magma";

	@Hint
	Item magma_cream = Items.MAGMA_CREAM;

	public static boolean staticEnabled;

	@LoadEvent
	public final void configChanged(ZConfigChanged event) {
		staticEnabled = enabled;
	}

	@PlayEvent
	public void onDeath(ZLivingDeath event) {
		if(event.getEntity().getType() == EntityType.SLIME && event.getSource() == event.getEntity().level().damageSources().hotFloor())
			event.getEntity().getPersistentData().putBoolean(TAG_MAGMAED, true);
	}

	public static EntityType<? extends Slime> getSlimeType(EntityType<? extends Slime> prev, Slime slime) {
		if(!staticEnabled)
			return prev;

		if(slime.getPersistentData().getBoolean(TAG_MAGMAED))
			return EntityType.MAGMA_CUBE;

		return prev;
	}

}
