package org.violetmoon.quark.integration.claim;

import io.github.flemmli97.flan.api.data.IPermissionContainer;
import io.github.flemmli97.flan.api.permission.PermissionRegistry;
import io.github.flemmli97.flan.claim.ClaimStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.violetmoon.quark.base.Quark;

import javax.annotation.Nonnull;

public final class FlanIntegration implements IClaimIntegration{

	@Override
    public boolean canBreak(@Nonnull Player player, @Nonnull BlockPos pos) {
        if (player.level.isClientSide) return true; //should only be used from server but client can call too
        try {
            ClaimStorage storage = ClaimStorage.get((ServerLevel) player.level);
            IPermissionContainer claim = storage.getForPermissionCheck(pos);
            return claim.canInteract((ServerPlayer) player, PermissionRegistry.BREAK, pos, true);
        } catch (Exception e) {
            Quark.LOG.error("Failed call break block event: [Player: {}, Pos: {}]", player, pos, e);
            return true;
        }
    }

	@Override
    public boolean canPlace(@Nonnull Player player, @Nonnull BlockPos pos) {
        if (player.level.isClientSide) return true; //should only be used from server but client can call too
        try {
            ClaimStorage storage = ClaimStorage.get((ServerLevel) player.level);
            IPermissionContainer claim = storage.getForPermissionCheck(pos);
            return claim.canInteract((ServerPlayer) player, PermissionRegistry.PLACE, pos);
        } catch (Exception e) {
            Quark.LOG.error("Failed call place block event: [Player: {}, Pos: {}]", player, pos, e);
            return true;
        }
    }

	@Override
    public boolean canReplace(@Nonnull Player player, @Nonnull BlockPos pos) {
        if (player.level.isClientSide) return true; //should only be used from server but client can call too
        try {
            ClaimStorage storage = ClaimStorage.get((ServerLevel) player.level);
            IPermissionContainer claim = storage.getForPermissionCheck(pos);
            return claim.canInteract((ServerPlayer) player, PermissionRegistry.PLACE, pos);
        } catch (Exception e) {
            Quark.LOG.error("Failed call replace block event: [Player: {}, Pos: {}]", player, pos, e);
            return true;
        }
    }

	@Override
    public boolean canAttack(@Nonnull Player player, @Nonnull Entity victim) {
        if (player.level.isClientSide) return true; //should only be used from server but client can call too
        try {
            ClaimStorage storage = ClaimStorage.get((ServerLevel) player.level);
            IPermissionContainer claim = storage.getForPermissionCheck(victim.blockPosition());
            return claim.canInteract((ServerPlayer) player, PermissionRegistry.HURTANIMAL, victim.blockPosition());
        } catch (Exception e) {
            Quark.LOG.error("Failed call attack entity event: [Player: {}, Victim: {}]", player, victim, e);
            return true;
        }
    }

	@Override
    public boolean canInteract(@Nonnull Player player, @Nonnull BlockPos targetPos) {
        if (player.level.isClientSide) return true; //should only be used from server but client can call too
        try {
            ClaimStorage storage = ClaimStorage.get((ServerLevel) player.level);
            IPermissionContainer claim = storage.getForPermissionCheck(targetPos);
            return claim.canInteract((ServerPlayer) player, PermissionRegistry.INTERACTBLOCK, targetPos);
        } catch (Exception e) {
            Quark.LOG.error("Failed call interact event: [Player: {}, Pos: {}]", player, targetPos, e);
            return true;
        }
    }
}

