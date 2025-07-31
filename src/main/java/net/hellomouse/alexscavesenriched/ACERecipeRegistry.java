package net.hellomouse.alexscavesenriched;

import net.hellomouse.alexscavesenriched.recipe.NeutronKillRecipe;
import net.hellomouse.alexscavesenriched.recipe.NuclearFurnanceRecipeAdditional;
import net.hellomouse.alexscavesenriched.recipe.NuclearTransmutationRecipe;
import net.minecraft.recipe.CookingRecipeSerializer;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.RegistryKeys;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ACERecipeRegistry {
    public static final DeferredRegister<RecipeType<?>> TYPE_DEF_REG = DeferredRegister.create(RegistryKeys.RECIPE_TYPE, AlexsCavesEnriched.MODID);
    public static final DeferredRegister<RecipeSerializer<?>> DEF_REG = DeferredRegister.create(RegistryKeys.RECIPE_SERIALIZER, AlexsCavesEnriched.MODID);

    public static final RegistryObject<RecipeType<NuclearFurnanceRecipeAdditional>> NUCLEAR_FURNACE_TYPE = TYPE_DEF_REG.register("nuclear_furnace_additional", () -> new RecipeType<>() {});
    public static final RegistryObject<RecipeSerializer<?>> NUCLEAR_FURNACE = DEF_REG.register("nuclear_furnace_additional", () -> new CookingRecipeSerializer<>(NuclearFurnanceRecipeAdditional::new, 100));

    public static final RegistryObject<RecipeType<NuclearTransmutationRecipe>> NUCLEAR_TRANSMUTATION_TYPE = TYPE_DEF_REG.register("nuclear_transmutation", () -> new RecipeType<>() {});
    public static final RegistryObject<RecipeSerializer<NuclearTransmutationRecipe>> NUCLEAR_TRANSMUTATION =
            DEF_REG.register("nuclear_transmutation", NuclearTransmutationRecipe.NuclearTransmutationRecipeSerializer::new);

    public static final RegistryObject<RecipeType<NeutronKillRecipe>> NEUTRON_KILL_TYPE = TYPE_DEF_REG.register("neutron_kill", () -> new RecipeType<>() {});
    public static final RegistryObject<RecipeSerializer<NeutronKillRecipe>> NEUTRON_KILL =
            DEF_REG.register("neutron_kill", NeutronKillRecipe.NeutronKillRecipeSerializer::new);
}
