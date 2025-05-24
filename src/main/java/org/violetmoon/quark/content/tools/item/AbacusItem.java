package org.violetmoon.quark.content.tools.item;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;
import org.violetmoon.quark.base.components.QuarkDataComponents;
import org.violetmoon.zeta.item.ZetaItem;
import org.violetmoon.zeta.module.ZetaModule;
import org.violetmoon.zeta.registry.CreativeTabManager;

public class AbacusItem extends ZetaItem {

	public static final String TAG_POS_X = "boundPosX";
	public static final String TAG_POS_Y = "boundPosY";
	public static final String TAG_POS_Z = "boundPosZ";

	public static int MAX_COUNT = 48;
	private static final int DEFAULT_Y = -999;

	public AbacusItem(ZetaModule module) {
		super("abacus", module, new Item.Properties().stacksTo(1));
		CreativeTabManager.addToCreativeTabNextTo(CreativeModeTabs.TOOLS_AND_UTILITIES, this, Items.SPYGLASS, true);
	}

	@NotNull
	@Override
	public InteractionResult useOn(UseOnContext context) {
		ItemStack stack = context.getItemInHand();
		BlockPos curr = getBlockPos(stack);
		if(curr != null)
			setBlockPos(stack, null);
		else
			setBlockPos(stack, context.getClickedPos());

		return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
	}

	public static void setBlockPos(ItemStack stack, BlockPos pos) {
		if(pos == null)
			stack.set(QuarkDataComponents.BOUNDS_POS, new BlockPos(0, DEFAULT_Y, 0));
		else {
			stack.set(QuarkDataComponents.BOUNDS_POS, pos);
		}
	}

	public static BlockPos getBlockPos(ItemStack stack) {
		BlockPos pos = stack.get(QuarkDataComponents.BOUNDS_POS);
		if (pos.getY() == DEFAULT_Y)
			return null;
		return pos;
	}

	public static int getCount(ItemStack stack, BlockPos target, Level world) {
		BlockPos pos = getBlockPos(stack);

		if(pos != null && !world.isEmptyBlock(target))
			return Mth.clamp(target.distManhattan(pos), 0, MAX_COUNT);

		return -1;
	}

	public static class Client {
		public static int getCount(ItemStack stack, LivingEntity entityIn) {
			int count = -1;
			Minecraft mc = Minecraft.getInstance();
			Player player = mc.player;

			if(entityIn == player && player != null) {
				HitResult result = mc.hitResult;
				if(result instanceof BlockHitResult) {
					BlockPos target = ((BlockHitResult) result).getBlockPos();
					count = AbacusItem.getCount(stack, target, player.level());
				}
			}

			return count;
		}

		public static final ClampedItemPropertyFunction ITEM_PROPERTY_FUNCTION = (stack, level, entityIn, id) -> {
			int count = getCount(stack, entityIn);
			if(count == -1)
				return 1F;

			return 0.01F * count + 0.005F;
		};
	}
}
