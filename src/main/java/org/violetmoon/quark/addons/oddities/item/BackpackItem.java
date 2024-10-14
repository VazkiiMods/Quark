package org.violetmoon.quark.addons.oddities.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import org.jetbrains.annotations.NotNull;

import org.jetbrains.annotations.Nullable;
import org.violetmoon.quark.addons.oddities.inventory.BackpackMenu;
import org.violetmoon.quark.addons.oddities.module.BackpackModule;
import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.base.handler.ProxiedItemStackHandler;
import org.violetmoon.zeta.item.IZetaItem;
import org.violetmoon.zeta.item.ZetaItem;
import org.violetmoon.zeta.item.ext.IZetaItemExtensions;
import org.violetmoon.zeta.module.ZetaModule;
import org.violetmoon.zeta.registry.CreativeTabManager;
import org.violetmoon.zeta.util.ItemNBTHelper;

import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class BackpackItem extends DyeableArmorItem implements IZetaItem, IZetaItemExtensions, MenuProvider {

	private static final String WORN_TEXTURE = Quark.MOD_ID + ":textures/misc/backpack_worn.png";
	private static final String WORN_OVERLAY_TEXTURE = Quark.MOD_ID + ":textures/misc/backpack_worn_overlay.png";

	@Nullable
	private final ZetaModule module;

	public BackpackItem(@Nullable ZetaModule module) {
		super(ArmorMaterials.LEATHER, Type.CHESTPLATE,
				new Item.Properties()
						.stacksTo(1)
						.durability(0)
						.rarity(Rarity.RARE));

		this.module = module;

		if (module == null)return;

		module.zeta().registry.registerItem(this, "backpack");

		CreativeTabManager.addToCreativeTabNextTo(CreativeModeTabs.TOOLS_AND_UTILITIES, this, Items.SADDLE, true);
	}

	@Override
	public int getDefaultTooltipHideFlagsZeta(@NotNull ItemStack stack) {
		return stack.isEnchanted() ? ItemStack.TooltipPart.ENCHANTMENTS.getMask() : 0;
	}

	@Override
	public ZetaModule getModule() {
		return module;
	}

	@Override
	public IZetaItem setCondition(BooleanSupplier condition) {
		return this;
	}

	@Override
	public boolean doesConditionApply() {
		return true;
	}

	public static boolean doesBackpackHaveItems(ItemStack stack) {
		LazyOptional<IItemHandler> handlerOpt = stack.getCapability(ForgeCapabilities.ITEM_HANDLER, null);

		if(!handlerOpt.isPresent())
			return false;

		IItemHandler handler = handlerOpt.orElse(new ItemStackHandler());
		for(int i = 0; i < handler.getSlots(); i++)
			if(!handler.getStackInSlot(i).isEmpty())
				return true;

		return false;
	}

	@Override
	public boolean canEquipZeta(ItemStack stack, EquipmentSlot equipmentSlot, Entity ent) {
		return equipmentSlot == EquipmentSlot.CHEST;
	}

	@Override
	public boolean isBookEnchantableZeta(ItemStack stack, ItemStack book) {
		return false;
	}

	@Override
	public boolean canApplyAtEnchantingTableZeta(ItemStack stack, Enchantment enchantment) {
		return false;
	}

	@Override
	public int getEnchantmentValueZeta(ItemStack stack) {
		return 0;
	}

	@Override
	public boolean canBeDepleted() {
		return false;
	}

	@Override
	public <T extends LivingEntity> int damageItemZeta(ItemStack stack, int amount, T entity, Consumer<T> onBroken) {
		return 0;
	}

	@Override
	public void inventoryTick(@NotNull ItemStack stack, Level worldIn, @NotNull Entity entityIn, int itemSlot, boolean isSelected) {
		if(worldIn.isClientSide)
			return;

		boolean hasItems = !BackpackModule.superOpMode && doesBackpackHaveItems(stack);

		Map<Enchantment, Integer> enchants = EnchantmentHelper.getEnchantments(stack);
		boolean isCursed = enchants.containsKey(Enchantments.BINDING_CURSE);
		boolean changedEnchants = false;

		if(hasItems) {
			if(BackpackModule.isEntityWearingBackpack(entityIn, stack)) {
				if(!isCursed) {
					enchants.put(Enchantments.BINDING_CURSE, 1);
					changedEnchants = true;
				}

				if(BackpackModule.itemsInBackpackTick) {
					LazyOptional<IItemHandler> handlerOpt = stack.getCapability(ForgeCapabilities.ITEM_HANDLER, null);
					IItemHandler handler = handlerOpt.orElse(new ItemStackHandler());
					for(int i = 0; i < handler.getSlots(); i++) {
						ItemStack inStack = handler.getStackInSlot(i);
						if(!inStack.isEmpty())
							inStack.getItem().inventoryTick(inStack, worldIn, entityIn, i, false);
					}
				}
			} else {
				ItemStack copy = stack.copy();
				stack.setCount(0);
				entityIn.spawnAtLocation(copy, 0);
			}
		} else if(isCursed) {
			enchants.remove(Enchantments.BINDING_CURSE);
			changedEnchants = true;
		}

		if(changedEnchants)
			EnchantmentHelper.setEnchantments(enchants, stack);
	}

	@Override
	public boolean onEntityItemUpdateZeta(ItemStack stack, ItemEntity entityItem) {
		if(BackpackModule.superOpMode || entityItem.level().isClientSide)
			return false;

		if(!stack.hasTag())
			return false;

		LazyOptional<IItemHandler> handlerOpt = stack.getCapability(ForgeCapabilities.ITEM_HANDLER, null);

		if(!handlerOpt.isPresent())
			return false;

		IItemHandler handler = handlerOpt.orElse(new ItemStackHandler());

		for(int i = 0; i < handler.getSlots(); i++) {
			ItemStack stackAt = handler.getStackInSlot(i);
			if(!stackAt.isEmpty()) {
				ItemStack copy = stackAt.copy();
				Containers.dropItemStack(entityItem.level(), entityItem.getX(), entityItem.getY(), entityItem.getZ(), copy);
			}
		}

		Map<Enchantment, Integer> enchants = EnchantmentHelper.getEnchantments(stack);
		boolean isCursed = enchants.containsKey(Enchantments.BINDING_CURSE);
		if(isCursed) {
			enchants.remove(Enchantments.BINDING_CURSE);
			EnchantmentHelper.setEnchantments(enchants, stack);
		}
		
		stack.removeTagKey("Inventory");
		
		return false;
	}

	//TODO: IForgeItem
	@NotNull
	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, CompoundTag oldCapNbt) {
		ProxiedItemStackHandler handler = new ProxiedItemStackHandler(stack, 27);

		if(oldCapNbt != null && oldCapNbt.contains("Parent")) {
			CompoundTag itemData = oldCapNbt.getCompound("Parent");
			ItemStackHandler stacks = new ItemStackHandler();
			stacks.deserializeNBT(itemData);

			for(int i = 0; i < stacks.getSlots(); i++)
				handler.setStackInSlot(i, stacks.getStackInSlot(i));

			oldCapNbt.remove("Parent");
		}

		return handler;
	}

	@Override
	public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot p_40390_) {
		return ImmutableMultimap.of();
	}

	@Override
	public String getArmorTextureZeta(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
		return type != null && type.equals("overlay") ? WORN_OVERLAY_TEXTURE : WORN_TEXTURE;
	}

	@Override
	public boolean isFoil(@NotNull ItemStack stack) {
		return false;
	}

	@Override
	public boolean isEnchantable(@NotNull ItemStack stack) {
		return false;
	}

	@Override
	public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player player) {
		return new BackpackMenu(id, player);
	}

	@NotNull
	@Override
	public Component getDisplayName() {
		return Component.translatable(getDescriptionId());
	}

}
