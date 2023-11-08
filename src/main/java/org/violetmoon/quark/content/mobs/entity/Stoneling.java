package org.violetmoon.quark.content.mobs.entity;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraftforge.common.Tags;
import net.minecraftforge.network.NetworkHooks;
import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.base.handler.MiscUtil;
import org.violetmoon.quark.base.handler.QuarkSounds;
import org.violetmoon.quark.base.util.IfFlagGoal;
import org.violetmoon.quark.content.mobs.ai.ActWaryGoal;
import org.violetmoon.quark.content.mobs.ai.FavorBlockGoal;
import org.violetmoon.quark.content.mobs.ai.RunAndPoofGoal;
import org.violetmoon.quark.content.mobs.module.StonelingsModule;
import org.violetmoon.quark.content.tools.entity.rang.Pickarang;
import org.violetmoon.quark.content.world.module.GlimmeringWealdModule;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

import static org.violetmoon.quark.content.world.module.NewStoneTypesModule.*;

public class Stoneling extends PathfinderMob {

	public static final ResourceLocation CARRY_LOOT_TABLE = new ResourceLocation("quark", "entities/stoneling_carry");

	private static final EntityDataAccessor<ItemStack> CARRYING_ITEM = SynchedEntityData.defineId(Stoneling.class, EntityDataSerializers.ITEM_STACK);
	private static final EntityDataAccessor<Byte> VARIANT = SynchedEntityData.defineId(Stoneling.class, EntityDataSerializers.BYTE);
	private static final EntityDataAccessor<Float> HOLD_ANGLE = SynchedEntityData.defineId(Stoneling.class, EntityDataSerializers.FLOAT);
	public static final EntityDataAccessor<Boolean> HAS_LICHEN = SynchedEntityData.defineId(Stoneling.class, EntityDataSerializers.BOOLEAN);

	private static final String TAG_CARRYING_ITEM = "carryingItem";
	private static final String TAG_VARIANT = "variant";
	private static final String TAG_HAS_LICHEN = "has_lichen";
	private static final String TAG_HOLD_ANGLE = "itemAngle";
	private static final String TAG_PLAYER_MADE = "playerMade";

	private ActWaryGoal waryGoal;

	private boolean isTame;

	public Stoneling(EntityType<? extends Stoneling> type, Level worldIn) {
		super(type, worldIn);
		this.setPathfindingMalus(BlockPathTypes.DAMAGE_CACTUS, 1.0F);
		this.setPathfindingMalus(BlockPathTypes.DANGER_CACTUS, 1.0F);
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();

		entityData.define(CARRYING_ITEM, ItemStack.EMPTY);
		entityData.define(VARIANT, (byte) 0);
		entityData.define(HOLD_ANGLE, 0F);
		entityData.define(HAS_LICHEN, false);
	}

