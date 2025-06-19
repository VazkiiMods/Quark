package org.violetmoon.quark.base.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
								ResourceLocation.CODEC.listOf().fieldOf("excluded").forGetter((exclusionRecipe -> exclusionRecipe.excluded))
								//Codec.STRING.optionalFieldOf("parent", "").forGetter(exclusionRecipe -> exclusionRecipe.parent)
								)
						.apply(instance, ExclusionRecipe::new)
		);

		@NotNull
		@Override
		public ExclusionRecipe fromJson(@NotNull ResourceLocation recipeId, @NotNull JsonObject json) {
			String trueType = GsonHelper.getAsString(json, "true_type");
			if(trueType.equals("quark:exclusion"))
				throw new JsonSyntaxException("Recipe type circularity");

			JsonArray excluded = GsonHelper.getAsJsonArray(json, "exclusions");
			List<ResourceLocation> excludedRecipes = new ArrayList<>();
			for(JsonElement el : excluded) {
				ResourceLocation loc = ResourceLocation.parse(el.getAsString());
				if(!loc.equals(recipeId))
					excludedRecipes.add(loc);
			}

			RecipeSerializer<?> serializer = BuiltInRegistries.RECIPE_SERIALIZER.get(ResourceLocation.parse(trueType));
			if(serializer == null)
				throw new JsonSyntaxException("Invalid or unsupported recipe type '" + trueType + "'");
			Recipe<?> parent = serializer.fromJson(recipeId, json);
			if(!(parent instanceof CraftingRecipe))
				throw new JsonSyntaxException("Type '" + trueType + "' is not a crafting recipe");

			if(parent instanceof ShapedRecipe)
				return new ShapedExclusionRecipe((CraftingRecipe) parent, excludedRecipes);
			return new ExclusionRecipe((CraftingRecipe) parent, excludedRecipes);
		}

		@NotNull
		@Override
		public ExclusionRecipe fromNetwork(@NotNull ResourceLocation recipeId, @NotNull FriendlyByteBuf buffer) {
			int exclusions = buffer.readVarInt();
			List<ResourceLocation> excludedRecipes = new ArrayList<>();
			for(int i = 0; i < exclusions; i++) {
				ResourceLocation loc = ResourceLocation.parse(buffer.readUtf(32767));
				if(!loc.equals(recipeId))
					excludedRecipes.add(loc);
			}
			String trueType = buffer.readUtf(32767);

			RecipeSerializer<?> serializer = BuiltInRegistries.RECIPE_SERIALIZER.get(ResourceLocation.parse(trueType));
			if(serializer == null)
				throw new IllegalArgumentException("Invalid or unsupported recipe type '" + trueType + "'");
			Recipe<?> parent = serializer.fromNetwork(recipeId, buffer);
			if(!(parent instanceof CraftingRecipe craftingRecipe))
				throw new IllegalArgumentException("Type '" + trueType + "' is not a crafting recipe");

			if(parent instanceof IShapedRecipe)
				return new ShapedExclusionRecipe(craftingRecipe, excludedRecipes);
			return new ExclusionRecipe(craftingRecipe, excludedRecipes);
		}

		@Override
		@SuppressWarnings("unchecked")
		public void toNetwork(@NotNull FriendlyByteBuf buffer, @NotNull ExclusionRecipe recipe) {
			buffer.writeVarInt(recipe.excluded.size());
			for(ResourceLocation loc : recipe.excluded)
				buffer.writeUtf(loc.toString(), 32767);
			buffer.writeUtf(Objects.toString(BuiltInRegistries.RECIPE_SERIALIZER.getKey(recipe.parent.getSerializer())), 32767);
			((RecipeSerializer<Recipe<?>>) recipe.parent.getSerializer()).toNetwork(buffer, recipe.parent);
		}

		@Override
		public MapCodec<ExclusionRecipe> codec() {
			return null;
		}

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, ExclusionRecipe> streamCodec() {
			return null;
		}
	}
}
