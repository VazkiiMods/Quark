package org.violetmoon.quark.content.building.module;

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.horse.AbstractChestedHorse;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.neoforged.neoforge.common.Tags;
import org.jetbrains.annotations.Nullable;
import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.base.QuarkClient;
import org.violetmoon.quark.base.util.BlockPropertyUtil;
import org.violetmoon.quark.content.building.block.VariantChestBlock;
import org.violetmoon.quark.content.building.block.VariantTrappedChestBlock;
import org.violetmoon.quark.content.building.block.be.VariantChestBlockEntity;
import org.violetmoon.quark.content.building.block.be.VariantTrappedChestBlockEntity;
import org.violetmoon.quark.content.building.client.render.be.VariantChestRenderer;
import org.violetmoon.quark.content.building.recipe.MixedExclusionRecipe;
import org.violetmoon.quark.mixin.mixins.accessor.AccessorAbstractChestedHorse;
import org.violetmoon.zeta.client.SimpleWithoutLevelRenderer;
import org.violetmoon.zeta.client.event.load.ZClientSetup;
import org.violetmoon.zeta.client.event.load.ZRegisterClientExtension;
import org.violetmoon.zeta.client.extensions.IZetaClientItemExtensions;
import org.violetmoon.zeta.config.Config;
import org.violetmoon.zeta.event.bus.LoadEvent;
import org.violetmoon.zeta.event.bus.PlayEvent;
import org.violetmoon.zeta.event.load.ZConfigChanged;
import org.violetmoon.zeta.event.load.ZRegister;
import org.violetmoon.zeta.event.play.entity.ZEntityJoinLevel;
import org.violetmoon.zeta.event.play.entity.living.ZLivingDeath;
import org.violetmoon.zeta.event.play.entity.player.ZPlayerInteract;
import org.violetmoon.zeta.module.ZetaLoadModule;
import org.violetmoon.zeta.module.ZetaModule;
import org.violetmoon.zeta.registry.CreativeTabManager;
import org.violetmoon.zeta.util.BooleanSuppliers;
import org.violetmoon.zeta.util.VanillaWoods;
import org.violetmoon.zeta.util.VanillaWoods.Wood;
import org.violetmoon.zeta.util.handler.StructureBlockReplacementHandler;
import org.violetmoon.zeta.util.handler.StructureBlockReplacementHandler.StructureHolder;

import java.util.*;
import java.util.function.BooleanSupplier;

@ZetaLoadModule(category = "building", antiOverlap = { "woodworks" })
public class VariantChestsModule extends ZetaModule {

	@Config(flag = "chest_reversion")
	private static boolean enableRevertingWoodenChests = true;

	// blocks
	public static final List<Block> regularChests = new ArrayList<>();
	public static final List<Block> trappedChests = new ArrayList<>();

	// the block entity type (all chests share it)
	public static BlockEntityType<VariantChestBlockEntity> chestTEType;
	public static BlockEntityType<VariantTrappedChestBlockEntity> trappedChestTEType;

	// structure replacement stuff
	@Config
	protected boolean replaceWorldgenChests = true;

	@Config(description = "Chests to put in structures. It's preferred to use worldgen tags for this. The format per entry is \"structure=chest\", where \"structure\" is a structure ID, and \"chest\" is a block ID, which must correspond to a standard chest block.")
	public List<String> structureChests = new ArrayList<>();

	protected final Map<ResourceLocation, Block> manualChestMappings = new HashMap<>();
	protected final Map<ResourceLocation, Block> manualTrappedChestMappings = new HashMap<>();
	protected final Map<TagKey<Structure>, Block> chestMappings = new HashMap<>();
	protected final Map<TagKey<Structure>, Block> trappedChestMappings = new HashMap<>();

	// donk chest!
	private static final String DONK_CHEST = "Quark:DonkChest";

	public interface IVariantChest {
		String getTexturePath();

		default String getTextureFolder(){
			return "quark_variant_chests";
		}
	}

	/// BLOCKS ///

