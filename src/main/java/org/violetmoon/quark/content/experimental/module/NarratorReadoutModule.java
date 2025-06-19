package org.violetmoon.quark.content.experimental.module;

import com.mojang.text2speech.Narrator;

import net.createmod.catnip.animation.AnimationTickHolder;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyModifier;

import org.violetmoon.quark.base.QuarkClient;
import org.violetmoon.zeta.client.event.load.ZKeyMapping;
import org.violetmoon.zeta.client.event.play.ZInput;
import org.violetmoon.zeta.event.bus.LoadEvent;
import org.violetmoon.zeta.event.bus.PlayEvent;
import org.violetmoon.zeta.module.ZetaLoadModule;
import org.violetmoon.zeta.module.ZetaModule;

import java.util.List;

@ZetaLoadModule(category = "experimental", enabledByDefault = false)
public class NarratorReadoutModule extends ZetaModule {
	@ZetaLoadModule(clientReplacement = true)
	public static class Client extends NarratorReadoutModule {
		private KeyMapping keybind;
		private KeyMapping keybindFull;
		private float last;

		@LoadEvent
		public void registerKeybinds(ZKeyMapping event) {
			keybind = event.init("quark.keybind.narrator_readout", null, QuarkClient.MISC_GROUP);
			keybindFull = event.init("quark.keybind.narrator_full_readout", null, QuarkClient.MISC_GROUP);
		}

		@PlayEvent
		public void onMouseInput(ZInput.MouseButton event) {
			boolean down = isDown(event.getButton(), 0, true, keybind);
			boolean full = isDown(event.getButton(), 0, true, keybindFull);

			acceptInput(down || full, down);
		}

		@PlayEvent
		public void onKeyInput(ZInput.Key event) {
			boolean down = isDown(event.getKey(), event.getScanCode(), false, keybind);
			boolean full = isDown(event.getKey(), event.getScanCode(), false, keybindFull);

			acceptInput(down || full, down);
		}

		private boolean isDown(int key, int scancode, boolean mouse, KeyMapping keybind) {
			Minecraft mc = Minecraft.getInstance();
			if(mc.screen != null) {
				if(mouse)
					return (keybind.matchesMouse(key) &&
							(keybind.getKeyModifier() == KeyModifier.NONE || keybind.getKeyModifier().isActive(KeyConflictContext.GUI)));

				else
					return (keybind.matches(key, scancode) &&
							(keybind.getKeyModifier() == KeyModifier.NONE || keybind.getKeyModifier().isActive(KeyConflictContext.GUI)));
			} else
				return keybind.isDown();
		}

		private void acceptInput(boolean down, boolean full) {
			Minecraft mc = Minecraft.getInstance();

			float curr = AnimationTickHolder.getTicks() + Minecraft.getInstance().getTimer().getGameTimeDeltaTicks();
			if(down && (curr - last) > 10) {
				Narrator narrator = Narrator.getNarrator();
				String readout = getReadout(mc, full);
				if(readout != null) {
					narrator.say(readout, true);
					last = curr;
				}
			}
		}

		private String getReadout(Minecraft mc, boolean full) {
			Player player = mc.player;
			if(player == null)
				return I18n.get("quark.readout.not_ingame");

			StringBuilder sb = new StringBuilder();

			if(mc.screen == null) {
				HitResult ray = mc.hitResult;
				if(ray != null && ray.getType() == HitResult.Type.BLOCK) {
					BlockPos pos = ((BlockHitResult) ray).getBlockPos();
					BlockState state = mc.level.getBlockState(pos);

					Item item = state.getBlock().asItem();
					if(item != null) {
						sb.append(I18n.get("quark.readout.looking", item.getName(new ItemStack(item)).getString()));

						if(full)
							sb.append(", ");
					}

					if(state.getBlock() instanceof SignBlock) {
						SignBlockEntity tile = (SignBlockEntity) mc.level.getBlockEntity(pos);
						sb.append(I18n.get("quark.readout.sign_says"));
						for(Component cmp : tile.getFrontText().getMessages(false)) {
							String msg = cmp.getString().trim();
							if(!msg.isEmpty()) {
								sb.append(cmp.getString());
								sb.append(" ");
							}
						}

						sb.append(". ");

						//todo: Can someone check if this would be a decent way to handle back parts of sign? Alternatively, perhaps we could see if the raycast gets the front or back of the sign?
						// No matter what, it should have a separate piece of text for front and back but still.

						sb.append(I18n.get("quark.readout.sign_says"));
						for(Component cmp : tile.getBackText().getMessages(false)) {
							String msg = cmp.getString().trim();
							if(!msg.isEmpty()) {
								sb.append(cmp.getString());
								sb.append(" ");
							}
						}

						sb.append(". ");
					}
				}

				if(full) {
					ItemStack stack = player.getMainHandItem();
					ItemStack stack2 = player.getOffhandItem();
					if(stack.isEmpty()) {
						stack = stack2;
						stack2 = ItemStack.EMPTY;
					}

					if(!stack.isEmpty()) {
						if(!stack2.isEmpty())
							sb.append(I18n.get("quark.readout.holding_with_off", stack.getCount(), stack.getHoverName().getString(), stack2.getCount(), stack2.getHoverName().getString()));
						else
							sb.append(I18n.get("quark.readout.holding", stack.getCount(), stack.getHoverName().getString()));

						sb.append(", ");
					}

					sb.append(I18n.get("quark.readout.health", (int) mc.player.getHealth()));
					sb.append(", ");

					sb.append(I18n.get("quark.readout.food", mc.player.getFoodData().getFoodLevel()));
				}
			}

			else {
				if(mc.screen instanceof AbstractContainerScreen<?> cnt) {
					Slot slot = cnt.getSlotUnderMouse();
					ItemStack stack = (slot == null ? ItemStack.EMPTY : slot.getItem());
					if(stack.isEmpty())
						sb.append(I18n.get("quark.readout.no_item"));
					else {
						List<Component> tooltip = Screen.getTooltipFromItem(mc, stack);

						for(Component t : tooltip) {
							Component print = t.copy();
							List<Component> bros = print.getSiblings();

							for(Component sib : bros) {
								if(sib instanceof MutableComponent mut && mut.getContents() instanceof TranslatableContents ttc) {
									if(ttc.getKey().contains("enchantment.level.")) {
										bros.set(bros.indexOf(sib), Component.translatable(ttc.getKey().substring("enchantment.level.".length())));
										break;
									}
								}
							}

							sb.append(print.getString());

							if(!full)
								break;

							sb.append(", ");
						}
					}
				} else
					sb.append(mc.screen.getNarrationMessage());
			}

			return sb.toString();
		}
	}
}
