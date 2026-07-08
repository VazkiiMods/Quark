package org.violetmoon.quark.integration.jei;

import mezz.jei.api.recipe.category.extensions.IRecipeCategoryExtension;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AnvilScreen;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.Block;
import org.violetmoon.quark.addons.oddities.util.Influence;
import org.violetmoon.quark.addons.oddities.util.InfluenceLocations;
import org.violetmoon.quark.base.components.QuarkDataComponents;
import org.violetmoon.quark.content.tools.base.RuneColor;
import org.violetmoon.quark.content.tools.module.ColorRunesModule;

import java.util.List;
import java.util.Optional;

public class InfluenceEntry implements IRecipeCategoryExtension {

	private final ItemStack candleStack;
	private final InfluenceLocations influenceLocations;
	private Optional<ItemStack> boost;
	private Optional<ItemStack> dampen;
	private Optional<List<ItemStack>> associatedBooks;

	public InfluenceEntry(Block candle, InfluenceLocations influenceLocations) {
		this.candleStack = new ItemStack(candle);
		this.influenceLocations = influenceLocations;
		this.boost = Optional.empty();
		this.dampen = Optional.empty();
		this.associatedBooks = Optional.empty();
	}

	public ItemStack getBoostBook() {
		initializeBooks();
		return this.boost.get();
	}

	public ItemStack getDampenBook() {
		initializeBooks();
		return this.dampen.get();
	}

	public ItemStack getCandleStack() {
		return this.candleStack;
	}

	public List<ItemStack> getAssociatedBooks() {
		initializeBooks();
		return this.associatedBooks.get();
	}

	private void initializeBooks() {
		if (this.boost.isEmpty() || this.dampen.isEmpty() || this.associatedBooks.isEmpty()) {
			Level level = Minecraft.getInstance().level;
			if (level != null) {
				Influence influence = this.influenceLocations.toInfluence(level);
				this.boost = Optional.of(getEnchantedBook(influence.boost(), RuneColor.GREEN, ChatFormatting.GREEN, "quark.jei.boost_influence"));
				this.dampen = Optional.of(getEnchantedBook(influence.dampen(), RuneColor.RED, ChatFormatting.RED, "quark.jei.dampen_influence"));
				this.associatedBooks = Optional.of(buildAssociatedBooks(influence));
			}
		}
	}

	private static ItemStack getEnchantedBook(List<Holder<Enchantment>> enchantments, RuneColor runeColor, ChatFormatting chatColor, String locKey) {
		ItemStack stack = ItemStack.EMPTY;

		for(Holder<Enchantment> enchantment : enchantments) {
			if(enchantment.value() != null) {
				if(stack.isEmpty()) {
					stack = EnchantedBookItem.createForEnchantment(new EnchantmentInstance(enchantment, enchantment.value().getMaxLevel()));
					stack = ColorRunesModule.withRune(stack, runeColor);
					stack.set(DataComponents.CUSTOM_NAME, Component.translatable(locKey).withStyle(chatColor));
					stack.set(QuarkDataComponents.TABLE_ONLY_ENCHANTS, true);
				}
				else {
					stack.enchant(enchantment, enchantment.value().getMaxLevel());
				}
			}
		}

		return stack;
	}

	private static List<ItemStack> buildAssociatedBooks(Influence influence) {
		NonNullList<ItemStack> books = NonNullList.create();
		for(Holder<Enchantment> boostedEnchants : influence.boost()) {
            if (boostedEnchants.value() == null) continue;
			for(int i = 0; i < boostedEnchants.value().getMaxLevel(); i++) {
				books.add(EnchantedBookItem.createForEnchantment(new EnchantmentInstance(boostedEnchants, i + 1)));
			}
		}

		for(Holder<Enchantment> dampenedEnchants : influence.dampen()) {
            if (dampenedEnchants.value() == null) continue;
            for(int i = 0; i < dampenedEnchants.value().getMaxLevel(); i++) {
				books.add(EnchantedBookItem.createForEnchantment(new EnchantmentInstance(dampenedEnchants, i + 1)));
			}
		}

		return books;
	}

	public boolean hasAny() {
		return !influenceLocations.boost().isEmpty() || !influenceLocations.dampen().isEmpty();
	}
}