	@LoadEvent
	public final void register(ZRegister event) {
		event.getRegistry().register(new MixedExclusionRecipe.Serializer(), "mixed_exclusion", Registries.RECIPE_SERIALIZER);

		for(Wood s : VanillaWoods.ALL)
			makeChestBlocks(s.name(), Blocks.CHEST, s.soundPlanks());
		makeChestBlocks("nether_brick", Blocks.NETHER_BRICKS, null);
		makeChestBlocks("purpur", Blocks.PURPUR_BLOCK, null);
		makeChestBlocks("prismarine", Blocks.PRISMARINE, null);

		CreativeTabManager.daisyChain();
		for(Block regularChest : regularChests)
			CreativeTabManager.addToCreativeTabNextTo(CreativeModeTabs.FUNCTIONAL_BLOCKS, regularChest, Blocks.CHEST, true);
		CreativeTabManager.endDaisyChain();

		CreativeTabManager.daisyChain();
		for(Block trappedChest : trappedChests)
			CreativeTabManager.addToCreativeTabNextTo(CreativeModeTabs.REDSTONE_BLOCKS, trappedChest, Blocks.TRAPPED_CHEST, true);
		CreativeTabManager.endDaisyChain();

		StructureBlockReplacementHandler.addReplacement(this::getGenerationChestBlockState);
	}

	private void makeChestBlocks(String name, Block base, @Nullable SoundType sound) {
		makeChestBlocks(this, name, base, sound, BooleanSuppliers.TRUE);
	}

	private void makeChestBlocks(ZetaModule module, String name, Block base, @Nullable SoundType sound, BooleanSupplier condition) {
		BlockBehaviour.Properties props = BlockPropertyUtil.copyPropertySafe(base);
		if(sound != null)
			props = props.sound(sound);

		VariantChestBlock regularChest = new VariantChestBlock(name, module, () -> chestTEType, props).setCondition(condition);
		regularChests.add(regularChest);
		chestMappings.put(Quark.asTagKey(Registries.STRUCTURE, name + "_chest_structures"), regularChest);

		VariantTrappedChestBlock trappedChest = new VariantTrappedChestBlock(name, module, () -> trappedChestTEType, props).setCondition(condition);
		trappedChests.add(trappedChest);
		trappedChestMappings.put(Quark.asTagKey(Registries.STRUCTURE, name + "_chest_structures"), trappedChest);

		Quark.LOOTR_INTEGRATION.makeChestBlocks(module, name, base, condition, regularChest, trappedChest);
	}

	//only enables the block if the variant chests module is enabled
	public static void makeChestBlocksExternal(ZetaModule module, String name, Block base, @Nullable SoundType sound, BooleanSupplier condition) {
		VariantChestsModule me = Quark.ZETA.modules.get(VariantChestsModule.class);
		me.makeChestBlocks(module, name, base, sound, () -> me.isEnabled() && condition.getAsBoolean());
	}

	/// STUFF that has to happen after all the makeChestBlocks calls are performed...! ///

	@LoadEvent
	public void postRegister(ZRegister.Post e) {
		chestTEType = BlockEntityType.Builder.of(VariantChestBlockEntity::new, regularChests.toArray(new Block[0])).build(null);
		trappedChestTEType = BlockEntityType.Builder.of(VariantTrappedChestBlockEntity::new, trappedChests.toArray(new Block[0])).build(null);

		Quark.ZETA.registry.register(chestTEType, "variant_chest", Registries.BLOCK_ENTITY_TYPE);
		Quark.ZETA.registry.register(trappedChestTEType, "variant_trapped_chest", Registries.BLOCK_ENTITY_TYPE);

		Quark.LOOTR_INTEGRATION.postRegister();
	}

	/// WORLDGEN ///

	@LoadEvent
	public final void configChanged(ZConfigChanged event) {
		manualChestMappings.clear();
		manualTrappedChestMappings.clear();
		List<String> chestsClone = new ArrayList<>(structureChests);

		for(String s : chestsClone) {
			String[] toks = s.split("=");
			if(toks.length == 2) {
				String left = toks[0];
				String right = toks[1];

				Block block = BuiltInRegistries.BLOCK.get(ResourceLocation.parse(right));
				if(block != Blocks.AIR) {
					manualChestMappings.put(ResourceLocation.parse(left), block);
					if(regularChests.contains(block)) {
						var trapped = trappedChests.get(regularChests.indexOf(block));
						manualTrappedChestMappings.put(ResourceLocation.parse(left), trapped);
					}
				}
			}
		}
	}

	private BlockState getGenerationChestBlockState(ServerLevelAccessor accessor, BlockState current, StructureHolder structure) {
		if(isEnabled() && replaceWorldgenChests) {
			if(current.getBlock() == Blocks.CHEST) {
				return replaceChestState(accessor, current, structure, chestMappings, manualChestMappings);
			} else if(current.getBlock() == Blocks.TRAPPED_CHEST) {
				return replaceChestState(accessor, current, structure, trappedChestMappings, manualTrappedChestMappings);
			}
		}

		return null; // no change
	}