	@Override
	protected void registerGoals() {
		goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.2, 0.98F));
		goalSelector.addGoal(4, new FavorBlockGoal(this, 0.2, s -> s.is(Tags.Blocks.ORES_DIAMOND)));
		goalSelector.addGoal(3, new IfFlagGoal(new TemptGoal(this, 0.6, Ingredient.of(temptTag()), false), () -> StonelingsModule.enableDiamondHeart && !StonelingsModule.tamableStonelings));
		goalSelector.addGoal(2, new RunAndPoofGoal<>(this, Player.class, 4, 0.5, 0.5));
		goalSelector.addGoal(1, waryGoal = new ActWaryGoal(this, 0.1, 6, () -> StonelingsModule.cautiousStonelings));
		goalSelector.addGoal(0, new IfFlagGoal(new TemptGoal(this, 0.6, Ingredient.of(temptTag()), false), () -> StonelingsModule.tamableStonelings));
	}

	private TagKey<Item> temptTag() {
		return Quark.ZETA.modules.isEnabled(GlimmeringWealdModule.class) ? GlimmeringWealdModule.glowShroomFeedablesTag : Tags.Items.GEMS_DIAMOND;
	}

	public static AttributeSupplier.Builder prepareAttributes() {
		return Mob.createMobAttributes()
				.add(Attributes.MAX_HEALTH, 8.0D)
				.add(Attributes.KNOCKBACK_RESISTANCE, 1D);
	}

	@Override
	public void tick() {
		super.tick();

		if (wasTouchingWater)
			maxUpStep = 1F;
		else
			maxUpStep = 0.6F;

		this.yBodyRotO = this.yRotO;
		this.yBodyRot = this.getYRot();
	}

	@Override
	public MobCategory getClassification(boolean forSpawnCount) {
		if (isTame)
			return MobCategory.CREATURE;
		return MobCategory.MONSTER;
	}

	@Override
	public boolean removeWhenFarAway(double distanceToClosestPlayer) {
		return !isTame;
	}

	@Override
	public void checkDespawn() {
		boolean wasAlive = isAlive();
		super.checkDespawn();
		if (!isAlive() && wasAlive)
			for (Entity passenger : getIndirectPassengers())
				if (!(passenger instanceof Player))
					passenger.discard();
	}

	@Nonnull
	@Override // processInteract
	public InteractionResult mobInteract(Player player, @Nonnull InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);

		if(!stack.isEmpty() && stack.getItem() == Items.NAME_TAG)
			return stack.getItem().interactLivingEntity(stack, player, this, hand);
		else
			return super.mobInteract(player, hand);
	}

	@Nonnull
	@Override
	public InteractionResult interactAt(@Nonnull Player player, @Nonnull Vec3 vec, @Nonnull InteractionHand hand) {
		if(hand == InteractionHand.MAIN_HAND && isAlive()) {
			ItemStack playerItem = player.getItemInHand(hand);
			Vec3 pos = position();

			if(!level.isClientSide) {
				if (isPlayerMade()) {
					if (!player.isDiscrete() && !playerItem.isEmpty()) {
						StonelingVariant currentVariant = getVariant();
						StonelingVariant targetVariant = null;
						Block targetBlock = null;
						mainLoop: for (StonelingVariant variant : StonelingVariant.values()) {
							for (Block block : variant.getBlocks()) {
								if (block.asItem() == playerItem.getItem()) {
									targetVariant = variant;
									targetBlock = block;
									break mainLoop;
								}
							}
						}

						if (targetVariant != null) {
							if (level instanceof ServerLevel serverLevel) {
								serverLevel.sendParticles(ParticleTypes.HEART, pos.x, pos.y + getBbHeight(), pos.z, 1, 0.1, 0.1, 0.1, 0.1);
								if (targetVariant != currentVariant)
									serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, targetBlock.defaultBlockState()), pos.x, pos.y + getBbHeight() / 2, pos.z, 16, 0.1, 0.1, 0.1, 0.25);
							}

							if (targetVariant != currentVariant) {
								playSound(QuarkSounds.ENTITY_STONELING_EAT, 1F, 1F);
								entityData.set(VARIANT, targetVariant.getIndex());
							}

							playSound(QuarkSounds.ENTITY_STONELING_PURR, 1F, 1F + level.random.nextFloat());

							heal(1);

							if (!player.getAbilities().instabuild)
								playerItem.shrink(1);

							return InteractionResult.SUCCESS;
						}

						return InteractionResult.PASS;
					}

					ItemStack stonelingItem = entityData.get(CARRYING_ITEM);

					if (!stonelingItem.isEmpty() || !playerItem.isEmpty()) {
						player.setItemInHand(hand, stonelingItem.copy());
						entityData.set(CARRYING_ITEM, playerItem.copy());

						if (playerItem.isEmpty())
							playSound(QuarkSounds.ENTITY_STONELING_GIVE, 1F, 1F);
						else playSound(QuarkSounds.ENTITY_STONELING_TAKE, 1F, 1F);
					}
				} else if (StonelingsModule.tamableStonelings && playerItem.is(temptTag())) {
					heal(8);

					setPlayerMade(true);

					playSound(QuarkSounds.ENTITY_STONELING_PURR, 1F, 1F + level.random.nextFloat());

					if (!player.getAbilities().instabuild)
						playerItem.shrink(1);

					if (level instanceof ServerLevel)
						((ServerLevel) level).sendParticles(ParticleTypes.HEART, pos.x, pos.y + getBbHeight(), pos.z, 4, 0.1, 0.1, 0.1, 0.1);

					return InteractionResult.SUCCESS;
				}
			}
		}

		return InteractionResult.PASS;
	}

	@Nullable
	@Override
	public SpawnGroupData finalizeSpawn(ServerLevelAccessor world, @Nonnull DifficultyInstance difficulty, @Nonnull MobSpawnType spawnReason, @Nullable SpawnGroupData data, @Nullable CompoundTag compound) {
		RandomSource rand = world.getRandom();
		byte variant;
		if (data instanceof StonelingVariant stonelingVariant)
			variant = stonelingVariant.getIndex();
		else
			variant = (byte) rand.nextInt(StonelingVariant.values().length);

		entityData.set(VARIANT, variant);
		entityData.set(HAS_LICHEN, world.getBiome(getOnPos()).is(GlimmeringWealdModule.BIOME_NAME) && rand.nextInt(5) < 3);
		entityData.set(HOLD_ANGLE, world.getRandom().nextFloat() * 90 - 45);

		if(!isTame && !world.isClientSide()) {
			List<ItemStack> items = world.getServer().getLootTables()
					.get(CARRY_LOOT_TABLE).getRandomItems(new LootContext.Builder((ServerLevel) world)
							.withParameter(LootContextParams.ORIGIN, position())
							.create(LootContextParamSets.CHEST));
			if (!items.isEmpty())
				entityData.set(CARRYING_ITEM, items.get(0));
		}

		return super.finalizeSpawn(world, difficulty, spawnReason, data, compound);
	}

	@Override
	public boolean isInvulnerableTo(@Nonnull DamageSource source) {
		return source == DamageSource.CACTUS ||
				isProjectileWithoutPiercing(source) ||
				super.isInvulnerableTo(source);
	}

	private static boolean isProjectileWithoutPiercing(DamageSource source) {
		if (!source.isProjectile())
			return false;

		Entity sourceEntity = source.getDirectEntity();

		if (sourceEntity instanceof Pickarang pickarang)
			return pickarang.getPiercingModifier() <= 0;
		else if (sourceEntity instanceof AbstractArrow arrow)
			return arrow.getPierceLevel() <= 0;
		return true;
	}

	@Override
	public boolean canBreatheUnderwater() {
		return true;
	}


	@Override
	public boolean checkSpawnObstruction(LevelReader worldReader) {
		return worldReader.isUnobstructed(this, Shapes.create(getBoundingBox()));
	}

	@Override
	public double getPassengersRidingOffset() {
		return this.getBbHeight();
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
	public boolean causeFallDamage(float distance, float damageMultiplier, @Nonnull DamageSource source) {
		return false;
	}

	@Override
	protected void actuallyHurt(@Nonnull DamageSource damageSrc, float damageAmount) {
		super.actuallyHurt(damageSrc, damageAmount);

		if(!isPlayerMade() && damageSrc.getEntity() instanceof Player) {
			startle();
			for (Entity entity : level.getEntities(this,
					getBoundingBox().inflate(16))) {
				if (entity instanceof Stoneling stoneling) {
					if (!stoneling.isPlayerMade() && stoneling.getSensing().hasLineOfSight(this)) {
						startle();
					}
				}
			}
		}
	}

	public boolean isStartled() {
		return waryGoal.isStartled();
	}

	public void startle() {
		waryGoal.startle();
		Set<WrappedGoal> entries = Sets.newHashSet(goalSelector.getAvailableGoals());

		for (WrappedGoal task : entries)
			if (task.getGoal() instanceof TemptGoal)
				goalSelector.removeGoal(task.getGoal());
	}

	@Override
	protected void dropCustomDeathLoot(@Nonnull DamageSource damage, int looting, boolean wasRecentlyHit) {
		super.dropCustomDeathLoot(damage, looting, wasRecentlyHit);

		ItemStack stack = getCarryingItem();
		if(!stack.isEmpty())
			spawnAtLocation(stack, 0F);
	}

	public void setPlayerMade(boolean value) {
		isTame = value;
	}

	public ItemStack getCarryingItem() {
		return entityData.get(CARRYING_ITEM);
	}

	public StonelingVariant getVariant() {
		return StonelingVariant.byIndex(entityData.get(VARIANT));
	}

	public float getItemAngle() {
		return entityData.get(HOLD_ANGLE);
	}

	public boolean isPlayerMade() {
		return isTame;
	}

	@Override
	public void readAdditionalSaveData(@Nonnull CompoundTag compound) {
		super.readAdditionalSaveData(compound);

		if(compound.contains(TAG_CARRYING_ITEM, 10)) {
			CompoundTag itemCmp = compound.getCompound(TAG_CARRYING_ITEM);
			ItemStack stack = ItemStack.of(itemCmp);
			entityData.set(CARRYING_ITEM, stack);
		}

		entityData.set(VARIANT, compound.getByte(TAG_VARIANT));
		entityData.set(HOLD_ANGLE, compound.getFloat(TAG_HOLD_ANGLE));
		entityData.set(HAS_LICHEN, compound.getBoolean(TAG_HAS_LICHEN));
		setPlayerMade(compound.getBoolean(TAG_PLAYER_MADE));
	}

	@Override
	public boolean hasLineOfSight(Entity entityIn) {
		Vec3 pos = position();
		Vec3 epos = entityIn.position();

		Vec3 origin = new Vec3(pos.x, pos.y + getEyeHeight(), pos.z);
		float otherEyes = entityIn.getEyeHeight();
		for (float height = 0; height <= otherEyes; height += otherEyes / 8) {
			if (this.level.clip(new ClipContext(origin, epos.add(0, height, 0), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this)).getType() == HitResult.Type.MISS)
				return true;
		}

		return false;
	}

	@Override
	public void addAdditionalSaveData(@Nonnull CompoundTag compound) {
		super.addAdditionalSaveData(compound);

		compound.put(TAG_CARRYING_ITEM, getCarryingItem().serializeNBT());

		compound.putByte(TAG_VARIANT, getVariant().getIndex());
		compound.putFloat(TAG_HOLD_ANGLE, getItemAngle());
		compound.putBoolean(TAG_PLAYER_MADE, isPlayerMade());
		compound.putBoolean(TAG_HAS_LICHEN, entityData.get(HAS_LICHEN));
	}

	public static boolean spawnPredicate(EntityType<? extends Stoneling> type, ServerLevelAccessor world, MobSpawnType reason, BlockPos pos, RandomSource rand) {
		return pos.getY() <= StonelingsModule.maxYLevel
				&& (MiscUtil.validSpawnLight(world, pos, rand) || world.getBiome(pos).is(GlimmeringWealdModule.BIOME_NAME))
				&& MiscUtil.validSpawnLocation(type, world, reason, pos);
	}

	@Override
	public boolean checkSpawnRules(@Nonnull LevelAccessor world, @Nonnull MobSpawnType reason) {
		BlockState state = world.getBlockState(new BlockPos(position()).below());
		if (state.getMaterial() != Material.STONE)
			return false;

		return StonelingsModule.dimensions.canSpawnHere(world) && super.checkSpawnRules(world, reason);
	}

	@Nullable
	@Override
	protected SoundEvent getHurtSound(@Nonnull DamageSource damageSourceIn) {
		return QuarkSounds.ENTITY_STONELING_CRY;
	}

	@Nullable
	@Override
	protected SoundEvent getDeathSound() {
		return QuarkSounds.ENTITY_STONELING_DIE;
	}

	@Override
	public int getAmbientSoundInterval() {
		return 1200;
	}

	@Override
	public void playAmbientSound() {
		SoundEvent sound = this.getAmbientSound();

		if (sound != null) this.playSound(sound, this.getSoundVolume(), 1f);
	}

	@Nullable
	@Override
	protected SoundEvent getAmbientSound() {
		if (hasCustomName()) {
			String customName = getName().getString();
			if (customName.equalsIgnoreCase("michael stevens") || customName.equalsIgnoreCase("vsauce"))
				return QuarkSounds.ENTITY_STONELING_MICHAEL;
		}

		return null;
	}

	@Nonnull
	@Override
	public Packet<?> getAddEntityPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}

	@Override
	public float getWalkTargetValue(@Nonnull BlockPos pos, LevelReader world) {
		return 0.5F - world.getRawBrightness(pos, 0);
	}

	public enum StonelingVariant implements SpawnGroupData {
		STONE("stone", Blocks.COBBLESTONE, Blocks.STONE),
		ANDESITE("andesite", Blocks.ANDESITE, Blocks.POLISHED_ANDESITE),
		DIORITE("diorite", Blocks.DIORITE, Blocks.POLISHED_DIORITE),
		GRANITE("granite", Blocks.GRANITE, Blocks.POLISHED_GRANITE),
		LIMESTONE("limestone", limestoneBlock, polishedBlocks.get(limestoneBlock)),
		CALCITE("calcite", Blocks.CALCITE),
		SHALE("shale", shaleBlock, polishedBlocks.get(shaleBlock)),
		JASPER("jasper", jasperBlock, polishedBlocks.get(jasperBlock)),
		DEEPSLATE("deepslate", Blocks.DEEPSLATE, Blocks.POLISHED_DEEPSLATE),
		TUFF("tuff", Blocks.TUFF, polishedBlocks.get(Blocks.TUFF)),
		DRIPSTONE("dripstone", Blocks.DRIPSTONE_BLOCK, polishedBlocks.get(Blocks.DRIPSTONE_BLOCK));

		private final ResourceLocation texture;
		private final List<Block> blocks;

		StonelingVariant(String variantPath, Block... blocks) {
			this.texture = new ResourceLocation(Quark.MOD_ID, "textures/model/entity/stoneling/" + variantPath + ".png");
			this.blocks = Lists.newArrayList(blocks);
		}

		public static StonelingVariant byIndex(byte index) {
			StonelingVariant[] values = values();
			return values[Mth.clamp(index, 0, values.length - 1)];
		}

		public byte getIndex() {
			return (byte) ordinal();
		}

		public ResourceLocation getTexture() {
			return texture;
		}

		public List<Block> getBlocks() {
			return blocks;
		}
	}

}
