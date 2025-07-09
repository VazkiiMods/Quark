package org.violetmoon.quark.content.tools.entity.rang;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.event.EventHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.base.handler.QuarkSounds;
import org.violetmoon.quark.content.mobs.entity.Toretoise;
import org.violetmoon.quark.content.tools.config.PickarangType;
import org.violetmoon.quark.content.tools.module.PickarangModule;

import java.util.List;
import java.util.UUID;

public abstract class AbstractPickarang<T extends AbstractPickarang<T>> extends Projectile {

	private static final EntityDataAccessor<ItemStack> STACK = SynchedEntityData.defineId(AbstractPickarang.class, EntityDataSerializers.ITEM_STACK);
	private static final EntityDataAccessor<Boolean> RETURNING = SynchedEntityData.defineId(AbstractPickarang.class, EntityDataSerializers.BOOLEAN);

	private UUID ownerId;
	protected int liveTime;
	private int slot;
	private int blockHitCount;
	private IntOpenHashSet entitiesHit;

	private static final String TAG_RETURNING = "returning";
	private static final String TAG_LIVE_TIME = "liveTime";
	private static final String TAG_BLOCKS_BROKEN = "hitCount";
	private static final String TAG_RETURN_SLOT = "returnSlot";
	private static final String TAG_ITEM_STACK = "itemStack";

	public AbstractPickarang(EntityType<? extends AbstractPickarang<?>> type, Level worldIn) {
		super(type, worldIn);
	}

	public AbstractPickarang(EntityType<? extends AbstractPickarang<?>> type, Level worldIn, LivingEntity throwerIn) {
		super(type, worldIn);
		Vec3 pos = throwerIn.position();
		this.setPos(pos.x, pos.y + throwerIn.getEyeHeight(), pos.z);
		ownerId = throwerIn.getUUID();
	}

	@Override
	public boolean shouldRenderAtSqrDistance(double distance) {
		double d0 = this.getBoundingBox().getSize() * 4.0D;
		if(Double.isNaN(d0))
			d0 = 4.0D;

		d0 = d0 * 64.0D;
		return distance < d0 * d0;
	}

	public void shoot(Entity entityThrower, float rotationPitchIn, float rotationYawIn, float pitchOffset, float velocity, float inaccuracy) {
		float f = -Mth.sin(rotationYawIn * ((float) Math.PI / 180F)) * Mth.cos(rotationPitchIn * ((float) Math.PI / 180F));
		float f1 = -Mth.sin((rotationPitchIn + pitchOffset) * ((float) Math.PI / 180F));
		float f2 = Mth.cos(rotationYawIn * ((float) Math.PI / 180F)) * Mth.cos(rotationPitchIn * ((float) Math.PI / 180F));
		this.shoot(f, f1, f2, velocity, inaccuracy);
		Vec3 Vector3d = entityThrower.getDeltaMovement();
		this.setDeltaMovement(this.getDeltaMovement().add(Vector3d.x, entityThrower.onGround() ? 0.0D : Vector3d.y, Vector3d.z));
	}

	@Override
	public void shoot(double x, double y, double z, float velocity, float inaccuracy) {
		Vec3 vec = (new Vec3(x, y, z)).normalize().add(this.random.nextGaussian() * 0.0075F * inaccuracy, this.random.nextGaussian() * 0.0075F * inaccuracy, this.random.nextGaussian() * 0.0075F * inaccuracy).scale(velocity);
		this.setDeltaMovement(vec);
		float f = (float) vec.horizontalDistance();
		setYRot((float) (Mth.atan2(vec.x, vec.z) * (180F / (float) Math.PI)));
		setXRot((float) (Mth.atan2(vec.y, f) * (180F / (float) Math.PI)));
		this.yRotO = this.getYRot();
		this.xRotO = this.getXRot();
	}

	@Override
	public void lerpMotion(double x, double y, double z) {
		this.setDeltaMovement(x, y, z);
		if(this.xRotO == 0.0F && this.yRotO == 0.0F) {
			float f = (float) Math.sqrt(x * x + z * z);
			setYRot((float) (Mth.atan2(x, z) * (180F / (float) Math.PI)));
			setXRot((float) (Mth.atan2(y, f) * (180F / (float) Math.PI)));
			this.yRotO = this.getYRot();
			this.xRotO = this.getXRot();
		}

	}

