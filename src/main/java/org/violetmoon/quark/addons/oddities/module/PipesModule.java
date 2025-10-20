package org.violetmoon.quark.addons.oddities.module;

import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

import org.violetmoon.quark.addons.oddities.block.be.PipeBlockEntity;
import org.violetmoon.quark.addons.oddities.block.pipe.EncasedPipeBlock;
import org.violetmoon.quark.addons.oddities.block.pipe.CopperPipeBlock;
import org.violetmoon.quark.addons.oddities.client.render.be.PipeRenderer;
import org.violetmoon.quark.base.Quark;
import org.violetmoon.zeta.client.event.load.ZAddModels;
import org.violetmoon.zeta.client.event.load.ZClientSetup;
import org.violetmoon.zeta.config.Config;
import org.violetmoon.zeta.event.bus.LoadEvent;
import org.violetmoon.zeta.event.load.ZCommonSetup;
import org.violetmoon.zeta.event.load.ZConfigChanged;
import org.violetmoon.zeta.event.load.ZRegister;
import org.violetmoon.zeta.module.ZetaLoadModule;
import org.violetmoon.zeta.module.ZetaModule;
import org.violetmoon.zeta.util.Hint;

@ZetaLoadModule(category = "oddities")
public class PipesModule extends ZetaModule {

	public static BlockEntityType<PipeBlockEntity> blockEntityType;

	@Config(description = "How long it takes for an item to cross a pipe. Bigger = slower.")
	private static int pipeSpeed = 5;

	@Config(description = "Set to 0 if you don't want pipes to have a max amount of items")
	public static int maxPipeItems = 16;

	@Config(description = "When items eject or are absorbed by pipes, should they make sounds?")
	public static boolean doPipesWhoosh = true;

	@Config(flag = "encased_pipes")
	public static boolean enableEncasedPipes = true;

	@Config
	public static boolean renderPipeItems = true;
	@Config
	public static boolean emitVibrations = true;

	@Hint
	public static Block pipe;
	@Hint(key = "pipe", value = "encased_pipes")
	public static Block encasedPipe;

	public static TagKey<Block> pipesTag;

	public static int effectivePipeSpeed;

	@LoadEvent
	public final void register(ZRegister event) {
		pipe = new CopperPipeBlock(this);
		encasedPipe = new EncasedPipeBlock(this);

		blockEntityType = BlockEntityType.Builder.of(PipeBlockEntity::new, pipe, encasedPipe).build(null);
		Quark.ZETA.registry.register(blockEntityType, "pipe", Registries.BLOCK_ENTITY_TYPE);
	}

	@LoadEvent
	public final void setup(ZCommonSetup event) {
		pipesTag = BlockTags.create(Quark.asResource("pipes"));
	}

	@LoadEvent
	public final void configChanged(ZConfigChanged event) {
		effectivePipeSpeed = pipeSpeed * 2;
	}

	@ZetaLoadModule(clientReplacement = true)
	public static class Client extends PipesModule {
		@LoadEvent
		public final void clientSetup(ZClientSetup event) {
			BlockEntityRenderers.register(blockEntityType, PipeRenderer::new);
		}

		@LoadEvent
		public void registerAdditionalModels(ZAddModels event) {
			event.register(new ModelResourceLocation(Quark.asResource("extra/pipe_flare"), "standalone"));
		}
	}
}
