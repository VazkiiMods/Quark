package org.violetmoon.quark.base.util;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

// todo: move this into a better class and revisit in 1.21 as there will be more to add here
public class BlockPropertyUtil {

    /**
     * Like Property.copy but only copies ones that do not contain a funciton as those are not safe to copy since they could reference states we dont have
     */
    public static BlockBehaviour.Properties copyPropertySafe(Block blockBehaviour) {
        var p = BlockBehaviour.Properties.ofFullCopy(blockBehaviour);
        BlockState state = blockBehaviour.defaultBlockState();
        p.lightLevel(s -> state.getLightEmission());
        p.offsetType(BlockBehaviour.OffsetType.NONE);
        //default function. not optimal
        p.isValidSpawn( (blockState, blockGetter, pos, entityType) ->
                blockState.isFaceSturdy(blockGetter, pos, Direction.UP) && blockState.getLightEmission(blockGetter, pos) < 14);
        p.mapColor(blockBehaviour.defaultMapColor());
        p.emissiveRendering((blockState, blockGetter, blockPos) -> false);
        return p;
    }
}
