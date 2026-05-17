package org.violetmoon.quark.integration.lootr;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import noobanidus.mods.lootr.common.api.replacement.BlockReplacementMap;
import org.jetbrains.annotations.Nullable;
import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.base.util.BlockPropertyUtil;
import org.violetmoon.zeta.module.ZetaModule;

import java.util.*;
import java.util.function.BooleanSupplier;

/**
 * @author WireSegal
 *         Created at 11:40 AM on 7/3/23.
 */
public class LootrIntegration implements ILootrIntegration {

	public BlockEntityType<LootrVariantChestBlockEntity> chestTEType;
	public BlockEntityType<LootrVariantTrappedChestBlockEntity> trappedChestTEType;

	public final List<Block> lootrRegularChests = new ArrayList<>();
	public final List<Block> lootrTrappedChests = new ArrayList<>();

	@Override
	public BlockEntityType<? extends ChestBlockEntity> chestTE() {
		return chestTEType;
	}

	@Override
	public BlockEntityType<? extends ChestBlockEntity> trappedChestTE() {
		return trappedChestTEType;
	}

	@Override
	public void makeChestBlocks(ZetaModule module, String name, Block base, BooleanSupplier condition, Block quarkRegularChest, Block quarkTrappedChest) {
		Block lootrRegularChest = new LootrVariantChestBlock(name, module, () -> chestTEType,
				BlockPropertyUtil.copyPropertySafe(base)).setCondition(condition);
		lootrRegularChests.add(lootrRegularChest);

        QuarkLootrBlockReplacementProvider.addMapping(quarkRegularChest, lootrRegularChest);

		Block lootrTrappedChest = new LootrVariantTrappedChestBlock(name, module, () -> trappedChestTEType,
				BlockPropertyUtil.copyPropertySafe(base)).setCondition(condition);
        lootrTrappedChests.add(lootrTrappedChest);

        QuarkLootrBlockReplacementProvider.addMapping(quarkTrappedChest, lootrTrappedChest);
    }

	@Override
	@Nullable
	public Block lootrVariant(Block base) {
		return base;
	}

	public void populate(BlockReplacementMap map) {
	}

	@Override
	public void postRegister() {
		chestTEType = BlockEntityType.Builder.of(LootrVariantChestBlockEntity::new, lootrRegularChests.toArray(new Block[0])).build(null);
		trappedChestTEType = BlockEntityType.Builder.of(LootrVariantTrappedChestBlockEntity::new, lootrTrappedChests.toArray(new Block[0])).build(null);

		Quark.ZETA.registry.register(chestTEType, "lootr_variant_chest", Registries.BLOCK_ENTITY_TYPE);
		Quark.ZETA.registry.register(trappedChestTEType, "lootr_variant_trapped_chest", Registries.BLOCK_ENTITY_TYPE);
	}

}
