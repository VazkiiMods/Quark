package org.violetmoon.quark.content.tools.item;

import net.minecraft.core.RegistryAccess;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.violetmoon.quark.api.ITrowelable;
import org.violetmoon.quark.api.IUsageTickerOverride;
import org.violetmoon.quark.base.components.ItemWrapperComponent;
import org.violetmoon.quark.base.components.QuarkDataComponents;
import org.violetmoon.quark.content.tools.module.TrowelModule;
import org.violetmoon.zeta.item.ZetaItem;
import org.violetmoon.zeta.module.ZetaModule;
import org.violetmoon.zeta.registry.CreativeTabManager;
import org.violetmoon.zeta.util.MiscUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class TrowelItem extends ZetaItem implements IUsageTickerOverride {

	public TrowelItem(ZetaModule module) {
		super("trowel", module, new Item.Properties()
				.durability(255));
		CreativeTabManager.addToCreativeTabNextTo(CreativeModeTabs.TOOLS_AND_UTILITIES, this, Items.SHEARS, false);
	}

	@NotNull
	@Override
	public InteractionResult useOn(UseOnContext context) {
		Player player = context.getPlayer();
		if (player == null) return InteractionResult.PASS;
		InteractionHand hand = context.getHand();

		List<Integer> targets = new ArrayList<>();
		Inventory inventory = player.getInventory();
		for(int i = 0; i < Inventory.getSelectionSize(); i++) {
			ItemStack stack = inventory.getItem(i);
			if(isValidTarget(stack, context))
				targets.add(i);
		}

		if (targets.isEmpty()) return InteractionResult.PASS;

		ItemStack trowel = player.getItemInHand(hand);

		long seed = Optional.ofNullable(trowel.get(QuarkDataComponents.PLACING_SEED)).orElse(0L);
		Random rand = new Random(seed);
		trowel.set(QuarkDataComponents.PLACING_SEED, rand.nextLong());

		int targetSlot = targets.get(rand.nextInt(targets.size()));
		ItemStack toPlaceStack = inventory.getItem(targetSlot);

		player.setItemInHand(hand, toPlaceStack);
		InteractionResult result = toPlaceStack.useOn(new TrowelBlockItemUseContext(context, toPlaceStack));
		//get new item in hand
		ItemStack newHandItem = player.getItemInHand(hand);

		//reset
		player.setItemInHand(hand, trowel);
		inventory.setItem(targetSlot, newHandItem);

		if (result.consumesAction()) {
			trowel.set(QuarkDataComponents.LAST_STACK, new ItemWrapperComponent(toPlaceStack));

			if (TrowelModule.maxDamage > 0)
				MiscUtil.damageStack(context.getItemInHand(), 1, player, hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
		}

		return result;
	}

	private static boolean isValidTarget(ItemStack stack, UseOnContext context) {
		Item item = stack.getItem();
		//tags have priority and can override these. Dont accidentally tag stuff that has the interface if you want to use it
		if (stack.is(TrowelModule.whitelist)) return true;
		if (stack.is(TrowelModule.blacklist)) return false;
		if (item instanceof ITrowelable t) return t.canBeTroweled(stack, context);
		return !stack.isEmpty() && (item instanceof BlockItem);
	}

	public static ItemStack getLastStack(ItemStack stack) {
		if (stack.has(QuarkDataComponents.LAST_STACK))
			return stack.get(QuarkDataComponents.LAST_STACK).stack();
		else
			return ItemStack.EMPTY;
	}

	@Override
	public ItemStack getUsageTickerItem(ItemStack stack, RegistryAccess access) {
		return getLastStack(stack);
	}

	static class TrowelBlockItemUseContext extends BlockPlaceContext {

		public TrowelBlockItemUseContext(UseOnContext context, ItemStack stack) {
			super(context.getLevel(), context.getPlayer(), context.getHand(), stack,
					new BlockHitResult(context.getClickLocation(), context.getClickedFace(), context.getClickedPos(), context.isInside()));
		}
	}

}
