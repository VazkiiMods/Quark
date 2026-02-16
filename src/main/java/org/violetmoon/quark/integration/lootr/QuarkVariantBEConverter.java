package org.violetmoon.quark.integration.lootr;

import net.minecraft.world.level.block.entity.BlockEntityType;
import noobanidus.mods.lootr.common.api.ILootrBlockEntityConverter;
import noobanidus.mods.lootr.common.api.data.blockentity.ILootrBlockEntity;
import org.violetmoon.quark.base.Quark;

public class QuarkVariantBEConverter<T> implements ILootrBlockEntityConverter<T> {

    @Override
    public ILootrBlockEntity apply(T blockEntity) {
        return (ILootrBlockEntity) blockEntity;
    }

    @Override
    public BlockEntityType<?> getBlockEntityType() {
        return Quark.LOOTR_INTEGRATION.chestTE();
    }
}
