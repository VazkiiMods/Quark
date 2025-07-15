package org.violetmoon.quark.content.building.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.TagNetworkSerialization;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class MixedExclusionRecipe implements CraftingRecipe /*implements CraftingRecipe, IShapedRecipe<CraftingContainer>*/ {
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

    /*public static MixedExclusionRecipe forChest(String group, boolean log) {
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
    }*/

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
        return new Serializer();
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

    public static class Serializer implements RecipeSerializer<MixedExclusionRecipe> {
        public static final MapCodec<MixedExclusionRecipe> CODEC = RecordCodecBuilder.mapCodec(
                inst -> inst.group(
                                Codec.STRING.optionalFieldOf("group", "").forGetter(recipe -> recipe.group),
                                ItemStack.STRICT_CODEC.fieldOf("output").forGetter(recipe -> recipe.output),
                                TagKey.codec(Registries.ITEM).fieldOf("tag").forGetter(recipe -> recipe.tag),
                                ItemStack.CODEC.fieldOf("placeholder").forGetter(recipe -> recipe.placeholder)
                        )
                        .apply(inst, MixedExclusionRecipe::new)
        );
        public static final StreamCodec<RegistryFriendlyByteBuf, MixedExclusionRecipe> STREAM_CODEC = StreamCodec.of(
                MixedExclusionRecipe.Serializer::toNetwork, MixedExclusionRecipe.Serializer::fromNetwork
        );

        public static <T> StreamCodec<ByteBuf, TagKey<T>> evilBackportedTagKeyStreamCodec(ResourceKey<? extends Registry<T>> registry) {
            return ResourceLocation.STREAM_CODEC.map(resourceLocation -> TagKey.create(registry, resourceLocation), TagKey::location);
        }

        @Override
        public MapCodec<MixedExclusionRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, MixedExclusionRecipe> streamCodec() {
            return STREAM_CODEC;
        }

        private static MixedExclusionRecipe fromNetwork(RegistryFriendlyByteBuf byteBuf) {
            String group = byteBuf.readUtf();
            ItemStack output = ItemStack.STREAM_CODEC.decode(byteBuf);
            TagKey<Item> tagKey = evilBackportedTagKeyStreamCodec(Registries.ITEM).decode(byteBuf);
            ItemStack placeholder = ItemStack.STREAM_CODEC.decode(byteBuf);
            return new MixedExclusionRecipe(group, output, tagKey, placeholder);
        }

        private static void toNetwork(RegistryFriendlyByteBuf byteBuf, MixedExclusionRecipe recipe) {
            byteBuf.writeUtf(recipe.group);
            ItemStack.STREAM_CODEC.encode(byteBuf, recipe.output);
            evilBackportedTagKeyStreamCodec(Registries.ITEM).encode(byteBuf, recipe.tag);
            ItemStack.STREAM_CODEC.encode(byteBuf, recipe.placeholder);
        }
    }
}
