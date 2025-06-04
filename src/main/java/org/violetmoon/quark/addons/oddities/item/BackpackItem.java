package org.violetmoon.quark.addons.oddities.item;

import net.minecraft.core.Holder;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.HolderLookup.RegistryLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.violetmoon.quark.addons.oddities.inventory.BackpackMenu;
import org.violetmoon.quark.addons.oddities.module.BackpackModule;
import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.base.handler.ProxiedItemStackHandler;
import org.violetmoon.zeta.item.IZetaItem;
import org.violetmoon.zeta.item.ext.IZetaItemExtensions;
import org.violetmoon.zeta.module.ZetaModule;
import org.violetmoon.zeta.registry.CreativeTabManager;

import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class BackpackItem extends ArmorItem implements IZetaItem, IZetaItemExtensions, MenuProvider {

	private static final String WORN_TEXTURE = Quark.MOD_ID + ":textures/misc/backpack_worn.png";
	private static final String WORN_OVERLAY_TEXTURE = Quark.MOD_ID + ":textures/misc/backpack_worn_overlay.png";

	@Nullable
	private final ZetaModule module;

	public BackpackItem(@Nullable ZetaModule module) {
		super(ArmorMaterials.LEATHER, Type.CHESTPLATE,
				new Item.Properties()
						.stacksTo(1)
						.durability(0)
						.rarity(Rarity.RARE)
						.attributes(createAttributes()));

		this.module = module;

		if (module == null)return;

		module.zeta().registry.registerItem(this.getItem(), "backpack");

		CreativeTabManager.addToCreativeTabNextTo(CreativeModeTabs.TOOLS_AND_UTILITIES, this.getItem(), Items.SADDLE, true);
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
		Optional<IItemHandler> handlerOpt = Optional.ofNullable(stack.getCapability(Capabilities.ItemHandler.ITEM, null));

		if(handlerOpt.isEmpty())
			return false;

		IItemHandler handler = handlerOpt.orElse(new ItemStackHandler());
		for(int i = 0; i < handler.getSlots(); i++)
			if(!handler.getStackInSlot(i).isEmpty())
				return true;

		return false;
	}

	@Override
	public boolean canEquipZeta(ItemStack stack, EquipmentSlot armorType, LivingEntity entity) {
		return armorType == EquipmentSlot.CHEST;
	}

	@Override
	public boolean isBookEnchantableZeta(ItemStack stack, ItemStack book) {
		return false;
	}

	@Override
	public int getEnchantmentValueZeta(ItemStack stack) {
		return 0;
	}

	@Override
	public <T extends LivingEntity> int damageItemZeta(ItemStack stack, int amount, T entity, Consumer<Item> onBroken) {
		return 0;
	}

	@Override
	public void inventoryTick(@NotNull ItemStack stack, Level worldIn, @NotNull Entity entityIn, int itemSlot, boolean isSelected) {
		if(worldIn.isClientSide) return;

		RegistryLookup<Enchantment> enchantmentLookup = worldIn.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);

		Reference<Enchantment> bindingCurse = enchantmentLookup.getOrThrow(Enchantments.BINDING_CURSE);
		
		boolean hasItems = !BackpackModule.superOpMode && doesBackpackHaveItems(stack);
		ItemEnchantments.Mutable enchants = new ItemEnchantments.Mutable(stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY));
		boolean isCursed = enchants.getLevel(bindingCurse) == 1;

		boolean changedEnchants = false;

		if(hasItems) {
			if(BackpackModule.isEntityWearingBackpack(entityIn, stack)) {
				if(!isCursed) {
					enchants.set(bindingCurse, 1);
					changedEnchants = true;
				}

				if(BackpackModule.itemsInBackpackTick) {
					Optional<IItemHandler> handlerOpt = Optional.ofNullable(stack.getCapability(Capabilities.ItemHandler.ITEM, null));
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
			enchants.removeIf(e -> e.is(bindingCurse.key()));
			changedEnchants = true;
		}

		if(changedEnchants)
			stack.set(DataComponents.ENCHANTMENTS, enchants.toImmutable());
	}

	@Override
	public boolean onEntityItemUpdateZeta(ItemStack stack, ItemEntity entityItem) {
		if (BackpackModule.superOpMode || entityItem.level().isClientSide) return false;

		Optional<IItemHandler> handlerOpt = Optional.ofNullable(stack.getCapability(Capabilities.ItemHandler.ITEM, null));

		if (handlerOpt.isEmpty()) return false;

		IItemHandler handler = handlerOpt.orElse(new ItemStackHandler());

		for(int i = 0; i < handler.getSlots(); i++) {
			ItemStack stackAt = handler.getStackInSlot(i);
			if(!stackAt.isEmpty()) {
				ItemStack copy = stackAt.copy();
				Containers.dropItemStack(entityItem.level(), entityItem.getX(), entityItem.getY(), entityItem.getZ(), copy);
			}
		}

		ItemEnchantments enchantments = Optional.ofNullable(stack.get(DataComponents.ENCHANTMENTS)).orElse(ItemEnchantments.EMPTY);
		Holder<Enchantment> binding_curse = entityItem.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.BINDING_CURSE);
		ItemEnchantments.Mutable replaceEnch = new ItemEnchantments.Mutable(enchantments);
		if (replaceEnch.keySet().contains(binding_curse)) {
			replaceEnch.set(binding_curse, 0);
			stack.set(DataComponents.ENCHANTMENTS, replaceEnch.toImmutable());
		}

		stack.removeTagKey("Inventory");
		return false;
	}

	//TODO: IForgeItem
	@NotNull
	@Override
	public IC initCapabilities(ItemStack stack, CompoundTag oldCapNbt) {
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

	public static ItemAttributeModifiers createAttributes(){
		return ItemAttributeModifiers.builder().build();
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
