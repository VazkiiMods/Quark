package org.violetmoon.quark.content.world.module;

import com.google.common.base.Functions;
import com.google.common.collect.ImmutableSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.base.handler.WoodSetHandler;
import org.violetmoon.quark.base.handler.WoodSetHandler.WoodSet;
import org.violetmoon.quark.content.world.feature.AncientTreeTopperDecorator;
import org.violetmoon.quark.content.world.feature.MultiFoliageStraightTrunkPlacer;
import org.violetmoon.quark.content.world.feature.OffsetFancyFoliagePlacer;
import org.violetmoon.quark.content.world.item.AncientFruitItem;
import org.violetmoon.zeta.advancement.ManualTrigger;
import org.violetmoon.zeta.advancement.modifier.BalancedDietModifier;
import org.violetmoon.zeta.block.ZetaLeavesBlock;
import org.violetmoon.zeta.block.ZetaSaplingBlock;
import org.violetmoon.zeta.config.Config;
import org.violetmoon.zeta.config.Config.Min;
import org.violetmoon.zeta.event.bus.LoadEvent;
import org.violetmoon.zeta.event.bus.PlayEvent;
import org.violetmoon.zeta.event.load.ZCommonSetup;
import org.violetmoon.zeta.event.load.ZRegister;
import org.violetmoon.zeta.event.play.loading.ZLootTableLoad;
import org.violetmoon.zeta.module.ZetaLoadModule;
import org.violetmoon.zeta.module.ZetaModule;
import org.violetmoon.zeta.util.Hint;

import java.util.Optional;

@ZetaLoadModule(category = "world")
public class AncientWoodModule extends ZetaModule {

	@Config(flag = "ancient_fruit_xp")
	public static boolean ancientFruitGivesExp = true;

	@Config
	@Min(1)
	public static int ancientFruitExpValue = 10;

	@Config(description = "Set to a value other than 0 to enable Ancient City loot chest generation (8 recommended if you do)")
	@Min(0)
	public static int ancientCityLootWeight = 0;

	@Config
	@Min(0)
	public static int ancientCityLootQuality = 1;

	@Config(description = "Set to 0 to disable sniffer sniffing. The vanilla loot table has every entry at weight 1, so without editing it, it's impossible to make the sapling more rare")
	@Min(0)
	public static int sniffingLootWeight = 1;

	@Config
	@Min(0)
	public static int sniffingLootQuality = 0;

	public static WoodSet woodSet;
	public static Block ancient_leaves;
	@Hint
	public static Block ancient_sapling;
	@Hint
	public static Item ancient_fruit;
	public static final ResourceKey<ConfiguredFeature<?, ?>> configuredFeatureKey = Quark.asResourceKey(Registries.CONFIGURED_FEATURE, "ancient_tree");

	public static ManualTrigger ancientFruitTrigger;

	@LoadEvent
	public void setup(ZCommonSetup e) {
		e.enqueueWork(() -> {
			ComposterBlock.COMPOSTABLES.put(ancient_sapling.asItem(), 0.3F);
			ComposterBlock.COMPOSTABLES.put(ancient_leaves.asItem(), 0.3F);
			ComposterBlock.COMPOSTABLES.put(ancient_fruit.asItem(), 0.65F);
			
			this.zeta().fuel.addFuel(ancient_sapling, 100);
		});
	}

	@LoadEvent
	public void register(ZRegister event) {
		// wood //

		woodSet = WoodSetHandler.addWoodSet(event, this, "ancient", MapColor.TERRACOTTA_WHITE, MapColor.TERRACOTTA_WHITE, true);
		ancient_leaves = new ZetaLeavesBlock(woodSet.name, this, MapColor.PLANT);
		ancient_sapling = new ZetaSaplingBlock("ancient", this, new TreeGrower("ancient_tree", Optional.empty(), Optional.of(configuredFeatureKey), Optional.empty())); //actually called "ancient_sapling"
		event.getVariantRegistry().addFlowerPot(ancient_sapling, "ancient_sapling", Functions.identity()); //actually "potted_ancient_sapling"

		// fruit //

		ancient_fruit = new AncientFruitItem(this);
		event.getAdvancementModifierRegistry().addModifier(new BalancedDietModifier(this, ImmutableSet.of(ancient_fruit)));
		ancientFruitTrigger = event.getAdvancementModifierRegistry().registerManualTrigger("ancient_fruit_overlevel");

		// tree //

		event.getRegistry().register(MultiFoliageStraightTrunkPlacer.TYPE, "multi_foliage_straight_trunk_placer", Registries.TRUNK_PLACER_TYPE);
		event.getRegistry().register(AncientTreeTopperDecorator.TYPE, "ancient_tree_topper_decorator", Registries.TREE_DECORATOR_TYPE);
		//TODO: this can likely be replaced with Fnacy Foliage Placer by inserting the offset somewhere in the trunk placer
		event.getRegistry().register(OffsetFancyFoliagePlacer.TYPE, "offset_fancy_foliage_placer", Registries.FOLIAGE_PLACER_TYPE);

	}

	@PlayEvent
	public void onLootTableLoad(ZLootTableLoad event) {
		int weight = 0;

		if(event.getName().equals(BuiltInLootTables.ANCIENT_CITY))
			weight = ancientCityLootWeight;

		if(event.getName().equals(BuiltInLootTables.SNIFFER_DIGGING))
			weight = sniffingLootWeight;

		if(weight > 0) {
			LootPoolEntryContainer entry = LootItem.lootTableItem(ancient_sapling)
					.setWeight(weight)
					.setQuality(ancientCityLootQuality)
					.build();
			event.add(entry);
		}
	}

}
