package org.violetmoon.quark.content.tweaks.module;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.violetmoon.quark.base.Quark;
import org.violetmoon.zeta.client.event.play.ZInputUpdate;
import org.violetmoon.zeta.config.Config;
import org.violetmoon.zeta.event.bus.LoadEvent;
import org.violetmoon.zeta.event.bus.PlayEvent;
import org.violetmoon.zeta.event.load.ZCommonSetup;
import org.violetmoon.zeta.event.load.ZConfigChanged;
import org.violetmoon.zeta.event.load.ZGatherHints;
import org.violetmoon.zeta.event.play.entity.player.ZPlayerTick;
import org.violetmoon.zeta.event.play.entity.player.ZRightClickBlock;
import org.violetmoon.zeta.module.ZetaLoadModule;
import org.violetmoon.zeta.module.ZetaModule;
import org.violetmoon.zeta.util.RegistryUtil;

import java.util.List;

@ZetaLoadModule(category = "tweaks")
public class EnhancedLaddersModule extends ZetaModule {

	@Config.Max(0)
	@Config
	public double fallSpeed = -0.2;

	@Config
	public static boolean allowFreestanding = true;
	@Config
	public static boolean allowDroppingDown = true;
	@Config
	public static boolean allowSliding = true;
	@Config
	public static boolean allowInventorySneak = true;

	public static boolean staticEnabled;
	private static TagKey<Item> laddersTag;
	private static TagKey<Block> laddersBlockTag;

	private static final DirectionProperty FACING = LadderBlock.FACING;

	@LoadEvent
	public final void setup(ZCommonSetup event) {
		laddersTag = Quark.asTagKey(Registries.ITEM,"ladders");
		laddersBlockTag = Quark.asTagKey(Registries.BLOCK,"ladders");
	}

	@LoadEvent
	public final void configChanged(ZConfigChanged event) {
		staticEnabled = isEnabled();
	}

	@PlayEvent
	public void addAdditionalHints(ZGatherHints event) {
		if(!allowFreestanding && !allowDroppingDown && !allowSliding && !allowInventorySneak)
			return;

		MutableComponent comp = Component.empty();
		String pad = "";
		if(allowDroppingDown) {
			comp = comp.append(pad).append(Component.translatable("quark.jei.hint.ladder_dropping"));
			pad = " ";
		}
		if(allowFreestanding) {
			comp = comp.append(pad).append(Component.translatable("quark.jei.hint.ladder_freestanding"));
			pad = " ";
		}
		if(allowSliding) {
			comp = comp.append(pad).append(Component.translatable("quark.jei.hint.ladder_sliding"));
			pad = " ";
		}
		if(allowInventorySneak)
			comp = comp.append(pad).append(Component.translatable("quark.jei.hint.ladder_sneak"));

		List<Item> ladders = RegistryUtil.getTagValues(event.getRegistryAccess(), laddersTag);
		for(Item item : ladders)
			event.accept(item, comp);
	}

	// see LadderBlockMixin
	public static boolean canSurviveTweak(boolean vanillaSurvive, BlockState state, LevelReader world, BlockPos pos) {
		//With the freestanding feature enabled, ladders can survive if they are supported by a block (vanillaSurvive),
		//or if they are underneath a ladder on the same wall.
		if(vanillaSurvive)
			return true;

		if(!staticEnabled || !allowFreestanding || !state.is(laddersBlockTag) || !state.hasProperty(FACING))
			return false;

		BlockState aboveState = world.getBlockState(pos.above());
		return aboveState.is(laddersBlockTag) && aboveState.hasProperty(FACING) && aboveState.getValue(FACING) == state.getValue(FACING);
	}

	public static boolean shouldDoUpdateShapeTweak(BlockState state) {
		return staticEnabled && allowFreestanding && state.is(laddersBlockTag);
	}

	@PlayEvent
	public void onInteract(ZRightClickBlock event) {
		if(!allowDroppingDown) return;

		Player player = event.getPlayer();
		InteractionHand hand = event.getHand();
		ItemStack stack = player.getItemInHand(hand);

		if(!stack.isEmpty() && stack.is(laddersTag)) {
			Block block = Block.byItem(stack.getItem());
			Level world = event.getLevel();
			BlockPos pos = event.getPos();
			while(world.getBlockState(pos).getBlock() == block) {
				event.setCanceled(true);
				BlockPos posDown = pos.below();

				if(world.isOutsideBuildHeight(posDown))
					break;

				BlockState stateDown = world.getBlockState(posDown);

				if(stateDown.getBlock() == block)
					pos = posDown;
				else {
					boolean water = stateDown.getBlock() == Blocks.WATER;
					if(water || stateDown.isAir()) {
						BlockState copyState = world.getBlockState(pos);

						if(copyState.canSurvive(world, posDown)) {
							world.setBlockAndUpdate(posDown, copyState.setValue(BlockStateProperties.WATERLOGGED, water));
							world.playSound(null, posDown.getX(), posDown.getY(), posDown.getZ(), SoundEvents.LADDER_PLACE, SoundSource.BLOCKS, 1F, 1F);

							if(!player.getAbilities().instabuild) {
								stack.shrink(1);

								if(stack.getCount() <= 0)
									player.setItemInHand(hand, ItemStack.EMPTY);
							}

							event.setCancellationResult(InteractionResult.sidedSuccess(world.isClientSide));
						}
					}
					break;
				}
			}
		}
	}

	@ZetaLoadModule(clientReplacement = true)
	public static class Client extends EnhancedLaddersModule {

		@PlayEvent
		public void onInput(ZInputUpdate event) {
			if(!allowInventorySneak)
				return;

			Player player = event.getEntity();
			if(player.onClimbable() && !player.getAbilities().flying &&
					!isScaffolding(player.level().getBlockState(player.blockPosition()), player)
					&& Minecraft.getInstance().screen != null && !(player.zza == 0 && player.getXRot() > 70) && !player.onGround()) {
				Input input = event.getInput();
				if(input != null)
					input.shiftKeyDown = true; // sneaking
			}
		}

		@PlayEvent
		public void onPlayerTick(ZPlayerTick.Start event) {
			if(!allowSliding)
				return;

			Player player = event.getPlayer();
			if(player.onClimbable() && player.level().isClientSide) {
				BlockPos playerPos = player.blockPosition();
				BlockPos downPos = playerPos.below();

				boolean scaffold = isScaffolding(player.level().getBlockState(playerPos), player);
				if(player.isCrouching() == scaffold &&
						player.zza == 0 &&
						player.yya <= 0 &&
						player.xxa == 0 &&
						player.getXRot() > 70 &&
						!player.jumping &&
						!player.getAbilities().flying &&
						player.level().getBlockState(downPos).isLadder(player.level(), downPos, player)) {

					Vec3 move = new Vec3(0, fallSpeed, 0);
					AABB target = player.getBoundingBox().move(move);

					Iterable<VoxelShape> collisions = player.level().getBlockCollisions(player, target);
					if(!collisions.iterator().hasNext()) {
						player.setBoundingBox(target);
						player.move(MoverType.SELF, move);
					}
				}
			}
		}

	}

	protected boolean isScaffolding(BlockState state, LivingEntity entity) {
		return zeta().blockExtensions.get(state).isScaffoldingZeta(state, entity.level(), entity.blockPosition(), entity);
	}
}
