package org.violetmoon.quark.content.tweaks.module;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.tuple.Pair;
import org.violetmoon.quark.base.Quark;
import org.violetmoon.zeta.client.event.play.ZClientTick;
import org.violetmoon.zeta.client.event.play.ZRenderGuiOverlay;
import org.violetmoon.zeta.config.Config;
import org.violetmoon.zeta.event.bus.LoadEvent;
import org.violetmoon.zeta.event.bus.PlayEvent;
import org.violetmoon.zeta.event.load.ZCommonSetup;
import org.violetmoon.zeta.event.play.entity.player.ZRightClickItem;
import org.violetmoon.zeta.module.ZetaLoadModule;
import org.violetmoon.zeta.module.ZetaModule;

import java.util.List;

@ZetaLoadModule(category = "tweaks")
public class ReacharoundPlacingModule extends ZetaModule {

	public static final ResourceLocation OVERLAY_HORIZONTAL = new ResourceLocation(Quark.MOD_ID, "textures/gui/reacharound_overlay_horizontal.png");
	public static final ResourceLocation OVERLAY_VERTICAL = new ResourceLocation(Quark.MOD_ID, "textures/gui/reacharound_overlay_vertical.png");

	@Config
	@Config.Min(0)
	@Config.Max(1)
	public double leniency = 0.5;

	@Config
	public List<String> whitelist = Lists.newArrayList();
	@Config
	public List<String> blacklist = Lists.newArrayList();


	protected ReacharoundTarget currentTarget;
	protected int ticksDisplayed;

	public static TagKey<Item> reacharoundTag;

	@LoadEvent
	public final void setup(ZCommonSetup event) {
		reacharoundTag = ItemTags.create(new ResourceLocation(Quark.MOD_ID, "reacharound_able"));
	}

	@PlayEvent
	public void onRightClick(ZRightClickItem event) {
		Player player = event.getEntity();
		ReacharoundTarget target = getPlayerReacharoundTarget(player);

		if(target != null && event.getHand() == target.hand) {
			ItemStack stack = event.getItemStack();
			if(!player.mayUseItemAt(target.pos, target.dir, stack) || !player.level().mayInteract(player, target.pos))
				return;

			if(!Quark.FLAN_INTEGRATION.canPlace(player, target.pos))
				return;

			int count = stack.getCount();
			InteractionHand hand = event.getHand();

			UseOnContext context = new UseOnContext(player, hand, new BlockHitResult(target.pos.getCenter().add(0, 0.5, 0), target.dir, target.pos, false));
			boolean remote = player.level().isClientSide;
			InteractionResult res = remote ? InteractionResult.SUCCESS : stack.useOn(context);

			if(res != InteractionResult.PASS) {
				event.setCanceled(true);
				event.setCancellationResult(res);

				if(res == InteractionResult.SUCCESS)
					player.swing(hand);
				else if(res == InteractionResult.CONSUME) {
					BlockPos placedPos = target.pos;
					BlockState state = player.level().getBlockState(placedPos);
					SoundType soundtype = state.getSoundType(player.level(), placedPos, context.getPlayer());

					if(player.level() instanceof ServerLevel)
						player.level().playSound(null, placedPos, soundtype.getPlaceSound(), SoundSource.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);

				}

				if(player.getAbilities().instabuild && stack.getCount() < count && !remote)
					stack.setCount(count);
			}
		}
	}

	protected ReacharoundTarget getPlayerReacharoundTarget(Player player) {
		InteractionHand hand = null;
		if(validateReacharoundStack(player.getMainHandItem()))
			hand = InteractionHand.MAIN_HAND;
		else if(validateReacharoundStack(player.getOffhandItem()))
			hand = InteractionHand.OFF_HAND;

		if(hand == null)
			return null;

		Level world = player.level();

		Pair<Vec3, Vec3> params = Quark.ZETA.raytracingUtil.getEntityParams(player);
		double range = Quark.ZETA.raytracingUtil.getEntityRange(player);
		Vec3 rayPos = params.getLeft();
		Vec3 ray = params.getRight().scale(range);

		HitResult normalRes = Quark.ZETA.raytracingUtil.rayTrace(player, world, rayPos, ray, Block.OUTLINE, Fluid.NONE);

		if(normalRes.getType() == HitResult.Type.MISS) {
			ReacharoundTarget target = getPlayerVerticalReacharoundTarget(player, hand, world, rayPos, ray);
			if(target != null)
				return target;

			target = getPlayerHorizontalReacharoundTarget(player, hand, world, rayPos, ray);
			return target;
		}

		return null;
	}

