package org.violetmoon.quark.addons.oddities.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;
import org.violetmoon.quark.addons.oddities.item.BackpackItem;
import org.violetmoon.quark.addons.oddities.module.TotemOfHoldingModule;

import javax.annotation.Nonnull;
import java.util.LinkedList;
import java.util.List;

/**
 * @author WireSegal
 * Created at 1:34 PM on 3/30/20.
 */
public class TotemOfHoldingEntity extends Entity {
	private static final String TAG_ITEMS = "storedItems";
	private static final String TAG_DYING = "dying";
	private static final String TAG_OWNER = "owner";

	private static final EntityDataAccessor<Boolean> DYING = SynchedEntityData.defineId(TotemOfHoldingEntity.class, EntityDataSerializers.BOOLEAN);

	public static final int DEATH_TIME = 40;

	private int deathTicks = 0;
	private String owner;
	private List<ItemStack> storedItems = new LinkedList<>();

	public TotemOfHoldingEntity(EntityType<? extends TotemOfHoldingEntity> entityType, Level worldIn) {
		super(entityType, worldIn);
	}

	@Override
	protected void defineSynchedData() {
		entityData.define(DYING, false);
	}

	public void addItem(ItemStack stack) {
		storedItems.add(stack);
	}

	public void setOwner(Player player) {
		owner = player.getUUID().toString();
	}

	private Player getOwnerEntity() {
		for(Player player : level.players()) {
			String uuid = player.getUUID().toString();
			if(uuid.equals(owner))
				return player;
		}

		return null;
	}

	@Override
	public boolean skipAttackInteraction(@Nonnull Entity e) {
		if(!level.isClientSide && e instanceof Player player) {

			if(!TotemOfHoldingModule.allowAnyoneToCollect && !player.getAbilities().instabuild) {
				Player owner = getOwnerEntity();
				if(e != owner)
					return false;
			}

			int drops = Math.min(storedItems.size(), 3 + level.random.nextInt(4));

			for(int i = 0; i < drops; i++) {
				ItemStack stack = storedItems.remove(0);

				if(stack.getItem() instanceof ArmorItem armor) {
					EquipmentSlot slot = armor.getSlot();
					ItemStack curr = player.getItemBySlot(slot);

					if(curr.isEmpty()) {
						player.setItemSlot(slot, stack);
						stack = null;
					} else if(!(curr.getItem() instanceof BackpackItem) &&
							!EnchantmentHelper.hasBindingCurse(curr) && !EnchantmentHelper.hasBindingCurse(stack)) {
						player.setItemSlot(slot, stack);
						stack = curr;
					}
				} else if(stack.getItem() instanceof ShieldItem) {
					ItemStack curr = player.getItemBySlot(EquipmentSlot.OFFHAND);

					if(curr.isEmpty()) {
						player.setItemSlot(EquipmentSlot.OFFHAND, stack);
						stack = null;
					}
				}

				if(stack != null)
					if(!player.addItem(stack))
						spawnAtLocation(stack, 0);
			}

			if(level instanceof ServerLevel serverLevel) {
				serverLevel.sendParticles(ParticleTypes.DAMAGE_INDICATOR, getX(), getY() + 0.5, getZ(), drops, 0.1, 0.5, 0.1, 0);
				serverLevel.sendParticles(ParticleTypes.ENCHANTED_HIT, getX(), getY() + 0.5, getZ(), drops, 0.4, 0.5, 0.4, 0);
			}
		}

		return false;
	}

	@Override
	public boolean isPickable() {
		return true;
	}

	@Override
	public void tick() {
		super.tick();

		if(!isAlive())
			return;

		if(TotemOfHoldingModule.darkSoulsMode) {
			Player owner = getOwnerEntity();
			if(owner != null && !level.isClientSide) {
				String ownerTotem = TotemOfHoldingModule.getTotemUUID(owner);
				if(!getUUID().toString().equals(ownerTotem))
					dropEverythingAndDie();
			}
		}

		if(storedItems.isEmpty() && !level.isClientSide)
			entityData.set(DYING, true);

		if(isDying()) {
			if(deathTicks > DEATH_TIME)
				discard();
			else deathTicks++;
		}

		else if(level.isClientSide)
			level.addParticle(ParticleTypes.PORTAL, getX(), getY() + (Math.random() - 0.5) * 0.2, getZ(), Math.random() - 0.5, Math.random() - 0.5, Math.random() - 0.5);
	}

	private void dropEverythingAndDie() {
		if(!TotemOfHoldingModule.destroyLostItems)
			for (ItemStack storedItem : storedItems)
				spawnAtLocation(storedItem, 0);

		storedItems.clear();

		discard();
	}

	public int getDeathTicks() {
		return deathTicks;
	}

	public boolean isDying() {
		return entityData.get(DYING);
	}

	@Override
	public void readAdditionalSaveData(@Nonnull CompoundTag compound) {
		ListTag list = compound.getList(TAG_ITEMS, 10);
		storedItems = new LinkedList<>();

		for(int i = 0; i < list.size(); i++) {
			CompoundTag cmp = list.getCompound(i);
			ItemStack stack = ItemStack.of(cmp);
			storedItems.add(stack);
		}

		boolean dying = compound.getBoolean(TAG_DYING);
		entityData.set(DYING, dying);

		owner = compound.getString(TAG_OWNER);
	}

	@Override
	protected void addAdditionalSaveData(@Nonnull CompoundTag compound) {
		ListTag list = new ListTag();
		for(ItemStack stack : storedItems) {
			list.add(stack.serializeNBT());
		}

		compound.put(TAG_ITEMS, list);
		compound.putBoolean(TAG_DYING, isDying());
		if (owner != null)
			compound.putString(TAG_OWNER, owner);
	}

	@Nonnull
	@Override
	public Packet<?> getAddEntityPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}
}
