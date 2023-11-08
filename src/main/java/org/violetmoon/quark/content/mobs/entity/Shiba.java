package org.violetmoon.quark.content.mobs.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.AbstractArrow.Pickup;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import org.violetmoon.quark.content.mobs.ai.BarkAtDarknessGoal;
import org.violetmoon.quark.content.mobs.ai.DeliverFetchedItemGoal;
import org.violetmoon.quark.content.mobs.ai.FetchArrowGoal;
import org.violetmoon.quark.content.mobs.module.ShibaModule;
import org.violetmoon.quark.content.tweaks.ai.NuzzleGoal;
import org.violetmoon.quark.content.tweaks.ai.WantLoveGoal;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.UUID;

public class Shiba extends TamableAnimal {

	private static final EntityDataAccessor<Integer> COLLAR_COLOR = SynchedEntityData.defineId(Shiba.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<ItemStack> MOUTH_ITEM = SynchedEntityData.defineId(Shiba.class, EntityDataSerializers.ITEM_STACK);
	private static final EntityDataAccessor<Integer> FETCHING = SynchedEntityData.defineId(Shiba.class, EntityDataSerializers.INT);

	public BlockPos currentHyperfocus = null;
	private int hyperfocusCooldown = 0;

	public Shiba(EntityType<? extends Shiba> type, Level worldIn) {
		super(type, worldIn);
		setTame(false);
	}

	@Override
	protected void registerGoals() {
		goalSelector.addGoal(1, new FloatGoal(this));
		goalSelector.addGoal(2, new SitWhenOrderedToGoal(this));
		goalSelector.addGoal(3, new BarkAtDarknessGoal(this));
		goalSelector.addGoal(4, new FetchArrowGoal(this));
		goalSelector.addGoal(5, new DeliverFetchedItemGoal(this, 1.1D, -1F, 32.0F, false));
		goalSelector.addGoal(6, new FollowOwnerGoal(this, 1.0D, 10.0F, 2.0F, false));
		goalSelector.addGoal(7, new TemptGoal(this, 1, Ingredient.of(Items.BONE), false));
		goalSelector.addGoal(8, new BreedGoal(this, 1.0D));
		goalSelector.addGoal(9, new NuzzleGoal(this, 0.5F, 16, 2, SoundEvents.WOLF_WHINE));
		goalSelector.addGoal(10, new WantLoveGoal(this, 0.2F));
		goalSelector.addGoal(11, new WaterAvoidingRandomStrollGoal(this, 1.0D));
		goalSelector.addGoal(12, new LookAtPlayerGoal(this, Player.class, 8.0F));
		goalSelector.addGoal(13, new RandomLookAroundGoal(this));
	}

	@Override
	public void tick() {
		super.tick();

		AbstractArrow fetching = getFetching();
		if(fetching != null && (isSleeping() || fetching.level != level || !fetching.isAlive() || fetching.pickup == Pickup.DISALLOWED))
			setFetching(null);

		if(!level.isClientSide) {
			if(hyperfocusCooldown > 0)
				hyperfocusCooldown--;

			if(fetching != null || isSleeping() || isInSittingPose() || !isTame() || isLeashed())
				currentHyperfocus = null;
			else {
				LivingEntity owner = getOwner();

				if(currentHyperfocus != null) {
					boolean hyperfocusClear = level.getBrightness(LightLayer.BLOCK, currentHyperfocus) > 0; 
					boolean ownerAbsent = owner == null
							|| (owner instanceof Player
								&& (!owner.getMainHandItem().is(Items.TORCH) && !owner.getOffhandItem().is(Items.TORCH)));
					
					if(hyperfocusClear || ownerAbsent) {
						currentHyperfocus = null;
						hyperfocusCooldown = 40;
						
						if(hyperfocusClear && !ownerAbsent && owner instanceof ServerPlayer sp)
							ShibaModule.shibaHelpTrigger.trigger(sp);
					}
				}

				if(currentHyperfocus == null && owner instanceof Player player && hyperfocusCooldown == 0) {
					if(player.getMainHandItem().is(Items.TORCH) || player.getOffhandItem().is(Items.TORCH)) {
						BlockPos ourPos = blockPosition();
						final int searchRange = 10;
						for(int i = 0; i < 20; i++) {
							BlockPos test = ourPos.offset(random.nextInt(searchRange * 2 + 1) - searchRange, random.nextInt(searchRange * 2 + 1) - searchRange, random.nextInt(searchRange * 2 + 1) - searchRange);
							if(hasLineOfSight(test.above(), searchRange)
									&& level.getBlockState(test).isAir()
									&& level.getBlockState(test.below()).isSolidRender(level, test.below())
									&& level.getBrightness(LightLayer.BLOCK, test) == 0
									&& !(ShibaModule.ignoreAreasWithSkylight && level.canSeeSky(test))) {

								currentHyperfocus = test;
							}
						}
					}
				}
			}
		}

		if(!isSleeping() && !level.isClientSide && fetching == null && getMouthItem().isEmpty()) {
			LivingEntity owner = getOwner();
			if(owner != null) {
				AABB check = owner.getBoundingBox().inflate(2);
				List<AbstractArrow> arrows = level.getEntitiesOfClass(AbstractArrow.class, check,
						a -> a.getOwner() == owner && a.pickup != Pickup.DISALLOWED);

				if(arrows.size() > 0) {
					AbstractArrow arrow = arrows.get(level.random.nextInt(arrows.size()));
					setFetching(arrow);
				}
			}
		}
	}

	public boolean hasLineOfSight(BlockPos pos, double maxRange) {
		Vec3 vec3 = new Vec3(this.getX(), this.getEyeY(), this.getZ());
		Vec3 vec31 = new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
		if(vec31.distanceTo(vec3) > maxRange) {
			return false;
		} else {
			return this.level.clip(new ClipContext(vec3, vec31, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this)).getType() == HitResult.Type.MISS;
		}
	}

	public AbstractArrow getFetching() {
		int id = entityData.get(FETCHING);
		if(id == -1)
			return null;

		Entity e = level.getEntity(id);
		if(!(e instanceof AbstractArrow))
			return null;

		return (AbstractArrow) e;
	}

	public void setFetching(AbstractArrow e) {
		entityData.set(FETCHING, e == null ? -1 : e.getId());
	}

	@Override
	public boolean isFood(ItemStack stack) {
		Item item = stack.getItem();
		return item.isEdible() && item.getFoodProperties().isMeat();
	}

	@Nonnull
	@Override
	public Packet<?> getAddEntityPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		entityData.define(COLLAR_COLOR, DyeColor.RED.getId());
		entityData.define(MOUTH_ITEM, ItemStack.EMPTY);
		entityData.define(FETCHING, -1);
	}

