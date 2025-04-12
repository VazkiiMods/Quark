package org.violetmoon.quark.content.tweaks.module;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import com.mojang.blaze3d.platform.GlStateManager;
import org.violetmoon.quark.api.IRotationLockable;
import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.base.QuarkClient;
import org.violetmoon.quark.base.client.handler.ClientUtil;
import org.violetmoon.quark.base.network.message.SetLockProfileMessage;
import org.violetmoon.quark.content.building.block.QuarkVerticalSlabBlock;
import org.violetmoon.quark.content.building.block.VerticalSlabBlock;
import org.violetmoon.zeta.client.event.load.ZKeyMapping;
import org.violetmoon.zeta.client.event.play.ZInput;
import org.violetmoon.zeta.client.event.play.ZRenderGuiOverlay;
import org.violetmoon.zeta.config.Config;
import org.violetmoon.zeta.event.bus.LoadEvent;
import org.violetmoon.zeta.event.bus.PlayEvent;
import org.violetmoon.zeta.event.load.ZConfigChanged;
import org.violetmoon.zeta.event.play.entity.player.ZPlayer;
import org.violetmoon.zeta.module.ZetaLoadModule;
import org.violetmoon.zeta.module.ZetaModule;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.Vec3;

@ZetaLoadModule(category = "tweaks")
public class LockRotationModule extends ZetaModule {

	private static final String TAG_LOCKED_ONCE = "quark:locked_once";

	private static final HashMap<UUID, LockProfile> lockProfiles = new HashMap<>();

	@Config(description = "When true, lock rotation indicator in the same style as crosshair")
	public static boolean renderLikeCrossHair = true;

	@LoadEvent
	public final void configChanged(ZConfigChanged event) {
		lockProfiles.clear();
	}

	public static BlockState fixBlockRotation(BlockState state, BlockPlaceContext ctx) {
		if(state == null || ctx.getPlayer() == null || !Quark.ZETA.modules.isEnabled(LockRotationModule.class))
			return state;

		UUID uuid = ctx.getPlayer().getUUID();
		if(lockProfiles.containsKey(uuid)) {
			LockProfile profile = lockProfiles.get(uuid);
			BlockState transformed = getRotatedState(ctx.getLevel(), ctx.getClickedPos(), state, profile.facing.getOpposite(), profile.half);

			if(!transformed.equals(state))
				return Block.updateFromNeighbourShapes(transformed, ctx.getLevel(), ctx.getClickedPos());
		}

		return state;
	}

	public static BlockState getRotatedState(Level world, BlockPos pos, BlockState state, Direction face, int half) {
		BlockState setState = state;
		Map<Property<?>, Comparable<?>> props = state.getValues();
		Block block = state.getBlock();

		if(block instanceof IRotationLockable lockable)
			setState = lockable.applyRotationLock(world, pos, state, face, half);

		// General Facing
		else if(props.containsKey(BlockStateProperties.FACING))
			setState = state.setValue(BlockStateProperties.FACING, face);

		// Vertical Slabs
		else if(props.containsKey(QuarkVerticalSlabBlock.TYPE) && props.get(QuarkVerticalSlabBlock.TYPE) != VerticalSlabBlock.VerticalSlabType.DOUBLE && face.getAxis() != Axis.Y)
			setState = state.setValue(QuarkVerticalSlabBlock.TYPE, Objects.requireNonNull(VerticalSlabBlock.VerticalSlabType.fromDirection(face)));

		// Horizontal Facing
		else if(props.containsKey(BlockStateProperties.HORIZONTAL_FACING) && face.getAxis() != Axis.Y) {
			if(block instanceof StairBlock)
				setState = state.setValue(BlockStateProperties.HORIZONTAL_FACING, face.getOpposite());
			else
				setState = state.setValue(BlockStateProperties.HORIZONTAL_FACING, face);
		}

		// Pillar Axis
		else if(props.containsKey(BlockStateProperties.AXIS))
			setState = state.setValue(BlockStateProperties.AXIS, face.getAxis());

		// Hopper Facing
		else if(props.containsKey(BlockStateProperties.FACING_HOPPER))
			setState = state.setValue(BlockStateProperties.FACING_HOPPER, face == Direction.DOWN ? face : face.getOpposite());

		// Half
		if(half != -1) {
			// Slab type
			if(props.containsKey(BlockStateProperties.SLAB_TYPE) && props.get(BlockStateProperties.SLAB_TYPE) != SlabType.DOUBLE)
				setState = setState.setValue(BlockStateProperties.SLAB_TYPE, half == 1 ? SlabType.TOP : SlabType.BOTTOM);

			// Half (stairs)
			else if(props.containsKey(BlockStateProperties.HALF))
				setState = setState.setValue(BlockStateProperties.HALF, half == 1 ? Half.TOP : Half.BOTTOM);
		}

		return setState;
	}

