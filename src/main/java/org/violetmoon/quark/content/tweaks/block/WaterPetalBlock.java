package org.violetmoon.quark.content.tweaks.block;

import java.util.function.BooleanSupplier;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;
import org.violetmoon.zeta.block.IZetaBlock;
import org.violetmoon.zeta.module.ZetaModule;
import org.violetmoon.zeta.registry.RenderLayerRegistry;
import org.violetmoon.zeta.util.BooleanSuppliers;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PinkPetalsBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

public class WaterPetalBlock extends PinkPetalsBlock implements IZetaBlock {

	private final Item base;
    private final @Nullable ZetaModule module;
    private BooleanSupplier enabledSupplier = BooleanSuppliers.TRUE;

    public WaterPetalBlock(Item base, String regname, @Nullable ZetaModule module, Properties properties) {
        super(properties);
        this.base = base;
        this.module = module;

        if(module == null) //auto registration below this line
            return;

        module.zeta.registry.registerBlock(this, regname, false);

        module.zeta.renderLayerRegistry.put(this, RenderLayerRegistry.Layer.CUTOUT);
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, LevelReader level, BlockPos pos, Player player) {
        return new ItemStack(base);
    }
    
    @Override
    public boolean canSurvive(BlockState pState, LevelReader pLevel, BlockPos pPos) {
        BlockPos blockpos = pPos.below();
        BlockState state = pLevel.getBlockState(blockpos);
        if(state.getBlock() == Blocks.WATER) {
        	FluidState fluid = pLevel.getFluidState(blockpos);
        	return fluid.isSource();
        }
        
        return false;
    }
    
    @Override
    public WaterPetalBlock setCondition(BooleanSupplier enabledSupplier) {
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
