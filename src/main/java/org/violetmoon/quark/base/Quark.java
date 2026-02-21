package org.violetmoon.quark.base;

import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import net.neoforged.neoforgespi.locating.IModFile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.violetmoon.quark.base.proxy.ClientProxy;
import org.violetmoon.quark.base.proxy.CommonProxy;
import org.violetmoon.quark.content.building.module.VariantChestsModule;
import org.violetmoon.quark.content.mobs.module.CrabsModule;
import org.violetmoon.quark.integration.claim.FlanIntegration;
import org.violetmoon.quark.integration.claim.IClaimIntegration;
import org.violetmoon.quark.integration.lootr.ILootrIntegration;
import org.violetmoon.quark.integration.lootr.LootrIntegration;
import org.violetmoon.zeta.Zeta;
import org.violetmoon.zeta.multiloader.Env;
import org.violetmoon.zetaimplforge.ForgeZeta;

import java.nio.file.Path;
import java.util.Optional;

import static org.violetmoon.quark.content.mobs.module.CrabsModule.EFFECTS;
import static org.violetmoon.quark.content.mobs.module.CrabsModule.POTIONS;

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
		EFFECTS.register(bus);
		POTIONS.register(bus);
		ZETA.start();

		proxy = Env.unsafeRunForDist(() -> ClientProxy::new, () -> CommonProxy::new);
		proxy.start();

		if (Boolean.parseBoolean(System.getProperty("quark.auditMixins", "false"))) // force all mixins to load in dev
			MixinEnvironment.getCurrentEnvironment().audit();

		bus.addListener(Quark::addPackFinders);
		bus.addListener(Quark::registerCapabilities);
	}

	public static final IClaimIntegration FLAN_INTEGRATION = ZETA.modIntegration("flan",
			() -> FlanIntegration::new,
			() -> IClaimIntegration.Dummy::new);

	public static final ILootrIntegration LOOTR_INTEGRATION = ZETA.modIntegration("lootr",
			() -> LootrIntegration::new,
			() -> ILootrIntegration.Dummy::new);

    public static ResourceLocation asResource(String path) {
		return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
	}

	public static <T> ResourceKey<T> asResourceKey(ResourceKey<? extends Registry<T>> base, String name) {
		return ResourceKey.create(base, asResource(name));
	}

	public static <T> TagKey<T> asTagKey(ResourceKey<? extends Registry<T>> base, String name) {
		return TagKey.create(base, asResource(name));
	}

	private static void addPackFinders(AddPackFindersEvent event){
		final boolean QUARK_VDO = true; //make config maybe?
		IModFile quarkJar = ModList.get().getModFileById(MOD_ID).getFile();
		if (event.getPackType() == PackType.SERVER_DATA && QUARK_VDO) {
			Path path = quarkJar.findResource("datapacks", "quark_vdo");

			PackSelectionConfig packSelectionConfig = new PackSelectionConfig(false, Pack.Position.TOP, true);
			PackLocationInfo packLocationInfo = new PackLocationInfo("quark_vdo", Component.literal("Quark VDO"), PackSource.BUILT_IN,
					Optional.empty()
			);

			PathPackResources.PathResourcesSupplier pathResourcesSupplier = new PathPackResources.PathResourcesSupplier(path);
			Pack pack = Pack.readMetaAndCreate(packLocationInfo, pathResourcesSupplier, PackType.SERVER_DATA, packSelectionConfig);
			event.addRepositorySource(packConsumer -> {
				packConsumer.accept(pack);
			});
		}
	}

	@EventBusSubscriber(modid = Quark.MOD_ID)
	private static class RegisterBrewing {

		@SubscribeEvent
		private static void brewingRecipesEvent(RegisterBrewingRecipesEvent event) {
			event.getBuilder().addMix(Potions.WATER, CrabsModule.crab_shell, Potions.MUNDANE);
			event.getBuilder().addMix(Potions.AWKWARD, CrabsModule.crab_shell, CrabsModule.RESILIENCE_NORMAL);
			event.getBuilder().addMix(CrabsModule.RESILIENCE_NORMAL, Items.REDSTONE, CrabsModule.RESILIENCE_LONG);
			event.getBuilder().addMix(CrabsModule.RESILIENCE_NORMAL, Items.GLOWSTONE_DUST, CrabsModule.RESILIENCE_STRONG);
		}
	}

	public static void registerCapabilities(RegisterCapabilitiesEvent event) {
		Quark.LOG.info("Registering capabilities for " + VariantChestsModule.regularChests.values().size() + " variant chests");

		for(Block chest: VariantChestsModule.regularChests.values()){
			event.registerBlock(Capabilities.ItemHandler.BLOCK, (level, pos, state, blockEntity, side) -> {
				return new InvWrapper(ChestBlock.getContainer((ChestBlock) state.getBlock(), state, level, pos, true));
			}, chest);
		}
		for(Block chest: VariantChestsModule.trappedChests.values()){
			event.registerBlock(Capabilities.ItemHandler.BLOCK, (level, pos, state, blockEntity, side) -> {
				return new InvWrapper(ChestBlock.getContainer((ChestBlock) state.getBlock(), state, level, pos, true));
			}, chest);
		}

	}
}
