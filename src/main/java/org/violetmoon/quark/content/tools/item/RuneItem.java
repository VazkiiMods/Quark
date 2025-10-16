package org.violetmoon.quark.content.tools.item;

import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;
import org.violetmoon.quark.api.IRuneColorProvider;
import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.content.tools.base.RuneColor;
import org.violetmoon.zeta.item.ZetaSmithingTemplateItem;
import org.violetmoon.zeta.module.ZetaModule;
import org.violetmoon.zeta.registry.CreativeTabManager;

import java.util.List;

/**
 * @author WireSegal
 *         Created at 2:27 PM on 8/17/19.
 */
public class RuneItem extends ZetaSmithingTemplateItem implements IRuneColorProvider {

	private static final Component RUNE_APPLIES_TO = Component.translatable(Util.makeDescriptionId("item", Quark.asResource("smithing_template.rune.applies_to"))).withStyle(Z_DESCRIPTION_FORMAT);
	private static final Component RUNE_INGREDIENTS = Component.translatable(Util.makeDescriptionId("item", Quark.asResource("smithing_template.rune.ingredients"))).withStyle(Z_DESCRIPTION_FORMAT);
	private static final Component RUNE_UPGRADE = Component.translatable(Util.makeDescriptionId("upgrade", Quark.asResource("rune_upgrade"))).withStyle(Z_TITLE_FORMAT);
	private static final Component RUNE_BASE_SLOT_DESCRIPTION = Component.translatable(Util.makeDescriptionId("item", Quark.asResource("smithing_template.rune.base_slot_description")));
	private static final Component RUNE_ADDITIONS_SLOT_DESCRIPTION = Component.translatable(Util.makeDescriptionId("item", Quark.asResource("smithing_template.rune.additions_slot_description")));

	private static final ResourceLocation EMPTY_SLOT_BLAZE_POWDER = Quark.asResource("item/empty_slot_blaze_powder");
	private static final ResourceLocation EMPTY_SLOT_DYE = Quark.asResource("item/empty_slot_dye");
	private static final ResourceLocation EMPTY_SLOT_NOTHING = Quark.asResource("item/empty_slot_nothing");

	public RuneItem(String regname, ZetaModule module) {
		super(regname, module, RUNE_APPLIES_TO, RUNE_INGREDIENTS, RUNE_UPGRADE, RUNE_BASE_SLOT_DESCRIPTION, RUNE_ADDITIONS_SLOT_DESCRIPTION, anyToolIconList(),
			List.of(EMPTY_SLOT_BLAZE_POWDER, EMPTY_SLOT_DYE, EMPTY_SLOT_NOTHING));

		CreativeTabManager.addNextToItem(CreativeModeTabs.INGREDIENTS, this, Items.SPIRE_ARMOR_TRIM_SMITHING_TEMPLATE, false);
	}

	@Override
	public boolean isFoil(@NotNull ItemStack stack) {
		return true;
	}

	@Override
	public RuneColor getRuneColor(ItemStack stack) {
		return RuneColor.RAINBOW;
	}
}
