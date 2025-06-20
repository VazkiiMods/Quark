package org.violetmoon.quark.mixin.mixins;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.LevelReader;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;
import net.neoforged.neoforge.common.extensions.IItemExtension;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.violetmoon.zeta.item.ext.IZetaItemExtensions;

import java.util.function.Consumer;

// I am commiting a horrible crime.
@Mixin(IZetaItemExtensions.class)
public interface IZetaItemMixin_REAL extends IItemExtension {
    @Shadow InteractionResult onItemUseFirstZeta(ItemStack stack, UseOnContext context);


    @Shadow boolean isRepairableZeta(ItemStack stack);

    @Shadow boolean onEntityItemUpdateZeta(ItemStack stack, ItemEntity entity);

    @Shadow boolean doesSneakBypassUseZeta(ItemStack stack, LevelReader level, BlockPos pos, Player player);

    @Shadow boolean canEquipZeta(ItemStack stack, EquipmentSlot armorType, LivingEntity entity);

    @Shadow boolean isBookEnchantableZeta(ItemStack stack, ItemStack book);

    @Shadow int getEnchantmentValueZeta(ItemStack stack);

    @Shadow boolean shouldCauseReequipAnimationZeta(ItemStack oldStack, ItemStack newStack, boolean slotChanged);

    @Shadow int getBurnTimeZeta(ItemStack stack, @Nullable RecipeType<?> recipeType);

    @Shadow <T extends LivingEntity> int damageItemZeta(ItemStack stack, int amount, T entity, Consumer<Item> onBroken);

    @Shadow boolean isEnderMaskZeta(ItemStack stack, Player player, EnderMan enderboy);

    @Shadow boolean canElytraFlyZeta(ItemStack stack, LivingEntity entity);

    @Override
    public default InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        return onItemUseFirstZeta(stack, context);
    }

    @Override
    public default boolean isRepairable(ItemStack stack) {
        return isRepairableZeta(stack);
    }

    @Override
    public default boolean onEntityItemUpdate(ItemStack stack, ItemEntity entity) {
        return onEntityItemUpdateZeta(stack, entity);
    }

    @Override
    public default boolean doesSneakBypassUse(ItemStack stack, LevelReader level, BlockPos pos, Player player) {
        return doesSneakBypassUseZeta(stack, level, pos, player);
    }

    @Override
    public default boolean canEquip(ItemStack stack, EquipmentSlot armorType, LivingEntity entity) {
        return canEquipZeta(stack, armorType, entity);
    }

    @Override
    public default boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        return isBookEnchantableZeta(stack, book);
    }

    @Override
    public default boolean canPerformAction(ItemStack stack, ItemAbility itemAbility) {
        return (itemAbility == ItemAbilities.SHEARS_CARVE);
    }

    @Override
    public default int getEnchantmentValue(ItemStack stack) {
        return getEnchantmentValueZeta(stack);
    }

    @Override
    public default boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return shouldCauseReequipAnimationZeta(oldStack, newStack, slotChanged);
    }

    @Override
    public default int getBurnTime(ItemStack itemStack, @Nullable RecipeType<?> recipeType) {
        return getBurnTimeZeta(itemStack, recipeType);
    }

    @Override
    public default <T extends LivingEntity> int damageItem(ItemStack stack, int amount, T entity, Consumer<Item> onBroken) {
        return damageItemZeta(stack, amount, entity, onBroken);
    }

    @Override
    public default boolean isEnderMask(ItemStack stack, Player player, EnderMan endermanEntity) {
        return isEnderMaskZeta(stack, player, endermanEntity);
    }

    @Override
    public default boolean canElytraFly(ItemStack stack, LivingEntity entity) {
        return canElytraFlyZeta(stack, entity);
    }
}
