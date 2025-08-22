package net.hellomouse.alexscavesenriched.recipe;

import com.github.alexmodguy.alexscaves.server.block.ACBlockRegistry;
import net.hellomouse.alexscavesenriched.ACERecipeRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

// Since base nuclear_furnace recipe type may not exist depending on Alex's caves config
// the nuclear_furnace_additional recipe type always works in a nuclear furnace
public class NuclearFurnanceRecipeAdditional extends AbstractCookingRecipe {
    public NuclearFurnanceRecipeAdditional(ResourceLocation name, String group, CookingBookCategory category, Ingredient ingredient, ItemStack result, float experience, int cookingTime) {
        super(ACERecipeRegistry.NUCLEAR_FURNACE_TYPE.get(), name, group, category, ingredient, result, experience, cookingTime);
    }

    public ItemStack getToastSymbol() {
        return new ItemStack(ACBlockRegistry.NUCLEAR_FURNACE.get());
    }

    public RecipeSerializer<?> getSerializer() {
        return ACERecipeRegistry.NUCLEAR_FURNACE.get();
    }
}
