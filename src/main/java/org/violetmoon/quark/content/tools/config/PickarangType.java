package org.violetmoon.quark.content.tools.config;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.content.tools.entity.rang.AbstractPickarang;
import org.violetmoon.zeta.config.Config;
import org.violetmoon.zeta.config.type.IConfigType;
import org.violetmoon.zeta.item.ext.IZetaItemExtensions;

public class PickarangType<T extends AbstractPickarang<T>> implements IConfigType {

	public final Item repairMaterial;
	public final Item pickaxeEquivalent;
	public final IZetaItemExtensions pickaxeEquivalentExt;

	@Config(description = "How long it takes before the Pickarang starts returning to the player if it doesn't hit anything.")
	public int timeout;

	@Config(description = "Pickarang harvest level. 2 is Iron, 3 is Diamond, 4 is Netherite.")
	public int harvestLevel;

	@Config(description = "Pickarang durability. Set to -1 to have the Pickarang be unbreakable.")
	public int durability;

	@Config(description = "Pickarang max hardness breakable. 22.5 is ender chests, 25.0 is monster boxes, 50 is obsidian. Most things are below 5.")
	public double maxHardness;

	@Config(description = "How much damage the Pickarang deals when swung as an item")
	public int attackDamage;

	@Config(description = "How many ticks do you have to wait between using the pickarang again")
	public int cooldown;

	@Config(description = "Whether this pickarang type can act as a hoe.")
	public boolean canActAsHoe = false;

	@Config(description = "Whether this pickarang type can act as a shovel.")
	public boolean canActAsShovel = true;

	@Config(description = "Whether this pickarang type can act as an axe.")
	public boolean canActAsAxe = true;

	private EntityType<T> entityType;
	private PickarangConstructor<T> pickarangConstructor;

	public PickarangType(Item repairMaterial, Item pickaxeEquivalent, int timeout, int harvestLevel, int durability, double maxHardness, int attackDamage, int cooldown) {
		this.repairMaterial = repairMaterial;
		this.pickaxeEquivalent = pickaxeEquivalent;
		this.pickaxeEquivalentExt = Quark.ZETA.itemExtensions.get(new ItemStack(pickaxeEquivalent));

		this.timeout = timeout;
		this.harvestLevel = harvestLevel;
		this.durability = durability;
		this.maxHardness = maxHardness;
		this.attackDamage = attackDamage;
		this.cooldown = cooldown;
	}

	public PickarangType<T> canActAsHoe(boolean hoe) {
		this.canActAsHoe = hoe;
		return this;
	}

	public PickarangType<T> canActAsAxe(boolean axe) {
		this.canActAsAxe = axe;
		return this;
	}

	public PickarangType<T> canActAsShovel(boolean shovel) {
		this.canActAsShovel = shovel;
		return this;
	}

	public boolean isFireResistant() {
		return pickaxeEquivalent != null && pickaxeEquivalent.components().has(DataComponents.FIRE_RESISTANT);
	}

	public EntityType<T> getEntityType() {
		return entityType;
	}

	public void setEntityType(EntityType<T> entityType, PickarangConstructor<T> cons) {
		this.entityType = entityType;
		this.pickarangConstructor = cons;
	}

	public AbstractPickarang<T> makePickarang(Level level, Player thrower) {
		return pickarangConstructor.makePickarang(entityType, level, thrower);
	}

	public interface PickarangConstructor<T extends AbstractPickarang<T>> {
		T makePickarang(EntityType<T> entityType, Level level, Player thrower);
	}

}
