package org.violetmoon.quark.addons.oddities.block.be;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.violetmoon.quark.addons.oddities.block.MagnetBlock;
import org.violetmoon.quark.addons.oddities.magnetsystem.MagnetSystem;
import org.violetmoon.quark.addons.oddities.module.MagnetsModule;
import org.violetmoon.quark.api.IMagneticEntity;
import org.violetmoon.quark.content.automation.entity.Gravisand;
import org.violetmoon.quark.mixin.mixins.accessor.AccessorServerGamePacketListener;
import org.violetmoon.zeta.api.ICollateralMover;

public class MagnetBlockEntity extends BlockEntity {

    public MagnetBlockEntity(BlockPos pos, BlockState state) {
        super(MagnetsModule.magnetType, pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, MagnetBlockEntity be) {
        boolean powered = state.getValue(MagnetBlock.POWERED);

        if (powered) {
            Direction dir = state.getValue(MagnetBlock.FACING);
            int power = level.getBestNeighborSignal(pos);
            be.magnetize(state, dir, dir, power);
            be.magnetize(state, dir.getOpposite(), dir, power);
        }
    }

    private void magnetize(BlockState state, Direction dir, Direction moveDir, int power) {
        if (level == null)
            return;

        double magnitude = (dir == moveDir ? 1 : -1);

        int blockDist = 1;
        for (; blockDist <= power; blockDist++) {
            BlockPos targetPos = worldPosition.relative(dir, blockDist);
            BlockState targetState = level.getBlockState(targetPos);


            if (!level.isClientSide) {
                var reaction = MagnetSystem.getPushAction(this, targetPos, targetState, moveDir);
                if (reaction == ICollateralMover.MoveResult.MOVE || reaction == ICollateralMover.MoveResult.BREAK) {
                    MagnetSystem.applyForce(level, targetPos, power - blockDist + 1, dir == moveDir, moveDir, blockDist, worldPosition);
                } else if (reaction == ICollateralMover.MoveResult.PREVENT) break;
            }

            if (targetState.getBlock() == MagnetsModule.magnetized_block && level.getBlockEntity(targetPos) instanceof MagnetizedBlockBlockEntity mbe) {
                targetState = mbe.getBlockState();
            }

            if (!canFluxPenetrate(targetPos, targetState)) break;
        }

        //TODO: move this into magnet system. although might not be needed as there it only serves since directions must be discrete
        if (MagnetsModule.affectEntities && blockDist > 1) {
            var entities = level.getEntities((Entity) null, new AABB(worldPosition)
                    .expandTowards(new Vec3(dir.step().mul(blockDist))), this::canPullEntity);
            for (Entity e : entities) {
                pushEntity(dir, magnitude, e);
            }
        }

        //particles
        if (level.isClientSide && !state.getValue(MagnetBlock.WAXED)){

            double particleMotion = 0.06 * magnitude;
            double particleChance = 0.2;
            double xOff = dir.getStepX() * particleMotion;
            double yOff = dir.getStepY() * particleMotion;
            double zOff = dir.getStepZ() * particleMotion;

            double particleOffset = moveDir.getAxisDirection() == Direction.AxisDirection.POSITIVE ? 1 : -1;

            for (int j = 1; j < blockDist; j++) {
                if (level.random.nextFloat() <= particleChance) {
                    BlockPos targetPos = worldPosition.relative(dir, j);
                    double x = targetPos.getX() + getParticlePos(xOff, level.random, particleOffset);
                    double y = targetPos.getY() + getParticlePos(yOff, level.random, particleOffset);
                    double z = targetPos.getZ() + getParticlePos(zOff, level.random, particleOffset);
                    var p = dir == moveDir ? MagnetsModule.repulsorParticle : MagnetsModule.attractorParticle;

                    level.addParticle(p, x, y, z, xOff, yOff, zOff);
                }
            }
        }
    }

    private void pushEntity(Direction dir, double magnitude, Entity e) {
        if(e instanceof Gravisand) magnitude = -magnitude; //idk why its needed

        double distanceFromMagnetSq = e.distanceToSqr(worldPosition.getCenter());
        double invSquared = 1 / distanceFromMagnetSq;
        // magic number chosen. around 1 block hover height for iron golems
        Vec3 vec = new Vec3(dir.step().mul((float) (invSquared * magnitude * MagnetsModule.entitiesPullForce)));
        if (e instanceof IMagneticEntity me) {
            me.moveByMagnet(e, vec, this);
        } else {
            e.push(vec.x(), vec.y(), vec.z());
            if (e instanceof ServerPlayer player) {
                //reset flying kick time
                ((AccessorServerGamePacketListener) player.connection).setAboveGroundTickCount(0);
            } else {
                //hurt mark everybody but the player. its handled by client side code
                e.hurtMarked = true;
            }
            if (e instanceof FallingBlockEntity fb) {
                fb.time--;
                //hack.
            }
            e.fallDistance = 0;
        }
    }

    private boolean canPullEntity(Entity e) {
        if (level.isClientSide) {
            if (MagnetsModule.affectsArmor && e instanceof Player player) {
                for (var armor : player.getArmorSlots()) {
                    if (MagnetSystem.isItemMagnetic(armor.getItem())) return true;
                }
            }
            return false;
        }
        if (e instanceof IMagneticEntity) return true;

        if (e instanceof ItemEntity ie) {
            return MagnetSystem.isItemMagnetic(ie.getItem().getItem());
        }

        if (e.getType().is(MagnetsModule.magneticEntities)) return true;

        if (e instanceof FallingBlockEntity fb) {
            return MagnetSystem.isBlockMagnetic(fb.getBlockState());
        }

        if (MagnetsModule.affectsArmor && e instanceof Player player) {
            for (var armor : player.getArmorSlots()) {
                if (MagnetSystem.isItemMagnetic(armor.getItem())) return true;
            }
        }
        return false;
    }

    private boolean canFluxPenetrate(BlockPos targetPos, BlockState targetState) {
        return (targetState.isAir()) || targetState.getCollisionShape(level, targetPos).isEmpty();
    }

    private double getParticlePos(double offset, RandomSource ran, double magnitude) {
        return (offset == 0 ? 0.5f + (ran.nextFloat() + ran.nextFloat() - 1) / 2f : (0.5f + magnitude * (ran.nextFloat() - 1.25)));
    }
}