	@Nullable
	private BlockState replaceChestState(ServerLevelAccessor accessor, BlockState current, StructureHolder structure, Map<TagKey<Structure>, Block> mappings, Map<ResourceLocation, Block> manualMappings) {
		Holder<Structure> structureHolder = StructureBlockReplacementHandler.getStructure(accessor, structure);
		if(structureHolder != null) {
			for(TagKey<Structure> structureTagKey : mappings.keySet()) {
				if(structureHolder.is(structureTagKey)) {
					Block block = mappings.get(structureTagKey);
					return block.withPropertiesOf(current);
				}
			}

			Optional<Block> manualMapping = structureHolder.unwrapKey().map(ResourceKey::location).map(manualMappings::get);
			if(manualMapping.isPresent())
				return manualMapping.get().withPropertiesOf(current);
		}

		return null; // no change
	}

	/// DONK CHEST ///

	@PlayEvent
	public void onClickEntity(ZPlayerInteract.EntityInteractSpecific event) {
		Entity target = event.getTarget();
		Player player = event.getEntity();
		ItemStack held = player.getItemInHand(event.getHand());

		if(!held.isEmpty() && target instanceof AbstractChestedHorse horse) {

			if(!horse.hasChest() && held.getItem() != Items.CHEST) {
				if(held.is(Tags.Items.CHESTS_WOODEN)) {
					event.setCanceled(true);
					event.setCancellationResult(InteractionResult.sidedSuccess(player.level().isClientSide));

					if(!target.level().isClientSide) {
						ItemStack copy = held.copy();
						copy.setCount(1);
						held.shrink(1);

						horse.getPersistentData().put(DONK_CHEST, copy.save(player.level().registryAccess()));

						horse.setChest(true);
						horse.createInventory();
						((AccessorAbstractChestedHorse) horse).quark$playChestEquipsSound();
					}
				}
			}
		}
	}

	private static final ThreadLocal<ItemStack> WAIT_TO_REPLACE_CHEST = new ThreadLocal<>();

	@PlayEvent
	public void onDeath(ZLivingDeath event) {
		Entity target = event.getEntity();
		if(target instanceof AbstractChestedHorse horse) {
			ItemStack chest = ItemStack.parseOptional(target.level().registryAccess(), horse.getPersistentData().getCompound(DONK_CHEST));
			if(!chest.isEmpty() && horse.hasChest())
				WAIT_TO_REPLACE_CHEST.set(chest);
		}
	}

	@PlayEvent
	public void onEntityJoinWorld(ZEntityJoinLevel event) {
		Entity target = event.getEntity();
		if(target instanceof ItemEntity item && item.getItem().getItem() == Items.CHEST) {
			ItemStack local = WAIT_TO_REPLACE_CHEST.get();
			if(local != null && !local.isEmpty())
				((ItemEntity) target).setItem(local);
			WAIT_TO_REPLACE_CHEST.remove();
		}
	}

	/// CLIENT STUFF ///

	@ZetaLoadModule(clientReplacement = true)
	public static class Client extends VariantChestsModule {

		@LoadEvent
		public final void clientSetup(ZClientSetup event) {
			BlockEntityRenderers.register(chestTEType, ctx -> new VariantChestRenderer(ctx, false));
			BlockEntityRenderers.register(trappedChestTEType, ctx -> new VariantChestRenderer(ctx, true));

			QuarkClient.LOOTR_INTEGRATION.clientSetup(event);
		}

		@LoadEvent
		public void setItemExtensions(ZRegisterClientExtension event) {
			for (Block b : regularChests) {
				event.registerItem(new IZetaClientItemExtensions() {
					@Override
					public BlockEntityWithoutLevelRenderer getBEWLR() {
						return new SimpleWithoutLevelRenderer(chestTEType, b.defaultBlockState());
					}
				}, b.asItem());
			}

			for (Block b : trappedChests) {
				event.registerItem(new IZetaClientItemExtensions() {
					@Override
					public BlockEntityWithoutLevelRenderer getBEWLR() {
						return new SimpleWithoutLevelRenderer(trappedChestTEType, b.defaultBlockState());
					}
				}, b.asItem());
			}
		}
	}
}