	public void setThrowData(int slot, ItemStack stack) {
		this.slot = slot;
		setStack(stack.copy());
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		builder.define(STACK, new ItemStack(PickarangModule.pickarang));
		builder.define(RETURNING, false);
	}

	protected void checkImpact() {
		if(level().isClientSide)
			return;

		Vec3 motion = getDeltaMovement();
		Vec3 position = position();
		Vec3 rayEnd = position.add(motion);

		boolean doEntities = true;
		int tries = 100;

		while(isAlive() && !isReturning()) {
			if(doEntities) {
				EntityHitResult result = raycastEntities(position, rayEnd);
				if(result != null)
					onHit(result);
				else
					doEntities = false;
			} else {
				HitResult result = level().clip(new ClipContext(position, rayEnd, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
				if(result.getType() == Type.MISS)
					return;

				else {
					onHit(result);
				}
			}

			if(tries-- <= 0) {
				(new RuntimeException("Pickarang hit way too much, this shouldn't happen")).printStackTrace();
				return;
			}
		}
	}

	@Nullable
	protected EntityHitResult raycastEntities(Vec3 from, Vec3 to) {
		return ProjectileUtil.getEntityHitResult(level(), this, from, to, getBoundingBox().expandTowards(getDeltaMovement()).inflate(1.0D), (entity) -> !entity.isSpectator()
				&& entity.isAlive()
				&& (entity.isPickable() || entity instanceof AbstractPickarang)
				&& entity != getOwner()
				&& (entitiesHit == null || !entitiesHit.contains(entity.getId())));
	}

	protected boolean isBlockBlackListead(BlockState state) {
		return state.is(PickarangModule.pickarangImmuneTag);
	}

	@Override
	protected void onHitBlock(BlockHitResult result) {
		super.onHitBlock(result);

		BlockPos hit = result.getBlockPos();
		BlockState state = level().getBlockState(hit);

		if(getPiercingModifier() == 0 || state.isSolid())
			addHit();

		if(!(getOwner() instanceof ServerPlayer player))
			return;

		ItemStack prev = player.getMainHandItem();

		try {
			//temporarily put the pickarang in the inventory, so the player can "mine" the block with it
			player.getInventory().setItem(player.getInventory().selected, getStack());
			player.setItemInHand(InteractionHand.MAIN_HAND, getStack());

			//more general way of doing it instead of just checking hardness
			float progress = getBlockDestroyProgress(state, player, level(), hit);
			if(progress == 0)
				return; //the "finally" block will still run

			//re calculates equivalent hardness assuming correct tool for drop
			float equivalentHardness = ((1 / progress) / 100) * (100 / 30f);

			if((equivalentHardness <= getPickarangType().maxHardness
				&& equivalentHardness >= 0
				&& !isBlockBlackListead(state)) || player.getAbilities().instabuild) {

				if(player.gameMode.destroyBlock(hit)) {
					level().levelEvent(null, LevelEvent.PARTICLES_DESTROY_BLOCK, hit, Block.getId(state));
				} else {
					clank(result);
				}

				setStack(player.getInventory().getSelected());

			} else
				clank(result);

		} finally {
			player.setItemInHand(InteractionHand.MAIN_HAND, prev);
			player.getInventory().setItem(player.getInventory().selected, prev);
		}
	}

	@Override
	protected void onHitEntity(EntityHitResult result) {
		super.onHitEntity(result);

		Entity owner = getOwner();
		Entity hit = result.getEntity();
		if(hit == owner) return;

		addHit(hit);
		if (hit instanceof AbstractPickarang<?> apg) {
			apg.setReturning();
			clank(result);
			return;
		}
		ItemStack pickarang = getStack();

		ItemAttributeModifiers modifiers = pickarang.getAttributeModifiers();
		Multimap<Holder<Attribute>, AttributeModifier> attributeModifiers = ArrayListMultimap.create();

		for (ItemAttributeModifiers.Entry entry : modifiers.modifiers()) {
			attributeModifiers.put(entry.attribute(), entry.modifier());
		}

		if (owner instanceof LivingEntity leOwner) {
			ItemStack prev = leOwner.getMainHandItem();
			leOwner.setItemInHand(InteractionHand.MAIN_HAND, pickarang);
			leOwner.getAttributes().addTransientAttributeModifiers(attributeModifiers);
			PickarangModule.setActivePickarangDamage(PickarangModule.getDamageSource(this, leOwner));

			int ticksSinceLastSwing = leOwner.attackStrengthTicker;
			leOwner.attackStrengthTicker = (int) (1.0 / leOwner.getAttributeValue(Attributes.ATTACK_SPEED) * 20.0) + 1;

			float prevHealth = hit instanceof LivingEntity ? ((LivingEntity) hit).getHealth() : 0;

			hitEntity:
			{
				if (hit instanceof Toretoise toretoise) {
					int ore = toretoise.getOreType();

					if (ore != 0) {
						addHit(toretoise);
						if (level() instanceof ServerLevel serverLevel) {
							LootParams.Builder lootBuilder = new LootParams.Builder(serverLevel)
									.withParameter(LootContextParams.TOOL, pickarang);
							if (owner instanceof Player player)
								lootBuilder.withLuck(player.getLuck());
							toretoise.dropOre(ore, lootBuilder);
						}
						break hitEntity;
					}
				}

				// we need to call attack for enchantments and whatnot. we need the damage source hack so we can set it as a projectile so its not the player damage source
				if (owner instanceof Player p)
					p.attack(hit);
				else
					leOwner.doHurtTarget(hit);

				if (hit instanceof LivingEntity && ((LivingEntity) hit).getHealth() == prevHealth)
					clank(result);
			}

			leOwner.attackStrengthTicker = ticksSinceLastSwing;

			setStack(leOwner.getMainHandItem());
			leOwner.setItemInHand(InteractionHand.MAIN_HAND, prev);
			leOwner.getAttributes().addTransientAttributeModifiers(attributeModifiers);
			PickarangModule.setActivePickarangDamage(null);
		} else {
			Builder mapBuilder = new Builder();
			mapBuilder.add(Attributes.ATTACK_DAMAGE, 1);
			AttributeSupplier map = mapBuilder.build();
			AttributeMap manager = new AttributeMap(map);
			manager.addTransientAttributeModifiers(attributeModifiers);

			ItemStack stack = getStack();
			stack.hurtAndBreak(1, (LivingEntity) owner, null);
			setStack(stack);

			hit.hurt(PickarangModule.getDamageSource(this, owner), (float) manager.getValue(Attributes.ATTACK_DAMAGE));
		}
	}

	private void setTransientModifiers(ItemAttributeModifiers modifiers, LivingEntity living) {
		for (ItemAttributeModifiers.Entry entry : modifiers.modifiers()) {
			AttributeInstance instance = living.getAttribute(entry.attribute());
			if (instance != null) {
				instance.addTransientModifier(entry.modifier());
			}
		}
	}

	//equivalent of BlockState::getDestroyProgress
	private float getBlockDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
		float f = state.getDestroySpeed(level, pos);
		if(f == -1.0F) {
			return 0.0F;
		} else {
			float i = EventHooks.doPlayerHarvestCheck(player, state, level, pos) ? 30 : 100;
			float digSpeed = getPlayerDigSpeed(player, state, pos);
			return (digSpeed / (f * i));
		}
	}

	//equivalent of Player::getDigSpeed but without held item stack stuff
	private float getPlayerDigSpeed(Player player, BlockState state, @Nullable BlockPos pos) {
		float f = 1;

		if(MobEffectUtil.hasDigSpeed(player)) {
			f *= 1.0F + (MobEffectUtil.getDigSpeedAmplification(player) + 1) * 0.2F;
		}

		if(player.hasEffect(MobEffects.DIG_SLOWDOWN)) {
			float f1 = switch(player.getEffect(MobEffects.DIG_SLOWDOWN).getAmplifier()) {
			case 0 -> 0.3F;
			case 1 -> 0.09F;
			case 2 -> 0.0027F;
			default -> 8.1E-4F;
			};

			f *= f1;
		}
		if(this.isEyeInFluidType(NeoForgeMod.WATER_TYPE.value())) {
			f /= 5.0F;
		}
		f = EventHooks.getBreakSpeed(player, state, f, pos);
		return f;
	}

	public void spark() {
		playSound(QuarkSounds.ENTITY_PICKARANG_SPARK, 1, 1);
		setReturning();
	}

	public void clank(HitResult hit) {
		if(hit instanceof BlockHitResult bh) {
			BlockState state = level().getBlockState(bh.getBlockPos());
			Vec3 hitPos = bh.getLocation();
			((ServerLevel) this.level()).sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, state),
					hitPos.x, hitPos.y, hitPos.z, 2, 0, 0, 0, 0);
		}
		playSound(QuarkSounds.ENTITY_PICKARANG_CLANK, 1, 1);
		setReturning();
	}

	public void addHit(Entity entity) {
		if(entitiesHit == null)
			entitiesHit = new IntOpenHashSet(5);
		entitiesHit.add(entity.getId());
		postHit();
	}

	public void postHit() {
		if((entitiesHit == null ? 0 : entitiesHit.size()) + blockHitCount > getPiercingModifier())
			setReturning();
		else if(getPiercingModifier() > 0)
			setDeltaMovement(getDeltaMovement().scale(0.8));
	}

	public void addHit() {
		blockHitCount++;
		postHit();
	}

	protected void setReturning() {
		entityData.set(RETURNING, true);
	}

	@Override
	public boolean isPushedByFluid() {
		return false;
	}

	@Override
	public void tick() {
		Vec3 pos = position();

		this.xOld = pos.x;
		this.yOld = pos.y;
		this.zOld = pos.z;
		super.tick();

		if(!isReturning())
			checkImpact();

		Vec3 ourMotion = this.getDeltaMovement();
		setPos(pos.x + ourMotion.x, pos.y + ourMotion.y, pos.z + ourMotion.z);

		float f = (float) ourMotion.horizontalDistance();
		setYRot((float) (Mth.atan2(ourMotion.x, ourMotion.z) * (180F / (float) Math.PI)));

		setXRot((float) (Mth.atan2(ourMotion.y, f) * (180F / (float) Math.PI)));
		while(this.getXRot() - this.xRotO < -180.0F)
			this.xRotO -= 360.0F;

		while(this.getXRot() - this.xRotO >= 180.0F)
			this.xRotO += 360.0F;

		while(this.getYRot() - this.yRotO < -180.0F)
			this.yRotO -= 360.0F;

		while(this.getYRot() - this.yRotO >= 180.0F)
			this.yRotO += 360.0F;

		setXRot(Mth.lerp(0.2F, this.xRotO, this.getXRot()));
		setYRot(Mth.lerp(0.2F, this.yRotO, this.getYRot()));

		float drag;
		Level level = this.level();
		if(this.isInWater()) {
			for(int i = 0; i < 4; ++i) {
				level.addParticle(ParticleTypes.BUBBLE, pos.x - ourMotion.x * 0.25D, pos.y - ourMotion.y * 0.25D, pos.z - ourMotion.z * 0.25D, ourMotion.x, ourMotion.y, ourMotion.z);
			}

			drag = 0.8F;
		} else
			drag = 0.99F;

		if(hasDrag())
			this.setDeltaMovement(ourMotion.scale(drag));

		pos = position();
		this.setPos(pos.x, pos.y, pos.z);

		if(!isAlive())
			return;

		ItemStack stack = getStack();
		emitParticles(pos, ourMotion);

		boolean returning = isReturning();
		liveTime++;

		Entity owner = getOwner();
		if(owner == null || !owner.isAlive() || !(owner instanceof Player)) {
			if(!level.isClientSide) {
				while(isInWall())
					setPos(getX(), getY() + 1, getZ());

				spawnAtLocation(stack, 0);
				discard();
			}

			return;
		}

		if(!returning) {
			if(liveTime > getPickarangType().timeout)
				setReturning();
			if(!level.getWorldBorder().isWithinBounds(getBoundingBox()))
				spark();
		} else {
			noPhysics = true;

			int eff = getEfficiencyModifier();

			List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, getBoundingBox().inflate(2));
			List<ExperienceOrb> xp = level.getEntitiesOfClass(ExperienceOrb.class, getBoundingBox().inflate(2));

			Vec3 ourPos = position();
			for(ItemEntity item : items) {
				if(item.isPassenger())
					continue;
				item.startRiding(this);

				item.setPickUpDelay(5);
			}

			for(ExperienceOrb xpOrb : xp) {
				if(xpOrb.isPassenger())
					continue;
				xpOrb.startRiding(this);
			}

			Vec3 ownerPos = owner.position().add(0, 1, 0);
			Vec3 motion = ownerPos.subtract(ourPos);
			double motionMag = 3.25 + eff * 0.25;

			if(motion.lengthSqr() < motionMag) {
				Player player = (Player) owner;
				Inventory inventory = player.getInventory();
				ItemStack stackInSlot = inventory.getItem(slot);

				if(!level.isClientSide) {
					playSound(QuarkSounds.ENTITY_PICKARANG_PICKUP, 1, 1);

					if(player instanceof ServerPlayer sp && (this instanceof Flamerang) && isOnFire() && !getPassengers().isEmpty())
						PickarangModule.useFlamerangTrigger.trigger(sp);

					if(!stack.isEmpty())
						if(player.isAlive() && stackInSlot.isEmpty())
							inventory.setItem(slot, stack);
						else if(!player.isAlive() || !inventory.add(stack))
							player.drop(stack, false);

					if(player.isAlive()) {
						for(ItemEntity item : items)
							if(item.isAlive())
								giveItemToPlayer(player, item);

						for(ExperienceOrb xpOrb : xp)
							if(xpOrb.isAlive())
								xpOrb.playerTouch(player);

						for(Entity riding : getPassengers()) {
							if(!riding.isAlive())
								continue;

							if(riding instanceof ItemEntity)
								giveItemToPlayer(player, (ItemEntity) riding);
							else if(riding instanceof ExperienceOrb)
								riding.playerTouch(player);
						}
					}

					discard();
				}
			} else
				setDeltaMovement(motion.normalize().scale(0.7 + eff * 0.325F));
		}
	}

	public boolean isReturning() {
		return entityData.get(RETURNING);
	}

	protected void emitParticles(Vec3 pos, Vec3 ourMotion) {
		// NO-OP
	}

	public boolean hasDrag() {
		return true;
	}

	public abstract PickarangType<T> getPickarangType();

	private void giveItemToPlayer(Player player, ItemEntity itemEntity) {
		itemEntity.setPickUpDelay(0);
		itemEntity.playerTouch(player);

		if(itemEntity.isAlive()) {
			// Player could not pick up everything
			ItemStack drop = itemEntity.getItem();

			player.drop(drop, false);
			itemEntity.discard();
		}
	}

	@Override
	protected boolean canAddPassenger(@NotNull Entity passenger) {
		return super.canAddPassenger(passenger) || passenger instanceof ItemEntity || passenger instanceof ExperienceOrb;
	}

	@NotNull
	@Override
	public SoundSource getSoundSource() {
		return SoundSource.PLAYERS;
	}

	public int getEfficiencyModifier() {
		Holder<Enchantment> holder = level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.EFFICIENCY);
		return Quark.ZETA.itemExtensions.get(getStack()).getEnchantmentLevelZeta(getStack(), holder);
	}

	public int getPiercingModifier() {
		Holder<Enchantment> holder = level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.PIERCING);
		return Quark.ZETA.itemExtensions.get(getStack()).getEnchantmentLevelZeta(getStack(), holder);
	}

	public ItemStack getStack() {
		return entityData.get(STACK);
	}

	public void setStack(ItemStack stack) {
		entityData.set(STACK, stack);
	}

	@Override
	public void readAdditionalSaveData(@NotNull CompoundTag compound) {
		entityData.set(RETURNING, compound.getBoolean(TAG_RETURNING));
		liveTime = compound.getInt(TAG_LIVE_TIME);
		blockHitCount = compound.getInt(TAG_BLOCKS_BROKEN);
		slot = compound.getInt(TAG_RETURN_SLOT);

		if(compound.contains(TAG_ITEM_STACK))
			setStack(ItemStack.parseOptional(level().registryAccess(), compound.getCompound(TAG_ITEM_STACK)));
		else
			setStack(new ItemStack(PickarangModule.pickarang));

		if(compound.contains("owner", 10)) {
			Tag owner = compound.get("owner");
			if(owner != null)
				this.ownerId = NbtUtils.loadUUID(owner);
		}
	}

	@Override
	public void addAdditionalSaveData(@NotNull CompoundTag compound) {
		compound.putBoolean(TAG_RETURNING, isReturning());
		compound.putInt(TAG_LIVE_TIME, liveTime);
		compound.putInt(TAG_BLOCKS_BROKEN, blockHitCount);
		compound.putInt(TAG_RETURN_SLOT, slot);
		compound.put(TAG_ITEM_STACK, getStack().save(level().registryAccess()));
		if(this.ownerId != null)
			compound.put("owner", NbtUtils.createUUID(this.ownerId));
	}
}
