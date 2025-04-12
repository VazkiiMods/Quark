package org.violetmoon.quark.addons.oddities.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;

import org.violetmoon.quark.addons.oddities.inventory.BackpackMenu;
import org.violetmoon.quark.addons.oddities.module.BackpackModule;
import org.violetmoon.quark.api.IQuarkButtonAllowed;
import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.base.QuarkClient;
import org.violetmoon.quark.base.network.message.oddities.HandleBackpackMessage;

import java.util.HashMap;
import java.util.Map;

public class BackpackInventoryScreen extends InventoryScreen implements IQuarkButtonAllowed {

	private static final ResourceLocation BACKPACK_INVENTORY_BACKGROUND = Quark.asResource("textures/misc/backpack_gui.png");

	private final Player player;
	private final Map<Button, Integer> buttonYs = new HashMap<>();

	private boolean closeHack = false;
	private static InventoryMenu oldContainer;

	public BackpackInventoryScreen(InventoryMenu backpack, Inventory inventory, Component component) {
		super(setBackpackContainer(inventory.player, backpack));

		this.player = inventory.player;
		setBackpackContainer(player, oldContainer);
	}

	public static Player setBackpackContainer(Player entity, InventoryMenu container) {
		oldContainer = entity.inventoryMenu;
		entity.inventoryMenu = container;
		return entity;
	}

	@Override
	public void init() {
		imageHeight = 224;
		super.init();

		buttonYs.clear();

		for(Renderable renderable : renderables)
			if(renderable instanceof Button b)
				if(b.getClass().getName().contains("GuiButtonInventoryBook")) { // class check for Patchouli
					if(!buttonYs.containsKey(b)) {
						b.setY(b.getY() - 29);
						buttonYs.put(b, b.getY());
					}
				}

	}

	@Override
	public void containerTick() {
		buttonYs.forEach(AbstractWidget::setY);

		super.containerTick();

		if(!BackpackModule.isEntityWearingBackpack(player)) {
			ItemStack curr = player.containerMenu.getCarried();
			BackpackMenu.saveCraftingInventory(player);
			closeHack = true;
			QuarkClient.ZETA_CLIENT.sendToServer(new HandleBackpackMessage(false));
			minecraft.setScreen(new InventoryScreen(player));
			player.inventoryMenu.setCarried(curr);
		}
	}

	@Override
	public void removed() {
		if(closeHack) {
			closeHack = false;
			return;
		}

		super.removed();
	}

	@Override
	protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		int i = leftPos;
		int j = topPos;
		guiGraphics.blit(BACKPACK_INVENTORY_BACKGROUND, i, j, 0, 0, imageWidth, imageHeight);
		renderEntityInInventoryFollowsMouse(guiGraphics, i + 51, j + 75, 30, i + 51 - mouseX, j + 75 - 50 - mouseY, minecraft.player);
		moveCharmsButtons();
	}

	private void moveCharmsButtons() {
		for(Renderable renderable : renderables) {
			//Charms buttons have a static Y pos, so use that to only focus on them.
			if(renderable instanceof ImageButton img) {
				if(img.getY() == height / 2 - 22)
					img.setPosition(img.getX(), img.getY() - 29);
			}
		}
	}

}
