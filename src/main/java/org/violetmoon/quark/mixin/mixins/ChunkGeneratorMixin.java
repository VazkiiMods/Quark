package org.violetmoon.quark.mixin.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.pools.EmptyPoolElement;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.violetmoon.quark.content.experimental.module.GameNerfsModule;
import org.violetmoon.quark.mixin.mixins.accessor.AccessorChunkAccess;
import org.violetmoon.quark.mixin.mixins.accessor.AccessorSinglePoolElement;

import java.util.Optional;

@Mixin(JigsawPlacement.class)
public class ChunkGeneratorMixin {

    @WrapOperation(method = "addPieces(Lnet/minecraft/world/level/levelgen/structure/Structure$GenerationContext;Lnet/minecraft/core/Holder;Ljava/util/Optional;ILnet/minecraft/core/BlockPos;ZLjava/util/Optional;ILnet/minecraft/world/level/levelgen/structure/pools/alias/PoolAliasLookup;Lnet/minecraft/world/level/levelgen/structure/pools/DimensionPadding;Lnet/minecraft/world/level/levelgen/structure/templatesystem/LiquidSettings;)Ljava/util/Optional;",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/levelgen/structure/pools/StructureTemplatePool;getRandomTemplate(Lnet/minecraft/util/RandomSource;)Lnet/minecraft/world/level/levelgen/structure/pools/StructurePoolElement;"))
    private static StructurePoolElement quark$reduceVillagesFrequency(
            StructureTemplatePool instance, RandomSource pRandom, Operation<StructurePoolElement> operation,
            @Local(argsOnly = true) Holder<StructureTemplatePool> structure, @Local(ordinal = 0, argsOnly = true) Optional<ResourceLocation> startPool, @Local(argsOnly = true) BlockPos pos, @Local(argsOnly = true) Structure.GenerationContext context) {
        var original = operation.call(instance, pRandom);
        if (GameNerfsModule.villageSpawnNerf) {
            Optional<String> left = quark$getPoolId(original);
            if (left.isPresent()) {
                String id = left.get();
                if (id.contains("village") && id.contains("town_centers")) {
                    if (context.heightAccessor() instanceof ProtoChunk pc &&
                            ((AccessorChunkAccess) pc).getLevelHeightAccessor() instanceof ServerLevel sl) {
                        BlockPos spawn = sl.getSharedSpawnPos();
                        double spawnDistanceSq = spawn.distSqr(pos);
                        RandomSource r = RandomSource.create(pos.asLong());
                        float maxDist = GameNerfsModule.villageSpawnNerfDistance;
                        double probability = 1 - (spawnDistanceSq / (maxDist * maxDist));
                        if (r.nextDouble() < probability) {
                            int tries = 0;
                            while (!id.contains("zombie")) {
                                if (tries++ > 4) {
                                    return EmptyPoolElement.INSTANCE;
                                }
                                original = operation.call(instance, pRandom);
                                Optional<String> s = quark$getPoolId(original);
                                if (s.isPresent()) {
                                    id = s.get();
                                }
                                tries++;
                            }
                        }
                    }
                }
            }
        }
        return original;
    }

    @Unique
    private static Optional<String> quark$getPoolId(StructurePoolElement le) {
        if (le instanceof AccessorSinglePoolElement se) {
            Optional<ResourceLocation> left = se.getTemplate().left();
            return left.map(ResourceLocation::getPath);
        }
        return Optional.empty();
    }
}