	public DyeColor getCollarColor() {
		return DyeColor.byId(this.entityData.get(COLLAR_COLOR));
	}

	public void setCollarColor(DyeColor collarcolor) {
		this.entityData.set(COLLAR_COLOR, collarcolor.getId());
	}

	public ItemStack getMouthItem() {
		return entityData.get(MOUTH_ITEM);
	}

	public void setMouthItem(ItemStack stack) {
		this.entityData.set(MOUTH_ITEM, stack);
	}

	@Override
	public int getMaxSpawnClusterSize() {
		return 8;
	}

	@Override
	public boolean canMate(@Nonnull Animal otherAnimal) {
		if (otherAnimal == this) {
			return false;
		} else if (!this.isTame()) {
			return false;
		} else if (!(otherAnimal instanceof Shiba wolfentity)) {
			return false;
		} else {
			if (!wolfentity.isTame()) {
				return false;
			} else if (wolfentity.isSleeping()) {
				return false;
			} else {
				return this.isInLove() && wolfentity.isInLove();
			}
		}
	}

	@Override
	public void addAdditionalSaveData(@Nonnull CompoundTag compound) {
		super.addAdditionalSaveData(compound);
		compound.putByte("CollarColor", (byte)this.getCollarColor().getId());

		CompoundTag itemcmp = new CompoundTag();
		ItemStack holding = getMouthItem();
		if(!holding.isEmpty())
			holding.save(itemcmp);
		compound.put("MouthItem", itemcmp);
	}

	@Override
	public void readAdditionalSaveData(@Nonnull CompoundTag compound) {
		super.readAdditionalSaveData(compound);
		if (compound.contains("CollarColor"))
			this.setCollarColor(DyeColor.byId(compound.getInt("CollarColor")));

		if(compound.contains("MouthItem")) {
			CompoundTag itemcmp = compound.getCompound("MouthItem");
			setMouthItem(ItemStack.of(itemcmp));
		}
	}

