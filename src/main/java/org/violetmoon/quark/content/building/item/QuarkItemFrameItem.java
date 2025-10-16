package org.violetmoon.quark.content.building.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

import org.jetbrains.annotations.NotNull;

import org.violetmoon.quark.base.util.TriFunction;
import org.violetmoon.zeta.item.ZetaItem;
import org.violetmoon.zeta.module.ZetaModule;
import org.violetmoon.zeta.registry.CreativeTabManager;

/**
 * @author WireSegal
 *         Created at 11:04 AM on 8/25/19.
 */
public class QuarkItemFrameItem extends ZetaItem {
	private final TriFunction<? extends HangingEntity, Level, BlockPos, Direction> entityProvider;

	public QuarkItemFrameItem(String name, ZetaModule module, TriFunction<? extends HangingEntity, Level, BlockPos, Direction> entityProvider) {
		super(name, module, new Item.Properties());
		this.entityProvider = entityProvider;
		CreativeTabManager.addNextToItem(CreativeModeTabs.FUNCTIONAL_BLOCKS, this, Items.GLOW_ITEM_FRAME, false);
	}

	@NotNull
	@Override
	public InteractionResult useOn(UseOnContext context) {
		BlockPos pos = context.getClickedPos();
		Direction facing = context.getClickedFace();
		BlockPos placeLocation = pos.relative(facing);
		Player player = context.getPlayer();
		ItemStack stack = context.getItemInHand();
		if(player != null && !this.canPlace(player, facing, stack, placeLocation)) {
			return InteractionResult.FAIL;
		} else {
			Level world = context.getLevel();
			HangingEntity frame = entityProvider.apply(world, placeLocation, facing);

			CustomData data = stack.get(DataComponents.CUSTOM_DATA);
			if(data != null)
				EntityType.updateCustomEntityTag(world, player, frame, data);

			if(frame.survives()) {
				if(!world.isClientSide) {
					world.gameEvent(player, GameEvent.ENTITY_PLACE, frame.position());

					frame.playPlacementSound();
					world.addFreshEntity(frame);
				}

				stack.shrink(1);
			}

			return InteractionResult.sidedSuccess(world.isClientSide);
		}
	}

	protected boolean canPlace(Player player, Direction facing, ItemStack stack, BlockPos pos) {
		return !player.level().isOutsideBuildHeight(pos) && player.mayUseItemAt(pos, facing, stack);
	}
}
