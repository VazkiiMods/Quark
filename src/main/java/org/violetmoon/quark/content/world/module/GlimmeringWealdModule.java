package org.violetmoon.quark.content.world.module;

import com.google.common.collect.ImmutableSet;
import com.terraformersmc.biolith.api.biome.BiomePlacement;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.content.world.block.GlowLichenGrowthBlock;
import org.violetmoon.quark.content.world.block.GlowShroomBlock;
import org.violetmoon.quark.content.world.block.GlowShroomRingBlock;
import org.violetmoon.quark.content.world.block.HugeGlowShroomBlock;
import org.violetmoon.quark.content.world.feature.GlowExtrasFeature;
import org.violetmoon.quark.content.world.feature.GlowShroomsFeature;
import org.violetmoon.zeta.advancement.modifier.AdventuringTimeModifier;
import org.violetmoon.zeta.event.bus.LoadEvent;
import org.violetmoon.zeta.event.load.ZCommonSetup;
import org.violetmoon.zeta.event.load.ZRegister;
import org.violetmoon.zeta.module.ZetaLoadModule;
import org.violetmoon.zeta.module.ZetaModule;
import org.violetmoon.zeta.registry.CreativeTabManager;
import org.violetmoon.zeta.util.Hint;

@ZetaLoadModule(category = "world")
public class GlimmeringWealdModule extends ZetaModule {

    public static final ResourceLocation BIOME_NAME = Quark.asResource("glimmering_weald");
    public static final ResourceKey<Biome> BIOME_KEY = Quark.asResourceKey(Registries.BIOME, "glimmering_weald");

	public static ResourceKey<PlacedFeature> GLOW_SHROOMS_FEATURE = Quark.asResourceKey(Registries.PLACED_FEATURE, "glow_shrooms");
	public static ResourceKey<PlacedFeature> GLOW_SHROOMS_EXTRAS_FEATURE = Quark.asResourceKey(Registries.PLACED_FEATURE, "glow_shrooms_extras");

    public static GlowShroomsFeature glow_shrooms_feature;
    public static GlowExtrasFeature glow_shrooms_extras_feature;

    public static Holder<PlacedFeature> ore_lapis_extra;
    public static Holder<PlacedFeature> placed_glow_shrooms;
    public static Holder<PlacedFeature> placed_glow_extras;

    @Hint
    public static Block glow_shroom;
    @Hint
    public static Block glow_lichen_growth;
    public static Block glow_shroom_block;
    public static Block glow_shroom_stem;
    public static Block glow_shroom_ring;

    public static TagKey<Item> glowShroomFeedablesTag;

    //TODO: add back some sort of config

    /*@Config(
            name = "Min Depth Range",
            description = "Experimental, dont change if you dont know what you are doing. Depth min value from which biome will spawn. Decreasing will make biome appear more often"
    )
    @Config.Min(-2)
    @Config.Max(2)
    public static double minDepthRange = 1.55F;


    @Config(
            name = "Max Weirdness Range",
            description = "Experimental, dont change if you dont know what you are doing. Depth max value until which biome will spawn. Increasing will make biome appear more often"
    )
    @Config.Min(-2)
    @Config.Max(2)
    public static double maxWeirdnessRange = 2;*/

    @LoadEvent
    public final void register(ZRegister event) {
        CreativeTabManager.startChain(CreativeModeTabs.NATURAL_BLOCKS, false, false, Blocks.HANGING_ROOTS);

        glow_shroom = new GlowShroomBlock(this).setCreativeTab(CreativeModeTabs.NATURAL_BLOCKS, Blocks.HANGING_ROOTS, false);
        glow_shroom_block = new HugeGlowShroomBlock("glow_shroom_block", this, true);
        glow_shroom_stem = new HugeGlowShroomBlock("glow_shroom_stem", this, false);
        glow_shroom_ring = new GlowShroomRingBlock(this);
        glow_lichen_growth = new GlowLichenGrowthBlock(this);
        CreativeTabManager.endChain();


        event.getVariantRegistry().addFlowerPot(glow_lichen_growth, "glow_lichen_growth", prop -> prop.lightLevel((state) -> 8));
        event.getVariantRegistry().addFlowerPot(glow_shroom, "glow_shroom", prop -> prop.lightLevel((state) -> 10));


        // Feature
        glow_shrooms_feature = new GlowShroomsFeature();
        event.getRegistry().register(glow_shrooms_feature, GLOW_SHROOMS_FEATURE.location(), Registries.FEATURE);
        glow_shrooms_extras_feature = new GlowExtrasFeature();
        event.getRegistry().register(glow_shrooms_extras_feature, GLOW_SHROOMS_EXTRAS_FEATURE.location(), Registries.FEATURE);
    }

    @LoadEvent
    public void postRegister(ZRegister.Post e) {
        Climate.Parameter FULL_RANGE = Climate.Parameter.span(-1.0F, 1.0F);
        //Climate.Parameter weirdnessRange = Climate.Parameter.span(-1, maxWeirdnessRange)''
		Climate.ParameterPoint climatePoint = Climate.parameters(FULL_RANGE, FULL_RANGE,
                Climate.Parameter.span(-1.2f, 0.75f), //set like that to stay away from dripstone caves
				Climate.Parameter.span(-0.28f, 0.08f), //erosion. a bit after deep dark biome
                //decrease min depth to make it bigger. prob best place to do so at least
				Climate.Parameter.span(0.95f, 1.1f), //depth. A bit lower depth to accommodate for erosion being a bit higher
				FULL_RANGE, //weirdness
				0F);

        BiomePlacement.addOverworld(BIOME_KEY, climatePoint);

        Quark.ZETA.advancementModifierRegistry.addModifier(new AdventuringTimeModifier(this, ImmutableSet.of(BIOME_KEY)));
    }

    @LoadEvent
    public void setup(ZCommonSetup e) {
        glowShroomFeedablesTag = Quark.asTagKey(Registries.ITEM,"glow_shroom_feedables");

        e.enqueueWork(() -> {
            ComposterBlock.COMPOSTABLES.put(glow_shroom.asItem(), 0.65F);
            ComposterBlock.COMPOSTABLES.put(glow_shroom_block.asItem(), 0.65F);
            ComposterBlock.COMPOSTABLES.put(glow_shroom_stem.asItem(), 0.65F);
            ComposterBlock.COMPOSTABLES.put(glow_shroom_ring.asItem(), 0.65F);

            ComposterBlock.COMPOSTABLES.put(glow_lichen_growth.asItem(), 0.5F);
        });
    }
}
