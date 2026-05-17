package org.violetmoon.quark.integration.lootr;

import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import noobanidus.mods.lootr.common.api.replacement.ILootrBlockReplacementProvider;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class QuarkLootrBlockReplacementProvider implements ILootrBlockReplacementProvider {
    private static Map<Block, Block> quarkLootrReplacementMappings = new HashMap<>();

    @Override
    public TagKey<Block> getApplicableTag() {
        return null;
    }

    @Override
    public Block getBlock() {
        return null;
    }

    @Override
    @Nullable public Block apply(Block block) {
        return quarkLootrReplacementMappings.get(block);
    }

    public static void addMapping(Block input, Block output) {
        quarkLootrReplacementMappings.put(input, output);
    }
}
