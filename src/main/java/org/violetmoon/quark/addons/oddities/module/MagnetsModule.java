package org.violetmoon.quark.addons.oddities.module;

import com.google.common.collect.Lists;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.core.Registry;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.violetmoon.quark.addons.oddities.block.MagnetBlock;
import org.violetmoon.quark.addons.oddities.block.MovingMagnetizedBlock;
import org.violetmoon.quark.addons.oddities.block.be.MagnetBlockEntity;
import org.violetmoon.quark.addons.oddities.block.be.MagnetizedBlockBlockEntity;
import org.violetmoon.quark.addons.oddities.client.render.be.MagnetizedBlockRenderer;
import org.violetmoon.quark.addons.oddities.magnetsystem.MagnetSystem;
import org.violetmoon.quark.api.event.RecipeCrawlEvent;
import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.base.config.Config;
import org.violetmoon.zeta.client.event.load.ZClientSetup;
import org.violetmoon.zeta.event.bus.LoadEvent;
import org.violetmoon.zeta.event.bus.PlayEvent;
import org.violetmoon.zeta.event.load.ZRegister;
import org.violetmoon.zeta.event.play.ZLevelTick;
import org.violetmoon.zeta.module.ZetaLoadModule;
import org.violetmoon.zeta.module.ZetaModule;
import org.violetmoon.zeta.util.Hint;

import java.util.List;

@ZetaLoadModule(category = "oddities")
public class MagnetsModule extends ZetaModule {

	public static BlockEntityType<MagnetBlockEntity> magnetType;
	public static BlockEntityType<MagnetizedBlockBlockEntity> magnetizedBlockType;

	@Config(description = "Any items you place in this list will be derived so that any block made of it will become magnetizable")
	public static List<String> magneticDerivationList = Lists.newArrayList("minecraft:iron_ingot", "minecraft:copper_ingot", "minecraft:exposed_copper", "minecraft:weathered_copper", "minecraft:oxidized_copper", "minecraft:raw_iron", "minecraft:raw_copper", "minecraft:iron_ore", "minecraft:deepslate_iron_ore", "minecraft:copper_ore", "minecraft:deepslate_copper_ore");

	@Config public static List<String> magneticWhitelist = Lists.newArrayList("minecraft:chipped_anvil", "minecraft:damaged_anvil");
	@Config public static List<String> magneticBlacklist = Lists.newArrayList("minecraft:tripwire_hook");

	@Config(flag = "magnet_pre_end")  
	public static boolean usePreEndRecipe = false;
	
	@Hint
	public static Block magnet;
	public static Block magnetized_block;

	@LoadEvent
	public final void register(ZRegister event) {
		magnet = new MagnetBlock(this);
		magnetized_block = new MovingMagnetizedBlock(this);

		magnetType = BlockEntityType.Builder.of(MagnetBlockEntity::new, magnet).build(null);
		Quark.ZETA.registry.register(magnetType, "magnet", Registry.BLOCK_ENTITY_TYPE_REGISTRY);

		magnetizedBlockType = BlockEntityType.Builder.of(MagnetizedBlockBlockEntity::new, magnetized_block).build(null);
		Quark.ZETA.registry.register(magnetizedBlockType, "magnetized_block", Registry.BLOCK_ENTITY_TYPE_REGISTRY);
	}

	@LoadEvent
	public final void clientSetup(ZClientSetup event) {
		BlockEntityRenderers.register(magnetizedBlockType, MagnetizedBlockRenderer::new);
	}

	@PlayEvent
	public void tickStart(ZLevelTick.Start event) {
		MagnetSystem.tick(true, event.getLevel());
	}

	@PlayEvent
	public void tickEnd(ZLevelTick.End event) {
		MagnetSystem.tick(false, event.getLevel());
	}

	//fixme Switch to Zeta - IThundxr
	@SubscribeEvent
	public void crawlReset(RecipeCrawlEvent.Reset event) {
		MagnetSystem.onRecipeReset();
	}

	//fixme Switch to Zeta - IThundxr
	@SubscribeEvent
	public void crawlDigest(RecipeCrawlEvent.Digest event) {
		MagnetSystem.onDigest();
	}

}