	private ReacharoundTarget getPlayerVerticalReacharoundTarget(Player player, InteractionHand hand, Level world, Vec3 rayPos, Vec3 ray) {
		if(player.getXRot() < 0)
			return null;

		rayPos = rayPos.add(0, leniency, 0);
		HitResult take2Res = Quark.ZETA.raytracingUtil.rayTrace(player, world, rayPos, ray, Block.OUTLINE, Fluid.NONE);

		if(take2Res.getType() == HitResult.Type.BLOCK) {
			BlockPos pos = ((BlockHitResult) take2Res).getBlockPos().below();
			BlockState state = world.getBlockState(pos);

			if(player.position().y - pos.getY() > 1 && (world.isEmptyBlock(pos) || state.canBeReplaced()))
				return new ReacharoundTarget(pos, Direction.DOWN, hand);
		}

		return null;
	}

	private ReacharoundTarget getPlayerHorizontalReacharoundTarget(Player player, InteractionHand hand, Level world, Vec3 rayPos, Vec3 ray) {
		Direction dir = Direction.fromYRot(player.getYRot());
		rayPos = rayPos.subtract(leniency * dir.getStepX(), 0, leniency * dir.getStepZ());
		HitResult take2Res = Quark.ZETA.raytracingUtil.rayTrace(player, world, rayPos, ray, Block.OUTLINE, Fluid.NONE);

		if(take2Res.getType() == HitResult.Type.BLOCK) {
			BlockPos pos = ((BlockHitResult) take2Res).getBlockPos().relative(dir);
			BlockState state = world.getBlockState(pos);

			if((world.isEmptyBlock(pos) || state.canBeReplaced()))
				return new ReacharoundTarget(pos, dir.getOpposite(), hand);
		}

		return null;
	}

	private boolean validateReacharoundStack(ItemStack stack) {
		Item item = stack.getItem();
		String name = BuiltInRegistries.ITEM.getKey(item).toString();
		if(blacklist.contains(name))
			return false;
		return item instanceof BlockItem || stack.is(reacharoundTag) || whitelist.contains(name);
	}

	protected record ReacharoundTarget(BlockPos pos, Direction dir, InteractionHand hand) {
	}

	@ZetaLoadModule(clientReplacement = true)
	public static class Client extends ReacharoundPlacingModule {

		@PlayEvent
		public void onRender(ZRenderGuiOverlay.Crosshair.Post event) {
			GuiGraphics guiGraphics = event.getGuiGraphics();

			Minecraft mc = Minecraft.getInstance();
			Player player = mc.player;

			if(mc.options.hideGui)
				return;
			
			HitResult result = mc.hitResult;
			if(result instanceof BlockHitResult bhr) {
				BlockPos hitPos = bhr.getBlockPos();
				BlockState stateAt = player.level().getBlockState(hitPos);
				if(!stateAt.isAir())
					return;
			}
				

			if(player != null && currentTarget != null) {
				Window res = event.getWindow();
				PoseStack matrix = event.getGuiGraphics().pose();

				boolean vertical = (currentTarget.dir.getAxis() == Axis.Y);
				ResourceLocation texture = (vertical ? OVERLAY_VERTICAL : OVERLAY_HORIZONTAL);

				RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

				matrix.pushPose();
				int x = (res.getGuiScaledWidth() - 15) / 2;
				int y = (res.getGuiScaledHeight() - 15) / 2;
				guiGraphics.blit(texture, x, y, 0, 0, 16, 16, 16, 16);

				matrix.popPose();

				RenderSystem.defaultBlendFunc();

			}
		}

		@PlayEvent
		public void clientTick(ZClientTick.End event) {

			currentTarget = null;

			Player player = Minecraft.getInstance().player;
			if(player != null)
				currentTarget = getPlayerReacharoundTarget(player);

			if(currentTarget != null) {
				if(ticksDisplayed < 5)
					ticksDisplayed++;
			} else
				ticksDisplayed = 0;
		}

	}
}
