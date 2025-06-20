package org.violetmoon.quark.addons.oddities.module;

import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.Holder;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import org.violetmoon.quark.addons.oddities.client.screen.BackpackInventoryScreen;
import org.violetmoon.quark.addons.oddities.inventory.BackpackMenu;
import org.violetmoon.quark.addons.oddities.item.BackpackItem;
import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.base.QuarkClient;
import org.violetmoon.quark.base.client.handler.ModelHandler;
import org.violetmoon.quark.base.network.message.oddities.HandleBackpackMessage;
import org.violetmoon.quark.mixin.mixins.client.accessor.AccessorMenuScreens;
import org.violetmoon.zeta.block.ZetaBlock;
import org.violetmoon.zeta.client.event.load.ZAddItemColorHandlers;
import org.violetmoon.zeta.client.event.load.ZClientSetup;
import org.violetmoon.zeta.client.event.play.ZClientTick;
import org.violetmoon.zeta.client.event.play.ZScreen;
import org.violetmoon.zeta.config.Config;
import org.violetmoon.zeta.event.bus.LoadEvent;
import org.violetmoon.zeta.event.bus.PlayEvent;
import org.violetmoon.zeta.event.load.ZCommonSetup;
import org.violetmoon.zeta.event.load.ZRegister;
import org.violetmoon.zeta.event.play.entity.living.ZLivingDrops;
import org.violetmoon.zeta.event.play.entity.player.ZPlayerInteract;
import org.violetmoon.zeta.item.ZetaItem;
import org.violetmoon.zeta.module.ZetaLoadModule;
import org.violetmoon.zeta.module.ZetaModule;
import org.violetmoon.zeta.util.Hint;

@ZetaLoadModule(category = "oddities")
public class BackpackModule extends ZetaModule {

	@Config(description = "Set this to true to allow the backpacks to be unequipped even with items in them")
	public static boolean superOpMode = false;

	@Config(flag = "ravager_hide")
	public static boolean enableRavagerHide = true;

	@Config
	public static boolean itemsInBackpackTick = true;

	@Config
	public static int baseRavagerHideDrop = 1;
	@Config
	public static double extraChancePerLooting = 0.5;
	
	@Config
	public static boolean allowArmorStandUnloading = true;

	@Hint
	public static Item backpack;
	@Hint("ravager_hide")
	public static Item ravager_hide;

	public static Block bonded_ravager_hide;

	public static TagKey<Item> backpackBlockedTag;

	public static MenuType<BackpackMenu> menyType;
	private static ItemStack heldStack = null;

	@LoadEvent
	public final void register(ZRegister event) {
		backpack = new BackpackItem(this);
		ravager_hide = new ZetaItem("ravager_hide", this, new Item.Properties().rarity(Rarity.RARE)).setCondition(() -> enableRavagerHide).setCreativeTab(CreativeModeTabs.INGREDIENTS, Items.RABBIT_HIDE, false);

		menyType = IMenuTypeExtension.create(BackpackMenu::fromNetwork);
		Quark.ZETA.registry.register(menyType, "backpack", Registries.MENU);

		bonded_ravager_hide = new ZetaBlock("bonded_ravager_hide", this, Block.Properties.of()
				.mapColor(DyeColor.BLACK)
				.instrument(NoteBlockInstrument.GUITAR)
				.strength(1F)
				.sound(SoundType.WOOL)
				.ignitedByLava())
				.setCondition(() -> enableRavagerHide)
				.setCreativeTab(CreativeModeTabs.BUILDING_BLOCKS);

		CauldronInteraction.WATER.map().put(backpack, CauldronInteraction.DYED_ITEM);
	}

	@LoadEvent
	public final void setup(ZCommonSetup event) {
		backpackBlockedTag = ItemTags.create(Quark.asResource("backpack_blocked"));
	}

