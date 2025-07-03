package org.violetmoon.quark.base;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.violetmoon.quark.base.proxy.ClientProxy;
import org.violetmoon.quark.base.proxy.CommonProxy;
import org.violetmoon.quark.integration.lootr.ILootrIntegration;
import org.violetmoon.quark.integration.lootr.LootrIntegration;
import org.violetmoon.quark.integration.terrablender.AbstractUndergroundBiomeHandler;
import org.violetmoon.quark.integration.terrablender.TerrablenderUndergroundBiomeHandler;
import org.violetmoon.quark.integration.terrablender.VanillaUndergroundBiomeHandler;
import org.violetmoon.zeta.Zeta;
import org.violetmoon.zeta.multiloader.Env;
import org.violetmoon.zetaimplforge.ForgeZeta;

@Mod(Quark.MOD_ID)
public class Quark {

	public static final String MOD_ID = "quark";

	public static final Logger LOG = LogManager.getLogger(MOD_ID);

	public static final Zeta ZETA = new ForgeZeta(MOD_ID, LogManager.getLogger("quark-zeta"));
	public static final String ODDITIES_ID = ZETA.isProduction ? "quarkoddities" : "quark";

	public static Quark instance;
	public static CommonProxy proxy;

	public Quark(IEventBus bus) {
		instance = this;

		ZETA.start();

		proxy = Env.unsafeRunForDist(() -> ClientProxy::new, () -> CommonProxy::new);
		proxy.start();

		// Causes issues for now, will see about re-enabling later
		//zif (!ZETA.isProduction) // force all mixins to load in dev
		//	MixinEnvironment.getCurrentEnvironment().audit();
	}

	/*
	public static final IClaimIntegration FLAN_INTEGRATION = ZETA.modIntegration("flan",
			() -> FlanIntegration::new,
			() -> IClaimIntegration.Dummy::new);
	 */

	public static final ILootrIntegration LOOTR_INTEGRATION = ZETA.modIntegration("lootr",
			() -> LootrIntegration::new,
			() -> ILootrIntegration.Dummy::new);

	public static final AbstractUndergroundBiomeHandler TERRABLENDER_INTEGRATION = ZETA.modIntegration("terrablender",
			() -> TerrablenderUndergroundBiomeHandler::new,
			() -> VanillaUndergroundBiomeHandler::new);

	public static ResourceLocation asResource(String path) {
		return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
	}

	public static <T> ResourceKey<T> asResourceKey(ResourceKey<? extends Registry<T>> base, String name) {
		return ResourceKey.create(base, asResource(name));
	}

	public static <T> TagKey<T> asTagKey(ResourceKey<? extends Registry<T>> base, String name) {
		return TagKey.create(base, asResource(name));
	}
}
