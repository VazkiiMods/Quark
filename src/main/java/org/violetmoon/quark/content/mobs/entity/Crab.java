/**
 * This class was created by <WireSegal>. It's distributed as
 * part of the Quark Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Quark
 * <p>
 * Quark is Open Source and distributed under the
 * CC-BY-NC-SA 3.0 License: https://creativecommons.org/licenses/by-nc-sa/3.0/deed.en_GB
 * <p>
 * File Created @ [Jul 13, 2019, 19:51 AM (EST)]
 */
package org.violetmoon.quark.content.mobs.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Bucketable;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.*;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.network.NetworkHooks;
import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.base.handler.QuarkSounds;
import org.violetmoon.quark.content.mobs.ai.RaveGoal;
import org.violetmoon.quark.content.mobs.module.CrabsModule;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.BiConsumer;

public class Crab extends Animal implements IEntityAdditionalSpawnData, Bucketable {

	public static final int COLORS = 3;
	public static final ResourceLocation CRAB_LOOT_TABLE = new ResourceLocation("quark", "entities/crab");

	private static final EntityDataAccessor<Float> SIZE_MODIFIER = SynchedEntityData.defineId(Crab.class, EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Integer> VARIANT = SynchedEntityData.defineId(Crab.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Boolean> RAVING = SynchedEntityData.defineId(Crab.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Boolean> FROM_BUCKET = SynchedEntityData.defineId(Crab.class, EntityDataSerializers.BOOLEAN);

	private int lightningCooldown;
	private Ingredient temptationItems;

	private boolean noSpike;
	private BlockPos jukeboxPosition;
	private final DynamicGameEventListener<JukeboxListener> dynamicJukeboxListener;

	public Crab(EntityType<? extends Crab> type, Level worldIn) {
		this(type, worldIn, 1);
	}

	public Crab(EntityType<? extends Crab> type, Level worldIn, float sizeModifier) {
		super(type, worldIn);
		this.setPathfindingMalus(BlockPathTypes.LAVA, -1.0F);
		if (sizeModifier != 1)
			entityData.set(SIZE_MODIFIER, sizeModifier);

		PositionSource source = new EntityPositionSource(this, this.getEyeHeight());
		this.dynamicJukeboxListener = new DynamicGameEventListener<>(new JukeboxListener(source, GameEvent.JUKEBOX_PLAY.getNotificationRadius()));
	}

	@Override
	public boolean fromBucket() {
		return this.entityData.get(FROM_BUCKET);
	}

	@Override
	public void setFromBucket(boolean fromBucket) {
		this.entityData.set(FROM_BUCKET, fromBucket);
	}

	@Override
	@SuppressWarnings("deprecation")
	public void saveToBucketTag(@Nonnull ItemStack stack) {
		Bucketable.saveDefaultDataToBucketTag(this, stack);
		CompoundTag tag = stack.getOrCreateTag();

		if (noSpike) tag.putBoolean("NoSpike", true);
		tag.putInt(Axolotl.VARIANT_TAG, getVariant());
	}

	@Override
	@SuppressWarnings("deprecation")
	public void loadFromBucketTag(@Nonnull CompoundTag tag) {
		Bucketable.loadDefaultDataFromBucketTag(this, tag);

		if (tag.contains("NoSpike")) noSpike = tag.getBoolean("NoSpike");
		entityData.set(VARIANT, tag.getInt(Axolotl.VARIANT_TAG));
	}

	@Nonnull
	@Override
	public ItemStack getBucketItemStack() {
		return new ItemStack(CrabsModule.crab_bucket);
	}

	@Nonnull
	@Override
	public SoundEvent getPickupSound() {
		return QuarkSounds.BUCKET_FILL_CRAB;
	}

	@Override
	public boolean requiresCustomPersistence() {
		return super.requiresCustomPersistence() || this.fromBucket();
	}

	@Override
	public boolean removeWhenFarAway(double dist) {
		return !this.fromBucket() && !this.hasCustomName();
	}

	@Override
	public void updateDynamicGameEventListener(@Nonnull BiConsumer<DynamicGameEventListener<?>, ServerLevel> acceptor) {
		Level level = this.level;
		if (level instanceof ServerLevel serverlevel) acceptor.accept(this.dynamicJukeboxListener, serverlevel);
	}

	public static boolean spawnPredicate(EntityType<? extends Animal> type, LevelAccessor world, MobSpawnType reason, BlockPos pos, RandomSource random) {
		return world.getBlockState(pos.below()).is(CrabsModule.crabSpawnableTag) && world.getMaxLocalRawBrightness(pos) > 8;
	}

	@Override
	public float getWalkTargetValue(BlockPos pos, LevelReader world) {
		return world.getBlockState(pos.below()).is(CrabsModule.crabSpawnableTag) ? 10.0F : world.getRawBrightness(pos, 0) - 0.5F;
	}

	@Override
	public boolean canBreatheUnderwater() {
		return true;
	}

	@Nonnull
	@Override
	public MobType getMobType() {
		return MobType.ARTHROPOD;
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();

		entityData.define(SIZE_MODIFIER, 1f);
		entityData.define(VARIANT, -1);
		entityData.define(RAVING, false);
		entityData.define(FROM_BUCKET, false);
	}

	@Nonnull
	@Override
	public InteractionResult mobInteract(@Nonnull Player player, @Nonnull InteractionHand hand) {
		if (getSizeModifier() >= 2) {
			if (!this.isFood(player.getItemInHand(hand)) && !this.isVehicle() && !player.isSecondaryUseActive()) {
				if (!this.level.isClientSide) player.startRiding(this);

				return InteractionResult.sidedSuccess(this.level.isClientSide);
			}
		} else {
			var result = Bucketable.bucketMobPickup(player, hand, this);
			if (result.isPresent())
				return result.get();
		}

		return super.mobInteract(player, hand);
	}

	@Override
	public double getPassengersRidingOffset() {
		return super.getPassengersRidingOffset() / 0.75 * 0.9;
	}

	@Nonnull
	@Override
	public Vec3 getDismountLocationForPassenger(@Nonnull LivingEntity entity) {
		Direction direction = this.getMotionDirection();
		if (direction.getAxis() != Direction.Axis.Y) {
			float scale = getScale();
			int[][] aint = DismountHelper.offsetsForDirection(direction);
			BlockPos blockpos = this.blockPosition();
			BlockPos.MutableBlockPos mutPos = new BlockPos.MutableBlockPos();

			for (Pose pose : entity.getDismountPoses()) {
				AABB aabb = entity.getLocalBoundsForPose(pose);

				for (int[] aint1 : aint) {
					mutPos.set(blockpos.getX() + aint1[0] * scale, blockpos.getY(), blockpos.getZ() + aint1[1] * scale);
					double d0 = this.level.getBlockFloorHeight(mutPos);
					if (DismountHelper.isBlockFloorValid(d0)) {
						Vec3 vec3 = Vec3.upFromBottomCenterOf(mutPos, d0);
						if (DismountHelper.canDismountTo(this.level, entity, aabb.move(vec3))) {
							entity.setPose(pose);
							return vec3;
						}
					}
				}
			}

		}
		return super.getDismountLocationForPassenger(entity);
	}

	@Nullable
	@Override
	protected SoundEvent getAmbientSound() {
		return QuarkSounds.ENTITY_CRAB_IDLE;
	}

	@Nullable
	@Override
	protected SoundEvent getDeathSound() {
		return QuarkSounds.ENTITY_CRAB_DIE;
	}

	@Nullable
	@Override
	protected SoundEvent getHurtSound(@Nonnull DamageSource source) {
		return QuarkSounds.ENTITY_CRAB_HURT;
	}

	@Override
	protected float getStandingEyeHeight(@Nonnull Pose pose, EntityDimensions size) {
		return 0.2f * size.height;
	}

	public float getSizeModifier() {
		return entityData.get(SIZE_MODIFIER);
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(1, new PanicGoal(this, 1.25D));
		this.goalSelector.addGoal(2, new RaveGoal(this));
		this.goalSelector.addGoal(3, new BreedGoal(this, 1.0D));
		this.goalSelector.addGoal(4, new TemptGoal(this, 1.2D, getTemptationItems(), false));
		this.goalSelector.addGoal(5, new FollowParentGoal(this, 1.1D));
		this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 1.0D));
		this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 6.0F));
		this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
	}

	public static AttributeSupplier.Builder prepareAttributes() {
		return Mob.createMobAttributes()
				.add(Attributes.MAX_HEALTH, 20.0D)
				.add(Attributes.MOVEMENT_SPEED, 0.25D)
				.add(Attributes.ARMOR, 3.0D)
				.add(Attributes.ARMOR_TOUGHNESS, 2.0D)
				.add(Attributes.KNOCKBACK_RESISTANCE, 0.5D);
	}

	@Override
	public void tick() {
		super.tick();

		if(!level.isClientSide && entityData.get(VARIANT) == -1) {
			int variant = 0;
			if(random.nextBoolean()) // Color change
				variant += random.nextInt(COLORS - 1) + 1;

			if(random.nextInt(3) == 0) // Mold
				variant += COLORS;

			entityData.set(VARIANT, variant);
		}

		if (lightningCooldown > 0) {
			lightningCooldown--;
			clearFire();
		}

		if(isRaving() && level.isClientSide && tickCount % 10 == 0) {
			BlockPos below = blockPosition().below();
			BlockState belowState = level.getBlockState(below);
			if(belowState.getMaterial() == Material.SAND)
				level.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, below, Block.getId(belowState));
		}

		if(isRaving() && !level.isClientSide && tickCount % 20 == 0 && shouldStopRaving()) {
			setRaving(false);
			jukeboxPosition = null;
		}
	}

	@Override
	public float getStepHeight() {
		float baseStep = wasTouchingWater ? 1F : 0.6F;
		AttributeInstance stepHeightAttribute = getAttribute(ForgeMod.STEP_HEIGHT_ADDITION.get());
		if (stepHeightAttribute != null) return (float) Math.max(0, baseStep + stepHeightAttribute.getValue());
		return baseStep;
	}

	@Nonnull
	@Override
	public EntityDimensions getDimensions(@Nonnull Pose poseIn) {
		return super.getDimensions(poseIn).scale(this.getSizeModifier());
	}

	@Override
	public boolean isPushedByFluid(FluidType type) {
		return false;
	}

	@Override
	protected int decreaseAirSupply(int air) {
		return air;
	}

	@Override
	public boolean isInvulnerableTo(@Nonnull DamageSource source) {
		return super.isInvulnerableTo(source) ||
			source == DamageSource.CACTUS ||
			source == DamageSource.SWEET_BERRY_BUSH ||
			source == DamageSource.LIGHTNING_BOLT ||
			getSizeModifier() > 1 && source.isFire();
	}

	@Override
	public boolean fireImmune() {
		return super.fireImmune() || getSizeModifier() > 1;
	}

	@Override
	public void thunderHit(@Nonnull ServerLevel sworld, @Nonnull LightningBolt lightningBolt) { // onStruckByLightning
		if (lightningCooldown > 0 || level.isClientSide)
			return;

		float sizeMod = getSizeModifier();
		if (sizeMod <= 15) {

			var healthAttr = this.getAttribute(Attributes.MAX_HEALTH);
			if (healthAttr != null) healthAttr.addPermanentModifier(new AttributeModifier("Lightning Bonus", 0.5, Operation.ADDITION));

			var speedAttr = this.getAttribute(Attributes.MOVEMENT_SPEED);
			if (speedAttr != null) speedAttr.addPermanentModifier(new AttributeModifier("Lightning Debuff", -0.05, Operation.ADDITION));

			var armorAttr = this.getAttribute(Attributes.ARMOR);
			if (armorAttr != null) armorAttr.addPermanentModifier(new AttributeModifier("Lightning Bonus", 0.125, Operation.ADDITION));

			float sizeModifier = Math.min(sizeMod + 1, 16);
			this.entityData.set(SIZE_MODIFIER, sizeModifier);
			refreshDimensions();

			lightningCooldown = 150;
		}
	}

	@Override
	public void push(@Nonnull Entity entityIn) {
		if (getSizeModifier() <= 1)
			super.push(entityIn);
	}

	@Override
	protected void doPush(@Nonnull Entity entityIn) {
		super.doPush(entityIn);
		if (level.getDifficulty() != Difficulty.PEACEFUL && !noSpike && !hasPassenger(entityIn))
			if (entityIn instanceof LivingEntity && !(entityIn instanceof Crab))
				entityIn.hurt(DamageSource.CACTUS, 1f);
	}

	@Override
	public boolean isFood(ItemStack stack) {
		return !stack.isEmpty() && getTemptationItems().test(stack);
	}

	private Ingredient getTemptationItems() {
		if(temptationItems == null)
			temptationItems = Ingredient.of(
					ItemTags.create(new ResourceLocation(Quark.MOD_ID, "crab_tempt_items")));

		return temptationItems;
	}

	@Nullable
	@Override // createChild
	public AgeableMob getBreedOffspring(@Nonnull ServerLevel sworld, @Nonnull AgeableMob other) {
		return new Crab(CrabsModule.crabType, level);
	}

	@Nonnull
	@Override
	protected ResourceLocation getDefaultLootTable() {
		return CRAB_LOOT_TABLE;
	}

	public int getVariant() {
		return Math.max(0, entityData.get(VARIANT));
	}

	public void party(BlockPos pos, boolean isPartying) {
		if (isPartying) {
			if (!isRaving()) {
				jukeboxPosition = pos;
				setRaving(true);
			}
		} else if (pos.equals(jukeboxPosition) || jukeboxPosition == null) {
			jukeboxPosition = null;
			setRaving(false);
		}
	}

	public boolean shouldStopRaving() {
		return jukeboxPosition == null ||
			!jukeboxPosition.closerToCenterThan(position(), GameEvent.JUKEBOX_PLAY.getNotificationRadius()) ||
			!level.getBlockState(jukeboxPosition).is(Blocks.JUKEBOX);

	}

	public boolean isRaving() {
		return entityData.get(RAVING);
	}

	public void setRaving(boolean raving) {
		entityData.set(RAVING, raving);
	}

	@Override
	public void onSyncedDataUpdated(@Nonnull EntityDataAccessor<?> parameter) {
		if (parameter.equals(SIZE_MODIFIER))
			refreshDimensions();

		super.onSyncedDataUpdated(parameter);
	}

	@Nonnull
	@Override
	public Packet<?> getAddEntityPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}

	@Override
	public void writeSpawnData(FriendlyByteBuf buffer) {
		buffer.writeFloat(getSizeModifier());
	}

	@Override
	public void readSpawnData(FriendlyByteBuf buffer) {
		entityData.set(SIZE_MODIFIER, buffer.readFloat());
	}

	@Override
	public void readAdditionalSaveData(@Nonnull CompoundTag compound) {
		super.readAdditionalSaveData(compound);

		lightningCooldown = compound.getInt("LightningCooldown");
		noSpike = compound.getBoolean("NoSpike");

		if (compound.contains("EnemyCrabRating")) {
			float sizeModifier = compound.getFloat("EnemyCrabRating");
			entityData.set(SIZE_MODIFIER, sizeModifier);
		}

		if(compound.contains("Variant"))
			entityData.set(VARIANT, compound.getInt("Variant"));

		this.setFromBucket(compound.getBoolean("FromBucket"));
	}

	@Override
	public void addAdditionalSaveData(@Nonnull CompoundTag compound) {
		super.addAdditionalSaveData(compound);
		compound.putFloat("EnemyCrabRating", getSizeModifier());
		compound.putInt("LightningCooldown", lightningCooldown);
		compound.putInt("Variant", entityData.get(VARIANT));
		compound.putBoolean("NoSpike", noSpike);
		compound.putBoolean("FromBucket", this.fromBucket());
	}

	public class JukeboxListener implements GameEventListener {
		private final PositionSource listenerSource;
		private final int listenerRadius;

		public JukeboxListener(PositionSource source, int radius) {
			this.listenerSource = source;
			this.listenerRadius = radius;
		}

		@Override
		@Nonnull
		public PositionSource getListenerSource() {
			return this.listenerSource;
		}

		@Override
		public int getListenerRadius() {
			return this.listenerRadius;
		}

		@Override
		public boolean handleGameEvent(@Nonnull ServerLevel level, GameEvent.Message message) {
			if (message.gameEvent() == GameEvent.JUKEBOX_PLAY) {
				Crab.this.party(new BlockPos(message.source()), true);
				return true;
			} else if (message.gameEvent() == GameEvent.JUKEBOX_STOP_PLAY) {
				Crab.this.party(new BlockPos(message.source()), false);
				return true;
			} else return false;
		}
	}

}
