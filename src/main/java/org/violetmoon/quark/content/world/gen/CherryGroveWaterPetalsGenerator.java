package org.violetmoon.quark.content.world.gen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PinkPetalsBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.content.building.module.LeafCarpetModule;
import org.violetmoon.quark.content.tweaks.module.PetalsOnWaterModule;
import org.violetmoon.quark.content.world.module.CherryGroveWaterPetalsModule;
import org.violetmoon.zeta.config.type.DimensionConfig;
import org.violetmoon.zeta.world.generator.Generator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CherryGroveWaterPetalsGenerator extends Generator {

    protected static final Direction[] CARDINAL_DIRECTIONS = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};

    public static Map<Direction, BlockState> edgeStates = new HashMap<>();

    static {
        edgeStates.put(Direction.NORTH, makeEdgeState(Direction.WEST));
        edgeStates.put(Direction.EAST, makeEdgeState(Direction.NORTH));
        edgeStates.put(Direction.SOUTH, makeEdgeState(Direction.EAST));
        edgeStates.put(Direction.WEST, makeEdgeState(Direction.SOUTH));
    }

    public CherryGroveWaterPetalsGenerator(DimensionConfig dimConfig) {
        super(dimConfig);
    }

    @Override
    public void generateChunk(WorldGenRegion worldIn, ChunkGenerator generator, RandomSource rand, BlockPos chunkCorner) {
        if(CherryGroveWaterPetalsModule.staticEnabled){
            if (rand.nextFloat() < CherryGroveWaterPetalsModule.chancePerChunk) {
                for (int i = 0; i < CherryGroveWaterPetalsModule.triesPerChunk; i++) {
                    BlockPos pos = chunkCorner.offset(rand.nextInt(16), 70, rand.nextInt(16));
                    Holder<Biome> biome = getBiome(worldIn, pos, false);
                    if (CherryGroveWaterPetalsModule.biomes.canSpawn(biome)) {
                        while (pos.getY() > 10) {
                            if (worldIn.getBlockState(pos.below()).is(Blocks.WATER)) {
                                place(worldIn, pos, rand);
                                break;
                            }
                            pos = pos.below();
                        }
                    }
                }
            }
        }
    }


    public void place(WorldGenRegion worldIn, BlockPos corner, RandomSource rand){
        int thisSize = 0;
        if(CherryGroveWaterPetalsModule.sizeVariation > 0){
            int[] sizes = {CherryGroveWaterPetalsModule.size - CherryGroveWaterPetalsModule.sizeVariation, CherryGroveWaterPetalsModule.size, CherryGroveWaterPetalsModule.size +  CherryGroveWaterPetalsModule.sizeVariation};
            thisSize = sizes[rand.nextIntBetweenInclusive(0, 2)];
        }
        else{
            thisSize = CherryGroveWaterPetalsModule.size;
        }

        if(CherryGroveWaterPetalsModule.useCarpet && Quark.ZETA.modules.isEnabled(LeafCarpetModule.class)){
            List<BlockPos> square = getCorePositions(thisSize, corner);
            for(BlockPos pos : square){
                if(worldIn.getBlockState(pos).is(Blocks.AIR) && worldIn.getBlockState(pos.below()).is(Blocks.WATER)){
                    worldIn.setBlock(pos, LeafCarpetModule.carpets.get(7).defaultBlockState(), 0);
                }
            }
        }
        else if (!CherryGroveWaterPetalsModule.useCarpet && Quark.ZETA.modules.isEnabled(PetalsOnWaterModule.class)){
            List<BlockPos> square = getCorePositions(thisSize, corner);
            for(BlockPos pos : square){
                if(worldIn.getBlockState(pos).is(Blocks.AIR) && worldIn.getBlockState(pos.below()).is(Blocks.WATER)){
                    worldIn.setBlock(pos, makePetalState(4, CARDINAL_DIRECTIONS[rand.nextIntBetweenInclusive(0, 3)]), 0);
                }
            }
            Map<Direction, List<BlockPos>> borders = getBorderPositions(worldIn, square);
            for(Direction d : borders.keySet()){
                for(BlockPos pos : borders.get(d)){
                    if(worldIn.getBlockState(pos.below()).is(Blocks.WATER)){
                        worldIn.setBlock(pos, edgeStates.get(d), 0);
                    }
                }
            }

            //worldIn.setBlock(corner.above(), Blocks.GLOWSTONE.defaultBlockState(), 0); //show corner
        }
        else{
            Quark.LOG.debug("could not place Cherry Grove Water Petals due to config");
        }
    }

    public static List<BlockPos> getCorePositions(int size, BlockPos corner){ //for both carpet and waterpetals
        List<BlockPos> ret = new ArrayList<>();
        ret.add(corner);

        for(int l = 0; l < size; l++){
            for (int w = 0; w < size; w++) {
                ret.add(new BlockPos(corner.getX() + l, corner.getY(), corner.getZ() + w));
            }
        }

        return ret;
    }

    public static Map<Direction, List<BlockPos>> getBorderPositions(WorldGenRegion worldIn, List<BlockPos> core){ //just for waterpetals
        //this method can probably be made better
        Map<Direction, List<BlockPos>> ret = new HashMap<>();
        List<BlockPos> northBorder = new ArrayList<>(), eastBorder = new ArrayList<>(), southBorder = new ArrayList<>(), westBorder = new ArrayList<>();

        for(BlockPos pos : core){
            if(worldIn.getBlockState(pos.north()).is(Blocks.AIR)){
                northBorder.add(pos.north());
            }
            if(worldIn.getBlockState(pos.east()).is(Blocks.AIR)){
                eastBorder.add(pos.east());
            }
            if(worldIn.getBlockState(pos.south()).is(Blocks.AIR)){
                southBorder.add(pos.south());
            }
            if(worldIn.getBlockState(pos.west()).is(Blocks.AIR)){
                westBorder.add(pos.west());
            }
        }

        ret.put(Direction.NORTH, northBorder);
        ret.put(Direction.EAST, eastBorder);
        ret.put(Direction.SOUTH, southBorder);
        ret.put(Direction.WEST, westBorder);

        return ret;
    }

    public static BlockState makePetalState(int amount, Direction direction){
        return PetalsOnWaterModule.water_pink_petals.defaultBlockState().setValue(PinkPetalsBlock.FACING, direction).setValue(PinkPetalsBlock.AMOUNT, amount);
    }

    public static BlockState makeEdgeState(Direction d){
        /*
        switch(d){
            case Direction.NORTH:
                return Blocks.RED_WOOL.defaultBlockState();
            case Direction.EAST:
                return Blocks.GREEN_WOOL.defaultBlockState();
            case Direction.SOUTH:
                return Blocks.LIGHT_BLUE_WOOL.defaultBlockState();
            case Direction.WEST:
                return Blocks.YELLOW_WOOL.defaultBlockState();
        }
         */


        return makePetalState(2, d);
    }
}
