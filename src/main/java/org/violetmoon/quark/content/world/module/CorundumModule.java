package org.violetmoon.quark.content.world.module;

import java.util.List;

import net.minecraft.core.registries.Registries;
import org.apache.commons.lang3.tuple.Pair;
import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.base.util.CorundumColor;
import org.violetmoon.quark.base.util.QuarkWorldGenWeights;
import org.violetmoon.quark.content.tools.module.BeaconRedirectionModule;
import org.violetmoon.quark.content.world.block.CorundumBlock;
import org.violetmoon.quark.content.world.block.CorundumClusterBlock;
import org.violetmoon.quark.content.world.undergroundstyle.CorundumStyle;
import org.violetmoon.quark.content.world.undergroundstyle.base.UndergroundStyleConfig;
import org.violetmoon.quark.content.world.undergroundstyle.base.UndergroundStyleGenerator;
import org.violetmoon.zeta.api.IIndirectConnector;
import org.violetmoon.zeta.block.ZetaInheritedPaneBlock;
import org.violetmoon.zeta.config.Config;
import org.violetmoon.zeta.event.bus.LoadEvent;
import org.violetmoon.zeta.event.bus.PlayEvent;
import org.violetmoon.zeta.event.load.ZCommonSetup;
import org.violetmoon.zeta.event.load.ZConfigChanged;
import org.violetmoon.zeta.event.load.ZGatherHints;
import org.violetmoon.zeta.event.load.ZRegister;
import org.violetmoon.zeta.module.ZetaLoadModule;
import org.violetmoon.zeta.module.ZetaModule;
import org.violetmoon.zeta.util.Hint;
import org.violetmoon.zeta.util.handler.ToolInteractionHandler;
import org.violetmoon.zeta.world.WorldGenHandler;

import com.google.common.collect.Lists;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.material.MapColor;

@ZetaLoadModule(category = "world")
public class CorundumModule extends ZetaModule {

	@Config
	public UndergroundStyleConfig generationSettings = UndergroundStyleConfig.styleBuilder()
		.style(new CorundumStyle())
		.biomeDeny(BiomeTags.IS_OCEAN)
		.rarity(400)
		.horizontalSize(36)
		.verticalSize(14)
		.horizontalVariation(8)
		.verticalVariation(6)
		.build();

	@Config
	@Config.Min(value = 0)
	@Config.Max(value = 1)
	public static double crystalChance = 0.16;

	@Config
	@Config.Min(value = 0)
	@Config.Max(value = 1)
	public static double crystalClusterChance = 0.2;

	@Config
	@Config.Min(value = 0)
	@Config.Max(value = 1)
	public static double crystalClusterOnSidesChance = 0.6;

	@Config
	@Config.Min(value = 0)
	@Config.Max(value = 1)
	public static double doubleCrystalChance = 0.2;

	@Config(description = "The chance that a crystal can grow, this is on average 1 in X world ticks, set to a higher value to make them grow slower. Minimum is 1, for every tick. Set to 0 to disable growth.")
	public static int caveCrystalGrowthChance = 5;

	@Config(flag = "cave_corundum_runes")
	public static boolean crystalsCraftRunes = true;

	@Config
	public static boolean enableCollateralMovement = true;

	public static boolean staticEnabled;

	public static List<CorundumBlock> crystals = Lists.newArrayList();
	public static List<CorundumBlock> waxedCrystals = Lists.newArrayList();
	public static List<CorundumClusterBlock> clusters = Lists.newArrayList();
	public static List<Block> panes = Lists.newArrayList();
	@Hint
	public static final TagKey<Block> corundumTag = Quark.asTagKey(Registries.BLOCK,"corundum");

	@LoadEvent
	public final void register(ZRegister event) {
		for(CorundumColor color : CorundumColor.values())
			add(color.name, color.beaconColor, color.mapColor);
	}

	@LoadEvent
	public final void configChanged(ZConfigChanged event) {
		staticEnabled = isEnabled();
	}

	@LoadEvent
	public final void setup(ZCommonSetup event) {
		WorldGenHandler.addGenerator(this,
			new UndergroundStyleGenerator(generationSettings, "corundum"),
			GenerationStep.Decoration.UNDERGROUND_DECORATION,
			QuarkWorldGenWeights.UNDERGROUND_BIOMES
		);
	}

	@LoadEvent
	public void addAdditionalHints(ZGatherHints event) {
		MutableComponent comp = Component.translatable("quark.jei.hint.corundum_cluster_grow");

		if(Quark.ZETA.modules.isEnabled(BeaconRedirectionModule.class))
			comp = comp.append(" ").append(Component.translatable("quark.jei.hint.corundum_cluster_redirect"));

		for(Block block : clusters)
			event.accept(block.asItem(), comp);
	}

	private void add(String name, int color, MapColor mapColor) {
		CorundumBlock crystal = new CorundumBlock(name + "_corundum", color, this, mapColor, false);
		crystals.add(crystal);

		CorundumBlock waxed = new CorundumBlock("waxed_" + name + "_corundum", color, this, mapColor, true);
		waxedCrystals.add(waxed);
		ToolInteractionHandler.registerWaxedBlock(this, crystal, waxed);

		Block pane = new ZetaInheritedPaneBlock(crystal).setCreativeTab(CreativeModeTabs.COLORED_BLOCKS);
		panes.add(pane);
		CorundumClusterBlock cluster = new CorundumClusterBlock(crystal);
		clusters.add(cluster);

		ClusterConnection connection = new ClusterConnection(cluster);
		IIndirectConnector.INDIRECT_STICKY_BLOCKS.add(Pair.of(connection::isValidState, connection));
	}

	public record ClusterConnection(CorundumClusterBlock cluster) implements IIndirectConnector {

		@Override
		public boolean isEnabled() {
			return enableCollateralMovement;
		}

		private boolean isValidState(BlockState state) {
			return state.getBlock() == cluster;
		}

		@Override
		public boolean canConnectIndirectly(Level world, BlockPos ourPos, BlockPos sourcePos, BlockState ourState, BlockState sourceState) {
			BlockPos offsetPos = ourPos.relative(ourState.getValue(CorundumClusterBlock.FACING).getOpposite());
			if(!offsetPos.equals(sourcePos))
				return false;

			return sourceState.getBlock() == cluster.base;
		}

	}

	public static Block getCrystal(CorundumColor corundumColor){
		return CorundumModule.crystals.get(corundumColor.ordinal());
	}

	public static Block getWaxedCrystal(CorundumColor corundumColor){
		return CorundumModule.waxedCrystals.get(corundumColor.ordinal());
	}

	public static Block getPane(CorundumColor corundumColor){
		return CorundumModule.panes.get(corundumColor.ordinal());
	}

}
