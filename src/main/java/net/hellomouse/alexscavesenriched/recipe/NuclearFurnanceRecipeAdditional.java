package net.hellomouse.alexscavesenriched.recipe;

import com.github.alexmodguy.alexscaves.server.block.ACBlockRegistry;
import net.hellomouse.alexscavesenriched.ACERecipeRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.AbstractCookingRecipe;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.book.CookingRecipeCategory;
import net.minecraft.util.Identifier;

// Since base nuclear_furnace recipe type may not exist depending on Alex's caves config
// the nuclear_furnace_additional recipe type always works in a nuclear furnace
public class NuclearFurnanceRecipeAdditional extends AbstractCookingRecipe {
    public NuclearFurnanceRecipeAdditional(Identifier name, String group, CookingRecipeCategory category, Ingredient ingredient, ItemStack result, float experience, int cookingTime) {
        super(ACERecipeRegistry.NUCLEAR_FURNACE_TYPE.get(), name, group, category, ingredient, result, experience, cookingTime);
    }

    public ItemStack createIcon() {
        return new ItemStack(ACBlockRegistry.NUCLEAR_FURNACE.get());
    }

    public RecipeSerializer<?> getSerializer() {
        return ACERecipeRegistry.NUCLEAR_FURNACE.get();
    }
}
