package org.violetmoon.quark.content.building.recipe;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class MixedExclusionRecipe /*implements CraftingRecipe, IShapedRecipe<CraftingContainer>*/ {

	/*public static final RecipeSerializer<MixedExclusionRecipe> SERIALIZER = new Serializer();

	private NonNullList<Ingredient> ingredients;

	private final String group;
	private final ItemStack output;
	private final TagKey<Item> tag;
	private final ItemStack placeholder;

	public MixedExclusionRecipe(String group, ItemStack output, TagKey<Item> tag, ItemStack placeholder) {
		this.group = group;
		this.output = output;
		this.tag = tag;
		this.placeholder = placeholder;
	}

	public static MixedExclusionRecipe forChest(String group, boolean log) {
		ItemStack output = new ItemStack(Items.CHEST, (log ? 4 : 1));
		TagKey<Item> tag = (log ? ItemTags.LOGS : ItemTags.PLANKS);
		ItemStack placeholder = new ItemStack(log ? Items.OAK_LOG : Items.OAK_PLANKS);
		return new MixedExclusionRecipe(group, output, tag, placeholder);
	}

	public static MixedExclusionRecipe forFurnace(String group) {
		ItemStack output = new ItemStack(Items.FURNACE);
		TagKey<Item> tag = ItemTags.STONE_CRAFTING_MATERIALS;
		ItemStack placeholder = new ItemStack(Items.COBBLESTONE);
		return new MixedExclusionRecipe(group, output, tag, placeholder);
	}

	@Override
	public boolean canCraftInDimensions(int x, int y) {
		return x == 3 && y == 3;
	}

	@Override
	@NotNull
	public ItemStack assemble(CraftingInput input, HolderLookup.Provider provider) {
		return output.copy();
	}

	@NotNull
	@Override
	public String getGroup() {
		return this.group;
	}

	@Override
	public ItemStack getResultItem(HolderLookup.Provider provider) {
		return output.copy();
	}

	@NotNull
	@Override
	public RecipeSerializer<?> getSerializer() {
		return SERIALIZER;
	}

	@Override
	public CraftingBookCategory category() {
		return CraftingBookCategory.MISC;
	}

	@Override
	public boolean matches(CraftingInput input, Level level) {
		if(input.getItem(4).isEmpty()) {
			ItemStack first = null;
			boolean foundDifference = false;

			for(int i = 0; i < 9; i++)
				if(i != 4) { // ignore center
					ItemStack stack = input.getItem(i);
					if(!stack.isEmpty() && stack.is(tag)) {
						if(first == null)
							first = stack;
						else if(!ItemStack.isSameItem(first, stack))
							foundDifference = true;
					} else
						return false;
				}
			return foundDifference;
		}
		return false;
	}

	@Override
	public int getWidth() {
		return 3;
	}

	@Override
	public int getHeight() {
		return 3;
	}

	@NotNull
	@Override
	public NonNullList<Ingredient> getIngredients() {
		if(ingredients == null) {
			NonNullList<Ingredient> list = NonNullList.withSize(9, Ingredient.EMPTY);
			Ingredient ingr = Ingredient.of(placeholder);
			for(int i = 0; i < 8; i++)
				list.set(i < 4 ? i : i + 1, ingr);
			ingredients = list;
		}

		return ingredients;
	}

	@Override
	public boolean isSpecial() {
		return true;
	}

	private static class Serializer implements RecipeSerializer<MixedExclusionRecipe> {


		@NotNull
		@Override
		public MixedExclusionRecipe fromJson(@NotNull ResourceLocation arg0, JsonObject arg1) {
			String type = arg1.get("subtype").getAsString();
			return forType(arg0, type);
		}

		@Override
		public MixedExclusionRecipe fromNetwork(@NotNull ResourceLocation arg0, FriendlyByteBuf arg1) {
			return forType(arg0, arg1.readUtf());
		}

		@Override
		public void toNetwork(FriendlyByteBuf arg0, MixedExclusionRecipe arg1) {
			arg0.writeUtf(arg1.type);
		}


		private MixedExclusionRecipe forType(String type) {
			return switch(type) {
			case "chest" -> MixedExclusionRecipe.forChest(type, false);
			case "chest4" -> MixedExclusionRecipe.forChest(type, true);
			case "furnace" -> MixedExclusionRecipe.forFurnace(type);
			default -> null;
			};
		}
	}
*/
}
