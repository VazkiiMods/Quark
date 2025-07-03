package org.violetmoon.quark.content.mobs.entity;

import net.minecraft.core.RegistryAccess;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RangedBowAttackGoal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.providers.VanillaEnchantmentProviders;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.loot.LootTable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.content.mobs.module.ForgottenModule;
import org.violetmoon.quark.content.tools.base.RuneColor;
import org.violetmoon.quark.content.tools.module.ColorRunesModule;

import java.util.stream.Stream;

public class Forgotten extends Skeleton {

	public static final EntityDataAccessor<ItemStack> SHEATHED_ITEM = SynchedEntityData.defineId(Forgotten.class, EntityDataSerializers.ITEM_STACK);

	public static final ResourceKey<LootTable> FORGOTTEN_LOOT_TABLE = Quark.asResourceKey(Registries.LOOT_TABLE, "entities/forgotten");

	public Forgotten(EntityType<? extends Forgotten> type, Level world) {
		super(type, world);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(SHEATHED_ITEM, ItemStack.EMPTY);
	}

	public static AttributeSupplier.Builder registerAttributes() {
		return Monster.createMonsterAttributes()
				.add(Attributes.MOVEMENT_SPEED, 0.3D)
				.add(Attributes.MAX_HEALTH, 60)
				.add(Attributes.KNOCKBACK_RESISTANCE, 1);
	}

	@Override
	@Nullable
	public SpawnGroupData finalizeSpawn(@NotNull ServerLevelAccessor worldIn, @NotNull DifficultyInstance difficultyIn, @NotNull MobSpawnType reason, @Nullable SpawnGroupData spawnDataIn) {
		SpawnGroupData ilivingentitydata = super.finalizeSpawn(worldIn, difficultyIn, reason, spawnDataIn);
		reassessWeaponGoal();

		return ilivingentitydata;
	}

	@Override
	public void tick() {
		super.tick();

		if(!level().isClientSide) {
			LivingEntity target = getTarget();
			boolean shouldUseBow = target == null;
			if(!shouldUseBow) {
				MobEffectInstance eff = target.getEffect(MobEffects.BLINDNESS);
				shouldUseBow = eff == null || eff.getDuration() < 20;
			}

			boolean isUsingBow = getMainHandItem().getItem() instanceof BowItem;
			if(shouldUseBow != isUsingBow)
				swap();
		}

		double w = getBbWidth() * 2;
		double h = getBbHeight();
		level().addParticle(ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, 0xffffff), getX() + Math.random() * w - w / 2, getY() + Math.random() * h, getZ() + Math.random() * w - w / 2, 0, 0, 0);
	}

	private void swap() {
		ItemStack curr = getMainHandItem();
		ItemStack off = entityData.get(SHEATHED_ITEM);

		setItemInHand(InteractionHand.MAIN_HAND, off);
		entityData.set(SHEATHED_ITEM, curr);

		Stream<WrappedGoal> stream = goalSelector.getAvailableGoals().stream().filter(WrappedGoal::isRunning);
		stream.map(WrappedGoal::getGoal)
				.filter(g -> g instanceof MeleeAttackGoal || g instanceof RangedBowAttackGoal<?>)
				.forEach(Goal::stop);

		reassessWeaponGoal();
	}

	@Override
	protected ResourceKey<LootTable> getDefaultLootTable() {
		return FORGOTTEN_LOOT_TABLE;
	}

	@Override
	public void addAdditionalSaveData(@NotNull CompoundTag compound) {
		super.addAdditionalSaveData(compound);

		CompoundTag sheathed = new CompoundTag();
		if (entityData.get(SHEATHED_ITEM) != ItemStack.EMPTY) entityData.get(SHEATHED_ITEM).save(level().registryAccess(), sheathed);
		compound.put("sheathed", sheathed);
	}

	@Override
	public void readAdditionalSaveData(@NotNull CompoundTag compound) {
		super.readAdditionalSaveData(compound);

		CompoundTag sheathed = compound.getCompound("sheathed");
		entityData.set(SHEATHED_ITEM, ItemStack.parseOptional(level().registryAccess(), sheathed));
	}

	@Override
	public double getEyeY() {
		return 2.1D;
	}

	@Override
	protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHitIn) {
		// NO OP
	}

	@Override
	public boolean canPickUpLoot() {
		return false;
	}

	@Override
	protected void populateDefaultEquipmentSlots(@NotNull RandomSource rand, @NotNull DifficultyInstance difficulty) {
		super.populateDefaultEquipmentSlots(rand, difficulty);
		prepareEquipment(level().registryAccess(), difficulty);
	}

	public void prepareEquipment(RegistryAccess access, DifficultyInstance difficulty) {
		ItemStack bow = new ItemStack(Items.BOW);
		ItemStack sheathed = new ItemStack(Items.IRON_SWORD);

		EnchantmentHelper.enchantItemFromProvider(bow, access, VanillaEnchantmentProviders.MOB_SPAWN_EQUIPMENT, difficulty, random);
		EnchantmentHelper.enchantItemFromProvider(sheathed, access, VanillaEnchantmentProviders.MOB_SPAWN_EQUIPMENT, difficulty, random);

		if(Quark.ZETA.modules.isEnabled(ColorRunesModule.class) && random.nextBoolean()) {
			DyeColor color = DyeColor.values()[random.nextInt(DyeColor.values().length)];
			RuneColor rune = RuneColor.byDyeColor(color);

			if (rune != null) {
				ColorRunesModule.withRune(bow, rune);
				ColorRunesModule.withRune(sheathed, rune);
			}
		}

		setItemSlot(EquipmentSlot.MAINHAND, bow);
		entityData.set(SHEATHED_ITEM, sheathed);

		setItemSlot(EquipmentSlot.HEAD, new ItemStack(ForgottenModule.forgotten_hat));
	}

	@NotNull
	@Override
	protected AbstractArrow getArrow(@NotNull ItemStack arrowStack, float distanceFactor, @Nullable ItemStack itemStack) {
		AbstractArrow arrow = super.getArrow(arrowStack, distanceFactor, itemStack);
		if(arrow instanceof Arrow arrowInstance) {
			arrowInstance.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 0));
		}

		return arrow;
	}
}
