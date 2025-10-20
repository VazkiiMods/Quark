package org.violetmoon.quark.content.world.gen;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.content.building.module.CompressedBlocksModule;
import org.violetmoon.quark.content.world.module.NetherObsidianSpikesModule;
import org.violetmoon.zeta.config.type.DimensionConfig;
import org.violetmoon.zeta.util.MiscUtil;
import org.violetmoon.zeta.world.generator.Generator;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.material.Fluids;

public class ObsidianSpikeGenerator extends Generator {

	public ObsidianSpikeGenerator(DimensionConfig dimConfig) {
		super(dimConfig);
	}

	@Override
	public void generateChunk(WorldGenRegion world, ChunkGenerator generator, RandomSource rand, BlockPos chunkCorner) {
		if(rand.nextFloat() < NetherObsidianSpikesModule.chancePerChunk) {
			for(int i = 0; i < NetherObsidianSpikesModule.triesPerChunk; i++) {
				BlockPos pos = chunkCorner.offset(rand.nextInt(16), 50, rand.nextInt(16));

				while(pos.getY() > 10) {
					BlockState state = world.getBlockState(pos);
					if(state.getBlock() == Blocks.LAVA) {
						placeSpikeAt(world, pos, rand);
						break;
					}
					pos = pos.below();
				}
			}
		}
	}

	public static void placeSpikeAt(WorldGenRegion world, BlockPos pos, RandomSource rand) {
		int heightBelow = 10;
		int heightBottom = 3 + rand.nextInt(3);
		int heightMiddle = 2 + rand.nextInt(4);
		int heightTop = 2 + rand.nextInt(3);

		boolean addSpawner = false;
		if(rand.nextFloat() < NetherObsidianSpikesModule.bigSpikeChance) {
			heightBottom += 7;
			heightMiddle += 8;
			heightTop += 4;
			addSpawner = NetherObsidianSpikesModule.bigSpikeSpawners;
		}

		int checkHeight = heightBottom + heightMiddle + heightTop + 2;
		for(int i = 0; i < 5; i++)
			for(int j = 0; j < 5; j++)
				for(int k = 0; k < checkHeight; k++) {
					BlockPos checkPos = pos.offset(i - 2, k, j - 2);
					//TODO 1.20: formerly a getMaterial == Material.LAVA check
					if(!(world.isEmptyBlock(checkPos) || world.getFluidState(checkPos).is(Fluids.LAVA) || world.getFluidState(checkPos).is(Fluids.FLOWING_LAVA)))
						return;
				}

		BlockState obsidian = Blocks.OBSIDIAN.defaultBlockState();

		for(int i = 0; i < 3; i++)
			for(int j = 0; j < 3; j++)
				for(int k = 0; k < heightBottom + heightBelow; k++) {
					BlockPos placePos = pos.offset(i - 1, k - heightBelow, j - 1);

					if(world.getBlockState(placePos).getDestroySpeed(world, placePos) != -1)
						world.setBlock(placePos, obsidian, 0);
				}

		for(int i = 0; i < heightMiddle; i++) {
			BlockPos placePos = pos.offset(0, heightBottom + i, 0);

			world.setBlock(placePos, obsidian, 0);
			for(Direction face : MiscUtil.HORIZONTALS)
				world.setBlock(placePos.relative(face), obsidian, 0);
		}

		for(int i = 0; i < heightTop; i++) {
			BlockPos placePos = pos.offset(0, heightBottom + heightMiddle + i, 0);
			world.setBlock(placePos, obsidian, 0);

			if(addSpawner && i == 0) {
				boolean useBlazeLantern = Quark.ZETA.modules.isEnabled(CompressedBlocksModule.class) && CompressedBlocksModule.enableBlazeLantern;
				world.setBlock(placePos, useBlazeLantern ? CompressedBlocksModule.blaze_lantern.defaultBlockState() : Blocks.GLOWSTONE.defaultBlockState(), 0);

				placePos = placePos.below();
				world.setBlock(placePos, Blocks.SPAWNER.defaultBlockState(), 0);

				// passing "null" for the Level parameter to band-aid over a worldgen deadlock that
				// occurs when SpawnerBlockEntity.spawner attempts to send a block update to the client.
				// if the level param is null, everything else works it just doesn't try to sync
				((SpawnerBlockEntity) world.getBlockEntity(placePos)).getSpawner().setEntityId(EntityType.BLAZE, null, rand, pos);

				placePos = placePos.below();
				world.setBlock(placePos, Blocks.CHEST.defaultBlockState(), 0);
				BlockEntity blockEntity = world.getBlockEntity(placePos);
				if (blockEntity instanceof RandomizableContainerBlockEntity container) {
					container.setLootTable(BuiltInLootTables.NETHER_BRIDGE);
				}
			}
		}
	}

}
