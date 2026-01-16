package org.violetmoon.quark.content.mobs.item;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import org.jetbrains.annotations.NotNull;
import org.violetmoon.quark.base.Quark;
import org.violetmoon.zeta.item.IZetaItem;
import org.violetmoon.zeta.item.ZetaArmorItem;
import org.violetmoon.zeta.module.ZetaModule;
import org.violetmoon.zeta.registry.CreativeTabManager;

import java.util.function.BooleanSupplier;

public class ForgottenHatItem extends ZetaArmorItem implements IZetaItem {

	private static final ResourceLocation TEXTURE = Quark.asResource("textures/misc/forgotten_hat_worn.png");

	private final ZetaModule module;

	public ForgottenHatItem(ZetaModule module) {
		super(ArmorMaterials.LEATHER, Type.HELMET,
				new Item.Properties()
						.stacksTo(1)
						.durability(55)
						.rarity(Rarity.RARE)
						.attributes(createAttributes()));

		Quark.ZETA.registry.registerItem(this, "forgotten_hat");
		this.module = module;
		CreativeTabManager.addNextToItem(CreativeModeTabs.TOOLS_AND_UTILITIES, this, Items.SADDLE, true);
	}
	
    @Override
    public void setDamage(ItemStack stack, int damage){
        super.setDamage(stack, 0);
    }

	@Override
	public ZetaModule getModule() {
		return module;
	}

	@Override
	public IZetaItem setCondition(BooleanSupplier condition) {
		return this;
	}

	@Override
	public boolean doesConditionApply() {
		return true;
	}

	@Override
	public boolean canEquipZeta(ItemStack stack, EquipmentSlot armorType, LivingEntity entity) {
		return armorType == EquipmentSlot.HEAD;
	}

	// TODO: Reimpl getArmorTextureZeta
	@Override
	public ResourceLocation getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, ArmorMaterial.Layer layer, boolean innerModel) {
		return TEXTURE;
	}

	@Override
	public boolean isEnchantable(@NotNull ItemStack stack) {
		return false;
	}

	public static ItemAttributeModifiers createAttributes() {
		ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
		builder.add(Attributes.ARMOR, new AttributeModifier(Quark.asResource("forgotten_hat_armor"), 1, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.HEAD);
		builder.add(Attributes.LUCK, new AttributeModifier(Quark.asResource("forgotten_hat_luck"), 1, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.HEAD);
		builder.add(Attributes.ENTITY_INTERACTION_RANGE, new AttributeModifier(Quark.asResource("forgotten_hat_entity_interaction_range"),  2, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.HEAD);
		builder.add(Attributes.BLOCK_INTERACTION_RANGE, new AttributeModifier(Quark.asResource("forgotten_hat_block_interaction_range"), 2, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.HEAD);
		return builder.build();
	}
}
