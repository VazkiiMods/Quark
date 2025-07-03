package org.violetmoon.quark.base.handler;

import com.google.common.collect.ImmutableSet;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.common.ItemAbilities;
import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.base.client.render.QuarkBoatRenderer;
import org.violetmoon.quark.base.item.boat.QuarkBoat;
import org.violetmoon.quark.base.item.boat.QuarkBoatDispenseItemBehavior;
import org.violetmoon.quark.base.item.boat.QuarkBoatItem;
import org.violetmoon.quark.base.item.boat.QuarkChestBoat;
import org.violetmoon.quark.content.building.block.HollowLogBlock;
import org.violetmoon.quark.content.building.block.VariantBookshelfBlock;
import org.violetmoon.quark.content.building.block.VariantLadderBlock;
import org.violetmoon.quark.content.building.block.WoodPostBlock;
import org.violetmoon.quark.content.building.module.*;
import org.violetmoon.zeta.block.*;
import org.violetmoon.zeta.client.event.load.ZClientSetup;
import org.violetmoon.zeta.event.bus.LoadEvent;
import org.violetmoon.zeta.event.load.ZCommonSetup;
import org.violetmoon.zeta.event.load.ZRegister;
import org.violetmoon.zeta.item.ZetaHangingSignItem;
import org.violetmoon.zeta.item.ZetaSignItem;
import org.violetmoon.zeta.module.ZetaModule;
import org.violetmoon.zeta.registry.CreativeTabManager;
import org.violetmoon.zeta.util.BooleanSuppliers;
import org.violetmoon.zeta.util.handler.ToolInteractionHandler;

import java.util.*;
import java.util.stream.Stream;

public class WoodSetHandler {

	public record QuarkBoatType(String name, Item boat, Item chestBoat, Block planks) {
	}

	private static final Map<String, QuarkBoatType> quarkBoatTypes = new HashMap<>();

	public static EntityType<QuarkBoat> quarkBoatEntityType = null;
	public static EntityType<QuarkChestBoat> quarkChestBoatEntityType = null;

	private static final List<WoodSet> woodSets = new ArrayList<>();

	@LoadEvent
	public static void register(ZRegister event) {
		quarkBoatEntityType = EntityType.Builder.<QuarkBoat>of(QuarkBoat::new, MobCategory.MISC)
				.sized(1.375F, 0.5625F)
				.clientTrackingRange(10)
				.build("quark_boat");

		quarkChestBoatEntityType = EntityType.Builder.<QuarkChestBoat>of(QuarkChestBoat::new, MobCategory.MISC)
				.sized(1.375F, 0.5625F)
				.clientTrackingRange(10)
				.build("quark_chest_boat");

		Quark.ZETA.registry.register(quarkBoatEntityType, "quark_boat", Registries.ENTITY_TYPE);
		Quark.ZETA.registry.register(quarkChestBoatEntityType, "quark_chest_boat", Registries.ENTITY_TYPE);
	}

	@LoadEvent
	public static void setup(ZCommonSetup event) {
		event.enqueueWork(() -> {
			Map<Item, DispenseItemBehavior> registry = DispenserBlock.DISPENSER_REGISTRY;
			for(WoodSet set : woodSets) {
				registry.put(set.boatItem, new QuarkBoatDispenseItemBehavior(set.name, false));
				registry.put(set.chestBoatItem, new QuarkBoatDispenseItemBehavior(set.name, true));
			}
		});
	}

