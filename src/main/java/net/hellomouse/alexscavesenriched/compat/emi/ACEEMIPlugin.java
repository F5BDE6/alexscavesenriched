package net.hellomouse.alexscavesenriched.compat.emi;

import com.github.alexmodguy.alexscaves.server.block.ACBlockRegistry;
import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiStack;
import net.hellomouse.alexscavesenriched.ACERecipeRegistry;
import net.hellomouse.alexscavesenriched.AlexsCavesEnriched;
import net.hellomouse.alexscavesenriched.recipe.NuclearFurnanceRecipeAdditional;
import net.hellomouse.alexscavesenriched.recipe.NuclearTransmutationRecipe;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Set;
import java.util.stream.Collectors;

@EmiEntrypoint
public class ACEEMIPlugin implements EmiPlugin {
    public static final Identifier MY_SPRITE_SHEET = Identifier.fromNamespaceAndPath(AlexsCavesEnriched.MODID, "textures/gui/emi_simplified_textures.png");
    public static final EmiStack NUCLEAR_FURNACE = EmiStack.of(ACBlockRegistry.NUCLEAR_FURNACE_COMPONENT.get().asItem());
    public static final EmiStack NUCLEAR_BOMB = EmiStack.of(ACBlockRegistry.NUCLEAR_BOMB.get().asItem());
    public static final EmiRecipeCategory ACE_NUCLEAR_FURNACE_CATEGORY
            = new EmiRecipeCategory(Identifier.fromNamespaceAndPath(AlexsCavesEnriched.MODID, "nuclear_furnace"),
            NUCLEAR_FURNACE, new EmiTexture(MY_SPRITE_SHEET, 0, 0, 16, 16));
    public static final EmiRecipeCategory ACE_NUCLEAR_TRANSMUTATION_CATEGORY
            = new EmiRecipeCategory(Identifier.fromNamespaceAndPath(AlexsCavesEnriched.MODID, "nuclear_transmutation"),
            NUCLEAR_BOMB, new EmiTexture(MY_SPRITE_SHEET, 0, 0, 16, 16));

    @Override
    public void register(EmiRegistry registry) {
        registry.addCategory(ACE_NUCLEAR_FURNACE_CATEGORY);
        registry.addWorkstation(ACE_NUCLEAR_FURNACE_CATEGORY, NUCLEAR_FURNACE);
        registry.addCategory(ACE_NUCLEAR_TRANSMUTATION_CATEGORY);
        registry.addWorkstation(ACE_NUCLEAR_TRANSMUTATION_CATEGORY, NUCLEAR_BOMB);

        {
            RecipeManager manager = registry.getRecipeManager();
            for (NuclearFurnanceRecipeAdditional recipe : manager.listAllOfType(ACERecipeRegistry.NUCLEAR_FURNACE_TYPE.get()))
                registry.addRecipe(new EMINuclearFurnaceRecipe(recipe));
        }
        {
            RecipeManager manager = registry.getRecipeManager();
            for (NuclearTransmutationRecipe recipe : manager.listAllOfType(ACERecipeRegistry.NUCLEAR_TRANSMUTATION_TYPE.get())) {
                if (recipe.getIsTag()) {
                    var tag = TagKey.of(RegistryKeys.BLOCK, recipe.getInputLocation());
                    Set<ItemStack> blocksInTag = ForgeRegistries.BLOCKS.getEntries().stream()
                            .filter((entry) -> entry.getValue().getDefaultState().isIn(tag))
                            .map((entry) -> entry.getValue().asItem().getDefaultStack())
                            .collect(Collectors.toSet());
                    for (var blockItem : blocksInTag)
                        registry.addRecipe(new EMINuclearTransmutationRecipe(recipe, blockItem));
                } else {
                    registry.addRecipe(new EMINuclearTransmutationRecipe(recipe));
                }
            }
        }
    }
}
