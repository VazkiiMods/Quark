package org.violetmoon.quark.base.block;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.RotatedPillarBlock;
import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.base.handler.CreativeTabHandler;
import org.violetmoon.zeta.module.ZetaModule;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.BooleanSupplier;

public class QuarkPillarBlock extends RotatedPillarBlock implements IQuarkBlock {

	private final ZetaModule module;
	private BooleanSupplier enabledSupplier = () -> true;

	public QuarkPillarBlock(String regname, ZetaModule module, CreativeModeTab creativeTab, Properties properties) {
		super(properties);
		this.module = module;
		Quark.ZETA.registry.registerBlock(this, regname, true);

		CreativeTabHandler.addTab(this, creativeTab);
	}

	@Override
	public void fillItemCategory(@Nonnull CreativeModeTab group, @Nonnull NonNullList<ItemStack> items) {
		if(isEnabled() || group == CreativeModeTab.TAB_SEARCH)
			super.fillItemCategory(group, items);
	}

	@Nullable
	@Override
	public ZetaModule getModule() {
		return module;
	}

	@Override
	public QuarkPillarBlock setCondition(BooleanSupplier enabledSupplier) {
		this.enabledSupplier = enabledSupplier;
		return this;
	}

	@Override
	public boolean doesConditionApply() {
		return enabledSupplier.getAsBoolean();
	}
}
