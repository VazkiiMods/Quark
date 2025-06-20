package org.violetmoon.quark.datagen.builder;

import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.ItemLike;

import javax.annotation.Nullable;
import java.util.Objects;

public class QuarkShapedRecipeBuilder extends ShapedRecipeBuilder {

    @Nullable
    private String flag;

    public QuarkShapedRecipeBuilder(RecipeCategory p_249996_, ItemLike p_251475_, int p_248948_) {
        super(p_249996_, p_251475_, p_248948_);
    }

    public QuarkShapedRecipeBuilder requiresFlag(String flag){
        this.flag = flag;
        return this;
    }

    /*
    @Override
    public void save(RecipeOutput recipeOutput, ResourceLocation resourceLocation) {
        ShapedRecipe shapedrecipe = new ShapedRecipe((String)Objects.requireNonNullElse(this.group, ""), RecipeBuilder.determineBookCategory(this.category), shapedrecipepattern, this.resultStack, this.showNotification);
        recipeOutput.accept(resourceLocation, shapedrecipe, null);
    }
     */
}
