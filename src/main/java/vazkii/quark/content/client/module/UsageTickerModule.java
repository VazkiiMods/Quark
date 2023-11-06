package vazkii.quark.content.client.module;

import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlot.Type;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.enchantment.Enchantments;
import vazkii.quark.api.IUsageTickerOverride;
import vazkii.quark.base.Quark;
import vazkii.zeta.client.event.ZRenderGuiOverlay;
import vazkii.zeta.client.event.ZStartClientTick;
import vazkii.zeta.event.bus.PlayEvent;
import vazkii.zeta.module.ZetaLoadModule;
import vazkii.zeta.module.ZetaModule;
import vazkii.quark.base.module.config.Config;
import vazkii.zeta.event.ZConfigChanged;
import vazkii.zeta.event.bus.LoadEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

@ZetaLoadModule(category = "client")
public class UsageTickerModule extends ZetaModule {

	@Config(description = "Switch the armor display to the off hand side and the hand display to the main hand side")
	public static boolean invert = false;

	@Config public static int shiftLeft = 0;
	@Config public static int shiftRight = 0;

	@Config public static boolean enableMainHand = true;
	@Config public static boolean enableOffHand = true;
	@Config public static boolean enableArmor = true;

	@ZetaLoadModule(clientReplacement = true)
	public static class Client extends UsageTickerModule {

		public static List<TickerElement> elements = new ArrayList<>();

		@LoadEvent
		public final void configChanged(ZConfigChanged event) {
			elements = new ArrayList<>();

			if(enableMainHand)
				elements.add(new TickerElement(EquipmentSlot.MAINHAND));
			if(enableOffHand)
				elements.add(new TickerElement(EquipmentSlot.OFFHAND));
			if(enableArmor) {
				elements.add(new TickerElement(EquipmentSlot.HEAD));
				elements.add(new TickerElement(EquipmentSlot.CHEST));
				elements.add(new TickerElement(EquipmentSlot.LEGS));
				elements.add(new TickerElement(EquipmentSlot.FEET));
			}
		}

		@PlayEvent
		public void clientTick(ZStartClientTick event) {
			Minecraft mc = Minecraft.getInstance();
			if(mc.player != null && mc.level != null)
				for(TickerElement ticker : elements)
					if(ticker != null)
						ticker.tick(mc.player);
		}

		@PlayEvent
		public void renderHUD(ZRenderGuiOverlay.Hotbar.Post event) {
			Window window = event.getWindow();
			Player player = Minecraft.getInstance().player;
			float partial = event.getPartialTick();

			for(TickerElement ticker : elements)
				if(ticker != null)
					ticker.render(window, player, invert, partial);
		}

		public static class TickerElement {

			private static final int MAX_TIME = 60;
			private static final int ANIM_TIME = 5;

			public int liveTicks;
			public final EquipmentSlot slot;
			public ItemStack currStack = ItemStack.EMPTY;
			public ItemStack currRealStack = ItemStack.EMPTY;
			public int currCount;

			public TickerElement(EquipmentSlot slot) {
				this.slot = slot;
			}

			public void tick(Player player) {
				ItemStack realStack = getStack(player);
				int count = getStackCount(player, realStack, realStack, false);

				ItemStack displayedStack = getLogicalStack(realStack, count, player, false);

				if(displayedStack.isEmpty())
					liveTicks = 0;
				else if(shouldChange(realStack, currRealStack, count, currCount) || shouldChange(displayedStack, currStack, count, currCount)) {
					boolean done = liveTicks == 0;
					boolean animatingIn = liveTicks > MAX_TIME - ANIM_TIME;
					boolean animatingOut = liveTicks < ANIM_TIME && !done;
					if(animatingOut)
						liveTicks = MAX_TIME - liveTicks;
					else if(!animatingIn) {
						if(!done)
							liveTicks = MAX_TIME - ANIM_TIME;
						else liveTicks = MAX_TIME;
					}
				} else if(liveTicks > 0)
					liveTicks--;

				currCount = count;
				currStack = displayedStack;
				currRealStack = realStack;
			}

