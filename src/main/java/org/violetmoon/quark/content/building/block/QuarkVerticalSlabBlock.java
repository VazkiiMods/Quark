package org.violetmoon.quark.content.building.block;

import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;

import org.jetbrains.annotations.Nullable;

import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.base.util.BlockPropertyUtil;
import org.violetmoon.quark.content.building.module.VerticalSlabsModule;
import org.violetmoon.zeta.block.IZetaBlock;
import org.violetmoon.zeta.block.ZetaSlabBlock;
import org.violetmoon.zeta.module.ZetaModule;
import org.violetmoon.zeta.registry.CreativeTabManager;
import org.violetmoon.zeta.util.BooleanSuppliers;

import java.util.function.BooleanSupplier;

public class QuarkVerticalSlabBlock extends VerticalSlabBlock implements IZetaBlock {

	private final ZetaModule module;
	private BooleanSupplier enabledSupplier = BooleanSuppliers.TRUE;

	public QuarkVerticalSlabBlock(Block parent, ZetaModule module) {
		super(() -> parent, BlockPropertyUtil.copyPropertySafe(parent));
		String resloc = Quark.ZETA.registryUtil.inherit(parent, s -> s.replace("_slab", "_vertical_slab"));
		Quark.ZETA.registry.registerBlock(this, resloc, true);

		this.module = module;

		if(!(parent instanceof SlabBlock))
			throw new IllegalArgumentException("Can't rotate a non-slab block into a vertical slab.");

		if(parent instanceof ZetaSlabBlock quarkSlab)
			setCondition(quarkSlab.parent::isEnabled);

		CreativeTabManager.addToCreativeTabNextTo(CreativeModeTabs.BUILDING_BLOCKS, this, parent, false);
		VerticalSlabsModule.blocks.put(parent, this);
	}

	@Override
	public QuarkVerticalSlabBlock setCondition(BooleanSupplier enabledSupplier) {
		this.enabledSupplier = enabledSupplier;
		return this;
	}

	@Override
	public boolean doesConditionApply() {
		return enabledSupplier.getAsBoolean();
	}

	@Nullable
	@Override
	public ZetaModule getModule() {
		return module;
	}

}