	@Nonnull
	@Override
	public InteractionResult mobInteract(Player player, @Nonnull InteractionHand hand) {
		ItemStack itemstack = player.getItemInHand(hand);
		Item item = itemstack.getItem();
		if(player.isDiscrete() && player.getMainHandItem().isEmpty()) {
			if(hand == InteractionHand.MAIN_HAND && WantLoveGoal.canPet(this)) {
				if(player.level instanceof ServerLevel serverLevel) {
					Vec3 pos = position();
					serverLevel.sendParticles(ParticleTypes.HEART, pos.x, pos.y + 0.5, pos.z, 1, 0, 0, 0, 0.1);
					playSound(SoundEvents.WOLF_WHINE, 0.6F, 0.5F + (float) Math.random() * 0.5F);
				} else player.swing(InteractionHand.MAIN_HAND);

				WantLoveGoal.setPetTime(this);
			}

			return InteractionResult.SUCCESS;
		} else
			if (this.level.isClientSide) {
				boolean flag = this.isOwnedBy(player) || this.isTame() || item == Items.BONE && !this.isTame();
				return flag ? InteractionResult.CONSUME : InteractionResult.PASS;
			} else {
				if (this.isTame()) {
					ItemStack mouthItem = getMouthItem();
					if(!mouthItem.isEmpty()) {
						ItemStack copy = mouthItem.copy();
						if(!player.addItem(copy))
							spawnAtLocation(copy);

						if(player.level instanceof ServerLevel serverLevel) {
							Vec3 pos = position();
							serverLevel.sendParticles(ParticleTypes.HEART, pos.x, pos.y + 0.5, pos.z, 1, 0, 0, 0, 0.1);
							playSound(SoundEvents.WOLF_WHINE, 0.6F, 0.5F + (float) Math.random() * 0.5F);
						}
						setMouthItem(ItemStack.EMPTY);
						return InteractionResult.SUCCESS;
					}

					if (this.isFood(itemstack) && this.getHealth() < this.getMaxHealth()) {
						if (!player.getAbilities().instabuild) {
							itemstack.shrink(1);
						}

						this.heal((float)item.getFoodProperties().getNutrition());
						return InteractionResult.SUCCESS;
					}

					if (!(item instanceof DyeItem)) {
						if(!itemstack.isEmpty() && mouthItem.isEmpty() && itemstack.getItem() instanceof SwordItem) {
							ItemStack copy = itemstack.copy();
							copy.setCount(1);
							itemstack.setCount(itemstack.getCount() - 1);

							setMouthItem(copy);
							return InteractionResult.SUCCESS;
						}

						InteractionResult actionresulttype = super.mobInteract(player, hand);
						if ((!actionresulttype.consumesAction() || this.isBaby()) && this.isOwnedBy(player)) {
							this.setOrderedToSit(!this.isOrderedToSit());
							this.jumping = false;
							this.navigation.stop();
							this.setTarget(null);
							return InteractionResult.SUCCESS;
						}

						return actionresulttype;
					}

					DyeColor dyecolor = ((DyeItem)item).getDyeColor();
					if (dyecolor != this.getCollarColor()) {
						this.setCollarColor(dyecolor);
						if (!player.getAbilities().instabuild) {
							itemstack.shrink(1);
						}

						return InteractionResult.SUCCESS;
					}
				} else if (item == Items.BONE) {
					if (!player.getAbilities().instabuild) {
						itemstack.shrink(1);
					}

					if (this.random.nextInt(3) == 0 && !net.minecraftforge.event.ForgeEventFactory.onAnimalTame(this, player)) {
						WantLoveGoal.setPetTime(this);

						this.tame(player);
						this.navigation.stop();
						this.setTarget(null);
						this.setOrderedToSit(true);
						this.level.broadcastEntityEvent(this, (byte)7);
					} else {
						this.level.broadcastEntityEvent(this, (byte)6);
					}

					return InteractionResult.SUCCESS;
				}

				return super.mobInteract(player, hand);
			}
	}

	@Override
	public void setTame(boolean tamed) {
		super.setTame(tamed);
		if(tamed) {
			getAttribute(Attributes.MAX_HEALTH).setBaseValue(20);
			setHealth(20);
		} getAttribute(Attributes.MAX_HEALTH).setBaseValue(8);

		getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(4);
	}

	@Override
	protected void playStepSound(@Nonnull BlockPos pos, @Nonnull BlockState blockIn) {
		playSound(SoundEvents.WOLF_STEP, 0.15F, 1.0F);
	}

	@Override
	protected SoundEvent getAmbientSound() {
		if(random.nextInt(3) == 0)
			return (getHealth() / getMaxHealth()) < 0.5f ? SoundEvents.WOLF_WHINE : SoundEvents.WOLF_PANT;
		else
			return SoundEvents.WOLF_AMBIENT;
	}

	@Override
	protected SoundEvent getHurtSound(@Nonnull DamageSource damageSourceIn) {
		return SoundEvents.WOLF_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.WOLF_DEATH;
	}

	@Override
	protected float getSoundVolume() {
		return 0.4F;
	}

	@Override // make baby
	public AgeableMob getBreedOffspring(@Nonnull ServerLevel world, @Nonnull AgeableMob mate) {
		Shiba wolfentity = ShibaModule.shibaType.create(world);
		UUID uuid = this.getOwnerUUID();
		if (uuid != null) {
			wolfentity.setOwnerUUID(uuid);
			wolfentity.setTame(true);
		}

		return wolfentity;
	}

}
