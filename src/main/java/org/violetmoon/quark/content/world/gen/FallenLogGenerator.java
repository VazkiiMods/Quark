package org.violetmoon.quark.content.world.gen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import org.violetmoon.quark.content.building.module.HollowLogsModule;
import org.violetmoon.quark.content.world.module.FallenLogsModule;
import org.violetmoon.zeta.config.type.DimensionConfig;
import org.violetmoon.zeta.util.MiscUtil;
import org.violetmoon.zeta.world.generator.Generator;

import java.util.ArrayList;
import java.util.List;

public class FallenLogGenerator extends Generator {

    public FallenLogGenerator(DimensionConfig dimConfig) {
        super(dimConfig);
    }

    @Override
    public void generateChunk(WorldGenRegion worldIn, ChunkGenerator generator, RandomSource rand, BlockPos corner) {
        int x = corner.getX() + rand.nextInt(16);
        int z = corner.getZ() + rand.nextInt(16);
        BlockPos center = new BlockPos(x, 128, z);

        Holder<Biome> biome = getBiome(worldIn, center, false);

        int chance = biome.is(FallenLogsModule.reducedLogsTag) ? FallenLogsModule.sparseBiomeRarity : FallenLogsModule.rarity;
        if (rand.nextInt(chance) == 0) {
            BlockPos pos = worldIn.getHeightmapPos(Types.OCEAN_FLOOR_WG, center);
            placeFallenLogAt(worldIn, pos);
        }
    }

    private static void placeFallenLogAt(LevelAccessor level, BlockPos pos) {
        placeFallenLogAt(level, pos, getLogBlockForPos(level, pos));
    }

    private static void placeFallenLogAt(LevelAccessor level, BlockPos pos, Block logBlock) {
        if (logBlock == Blocks.AIR)
            return;

        final int attempts = 5;

        BlockState state = logBlock.defaultBlockState();
        RandomSource rand = level.getRandom();

        for (int attempt = 0; attempt < attempts; attempt++) {
            int dirOrd = rand.nextInt(MiscUtil.HORIZONTALS.length);
            Direction dir = MiscUtil.HORIZONTALS[dirOrd];
            state = state.setValue(RotatedPillarBlock.AXIS, dir.getAxis());

            int len = 3 + rand.nextInt(2);

            if (!isPositionValid(level, pos, len, dir)) {
                continue;
            }

            createLog(level, pos, len, dir, state, rand, dirOrd);

            return;
        }
    }

    private static void createLog(LevelAccessor level, BlockPos pos, int len, Direction dir, BlockState state, RandomSource rand, int dirOrd) {
        List<Decor> decor = Decor.get(level, pos);
		for (int i = 0; i < len; i++) {
            BlockPos placePos = pos.relative(dir, i);
            level.setBlock(placePos, state, 3);

            if (rand.nextInt(10) < 7 && decor.contains(Decor.MOSS_CARPET)) {
                BlockPos abovePos = placePos.above();
                BlockState aboveState = level.getBlockState(abovePos);
                if (aboveState.isAir()) {
                    level.setBlock(abovePos, Blocks.MOSS_CARPET.defaultBlockState(), 3);
                }
            }

            final Direction[][] sideDirections = {
                    {Direction.EAST, Direction.WEST},
                    {Direction.EAST, Direction.WEST},
                    {Direction.NORTH, Direction.SOUTH},
                    {Direction.NORTH, Direction.SOUTH}
            };

            for (int j = 0; j < 2; j++)
                if (rand.nextInt(5) < 3) {
                    Direction side = sideDirections[dirOrd][j];
                    BlockPos sidePos = placePos.relative(side);
                    placeDecorIfPossible(level, rand, side, sidePos, decor);
                }

            if (rand.nextInt(10) < 4)
                placeDecorIfPossible(level, rand, dir, pos.relative(dir.getOpposite()), decor);
            if (rand.nextInt(10) < 4)
                placeDecorIfPossible(level, rand, dir.getOpposite(), pos.relative(dir, len), decor);
        }
    }

    private static boolean isPositionValid(LevelAccessor level, BlockPos pos, int len, Direction dir) {
        boolean canSpawnOnWater = level.getRandom().nextFloat() < FallenLogsModule.onWaterChance;
        for (int i = 0; i < len; i++) {
            BlockPos testPos = pos.relative(dir, i);
            BlockState testState = level.getBlockState(testPos);

            if (!testState.isAir() && !testState.canBeReplaced() && !testState.is(BlockTags.FLOWERS)) {
                return false;
            }

            BlockPos belowPos = testPos.below();
            BlockState belowState = level.getBlockState(belowPos);

            if (canSpawnOnWater && belowState.is(Blocks.WATER)){
                continue;
            }
            if (!belowState.is(FallenLogsModule.canSpawnOnTag)) {
                return false;
            }
        }
        return true;
    }

    private static void placeDecorIfPossible(LevelAccessor level, RandomSource rand, Direction side, BlockPos sidePos, List<Decor> decor) {
        if(decor.isEmpty()) return;
        BlockState sideState = level.getBlockState(sidePos);
        if (sideState.isAir()) {
            Decor selected = decor.get(rand.nextInt(decor.size()));
            BlockState placeState = switch (selected) {
				case MOSS_CARPET -> Blocks.MOSS_CARPET.defaultBlockState();
				case VINE -> Blocks.VINE.defaultBlockState()
						.setValue(VineBlock.getPropertyForFace(side.getOpposite()), true);
                case FERN -> Blocks.FERN.defaultBlockState();
            };
            if (placeState.canSurvive(level, sidePos) &&
                    //moss is dumb and will spawn on water
                    (selected != Decor.MOSS_CARPET || level.getBlockState(sidePos.below()).isSolid()))
                level.setBlock(sidePos, placeState, 3);
        }
    }

    private static Block getLogBlockForPos(LevelAccessor level, BlockPos pos) {
        Block base = getBaseLogBlockForPos(level, pos);

        if (HollowLogsModule.staticEnabled && level.getRandom().nextFloat() < FallenLogsModule.hollowChance){
            Block hollow = HollowLogsModule.logMap.get(base);
            if (hollow != null)
                return hollow;
        }

        return base;
    }

    private static Block getBaseLogBlockForPos(LevelAccessor level, BlockPos pos) {
        Holder<Biome> biome = level.getBiome(pos);
        List<Block> matched = new ArrayList<>();

        for (var e : FallenLogsModule.blocksPerTag.entrySet())
            if (biome.is(e.getKey()))
                matched.add(e.getValue());

        if (matched.isEmpty())
            return Blocks.AIR;

        return matched.get(level.getRandom().nextInt(matched.size()));
    }

    private enum Decor {
        MOSS_CARPET, VINE, FERN;

        public static List<Decor> get(LevelAccessor level, BlockPos pos) {
            Holder<Biome> b = level.getBiome(pos);
            var biome = b.value();
			List<Decor> list = new ArrayList<>();
            var climateSettings = biome.getModifiedClimateSettings();
            double temperature = Mth.clamp(climateSettings.temperature(), 0.0F, 1.0F);
            double humidify = Mth.clamp(climateSettings.downfall(), 0.0F, 1.0F);
            humidify *= temperature;

            if(temperature < 0.3) {
				list.add(FERN);
			}
			if(humidify > 0.5) {
				list.add(MOSS_CARPET);
			}
			if(humidify > 0.75) {
				list.add(VINE);
			}
			return list.stream().toList();
        }
    }

}
