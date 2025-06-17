package org.violetmoon.quark.content.tools.recipe;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.item.crafting.SmithingTrimRecipe;
import net.minecraft.world.level.Level;
import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.content.client.module.ImprovedTooltipsModule;
import org.violetmoon.quark.content.client.tooltip.EnchantedBookTooltips;
import org.violetmoon.quark.content.tools.base.RuneColor;
import org.violetmoon.quark.content.tools.module.ColorRunesModule;
import org.violetmoon.quark.content.tools.module.PickarangModule;
import org.violetmoon.zeta.module.IDisableable;

import javax.annotation.Nonnull;
import java.util.stream.Stream;

/**
 * @author WireSegal
 * Created at 9:54 AM on 12/23/23.
 */
public final class SmithingRuneRecipe extends SmithingTrimRecipe { // Extends to allow JEI to pick it up

	public static final Serializer SERIALIZER = new Serializer();
	private final Ingredient template;
	private final Ingredient addition;
	private final RuneColor runeColor;
	private static Ingredient used;

	private static ItemStack makeEnchantedDisplayItem(ItemStack input) {
		ItemStack stack = input.copy();
		stack.set(DataComponents.CUSTOM_NAME, Component.translatable("quark.jei.any_enchanted"));
		stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
		return stack;
	}

	private static Ingredient createBaseIngredient() {
		if (used == null) {
			Stream<ItemStack> displayItems;
			if (Quark.ZETA.modules.isEnabled(ImprovedTooltipsModule.class) && ImprovedTooltipsModule.enchantingTooltips) {
				displayItems = EnchantedBookTooltips.getTestItems().stream();
			} else {
				displayItems = Stream.of(Items.DIAMOND_SWORD, Items.DIAMOND_PICKAXE, Items.DIAMOND_AXE,
					Items.DIAMOND_SHOVEL, Items.DIAMOND_HOE, Items.DIAMOND_HELMET, Items.DIAMOND_CHESTPLATE,
					Items.DIAMOND_LEGGINGS, Items.DIAMOND_BOOTS, Items.ELYTRA, Items.SHIELD, Items.BOW, Items.CROSSBOW,
					Items.TRIDENT, Items.FISHING_ROD, Items.SHEARS, PickarangModule.pickarang).map(ItemStack::new);
			}

			used = Ingredient.of(displayItems
				.filter(it -> !(it.getItem() instanceof IDisableable<?> dis) || dis.isEnabled())
				.map(SmithingRuneRecipe::makeEnchantedDisplayItem));
		}

		return used;
	}

	private SmithingRuneRecipe(Ingredient template, Ingredient addition, RuneColor runeColor) {
		super(template, createBaseIngredient(), addition);
		this.template = template;
		this.addition = addition;
		this.runeColor = runeColor;
	}

	@Override
	public boolean matches(SmithingRecipeInput input, Level level) {
		return isTemplateIngredient(input.getItem(0)) && isBaseIngredient(input.getItem(1)) && isAdditionIngredient(input.getItem(2));
	}

	@Override
	public ItemStack assemble(SmithingRecipeInput input, HolderLookup.Provider provider) {
		ItemStack baseItem = input.getItem(1);
		if (isBaseIngredient(baseItem)) {
			if (ColorRunesModule.getStackColor(baseItem) == runeColor)
				return ItemStack.EMPTY;

			ItemStack newStack = baseItem.copy();
			newStack.setCount(1);
			return ColorRunesModule.withRune(newStack, runeColor);
		}
		return ItemStack.EMPTY;
	}

	@Nonnull
	@Override
	public ItemStack getResultItem(@Nonnull HolderLookup.Provider provider) {
		ItemStack displayStack = makeEnchantedDisplayItem(new ItemStack(Items.IRON_CHESTPLATE));
		ColorRunesModule.withRune(displayStack, runeColor);
		return displayStack;
	}

	@Override
	public boolean isTemplateIngredient(@Nonnull ItemStack stack) {
		return this.template.test(stack);
	}

	@Override
	public boolean isBaseIngredient(@Nonnull ItemStack stack) {
		return ColorRunesModule.canHaveRune(stack);
	}

	@Override
	public boolean isAdditionIngredient(@Nonnull ItemStack stack) {
		return this.addition.isEmpty() ? stack.isEmpty() : this.addition.test(stack);
	}

	@Nonnull
	@Override
	public RecipeSerializer<?> getSerializer() {
		return RecipeSerializer.SMITHING_TRIM;
	}
}
