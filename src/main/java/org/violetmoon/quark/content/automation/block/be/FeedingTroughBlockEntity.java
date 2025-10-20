package org.violetmoon.quark.content.automation.block.be;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DispenserMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayer;
import org.jetbrains.annotations.NotNull;
import org.violetmoon.quark.content.automation.block.FeedingTroughBlock;
import org.violetmoon.quark.content.automation.module.FeedingTroughModule;
import org.violetmoon.zeta.util.MiscUtil;

import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

/**
 * @author WireSegal
 *         Created at 9:39 AM on 9/20/19.
 */
public class FeedingTroughBlockEntity extends RandomizableContainerBlockEntity {

	private NonNullList<ItemStack> stacks;

	private long internalRng = 0;

	public FeedingTroughBlockEntity(BlockPos pos, BlockState state) {
		super(FeedingTroughModule.blockEntityType, pos, state);
		this.stacks = NonNullList.withSize(9, ItemStack.EMPTY);
	}

	public void updateFoodHolder(Animal mob, Predicate<ItemStack> temptations, FakePlayer foodHolder) {
		for(int i = 0; i < getContainerSize(); i++) {
			ItemStack stack = getItem(i);
			if(temptations.test(stack) && mob.isFood(stack)) {
				Inventory inventory = foodHolder.getInventory();
				inventory.items.set(inventory.selected, stack);
				Vec3 througPos = new Vec3(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ())
						.add(0.5, -1, 0.5);
				Vec3 mobPosition = mob.position();
				Vec3 direction = mobPosition.subtract(througPos);
				// Yes, this is lossy, however; it runs so frequently that is losses are fine
				// This ends up getting expensive quickly if we use the non-lossy version
				Vec2 angles = MiscUtil.getMinecraftAnglesLossy(direction);

				Vec3 newPos = Vec3.ZERO;
				// Fake player will always be at most maxDist blocks away from animal.
				// If animal is closer to target, then we will be on target itself.
				float maxDist = 5;
				if(direction.lengthSqr() > (maxDist*maxDist)){
					newPos = mobPosition.add(direction.normalize().scale(-maxDist));
				}else{
					//place slightly behind trough
					newPos = througPos.add(direction.normalize().scale(-1));
				}

				foodHolder.moveTo(newPos.x, newPos.y, newPos.z, angles.y, angles.x);
				return;
			}
		}
	}

	public enum FeedResult{
		FED,SECS,NONE
	}
	public FeedResult tryFeedingAnimal(Animal animal) {
		for(int i = 0; i < this.getContainerSize(); i++) {
			ItemStack stack = this.getItem(i);
			if(animal.isFood(stack)) {
				SoundEvent soundEvent = animal.getEatingSound(stack);
				if (soundEvent != null) { // Null check is kinda required, don't remove :) (why tho, intellij says its never null)
					animal.playSound(soundEvent, 0.5F + 0.5F * level.random.nextInt(2), (level.random.nextFloat() - level.random.nextFloat()) * 0.2F + 1.0F);
				}

				this.addItemParticles(animal, stack, 16);

				stack.shrink(1);
				this.setChanged();

				if(this.getSpecialRand().nextDouble() < FeedingTroughModule.loveChance) {
					List<Animal> animalsAround = level.getEntitiesOfClass(Animal.class, new AABB(this.worldPosition).inflate(FeedingTroughModule.range));
					if(animalsAround.size() <= FeedingTroughModule.maxAnimals)
						animal.setInLove(null);
					return FeedResult.SECS;
				}

				return FeedResult.FED;
			}
		}
		return FeedResult.NONE;
	}

	@Override
	public void setChanged() {
		super.setChanged();
		BlockState state = getBlockState();
		if(level != null && state.getBlock() instanceof FeedingTroughBlock) {
			boolean full = state.getValue(FeedingTroughBlock.FULL);
			boolean shouldBeFull = !isEmpty();

			if(full != shouldBeFull)
				level.setBlock(worldPosition, state.setValue(FeedingTroughBlock.FULL, shouldBeFull), 2);
		}
	}

	private void addItemParticles(Entity entity, ItemStack stack, int count) {
		for(int i = 0; i < count; ++i) {
			Vec3 direction = new Vec3((entity.level().random.nextFloat() - 0.5D) * 0.1D, Math.random() * 0.1D + 0.1D, 0.0D);
			direction = direction.xRot(-entity.getXRot() * ((float) Math.PI / 180F));
			direction = direction.yRot(-entity.getYRot() * ((float) Math.PI / 180F));
			double yVelocity = (-entity.level().random.nextFloat()) * 0.6D - 0.3D;
			Vec3 position = new Vec3((entity.level().random.nextFloat() - 0.5D) * 0.3D, yVelocity, 0.6D);
			Vec3 entityPos = entity.position();
			position = position.xRot(-entity.getXRot() * ((float) Math.PI / 180F));
			position = position.yRot(-entity.getYRot() * ((float) Math.PI / 180F));
			position = position.add(entityPos.x, entityPos.y + entity.getEyeHeight(), entityPos.z);
			if(this.level instanceof ServerLevel serverLevel)
				serverLevel.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, stack), position.x, position.y, position.z, 1, direction.x, direction.y + 0.05D, direction.z, 0.0D);
			else if(this.level != null)
				this.level.addParticle(new ItemParticleOption(ParticleTypes.ITEM, stack), position.x, position.y, position.z, direction.x, direction.y + 0.05D, direction.z);
		}
	}

	private Random getSpecialRand() {
		Random specialRand = new Random(internalRng);
		internalRng = specialRand.nextLong();
		return specialRand;
	}

	@Override
	public int getContainerSize() {
		return 9;
	}

	@Override
	public boolean isEmpty() {
		for(int i = 0; i < getContainerSize(); i++) {
			ItemStack stack = getItem(i);
			if(!stack.isEmpty())
				return false;
		}

		return true;
	}

	@Override
	@NotNull
	protected Component getDefaultName() {
		return Component.translatable("quark.container.feeding_trough");
	}

	@Override
	protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
		super.loadAdditional(tag, provider);
		this.internalRng = tag.getLong("rng");
		this.stacks = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
		if(!this.tryLoadLootTable(tag))
			ContainerHelper.loadAllItems(tag, this.stacks, provider);
	}

	@Override
	protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
		super.saveAdditional(tag, provider);
		tag.putLong("rng", internalRng);
		if(!this.trySaveLootTable(tag))
			ContainerHelper.saveAllItems(tag, this.stacks, provider);
	}

	@Override
	@NotNull
	protected NonNullList<ItemStack> getItems() {
		return this.stacks;
	}

	@Override
	protected void setItems(@NotNull NonNullList<ItemStack> items) {
		this.stacks = items;
	}

	@Override
	@NotNull
	protected AbstractContainerMenu createMenu(int id, @NotNull Inventory playerInventory) {
		return new DispenserMenu(id, playerInventory, this){
			@Override
			public MenuType<?> getType() {
				return FeedingTroughModule.menuType;
			}
		};
	}
}
