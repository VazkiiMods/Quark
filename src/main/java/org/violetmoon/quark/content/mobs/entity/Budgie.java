package org.violetmoon.quark.content.mobs.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.ShoulderRidingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.violetmoon.quark.content.mobs.module.BudgiesModule;

public class Budgie extends ShoulderRidingEntity {

    public static final int COLORS = 4;
    private Ingredient temptationItems;

    public Budgie(EntityType<? extends Budgie> type, Level worldIn) {
        super(type, worldIn);
        this.moveControl = new FlyingMoveControl(this, 10, false);
        this.setPathfindingMalus(PathType.DANGER_FIRE, -1.0F);
        this.setPathfindingMalus(PathType.DAMAGE_FIRE, -1.0F);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.4D));
        this.goalSelector.addGoal(2, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(3, new BreedGoal(this, 1.0D));
        this.goalSelector.addGoal(4, new TemptGoal(this, 1.2D, getTemptationItems(), false));
        this.goalSelector.addGoal(5, new FollowOwnerGoal(this, 1.0D, 5.0F, 1.0F));
        this.goalSelector.addGoal(6, new GroomFlowerGoal(this));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomFlyingGoal(this, 1.0D));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));
    }

    public static AttributeSupplier.Builder prepareAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 6.0D)
                .add(Attributes.FLYING_SPEED, 0.4D)
                .add(Attributes.MOVEMENT_SPEED, 0.2D);
    }

    @NotNull
    @Override
    protected PathNavigation createNavigation(@NotNull Level level) {
        FlyingPathNavigation flyingpathnavigation = new FlyingPathNavigation(this, level);
        flyingpathnavigation.setCanOpenDoors(false);
        flyingpathnavigation.setCanFloat(true);
        flyingpathnavigation.setCanPassDoors(true);
        return flyingpathnavigation;
    }

    @Override
    public boolean isFlying() {
        return !this.onGround();
    }

    @Override
    public boolean causeFallDamage(float distance, float damageMultiplier, @NotNull net.minecraft.world.damagesource.DamageSource source) {
        return false;
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, @NotNull BlockState state, @NotNull BlockPos pos) {
    }
  
    @Override
    public boolean isFood(ItemStack stack) {
        return getTemptationItems().test(stack);
    }

    private Ingredient getTemptationItems() {
        if (temptationItems == null)
            temptationItems = Ingredient.of(Items.WHEAT_SEEDS, Items.PUMPKIN_SEEDS, Items.MELON_SEEDS);
        return temptationItems;
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(@NotNull ServerLevel sworld, @NotNull AgeableMob other) {
        return new Budgie(BudgiesModule.budgieType, sworld);
    }

    @Override
    public void setRecordPlayingNearby(BlockPos pos, boolean isPartying) {
        }

    public Vec3 getRenderOffset(float partialTicks) {
        return new Vec3(0.4D, -0.2D, 0.0D);
        }
    
    public class GroomFlowerGoal extends MoveToBlockGoal {
        public GroomFlowerGoal(Budgie budgie) {
            super(budgie, 1.2D, 12);
        }

        @Override
        protected boolean isValidTarget(LevelReader world, @NotNull BlockPos pos) {
            return world.getBlockState(pos).is(net.minecraft.tags.BlockTags.SMALL_FLOWERS);
        }

        @Override
        public void tick() {
            super.tick();
            if (isReachedTarget()) {
                if (mob.getRandom().nextInt(BudgiesModule.flowerGroomingChance) == 0) {
                    mob.level().levelEvent(2005, blockPos, 0); 
                    this.stop();
                }
            }
        }
    }
}
