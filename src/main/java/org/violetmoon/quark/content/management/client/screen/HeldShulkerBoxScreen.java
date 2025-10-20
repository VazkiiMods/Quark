package org.violetmoon.quark.content.management.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;
import org.violetmoon.quark.addons.oddities.module.BackpackModule;
import org.violetmoon.quark.api.IQuarkButtonAllowed;
import org.violetmoon.quark.content.management.inventory.HeldShulkerBoxMenu;

public class HeldShulkerBoxScreen extends AbstractContainerScreen<HeldShulkerBoxMenu> implements IQuarkButtonAllowed {

	private static final ResourceLocation CONTAINER_TEXTURE = ResourceLocation.withDefaultNamespace("textures/gui/container/shulker_box.png");

	public HeldShulkerBoxScreen(HeldShulkerBoxMenu menu, Inventory playerInventory, Component component) {
		super(menu, playerInventory, component);
	}

	@Override
	public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		this.renderBackground(guiGraphics, mouseX, mouseY, partialTicks);
		super.render(guiGraphics, mouseX, mouseY, partialTicks);
		this.renderTooltip(guiGraphics, mouseX, mouseY);
	}

	@Override
	protected void renderBg(@NotNull GuiGraphics guiGraphics, float mouseX, int mouseY, int partialTicks) {
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		int i = (this.width - this.imageWidth) / 2;
		int j = (this.height - this.imageHeight) / 2;
		guiGraphics.blit(CONTAINER_TEXTURE, i, j, 0, 0, this.imageWidth, this.imageHeight);

		Minecraft mc = Minecraft.getInstance();
		Player player = mc.player;
		if(player != null) {
			int s = menu.blockedSlot;
			ItemStack stack = player.getInventory().getItem(s);

			int x = getGuiLeft() + (8 + (s % 9) * 18);
			int y = getGuiTop() + (s < 9 ? 142 : 84 + ((s - 9) / 9) * 18);

			guiGraphics.renderItem(stack, x, y);

			guiGraphics.fill(x, y, x + 16, y + 16, 0x88000000);
		}
	}

	@Override
	public void onClose() {
		Minecraft mc = Minecraft.getInstance();
		Player player = mc.player;
		if(player != null) {
			double mx = mc.mouseHandler.xpos();
			double my = mc.mouseHandler.ypos();

			player.playSound(SoundEvents.SHULKER_BOX_CLOSE, 1F, 1F);

			if(player.getItemBySlot(EquipmentSlot.CHEST).is(BackpackModule.backpack))
				BackpackModule.requestBackpack();
			else {
				player.closeContainer();
				mc.setScreen(new InventoryScreen(player));
			}

			GLFW.glfwSetCursorPos(mc.getWindow().getWindow(), mx, my);
		}
	}

}