	public static WoodSet addWoodSet(ZRegister event, ZetaModule module, String name, MapColor color, MapColor barkColor, boolean flammable) {
		CreativeTabManager.daisyChain();

		//TODO 1.20: maybe expose stuff like canOpenByHand, sound types, etc
		// builder api might be in order since there's a lot of parameters now :skull:
		BlockSetType setType = new BlockSetType(Quark.MOD_ID + ":" + name);
		SoundType sound = SoundType.WOOD;

		WoodType type = WoodType.register(new WoodType(Quark.MOD_ID + ":" + name, setType));
		WoodSet set = new WoodSet(name, module, type);

		set.log = log(name + "_log", module, color, barkColor).setCreativeTab(CreativeModeTabs.BUILDING_BLOCKS, Blocks.BAMBOO_BLOCK, true);
		set.hollowLog = new HollowLogBlock(set.log, module, flammable).setCondition(() -> Quark.ZETA.modules.isEnabledOrOverlapping(HollowLogsModule.class));
		
		set.wood = new ZetaPillarBlock(name + "_wood", module, OldMaterials.wood().mapColor(barkColor).strength(2.0F).sound(SoundType.WOOD)).setCreativeTab(CreativeModeTabs.BUILDING_BLOCKS);
		set.strippedLog = log("stripped_" + name + "_log", module, color, color).setCreativeTab(CreativeModeTabs.BUILDING_BLOCKS);
		set.strippedWood = new ZetaPillarBlock("stripped_" + name + "_wood", module, OldMaterials.wood().mapColor(color).strength(2.0F).sound(SoundType.WOOD)).setCreativeTab(CreativeModeTabs.BUILDING_BLOCKS);

		set.planks = new ZetaBlock(name + "_planks", module, OldMaterials.wood().mapColor(color).strength(2.0F, 3.0F).sound(SoundType.WOOD)).setCreativeTab(CreativeModeTabs.BUILDING_BLOCKS);

		set.verticalPlanks = VerticalPlanksModule.add(name, set.planks, module).setCondition(() -> Quark.ZETA.modules.isEnabledOrOverlapping(VerticalPlanksModule.class));
		set.slab = event.getVariantRegistry().addSlab((IZetaBlock) set.planks, null).getBlock();
		set.stairs = event.getVariantRegistry().addStairs((IZetaBlock) set.planks, null).getBlock();

		set.fence = new ZetaFenceBlock(name + "_fence", module, OldMaterials.wood().mapColor(color).strength(2.0F, 3.0F).sound(SoundType.WOOD));
		set.fenceGate = new ZetaFenceGateBlock(name + "_fence_gate", module, type, OldMaterials.wood().mapColor(color).strength(2.0F, 3.0F).sound(SoundType.WOOD).forceSolidOn()).setCreativeTab(CreativeModeTabs.BUILDING_BLOCKS);

		set.door = new ZetaDoorBlock(setType, name + "_door", module, OldMaterials.wood().mapColor(color).strength(3.0F).sound(SoundType.WOOD).noOcclusion());
		set.trapdoor = new ZetaTrapdoorBlock(setType, name + "_trapdoor", module, OldMaterials.wood().mapColor(color).strength(3.0F).sound(SoundType.WOOD).noOcclusion().isValidSpawn((s, g, p, e) -> false));

		set.pressurePlate = new ZetaPressurePlateBlock(name + "_pressure_plate", module, setType, OldMaterials.wood().mapColor(color).noCollission().strength(0.5F).sound(SoundType.WOOD));
		set.button = new ZetaWoodenButtonBlock(setType, name + "_button", module, OldMaterials.decoration().noCollission().strength(0.5F).sound(SoundType.WOOD));

		CreativeTabManager.endDaisyChain();

		((IZetaBlock) set.log).setCreativeTab(CreativeModeTabs.NATURAL_BLOCKS, Blocks.WARPED_STEM, false);

		set.sign = new ZetaStandingSignBlock(name + "_sign", module, type, OldMaterials.wood().forceSolidOn().mapColor(color).noCollission().strength(1.0F).sound(SoundType.WOOD));
		set.wallSign = new ZetaWallSignBlock(name + "_wall_sign", module, type, OldMaterials.wood().forceSolidOn().mapColor(color).noCollission().strength(1.0F).sound(SoundType.WOOD).lootFrom(() -> set.sign));

		set.ceilingHangingSign = new ZetaCeilingHangingSignBlock(name + "_hanging_sign", module, type, OldMaterials.wood().forceSolidOn().mapColor(color).noCollission().strength(1.0F).sound(SoundType.WOOD));
		set.wallHangingSign = new ZetaWallHangingSignBlock(name + "_wall_hanging_sign", module, type, OldMaterials.wood().forceSolidOn().mapColor(color).noCollission().strength(1.0F).sound(SoundType.WOOD).lootFrom(() -> set.sign));

		set.bookshelf = new VariantBookshelfBlock(name, module, true, sound).setCondition(() -> Quark.ZETA.modules.isEnabledOrOverlapping(VariantBookshelvesModule.class));
		set.ladder = new VariantLadderBlock(name, module, Block.Properties.ofFullCopy(Blocks.LADDER).sound(sound), true).setCondition(() -> Quark.ZETA.modules.isEnabledOrOverlapping(VariantLaddersModule.class));

		set.post = new WoodPostBlock(module, set.fence, "", sound).setCondition(() -> Quark.ZETA.modules.isEnabledOrOverlapping(WoodenPostsModule.class));
		set.strippedPost = new WoodPostBlock(module, set.fence, "stripped_", sound).setCondition(() -> Quark.ZETA.modules.isEnabledOrOverlapping(WoodenPostsModule.class));

		VariantChestsModule.makeChestBlocksExternal(module, name, Blocks.CHEST, sound, BooleanSuppliers.TRUE);

		set.signItem = new ZetaSignItem(module, set.sign, set.wallSign);
		set.hangingSignItem = new ZetaHangingSignItem(module, set.ceilingHangingSign, set.wallHangingSign);

		set.boatItem = new QuarkBoatItem(name, module, false);
		set.chestBoatItem = new QuarkBoatItem(name, module, true);

		makeSignWork(set.sign, set.wallSign, set.ceilingHangingSign, set.wallHangingSign);

		ToolInteractionHandler.registerInteraction(ItemAbilities.AXE_STRIP, set.log, set.strippedLog);
		ToolInteractionHandler.registerInteraction(ItemAbilities.AXE_STRIP, set.wood, set.strippedWood);
		ToolInteractionHandler.registerInteraction(ItemAbilities.AXE_STRIP, set.post, set.strippedPost);

		VariantLaddersModule.variantLadders.add(set.ladder);

		Quark.ZETA.fuel.addFuel(set.boatItem, 60 * 20);
		Quark.ZETA.fuel.addFuel(set.chestBoatItem, 60 * 20);

		addQuarkBoatType(name, new QuarkBoatType(name, set.boatItem, set.chestBoatItem, set.planks));

		woodSets.add(set);

		return set;
	}