			public void render(Window window, Player player, boolean invert, float partialTicks) {
				if(liveTicks > 0) {
					float animProgress;

					if(liveTicks < ANIM_TIME)
						animProgress = Math.max(0, liveTicks - partialTicks) / ANIM_TIME;
					else animProgress = Math.min(ANIM_TIME, (MAX_TIME - liveTicks) + partialTicks) / ANIM_TIME;

					float anim = -animProgress * (animProgress - 2) * 20F;

					float x = window.getGuiScaledWidth() / 2f;
					float y = window.getGuiScaledHeight() - anim;

					int barWidth = 190;
					boolean armor = slot.getType() == Type.ARMOR;

					HumanoidArm primary = player.getMainArm();
					HumanoidArm ourSide = (armor != invert) ? primary : primary.getOpposite();

					int slots = armor ? 4 : 2;
					int index = slots - slot.getIndex() - 1;
					float mul = ourSide == HumanoidArm.LEFT ? -1 : 1;

					if(ourSide != primary && !player.getItemInHand(InteractionHand.OFF_HAND).isEmpty())
						barWidth += 58;

					Minecraft mc = Minecraft.getInstance();
					x += (barWidth / 2f) * mul + index * 20;
					if(ourSide == HumanoidArm.LEFT) {
						x -= slots * 20;
						x += shiftLeft;
					} else x += shiftRight;

					ItemStack stack = getRenderedStack(player);

					mc.getItemRenderer().renderAndDecorateItem(stack, (int) x, (int) y);
					mc.getItemRenderer().renderGuiItemDecorations(Minecraft.getInstance().font, stack, (int) x, (int) y);
				}
			}

			public boolean shouldChange(ItemStack currStack, ItemStack prevStack, int currentTotal, int pastTotal) {
				return !prevStack.sameItem(currStack) || (currStack.isDamageableItem() && currStack.getDamageValue() != prevStack.getDamageValue()) || currentTotal != pastTotal;
			}

			public ItemStack getStack(Player player) {
				return player.getItemBySlot(slot);
			}

			public ItemStack getLogicalStack(ItemStack stack, int count, Player player, boolean renderPass) {
				boolean verifySize = true;
				ItemStack returnStack = stack;
				boolean logicLock = false;

				if(stack.getItem() instanceof IUsageTickerOverride over) {
					stack = over.getUsageTickerItem(stack);
					returnStack = stack;
					verifySize = over.shouldUsageTickerCheckMatchSize(currStack);
				} else if(isProjectileWeapon(stack)) {
					returnStack = player.getProjectile(stack);
					logicLock = true;
				}

				if(!logicLock) {
					if(!stack.isStackable() && slot.getType() == Type.HAND)
						returnStack = ItemStack.EMPTY;
					else if(verifySize && stack.isStackable() && count == stack.getCount())
						returnStack = ItemStack.EMPTY;
				}

				//TODO ZETA: readd this
				//UsageTickerEvent.GetStack event = new GetStack(slot, returnStack, stack, count, renderPass, player);
				//MinecraftForge.EVENT_BUS.post(event);
				//return event.isCanceled() ? ItemStack.EMPTY : event.getResultStack();
				return returnStack;
			}

			public int getStackCount(Player player, ItemStack displayStack, ItemStack original, boolean renderPass) {
				int val = 1;

				if(displayStack.isStackable()) {
					Predicate<ItemStack> predicate = (stackAt) -> ItemStack.isSameItemSameTags(stackAt, displayStack);

					int total = 0;
					Inventory inventory = player.getInventory();
					for(int i = 0; i < inventory.getContainerSize(); i++) {
						ItemStack stackAt = inventory.getItem(i);
						if(predicate.test(stackAt))
							total += stackAt.getCount();

						else if(stackAt.getItem() instanceof IUsageTickerOverride over) {
							total += over.getUsageTickerCountForItem(stackAt, predicate);
						}
					}

					val = Math.max(total, displayStack.getCount());
				}

				//TODO ZETA: readd this
				//UsageTickerEvent.GetCount event = new GetCount(slot, displayStack, original, val, renderPass, player);
				//MinecraftForge.EVENT_BUS.post(event);
				//return event.isCanceled() ? 0 : event.getResultCount();
				return val;
			}

			private static boolean isProjectileWeapon(ItemStack stack) {
				return (stack.getItem() instanceof ProjectileWeaponItem) && Quark.ZETA.getEnchantmentLevel(stack, Enchantments.INFINITY_ARROWS) == 0;
			}

			public ItemStack getRenderedStack(Player player) {
				ItemStack stack = getStack(player);
				int count = getStackCount(player, stack, stack, true);
				ItemStack logicalStack = getLogicalStack(stack, count, player, true).copy();
				if(logicalStack != stack)
					count = getStackCount(player, logicalStack, stack, true);
				logicalStack.setCount(count);

				if(logicalStack.isEmpty())
					return ItemStack.EMPTY;

				return logicalStack;
			}
		}

	}
}
