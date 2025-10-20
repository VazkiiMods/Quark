package org.violetmoon.quark.content.world.feature;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FancyFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;

// Literally FancyFoliagePlacer but applies a builtin offset since we cant offset it with negative values. Yes this has been cheating since it was added since coded wouldnt trip and make it fail cause of negative value
public class OffsetFancyFoliagePlacer extends FancyFoliagePlacer {

    public static final MapCodec<OffsetFancyFoliagePlacer> CODEC = RecordCodecBuilder.mapCodec((i) ->
            blobParts(i).apply(i, OffsetFancyFoliagePlacer::new));

    public static final FoliagePlacerType<OffsetFancyFoliagePlacer> TYPE = new FoliagePlacerType<>(CODEC);

    public OffsetFancyFoliagePlacer(IntProvider intProvider, IntProvider intProvider1, int i) {
        super(intProvider, intProvider1, i);
    }

    @Override
    protected FoliagePlacerType<?> type() {
        return TYPE;
    }

    @Override
    protected void createFoliage(LevelSimulatedReader levelSimulatedReader, FoliageSetter foliageSetter, RandomSource randomSource, TreeConfiguration treeConfiguration, int i, FoliageAttachment foliageAttachment, int i1, int i2, int offset) {
        super.createFoliage(levelSimulatedReader, foliageSetter, randomSource, treeConfiguration, i, foliageAttachment, i1, i2,  -3);
    }
}
