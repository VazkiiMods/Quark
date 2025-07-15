package org.violetmoon.quark.base.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

/**
 * @author WireSegal
 *         Created at 2:08 PM on 8/24/19.
 */
public class ExclusionRecipe implements CraftingRecipe {
	public static final Serializer SERIALIZER = new Serializer();

	protected final CraftingRecipe parent;
	private final List<ResourceLocation> excluded;

	public ExclusionRecipe(CraftingRecipe parent, List<ResourceLocation> excluded) {
		this.parent = parent;
		this.excluded = excluded;
	}

	@Override
	public boolean matches(CraftingInput input, Level level) {
		for(ResourceLocation recipeLoc : excluded) {
			Optional<RecipeHolder<?>> recipeHolder = level.getRecipeManager().byKey(recipeLoc);
			if(recipeHolder.isPresent()) {
				Recipe<?> recipe = recipeHolder.get().value();
				if (recipe instanceof CraftingRecipe craftingRecipe && craftingRecipe.matches(input, level)) {
					return false;
				}
			}
		}
		return parent.matches(input, level);
	}

	@Override
	public ItemStack assemble(CraftingInput input, HolderLookup.Provider provider) {
		return parent.assemble(input, provider);
	}

	@Override
	public boolean canCraftInDimensions(int width, int height) {
		return parent.canCraftInDimensions(width, height);
	}

	@Override
	public ItemStack getResultItem(HolderLookup.Provider provider) {
		return parent.getResultItem(provider);
	}

	@NotNull
	@Override
	public RecipeSerializer<?> getSerializer() {
		return SERIALIZER;
	}

	@NotNull
	@Override
	public RecipeType<?> getType() {
		return parent.getType();
	}

	@Override
	public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
		return parent.getRemainingItems(input);
	}

	@NotNull
	@Override
	public NonNullList<Ingredient> getIngredients() {
		return parent.getIngredients();
	}

	@Override
	public boolean isSpecial() {
		return parent.isSpecial();
	}

	@NotNull
	@Override
	public String getGroup() {
		return parent.getGroup();
	}

	@NotNull
	@Override
	public ItemStack getToastSymbol() {
		return parent.getToastSymbol();
	}

	@Override
	public CraftingBookCategory category() {
		return parent.category();
	}

	private static class ShapedExclusionRecipe extends ExclusionRecipe implements CraftingRecipe {
		private final ShapedRecipe shapedParent;

		public ShapedExclusionRecipe(CraftingRecipe shapedParent, List<ResourceLocation> excluded) {
			super(shapedParent, excluded);
			this.shapedParent = (ShapedRecipe)shapedParent;
		}
	}

	public static class Serializer implements RecipeSerializer<ExclusionRecipe> {
		public static final MapCodec<ExclusionRecipe> CODEC = RecordCodecBuilder.mapCodec(
				instance -> instance.group(
								ResourceLocation.CODEC.listOf().fieldOf("excluded").forGetter((exclusionRecipe -> exclusionRecipe.excluded)),
								Recipe.CODEC.fieldOf("parent").forGetter(exclusionRecipe -> exclusionRecipe.parent))
						.apply(instance, ((excluded, parent) -> new ExclusionRecipe((CraftingRecipe) parent, excluded)))
		);

        public static final StreamCodec<RegistryFriendlyByteBuf, ExclusionRecipe> STREAM_CODEC = new StreamCodec<>() {
			@Override
			public @NotNull ExclusionRecipe decode(@NotNull RegistryFriendlyByteBuf buf) {
				Recipe<?> parent = Recipe.STREAM_CODEC.decode(buf);
				List<ResourceLocation> output = buf.readList(ResourceLocation.STREAM_CODEC);
				return new ExclusionRecipe((CraftingRecipe) parent, output);
			}

			@Override
			public void encode(@NotNull RegistryFriendlyByteBuf buf, ExclusionRecipe recipe) {
				Recipe.STREAM_CODEC.encode(buf, recipe.parent);
				buf.writeCollection(recipe.excluded, ResourceLocation.STREAM_CODEC);
			}
		};

		@Override
		public MapCodec<ExclusionRecipe> codec() {
			return CODEC;
		}

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, ExclusionRecipe> streamCodec() {
			return STREAM_CODEC;
		}
	}
}