	public static void makeSignWork(Block sign, Block wallSign, Block hangingSign, Block wallHangingSign) {
		Set<Block> validBlocks = new HashSet<>();
		validBlocks.add(sign);
		validBlocks.add(wallSign);
		validBlocks.addAll(BlockEntityType.SIGN.validBlocks);
		BlockEntityType.SIGN.validBlocks = ImmutableSet.copyOf(validBlocks);

		validBlocks.clear();
		validBlocks.add(hangingSign);
		validBlocks.add(wallHangingSign);
		validBlocks.addAll(BlockEntityType.HANGING_SIGN.validBlocks);
		BlockEntityType.HANGING_SIGN.validBlocks = ImmutableSet.copyOf(validBlocks);
	}

	private static ZetaPillarBlock log(String name, ZetaModule module, MapColor topColor, MapColor sideColor) {
		return new ZetaPillarBlock(name, module,
				OldMaterials.wood()
						.mapColor(s -> s.getValue(RotatedPillarBlock.AXIS) == Direction.Axis.Y ? topColor : sideColor)
						.strength(2.0F).sound(SoundType.WOOD));
	}

	public static void addQuarkBoatType(String name, QuarkBoatType type) {
		quarkBoatTypes.put(name, type);
	}

	public static QuarkBoatType getQuarkBoatType(String name) {
		return quarkBoatTypes.get(name);
	}

	public static Stream<String> boatTypes() {
		return quarkBoatTypes.keySet().stream();
	}

	public static class WoodSet {

		public final String name;
		public final WoodType type;
		public final ZetaModule module;

		public Block log, wood, planks, strippedLog, strippedWood,
				slab, stairs, fence, fenceGate,
				door, trapdoor, button, pressurePlate, sign, wallSign,
				ceilingHangingSign, wallHangingSign, bookshelf, ladder, post,
				strippedPost, verticalPlanks, hollowLog;

		public Item signItem, boatItem, chestBoatItem, hangingSignItem;

		public WoodSet(String name, ZetaModule module, WoodType type) {
			this.name = name;
			this.module = module;
			this.type = type;
		}

		public List<Block> allBlocks() {
			return List.of(log, wood, planks, strippedLog, strippedWood, slab, stairs, fence, fenceGate, door, trapdoor, button, pressurePlate, sign, wallSign, ceilingHangingSign, wallHangingSign, bookshelf, ladder, post, strippedPost, verticalPlanks, hollowLog);
		}

		public List<Block> allLogs() {
			return List.of(log, wood, strippedLog, strippedWood);
		}

	}

	public static class Client {
		@LoadEvent
		public static void clientSetup(ZClientSetup event) {
			EntityRenderers.register(quarkBoatEntityType, r -> new QuarkBoatRenderer(r, false));
			EntityRenderers.register(quarkChestBoatEntityType, r -> new QuarkBoatRenderer(r, true));

			event.enqueueWork(() -> {
				for(WoodSet set : woodSets) {
					Sheets.addWoodType(set.type);
				}
			});
		}
	}
}
