package org.violetmoon.quark.content.mobs.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RodBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.piston.PistonMovingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.ItemAbilities;
import org.jetbrains.annotations.NotNull;
import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.base.handler.QuarkSounds;
import org.violetmoon.quark.content.mobs.module.ToretoiseModule;
import org.violetmoon.zeta.util.BlockUtils;
import org.violetmoon.zeta.util.MiscUtil;

import java.util.List;

public class Toretoise extends Animal {

	private static final TagKey<Block> BREAKS_TORETOISE_ORE = BlockTags.create(Quark.asResource("breaks_toretoise_ore"));

	public static final int ORE_TYPES = 5;
	public static final int ANGERY_TIME = 20;

	private static final String TAG_TAMED = "tamed";
	private static final String TAG_ORE = "oreType";
	private static final String TAG_EAT_COOLDOWN = "eatCooldown";
	private static final String TAG_ANGERY_TICKS = "angeryTicks";

	public int rideTime;
	private boolean isTamed;
	private int eatCooldown;
	public int angeryTicks;

	private Ingredient goodFood;
	private LivingEntity lastAggressor;

	private static final EntityDataAccessor<Integer> ORE_TYPE = SynchedEntityData.defineId(Toretoise.class, EntityDataSerializers.INT);

	public Toretoise(EntityType<? extends Toretoise> type, Level world) {
		super(type, world);
		setPathfindingMalus(PathType.WATER, 1.0F);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);

