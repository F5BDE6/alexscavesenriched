package net.hellomouse.alexscavesenriched.nuclear_furnace;

import net.hellomouse.alexscavesenriched.ACERecipeRegistry;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeType;

public class NuclearFurnaceUtil {
    public static RecipeType<? extends AbstractCookingRecipe> getRecipeTypeAdditional() {
        return ACERecipeRegistry.NUCLEAR_FURNACE_TYPE.get();
    }
}