	@PlayEvent
	public void onDrops(ZLivingDrops event) {
		LivingEntity entity = event.getEntity();
		ItemStack weapon = event.getSource().getWeaponItem();
		if(enableRavagerHide && entity.getType() == EntityType.RAVAGER) {
			Holder<Enchantment> looting = event.getEntity().level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.FORTUNE);
			int amount = baseRavagerHideDrop;
			int lootingLevel = weapon != null ? EnchantmentHelper.getTagEnchantmentLevel(looting, weapon) : 0;
			double chance = (double) lootingLevel * extraChancePerLooting;
			while(chance > baseRavagerHideDrop) {
				chance--;
				amount++;
			}
			if(chance > 0 && entity.level().random.nextDouble() < chance)
				amount++;

			event.getDrops().add(new ItemEntity(entity.level(), entity.getX(), entity.getY(), entity.getZ(), new ItemStack(ravager_hide, amount)));
		}
	}
	
	@PlayEvent
	public void onArmorStandInteract(ZPlayerInteract.EntityInteractSpecific event) {
		Entity target = event.getTarget();
		if(allowArmorStandUnloading && target instanceof ArmorStand as && event.getHand() == InteractionHand.MAIN_HAND && !event.getLevel().isClientSide) {
			Player player = event.getEntity();

			if(player.isCrouching() || !player.getMainHandItem().isEmpty() || !player.getOffhandItem().isEmpty())
				return;
			
			ItemStack playerChest = player.getItemBySlot(EquipmentSlot.CHEST);
			ItemStack armorStandChest = as.getItemBySlot(EquipmentSlot.CHEST);

			if(playerChest.is(backpack) || armorStandChest.is(backpack)) {
				ItemStack playerCopy = playerChest.copy();
				ItemStack armorStandCopy = armorStandChest.copy();
				
				as.setItemSlot(EquipmentSlot.CHEST, playerCopy);
				player.setItemSlot(EquipmentSlot.CHEST, armorStandCopy);
				
				player.swing(InteractionHand.MAIN_HAND);
				player.playSound(SoundEvents.ARMOR_EQUIP_LEATHER.value());
				
				event.setCanceled(true);
				event.setCancellationResult(InteractionResult.sidedSuccess(player.level().isClientSide));
			}
		}
	}

	public static void requestBackpack() {
		heldStack = Minecraft.getInstance().player.inventoryMenu.getCarried();
		CatnipServices.NETWORK.sendToServer(new HandleBackpackMessage(true));
	}

	public static boolean isEntityWearingBackpack(Entity e) {
		if(e instanceof LivingEntity living) {
			ItemStack chestArmor = living.getItemBySlot(EquipmentSlot.CHEST);
			return chestArmor.getItem() instanceof BackpackItem;
		}

		return false;
	}

	public static boolean isEntityWearingBackpack(Entity e, ItemStack stack) {
		if(e instanceof LivingEntity living) {
			ItemStack chestArmor = living.getItemBySlot(EquipmentSlot.CHEST);
			return chestArmor == stack;
		}

		return false;
	}

	@ZetaLoadModule(clientReplacement = true)
	public static class Client extends BackpackModule {
		private static boolean backpackRequested;

		@LoadEvent
		public void clientSetup(ZClientSetup e) {
			e.enqueueWork(() -> {
				AccessorMenuScreens.invokeRegister(menyType, BackpackInventoryScreen::new);

				ItemProperties.register(backpack, Quark.asResource("has_items"),
						(stack, world, entity, i) -> (!BackpackModule.superOpMode && BackpackItem.doesBackpackHaveItems(stack)) ? 1 : 0);

				QuarkClient.ZETA_CLIENT.setHumanoidArmorModel(backpack.asItem(), (living, stack, slot, original) -> ModelHandler.armorModel(ModelHandler.backpack, slot));
			});
		}

		@PlayEvent
		public void onOpenGUI(ZScreen.Opening event) {
			Player player = Minecraft.getInstance().player;
			if(player != null && isInventoryGUI(event.getScreen()) && !player.getAbilities().instabuild && isEntityWearingBackpack(player) && !player.portalProcess.isInsidePortalThisTick()) {
				requestBackpack();
				event.setCanceled(true);
			}
		}

		@PlayEvent
		public void clientTick(ZClientTick.Start event) {

			Minecraft mc = Minecraft.getInstance();
			if(isInventoryGUI(mc.screen) && !backpackRequested && isEntityWearingBackpack(mc.player) && !mc.player.portalProcess.isInsidePortalThisTick()) {
				requestBackpack();
				mc.player.inventoryMenu.setCarried(mc.player.getItemBySlot(EquipmentSlot.CHEST));
				backpackRequested = true;
			} else if(mc.screen instanceof BackpackInventoryScreen) {
				if(heldStack != null) {
					mc.player.inventoryMenu.setCarried(heldStack);
					heldStack = null;
				}

				backpackRequested = false;
			}
		}

		@LoadEvent
		public void registerItemColors(ZAddItemColorHandlers event) {
			ItemColor color = (stack, i) -> i > 0 ? -1 : DyedItemColor.getOrDefault(stack, -6265536);
			event.register(color, backpack);
		}

		private static boolean isInventoryGUI(Screen gui) {
			return gui != null && gui.getClass() == InventoryScreen.class;
		}
	}
}