	@PlayEvent
	public void onPlayerLogoff(ZPlayer.LoggedOut event) {
		lockProfiles.remove(event.getEntity().getUUID());
	}

	public static void setProfile(Player player, LockProfile profile) {
		UUID uuid = player.getUUID();

		if(profile == null)
			lockProfiles.remove(uuid);
		else {
			boolean locked = player.getPersistentData().getBoolean(TAG_LOCKED_ONCE);
			if(!locked) {
				Component keybind = Component.keybind("quark.keybind.lock_rotation").withStyle(ChatFormatting.AQUA);
				Component text = Component.translatable("quark.misc.rotation_lock", keybind);
				player.sendSystemMessage(text);

				player.getPersistentData().putBoolean(TAG_LOCKED_ONCE, true);
			}

			lockProfiles.put(uuid, profile);
		}
	}

	@PlayEvent
	public void respawn(ZPlayer.Clone event) {
		if(event.getOriginal().getPersistentData().getBoolean(TAG_LOCKED_ONCE)) {
			event.getEntity().getPersistentData().putBoolean(TAG_LOCKED_ONCE, true);
		}
	}

	public record LockProfile(Direction facing, int half) {

		public static LockProfile readProfile(FriendlyByteBuf buf, Field field) {
			boolean valid = buf.readBoolean();
			if(!valid)
				return null;

			int face = buf.readInt();
			int half = buf.readInt();
			return new LockProfile(Direction.from3DDataValue(face), half);
		}

		public static void writeProfile(FriendlyByteBuf buf, Field field, LockProfile p) {
			if(p == null)
				buf.writeBoolean(false);
			else {
				buf.writeBoolean(true);
				buf.writeInt(p.facing.get3DDataValue());
				buf.writeInt(p.half);
			}
		}

		@Override
		public boolean equals(Object other) {
			if(other == this)
				return true;
			if(!(other instanceof LockProfile otherProfile))
				return false;

			return otherProfile.facing == facing && otherProfile.half == half;
		}

		@Override
		public int hashCode() {
			return facing.hashCode() * 31 + half;
		}
	}

	@ZetaLoadModule(clientReplacement = true)
	public static class Client extends LockRotationModule {
		private LockProfile clientProfile;

		private KeyMapping keybind;

		@LoadEvent
		public void registerKeybinds(ZKeyMapping event) {
			keybind = event.init("quark.keybind.lock_rotation", "k", QuarkClient.MISC_GROUP);
		}

		@PlayEvent
		public void onMouseInput(ZInput.MouseButton event) {
			acceptInput();
		}

		@PlayEvent
		public void onKeyInput(ZInput.Key event) {
			acceptInput();
		}

		private void acceptInput() {
			Minecraft mc = Minecraft.getInstance();
			boolean down = keybind.isDown();
			if(mc.isWindowActive() && down && mc.screen == null) {
				LockProfile newProfile;
				HitResult result = mc.hitResult;

				if(result instanceof BlockHitResult bresult && result.getType() == Type.BLOCK) {
					Vec3 hitVec = bresult.getLocation();
					Direction face = bresult.getDirection();

					int half = Math.abs((int) ((hitVec.y - (int) hitVec.y) * 2));
					if(face.getAxis() == Axis.Y)
						half = -1;
					else if(hitVec.y < 0)
						half = 1 - half;

					newProfile = new LockProfile(face.getOpposite(), half);

				} else {
					Vec3 look = mc.player.getLookAngle();
					newProfile = new LockProfile(Direction.getNearest((float) look.x, (float) look.y, (float) look.z), -1);
				}

				if(clientProfile != null && clientProfile.equals(newProfile))
					clientProfile = null;
				else
					clientProfile = newProfile;
				QuarkClient.ZETA_CLIENT.sendToServer(new SetLockProfileMessage(clientProfile));
			}
		}

		@PlayEvent
		public void onHUDRender(ZRenderGuiOverlay.Crosshair.Post event) {
			if(clientProfile != null) {
				GuiGraphics guiGraphics = event.getGuiGraphics();

				RenderSystem.enableBlend();
				if(renderLikeCrossHair) {
					RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
					RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1);
				}else{
					RenderSystem.defaultBlendFunc();
					RenderSystem.setShaderColor(1, 1, 1, 0.5f);
				}

				Window window = event.getWindow();
				int x = window.getGuiScaledWidth() / 2 + 20;
				int y = window.getGuiScaledHeight() / 2 - 8;
				guiGraphics.blit(ClientUtil.GENERAL_ICONS, x, y, clientProfile.facing.ordinal() * 16, 65, 16, 16, 256, 256);

				if(clientProfile.half > -1)
					guiGraphics.blit(ClientUtil.GENERAL_ICONS, x + 16, y, clientProfile.half * 16, 79, 16, 16, 256, 256);

			}
		}
	}
}
