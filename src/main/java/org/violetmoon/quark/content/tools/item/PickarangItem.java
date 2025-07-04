package org.violetmoon.quark.content.tools.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.base.handler.QuarkSounds;
import org.violetmoon.quark.content.tools.config.PickarangType;
import org.violetmoon.quark.content.tools.entity.rang.AbstractPickarang;
import org.violetmoon.quark.content.tools.module.PickarangModule;
import org.violetmoon.zeta.item.ZetaItem;
import org.violetmoon.zeta.module.ZetaModule;

public class PickarangItem extends ZetaItem {

	public final PickarangType<?> type;

	public PickarangItem(String regname, ZetaModule module, Properties properties, PickarangType<?> type) {
		super(regname, module, properties);
		this.type = type;
	}

	@Override
	public boolean hurtEnemy(ItemStack stack, @NotNull LivingEntity target, @NotNull LivingEntity attacker) {
		stack.hurtAndBreak(2, attacker, EquipmentSlot.MAINHAND);
		return true;
	}

	@Override
	public boolean isCorrectToolForDrops(@NotNull ItemStack stack, @NotNull BlockState blockIn) {
		return switch(type.harvestLevel) {
		case 0 -> Items.WOODEN_PICKAXE.isCorrectToolForDrops(stack, blockIn) ||
				(type.canActAsAxe && Items.WOODEN_AXE.isCorrectToolForDrops(stack, blockIn)) ||
				(type.canActAsShovel && Items.WOODEN_SHOVEL.isCorrectToolForDrops(stack, blockIn)) ||
				(type.canActAsHoe && Items.WOODEN_HOE.isCorrectToolForDrops(stack, blockIn));
		case 1 -> Items.STONE_PICKAXE.isCorrectToolForDrops(stack, blockIn) ||
				(type.canActAsAxe && Items.STONE_AXE.isCorrectToolForDrops(stack, blockIn)) ||
				(type.canActAsShovel && Items.STONE_SHOVEL.isCorrectToolForDrops(stack, blockIn)) ||
				(type.canActAsHoe && Items.STONE_HOE.isCorrectToolForDrops(stack, blockIn));
		case 2 -> Items.IRON_PICKAXE.isCorrectToolForDrops(stack, blockIn) ||
				(type.canActAsAxe && Items.IRON_AXE.isCorrectToolForDrops(stack, blockIn)) ||
				(type.canActAsShovel && Items.IRON_SHOVEL.isCorrectToolForDrops(stack, blockIn)) ||
				(type.canActAsHoe && Items.IRON_HOE.isCorrectToolForDrops(stack, blockIn));
		case 3 -> Items.DIAMOND_PICKAXE.isCorrectToolForDrops(stack, blockIn) ||
				(type.canActAsAxe && Items.DIAMOND_AXE.isCorrectToolForDrops(stack, blockIn)) ||
				(type.canActAsShovel && Items.DIAMOND_SHOVEL.isCorrectToolForDrops(stack, blockIn)) ||
				(type.canActAsHoe && Items.DIAMOND_HOE.isCorrectToolForDrops(stack, blockIn));
		default -> Items.NETHERITE_PICKAXE.isCorrectToolForDrops(stack, blockIn) ||
				(type.canActAsAxe && Items.NETHERITE_AXE.isCorrectToolForDrops(stack, blockIn)) ||
				(type.canActAsShovel && Items.NETHERITE_SHOVEL.isCorrectToolForDrops(stack, blockIn)) ||
				(type.canActAsHoe && Items.NETHERITE_HOE.isCorrectToolForDrops(stack, blockIn));
		};
	}

	@Override
	public int getMaxDamage(ItemStack stack) {
		return Math.max(type.durability, 0);
	}

	@Override
	public boolean mineBlock(@NotNull ItemStack stack, @NotNull Level worldIn, BlockState state, @NotNull BlockPos pos, @NotNull LivingEntity entityLiving) {
		if(state.getDestroySpeed(worldIn, pos) != 0)
			stack.hurtAndBreak(1, entityLiving, EquipmentSlot.MAINHAND);
		return true;
	}

	@NotNull
	@Override
	public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, @NotNull InteractionHand handIn) {
		ItemStack itemstack = playerIn.getItemInHand(handIn);
		playerIn.setItemInHand(handIn, ItemStack.EMPTY);
		Holder<Enchantment> enchantment = playerIn.level().registryAccess().registry(Registries.ENCHANTMENT).get().getHolderOrThrow(Enchantments.EFFICIENCY);
		int eff = Quark.ZETA.itemExtensions.get(itemstack).getEnchantmentLevelZeta(itemstack, enchantment);
		Vec3 pos = playerIn.position();
		worldIn.playSound(null, pos.x, pos.y, pos.z, QuarkSounds.ENTITY_PICKARANG_THROW, SoundSource.NEUTRAL, 0.5F + eff * 0.14F, 0.4F / (worldIn.random.nextFloat() * 0.4F + 0.8F));

		if(!worldIn.isClientSide) {
			Inventory inventory = playerIn.getInventory();
			int slot = handIn == InteractionHand.OFF_HAND ? inventory.getContainerSize() - 1 : inventory.selected;
			AbstractPickarang<?> entity = type.makePickarang(worldIn, playerIn);
			entity.setThrowData(slot, itemstack);
			entity.shoot(playerIn, playerIn.getXRot(), playerIn.getYRot(), 0.0F, 1.5F + eff * 0.325F, 0F);
			entity.setOwner(playerIn);

			worldIn.addFreshEntity(entity);

			if(playerIn instanceof ServerPlayer sp)
				PickarangModule.throwPickarangTrigger.trigger(sp);
		}

		if(!playerIn.getAbilities().instabuild && type.cooldown > 0) {
			int cooldown = type.cooldown - eff;
			if(cooldown > 0)
				playerIn.getCooldowns().addCooldown(this, cooldown);
		}

		playerIn.awardStat(Stats.ITEM_USED.get(this));

		return InteractionResultHolder.sidedSuccess( itemstack, worldIn.isClientSide);
	}

	public static ItemAttributeModifiers createAttributes(PickarangType<?> type) {
		ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();

		builder.add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Quark.asResource("pickarang_attack_damage"), type.attackDamage, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
		builder.add(Attributes.ATTACK_SPEED, new AttributeModifier(Quark.asResource("pickarang_attack_speed"), -2.8, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);

		return builder.build();
	}

	@Override
	public float getDestroySpeed(@NotNull ItemStack stack, @NotNull BlockState state) {
		return 0F;
	}

	@Override
	public boolean isRepairableZeta(@NotNull ItemStack stack) {
		return true;
	}

	@Override
	public boolean isValidRepairItem(@NotNull ItemStack toRepair, ItemStack repair) {
		return type.repairMaterial != null && repair.getItem() == type.repairMaterial;
	}

	@Override
	public int getEnchantmentValueZeta(ItemStack stack) {
		//return type.pickaxeEquivalent != null ? type.pickaxeEquivalentExt.getEnchantmentValueZeta(stack) : 0; //TODO ZETA: stack overflows
		return getEnchantmentValue();
	}

	@SuppressWarnings("deprecation") //Forge replacement
	@Override
	public int getEnchantmentValue() {
		return type.pickaxeEquivalent != null ? type.pickaxeEquivalent.getEnchantmentValue() : 0;
	}
}
