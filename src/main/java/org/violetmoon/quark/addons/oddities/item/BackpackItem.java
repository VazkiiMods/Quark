package org.violetmoon.quark.addons.oddities.item;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.HolderLookup.RegistryLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
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
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.Unbreakable;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.*;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.violetmoon.quark.addons.oddities.inventory.BackpackContainer;
import org.violetmoon.quark.addons.oddities.inventory.BackpackMenu;
import org.violetmoon.quark.addons.oddities.module.BackpackModule;
import org.violetmoon.quark.base.Quark;
import org.violetmoon.zeta.item.IZetaItem;
import org.violetmoon.zeta.item.ext.IZetaItemExtensions;
import org.violetmoon.zeta.module.ZetaModule;
import org.violetmoon.zeta.registry.CreativeTabManager;

import java.util.EnumMap;
import java.util.List;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class BackpackItem extends ArmorItem implements IZetaItem, IZetaItemExtensions, MenuProvider {

	private static final ResourceLocation WORN_TEXTURE = Quark.asResource("textures/misc/backpack_worn.png");
	private static final ResourceLocation WORN_OVERLAY_TEXTURE = Quark.asResource("textures/misc/backpack_worn_overlay.png");

    public static final ArmorMaterial BACKPACK_MATERIAL = new ArmorMaterial(
            Util.make(new EnumMap<>(ArmorItem.Type.class), points -> {
                points.put(ArmorItem.Type.BOOTS, 0);
                points.put(ArmorItem.Type.LEGGINGS, 0);
                points.put(ArmorItem.Type.CHESTPLATE, 0);
                points.put(ArmorItem.Type.HELMET, 0);
                points.put(ArmorItem.Type.BODY, 0);
            }),
            0,
            SoundEvents.ARMOR_EQUIP_LEATHER,
            () -> Ingredient.of(Items.LEATHER),
            List.of(
                    new ArmorMaterial.Layer(Quark.asResource("backpack"), "", true),
                    new ArmorMaterial.Layer(Quark.asResource("backpack"), "_overlay", false)
            ),
            0.0F,
            0.0F
    );


	@Nullable
	private final ZetaModule module;

	public BackpackItem(@Nullable ZetaModule module) {
		super(Holder.direct(BACKPACK_MATERIAL), Type.CHESTPLATE,
				new Item.Properties()
						.stacksTo(1)
						.durability(0)
						.rarity(Rarity.RARE)
                        .component(DataComponents.UNBREAKABLE, new Unbreakable(false))
                        .attributes(createAttributes()));

		this.module = module;

		if (module == null)return;

        module.zeta().registry.register(BACKPACK_MATERIAL, "backpack", Registries.ARMOR_MATERIAL);
        module.zeta().registry.registerItem(this.getItem(), "backpack");

		CreativeTabManager.addNextToItem(CreativeModeTabs.TOOLS_AND_UTILITIES, this.getItem(), Items.SADDLE, true);
	}

    /*@Override
	public int getDefaultTooltipHideFlagsZeta(@NotNull ItemStack stack) {
		return stack.isEnchanted() & stack.has(DataComponents.HIDE_TOOLTIP) ? 1 : 0;
	}*/

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
        if (!stack.is(BackpackModule.backpack) || !stack.has(DataComponents.CONTAINER)) {
			return false;
		}

		BackpackContainer backpackInv = new BackpackContainer(stack);

		return !backpackInv.isEmpty();
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
	public ResourceLocation getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, ArmorMaterial.Layer layer, boolean innerModel) {
		return layer != null && layer.texture(innerModel).getPath().contains("overlay") ? WORN_OVERLAY_TEXTURE : WORN_TEXTURE;
	}

	@Override
	public void inventoryTick(@NotNull ItemStack stack, Level worldIn, @NotNull Entity entityIn, int itemSlot, boolean isSelected) {
		if(worldIn.isClientSide) return;

		RegistryLookup<Enchantment> enchantmentLookup = worldIn.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);

		Reference<Enchantment> bindingCurse = enchantmentLookup.getOrThrow(Enchantments.BINDING_CURSE);

        boolean canIgnoreEquip = BackpackModule.superOpMode || (entityIn instanceof Player player && (player.isCreative() || player.isSpectator()));

		ItemEnchantments.Mutable enchants = new ItemEnchantments.Mutable(stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY.withTooltip(false)));
		boolean armorChangePrevented = EnchantmentHelper.has(stack, EnchantmentEffectComponents.PREVENT_ARMOR_CHANGE);

		boolean changedEnchants = false;

        if (doesBackpackHaveItems(stack)) {
            if (BackpackModule.isEntityWearingBackpack(entityIn, stack)) {
                if(BackpackModule.itemsInBackpackTick) {
                    BackpackContainer container = new BackpackContainer(stack);
                    for(int i = 0; i < container.getContainerSize(); i++) {
                        ItemStack inStack = container.getItem(i);
                        if(!inStack.isEmpty())
                            inStack.getItem().inventoryTick(inStack, worldIn, entityIn, i, false);
                    }
                }

                if(!armorChangePrevented && !canIgnoreEquip) {
                    enchants.set(bindingCurse, 1);
                    changedEnchants = true;
                }
            } else if (!canIgnoreEquip) {
                BackpackContainer container = new BackpackContainer(stack);
                for (ItemStack item : container.getItems()) {
                    Containers.dropItemStack(worldIn, entityIn.getX(), entityIn.getY(), entityIn.getZ(), item);
                }
                stack.set(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
            } else if (!BackpackModule.superOpMode && entityIn instanceof Player player && (player.isCreative() || player.isSpectator())) {
                stack.set(DataComponents.LORE, ItemLore.EMPTY.withLineAdded(Component.translatable("item.quark.backpack.warning.line_1").withStyle(ChatFormatting.RED)).withLineAdded(Component.translatable("item.quark.backpack.warning.line_2")));
            }
        } else {
            if (armorChangePrevented) {
                enchants.removeIf(ench -> ench.value().effects().has(EnchantmentEffectComponents.PREVENT_ARMOR_CHANGE));
                changedEnchants = true;
            }


        }

        if (!doesBackpackHaveItems(stack) || BackpackModule.superOpMode || !canIgnoreEquip || BackpackModule.isEntityWearingBackpack(entityIn, stack)) {
            if (stack.has(DataComponents.LORE)) {
                stack.remove(DataComponents.LORE);
            }
        }

        if(changedEnchants) {
            stack.set(DataComponents.ENCHANTMENTS, enchants.toImmutable().withTooltip(false));
        }
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

		//Originally removed an inventory tag, but that no longer exists. I assume its the Container tag now?
		stack.remove(DataComponents.CONTAINER);
		return false;
	}

	//TODO: IForgeItem
	/*@NotNull
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
	}*/

	public static ItemAttributeModifiers createAttributes(){
		return ItemAttributeModifiers.builder().build();
	}

	/*@Override
	public String getArmorTextureZeta(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
		return type != null && type.equals("overlay") ? WORN_OVERLAY_TEXTURE : WORN_TEXTURE;
	}*/

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