		builder.define(ORE_TYPE, 0);
	}

	@Override
	protected void registerGoals() {
		goalSelector.addGoal(0, new BreedGoal(this, 1.0));
		goalSelector.addGoal(1, new TemptGoal(this, 1.25, getGoodFood(), false));
		goalSelector.addGoal(2, new FollowParentGoal(this, 1.25));
		goalSelector.addGoal(3, new RandomStrollGoal(this, 1.0D));
		goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 6.0F));
		goalSelector.addGoal(5, new RandomLookAroundGoal(this));
	}

	private Ingredient getGoodFood() {
		if(goodFood == null)
			computeGoodFood();

		return goodFood;
	}

	private void computeGoodFood() {
		goodFood = Ingredient.of(ToretoiseModule.foods.stream()
				.map(loc -> BuiltInRegistries.ITEM.get(ResourceLocation.parse(loc)))
				.filter(i -> i != Items.AIR)
				.map(ItemStack::new));
	}

	@Override
	public SpawnGroupData finalizeSpawn(@NotNull ServerLevelAccessor world, @NotNull DifficultyInstance difficulty, @NotNull MobSpawnType spawnType, SpawnGroupData spawnData) {
		popOre(true);
		return spawnData;
	}

	@Override
	public boolean isPushedByFluid() {
		return false;
	}

	@Override
	protected int decreaseAirSupply(int air) {
		return air;
	}

	@Override
	public boolean canBreed() {
		return getOreType() == 0 && eatCooldown == 0;
	}

	@NotNull
	@Override
	public SoundEvent getEatingSound(@NotNull ItemStack itemStackIn) {
		return eatCooldown == 0 ? QuarkSounds.ENTITY_TORETOISE_EAT : QuarkSounds.ENTITY_TORETOISE_EAT_SATIATED;
	}

	@Override
	protected AABB makeBoundingBox() {
		AABB aabb = super.makeBoundingBox();

		double rheight = getOreType() == 0 ? 0 : 0.4;
		return new AABB(aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY + rheight, aabb.maxZ);
	}

	@Override
	public void tick() {
		super.tick();

		Entity riding = getVehicle();
		if(riding != null)
			rideTime++;
		else
			rideTime = 0;

		if(eatCooldown > 0)
			eatCooldown--;

		if(angeryTicks > 0 && isAlive()) {
			angeryTicks--;

			if(onGround()) {
				int dangerRange = 3;
				double x = getX() + getBbWidth() / 2;
				double y = getY();
				double z = getZ() + getBbWidth() / 2;

				if(level() instanceof ServerLevel serverLevel) {
					if(angeryTicks == 3)
						playSound(QuarkSounds.ENTITY_TORETOISE_ANGRY, 1F, 0.2F);
					else if(angeryTicks == 0) {
						serverLevel.sendParticles(ParticleTypes.CLOUD, x, y, z, 200, dangerRange, 0.5, dangerRange, 0);
						gameEvent(GameEvent.ENTITY_ACTION);
					}
				}

				if(angeryTicks == 0) {
					AABB hurtAabb = new AABB(x - dangerRange, y - 1, z - dangerRange, x + dangerRange, y + 1, z + dangerRange);
					List<LivingEntity> hurtMeDaddy = level().getEntitiesOfClass(LivingEntity.class, hurtAabb, e -> !(e instanceof Toretoise));

					LivingEntity aggressor = lastAggressor == null ? this : lastAggressor;
					DamageSource damageSource = damageSources().mobAttack(aggressor);
					for(LivingEntity e : hurtMeDaddy) {
						DamageSource useSource = damageSource;
						if(e == aggressor)
							useSource = damageSources().mobAttack(this);

						e.hurt(useSource, 4 + level().getDifficulty().ordinal());
					}
				}
			}
		}

		if(level() instanceof ServerLevel serverLevel) {
			int ore = getOreType();
			if(ore != 0)
				breakOre: {
					AABB ourBoundingBox = getBoundingBox();
					BlockPos min = BlockPos.containing(Math.round(ourBoundingBox.minX), Math.round(ourBoundingBox.minY), Math.round(ourBoundingBox.minZ));
					BlockPos max = BlockPos.containing(Math.round(ourBoundingBox.maxX), Math.round(ourBoundingBox.maxY), Math.round(ourBoundingBox.maxZ));

					for(int ix = min.getX(); ix <= max.getX(); ix++)
						for(int iy = min.getY(); iy <= max.getY(); iy++)
							for(int iz = min.getZ(); iz <= max.getZ(); iz++) {
								BlockPos test = new BlockPos(ix, iy, iz);
								BlockState state = level().getBlockState(test);
								if(state.getBlock() == Blocks.MOVING_PISTON) {
									BlockEntity tile = level().getBlockEntity(test);
									if(tile instanceof PistonMovingBlockEntity piston && piston.isExtending()) {
										BlockState pistonState = piston.getMovedState();
										if(pistonState.is(BREAKS_TORETOISE_ORE)) {
											if(!pistonState.hasProperty(RodBlock.FACING) || pistonState.getValue(RodBlock.FACING) == piston.getMovementDirection()) {
												dropOre(ore, new LootParams.Builder(serverLevel)
														.withParameter(LootContextParams.TOOL, new ItemStack(Items.IRON_PICKAXE)));
												break breakOre;
											}
										}
									}
								}
							}
				}
		}
	}

	@Override
	public boolean hurt(DamageSource source, float amount) {
		Entity e = source.getDirectEntity();
		int ore = getOreType();

		if(e instanceof LivingEntity living) {
			ItemStack held = living.getMainHandItem();

			if(ore != 0 && held.getItem().canPerformAction(held, ItemAbilities.PICKAXE_DIG)) { //TODO: IForgeItem
				if(level() instanceof ServerLevel serverLevel) {
					if(held.isDamageableItem() && e instanceof Player player)
						held.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);

					LootParams.Builder lootBuilder = new LootParams.Builder(serverLevel)
							.withParameter(LootContextParams.TOOL, held);
					if(living instanceof Player player)
						lootBuilder.withLuck(player.getLuck());
					dropOre(ore, lootBuilder);

					if(living instanceof ServerPlayer sp) {
						ToretoiseModule.mineToretoiseTrigger.trigger(sp);
						if(isTamed)
							ToretoiseModule.mineFedToretoiseTrigger.trigger(sp);
					}
				}

				return false;
			}

			if(angeryTicks == 0) {
				angeryTicks = ANGERY_TIME;
				lastAggressor = living;
			}
		}

		return super.hurt(source, amount);
	}

	public void dropOre(int ore, LootParams.Builder lootContext) {
		lootContext.withParameter(LootContextParams.ORIGIN, position()).withParameter(LootContextParams.THIS_ENTITY, this);

		ResourceKey<LootTable> dropTableKey = null;
		switch(ore) {
			case 1 -> dropTableKey = ToretoiseModule.COAL_LOOT;
			case 2 -> dropTableKey = ToretoiseModule.IRON_LOOT;
			case 3 -> dropTableKey = ToretoiseModule.REDSTONE_LOOT;
			case 4 -> dropTableKey = ToretoiseModule.LAPIS_LOOT;
			case 5 -> dropTableKey = ToretoiseModule.COPPER_LOOT;
		}

		if(dropTableKey != null) {
			playSound(QuarkSounds.ENTITY_TORETOISE_HARVEST, 1F, 0.6F);
			gameEvent(GameEvent.ENTITY_INTERACT);

			LootTable dropTable = level().getServer().reloadableRegistries().getLootTable(dropTableKey);
			List<ItemStack> drops = dropTable.getRandomItems(lootContext.create(LootContextParamSets.GIFT));
			for(ItemStack drop : drops)
				spawnAtLocation(drop, 1.2F);
		}

		entityData.set(ORE_TYPE, 0);
		this.setBoundingBox(this.makeBoundingBox());
	}

	@Override
	public void setInLove(Player player) {
		setInLoveTime(0);
	}

	@Override
	public void setInLoveTime(int ticks) {
		if(level().isClientSide)
			return;

		playSound(eatCooldown == 0 ? QuarkSounds.ENTITY_TORETOISE_EAT : QuarkSounds.ENTITY_TORETOISE_EAT_SATIATED, 0.5F + 0.5F * level().random.nextInt(2), (level().random.nextFloat() - level().random.nextFloat()) * 0.2F + 1.0F);
		heal(8);

		if(!isTamed) {
			isTamed = true;

			if(level() instanceof ServerLevel serverLevel)
				serverLevel.sendParticles(ParticleTypes.HEART, getX(), getY(), getZ(), 20, 0.5, 0.5, 0.5, 0);
		} else if(eatCooldown == 0) {
			popOre(false);
		}
	}

	private void popOre(boolean natural) {
		if(!natural && ToretoiseModule.regrowChance == 0)
			return;
		if(getOreType() == 0 && (natural || level().random.nextInt(ToretoiseModule.regrowChance) == 0)) {
			int ore = random.nextInt(ORE_TYPES) + 1;
			entityData.set(ORE_TYPE, ore);
			this.setBoundingBox(this.makeBoundingBox());

			if(!natural) {
				eatCooldown = ToretoiseModule.cooldownTicks;

				if(level() instanceof ServerLevel serverLevel) {
					serverLevel.sendParticles(ParticleTypes.CLOUD, getX(), getY() + 0.5, getZ(), 100, 0.6, 0.6, 0.6, 0);
					playSound(QuarkSounds.ENTITY_TORETOISE_REGROW, 10F, 0.7F);
				}
			}
		}
	}

	@Override
	public boolean isFood(@NotNull ItemStack stack) {
		return getGoodFood().test(stack);
	}

	@Override
	public boolean removeWhenFarAway(double distanceToClosestPlayer) {
		return !isTamed;
	}

	public static boolean spawnPredicate(EntityType<? extends Toretoise> type, ServerLevelAccessor world, MobSpawnType reason, BlockPos pos, RandomSource rand) {
		return world.getDifficulty() != Difficulty.PEACEFUL && pos.getY() <= ToretoiseModule.maxYLevel && MiscUtil.validSpawnLight(world, pos, rand) && MiscUtil.validSpawnLocation(type, world, reason, pos);
	}

	@Override
	public boolean checkSpawnRules(@NotNull LevelAccessor world, @NotNull MobSpawnType reason) {
		BlockPos pos = (BlockPos.containing(position())).below();
		BlockState state = world.getBlockState(pos);
		//Todo: Make this a tag?
		if(!BlockUtils.isStoneBased(state, world, pos))
			return false;

		return ToretoiseModule.dimensions.canSpawnHere(world);
	}

	@Override
	public void jumpFromGround() {
		// NO-OP
	}

	@Override
	public boolean causeFallDamage(float distance, float damageMultiplier, @NotNull DamageSource source) {
		return false;
	}

	@Override
	protected float getWaterSlowDown() {
		return 0.9F;
	}

	@Override
	public boolean canBeLeashed() {
		return false;
	}

	@Override
	public float getVoicePitch() {
		return (random.nextFloat() - random.nextFloat()) * 0.2F + 0.6F;
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return QuarkSounds.ENTITY_TORETOISE_IDLE;
	}

	@Override
	protected SoundEvent getHurtSound(@NotNull DamageSource damageSourceIn) {
		return QuarkSounds.ENTITY_TORETOISE_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return QuarkSounds.ENTITY_TORETOISE_DIE;
	}

	public int getOreType() {
		return entityData.get(ORE_TYPE);
	}

	@Override
	public void addAdditionalSaveData(@NotNull CompoundTag compound) {
		super.addAdditionalSaveData(compound);
		compound.putBoolean(TAG_TAMED, isTamed);
		compound.putInt(TAG_ORE, getOreType());
		compound.putInt(TAG_EAT_COOLDOWN, eatCooldown);
		compound.putInt(TAG_ANGERY_TICKS, angeryTicks);
	}

	@Override
	public void readAdditionalSaveData(@NotNull CompoundTag compound) {
		super.readAdditionalSaveData(compound);
		isTamed = compound.getBoolean(TAG_TAMED);
		entityData.set(ORE_TYPE, compound.getInt(TAG_ORE));
		eatCooldown = compound.getInt(TAG_EAT_COOLDOWN);
		angeryTicks = compound.getInt(TAG_ANGERY_TICKS);
	}

	public static AttributeSupplier.Builder prepareAttributes() {
		return Mob.createMobAttributes()
				.add(Attributes.MAX_HEALTH, 60.0D)
				.add(Attributes.MOVEMENT_SPEED, 0.08D)
				.add(Attributes.KNOCKBACK_RESISTANCE, 1.0D)
				.add(Attributes.STEP_HEIGHT, 1.0D);
	}

	@Override // createChild
	public Toretoise getBreedOffspring(@NotNull ServerLevel sworld, @NotNull AgeableMob otherParent) {
		Toretoise e = new Toretoise(ToretoiseModule.toretoiseType, level());
		e.kill(); // kill the entity cuz toretoise doesn't make babies
		return e;
	}

}
