package org.violetmoon.quark.content.building.entity;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.WallHangingSignBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn;
import org.jetbrains.annotations.NotNull;
import org.violetmoon.quark.content.building.module.GlassItemFrameModule;

import java.util.UUID;

public class GlassItemFrame extends ItemFrame implements IEntityWithComplexSpawn {

	public static final EntityDataAccessor<Boolean> IS_SHINY = SynchedEntityData.defineId(GlassItemFrame.class, EntityDataSerializers.BOOLEAN);

	private static final String TAG_SHINY = "isShiny";
	private static final GameProfile DUMMY_PROFILE = new GameProfile(UUID.randomUUID(), "ItemFrame");

	private boolean didHackery = false;
	private int onSignRotation = 0;
	private SignAttachment attachment = SignAttachment.NOT_ATTACHED;

	public enum SignAttachment {
		NOT_ATTACHED,
		STANDING_IN_FRONT,
		STANDING_BEHIND,
		WALL_SIGN,
		HANGING_FROM_WALL,
		HANGING_IN_FRONT,
		HANGING_BEHIND
	}

	public GlassItemFrame(EntityType<? extends GlassItemFrame> type, Level level) {
		super(type, level);
	}

	public GlassItemFrame(Level level, BlockPos pos, Direction direction) {
		super(GlassItemFrameModule.glassFrameEntity, level);
		this.pos = pos;
		this.setDirection(direction);
	}

	@NotNull
	@Override
	public InteractionResult interact(Player player, @NotNull InteractionHand hand) {
		ItemStack item = getItem();
		if (!player.isShiftKeyDown() && !item.isEmpty() && !(item.getItem() instanceof BannerItem)) {
			BlockPos behind = getBehindPos();
			BlockEntity tile = level().getBlockEntity(behind);

			if (tile != null) {
				BlockState behindState = level().getBlockState(behind);
				InteractionResult result = behindState.useWithoutItem(level(), player, new BlockHitResult(new Vec3(getX(), getY(), getZ()), direction, behind, true));
				if (result.consumesAction()) return result;
			}
		}

		InteractionResult result = super.interact(player, hand);
		updateIsOnSign();
		return result;
	}

	@Override
	public void tick() {
		super.tick();
		boolean shouldUpdateMaps = GlassItemFrameModule.glassItemFramesUpdateMapsEveryTick;
		//same update as normal frames
		if(level().getGameTime() % 100 == 0) {
			updateIsOnSign();
			//not upating every tick otherwise lag
			shouldUpdateMaps = true;
		}

		if(!level().isClientSide && GlassItemFrameModule.glassItemFramesUpdateMaps && shouldUpdateMaps) {
			ItemStack stack = getItem();
			if(stack.getItem() instanceof MapItem map && level() instanceof ServerLevel sworld) {
				ItemStack clone = stack.copy();

				MapItemSavedData data = MapItem.getSavedData(clone, level());
				if(data != null && !data.locked) {
					var fakePlayer = FakePlayerFactory.get(sworld, DUMMY_PROFILE);

					clone.setEntityRepresentation(null);
					fakePlayer.setPos(getX(), getY(), getZ());
					fakePlayer.getInventory().setItem(0, clone);

					map.update(level(), fakePlayer, data);
				}
			}
		}
	}

	private void updateIsOnSign() {
		attachment = SignAttachment.NOT_ATTACHED;

		if(this.direction.getAxis() != Direction.Axis.Y) {
			BlockState back = level().getBlockState(getBehindPos());
			boolean standing = back.is(BlockTags.STANDING_SIGNS);
			boolean hangingCeil = back.is(BlockTags.CEILING_HANGING_SIGNS);

			if(standing || hangingCeil) {
				onSignRotation = back.getValue(BlockStateProperties.ROTATION_16);

				double[] angles = new double[] { 0, 0, 0, 180, -90, 90 };
				double ourRotation = angles[getDirection().getOpposite().ordinal()];
				double signRotation = back.getValue(BlockStateProperties.ROTATION_16) / 16.0 * 360.0;

				if(signRotation > 180)
					signRotation = signRotation - 360;

				double diff = ourRotation - signRotation;
				double absDiff = Math.abs(diff);
				if(diff < 0)
					absDiff -= 0.01; // hacky countermeasure to prevent two frames equidistant in different directions from both attaching

				if(absDiff < 45)
					attachment = (standing ? SignAttachment.STANDING_IN_FRONT : SignAttachment.HANGING_IN_FRONT);
				else if(absDiff >= 135 && absDiff < 225)
					attachment = (standing ? SignAttachment.STANDING_BEHIND : SignAttachment.HANGING_BEHIND);

				return;
			}

			if(back.is(BlockTags.WALL_SIGNS)) {
				Direction signDir = back.getValue(WallSignBlock.FACING);
				if(signDir == getDirection())
					attachment = SignAttachment.WALL_SIGN;

				return;
			}

			if(back.is(BlockTags.WALL_HANGING_SIGNS)) {
				Direction signDir = back.getValue(WallHangingSignBlock.FACING);
				if(signDir == getDirection() || signDir == getDirection().getOpposite())
					attachment = SignAttachment.HANGING_FROM_WALL;
			}
		}
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(IS_SHINY, false);
	}

	@Override
	public boolean survives() {
		return isOnSign() || super.survives();
	}

	public BlockPos getBehindPos() {
		return pos.relative(direction.getOpposite());
	}

	public SignAttachment getSignAttachment() {
		return attachment;
	}

	public boolean isOnSign() {
		return getSignAttachment() != SignAttachment.NOT_ATTACHED;
	}

	public int getOnSignRotation() {
		return onSignRotation;
	}

	@NotNull
	@Override
	public ItemEntity spawnAtLocation(@NotNull ItemStack stack, float offset) {
		if(stack.getItem() == Items.ITEM_FRAME && !didHackery) {
			stack = new ItemStack(getDroppedItem());
			didHackery = true;
		}

		return super.spawnAtLocation(stack, offset);
	}

	@NotNull
	@Override
	public ItemStack getPickedResult(@NotNull HitResult target) {
		ItemStack held = getItem();
        return held.isEmpty() ? new ItemStack(getDroppedItem()) : held.copy();
	}

	private Item getDroppedItem() {
		return entityData.get(IS_SHINY) ? GlassItemFrameModule.glowingGlassFrame : GlassItemFrameModule.glassFrame;
	}

	@Override
	public void addAdditionalSaveData(@NotNull CompoundTag cmp) {
		super.addAdditionalSaveData(cmp);
		cmp.putBoolean(TAG_SHINY, entityData.get(IS_SHINY));
	}

	@Override
	public void readAdditionalSaveData(@NotNull CompoundTag cmp) {
		super.readAdditionalSaveData(cmp);
		entityData.set(IS_SHINY, cmp.getBoolean(TAG_SHINY));
	}

	@Override
	public void writeSpawnData(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
		registryFriendlyByteBuf.writeBlockPos(this.pos);
		registryFriendlyByteBuf.writeVarInt(this.direction.get3DDataValue());
	}

	@Override
	public void readSpawnData(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
		this.pos = registryFriendlyByteBuf.readBlockPos();
		this.setDirection(Direction.from3DDataValue(registryFriendlyByteBuf.readVarInt()));
	}
}
