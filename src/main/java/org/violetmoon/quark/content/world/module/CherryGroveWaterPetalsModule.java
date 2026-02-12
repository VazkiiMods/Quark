package org.violetmoon.quark.content.world.module;

import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.neoforged.neoforge.common.Tags;
import org.violetmoon.quark.base.util.QuarkWorldGenWeights;
import org.violetmoon.quark.content.world.gen.CherryGroveWaterPetalsGenerator;
import org.violetmoon.zeta.config.Config;
import org.violetmoon.zeta.config.type.CompoundBiomeConfig;
import org.violetmoon.zeta.config.type.DimensionConfig;
import org.violetmoon.zeta.event.bus.LoadEvent;
import org.violetmoon.zeta.event.load.ZCommonSetup;
import org.violetmoon.zeta.event.load.ZConfigChanged;
import org.violetmoon.zeta.module.ZetaLoadModule;
import org.violetmoon.zeta.module.ZetaModule;
import org.violetmoon.zeta.world.WorldGenHandler;

@ZetaLoadModule(category = "world", antiOverlap = {"pinkpetalcarpet", "hanaikada"})
public class CherryGroveWaterPetalsModule extends ZetaModule {

    public static boolean staticEnabled;

    @Config(description = "The chance for a Cherry Grove chunk to attempt to spawn petals on top of water (1 is 100%, 0 is 0%)")
    public static double chancePerChunk = 0.5;

    @Config(description = "Should a chunk have water petals, how many would the generator try to place")
    public static int triesPerChunk = 1;

    @Config(description = "The 'size' of groups of petals")
    public static int size = 3;

    @Config(description = "Groups of petals will be +/- this size, set to 0 to disable variation")
    public static int sizeVariation = 1;

    @Config(description = "If true, Cherry Leaf Carpet from Leaf Carpet Module (from Building category) will be used instead of Pink Petals, assuming it is enabled; if false, requires Petals On Water Module (from Tweaks category) to be enabled")
    public static boolean useCarpet = false;

    @Config
    public static CompoundBiomeConfig biomes = CompoundBiomeConfig.fromBiomeReslocs(false, "minecraft:cherry_grove");

    @Config
    public static DimensionConfig dimensions = DimensionConfig.overworld(false);

    @LoadEvent
    public final void setup(ZCommonSetup event) {
        WorldGenHandler.addGenerator(this, new CherryGroveWaterPetalsGenerator(dimensions), GenerationStep.Decoration.TOP_LAYER_MODIFICATION, QuarkWorldGenWeights.CHERRY_WATER_PETALS);
    }

    @LoadEvent
    public final void configChanged(ZConfigChanged event) {
        staticEnabled = isEnabled();
    }
}
