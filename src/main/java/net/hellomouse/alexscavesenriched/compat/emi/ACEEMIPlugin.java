package net.hellomouse.alexscavesenriched.compat.emi;

import com.github.alexmodguy.alexscaves.server.block.ACBlockRegistry;
import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiStack;
import net.hellomouse.alexscavesenriched.ACEBlockRegistry;
import net.hellomouse.alexscavesenriched.ACEItemRegistry;
import net.hellomouse.alexscavesenriched.ACERecipeRegistry;
import net.hellomouse.alexscavesenriched.AlexsCavesEnriched;
import net.hellomouse.alexscavesenriched.recipe.NeutronKillRecipe;
import net.hellomouse.alexscavesenriched.recipe.NuclearFurnanceRecipeAdditional;
import net.hellomouse.alexscavesenriched.recipe.NuclearTransmutationRecipe;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@EmiEntrypoint
public class ACEEMIPlugin implements EmiPlugin {
    public static final Identifier MY_SPRITE_SHEET = Identifier.fromNamespaceAndPath(AlexsCavesEnriched.MODID, "textures/gui/emi_simplified_textures.png");
    public static final EmiStack NUCLEAR_FURNACE = EmiStack.of(ACBlockRegistry.NUCLEAR_FURNACE_COMPONENT.get().asItem());
    public static final EmiStack NUCLEAR_BOMB = EmiStack.of(ACBlockRegistry.NUCLEAR_BOMB.get().asItem());
    public static final EmiStack NEUTRON_BOMB = EmiStack.of(ACEBlockRegistry.NEUTRON_BOMB.get().asItem());
    public static final EmiStack GAMMA_FLASHLIGHT = EmiStack.of(ACEItemRegistry.GAMMA_FLASHLIGHT.get().asItem());

    public static final EmiRecipeCategory ACE_NUCLEAR_FURNACE_CATEGORY
            = new EmiRecipeCategory(Identifier.fromNamespaceAndPath(AlexsCavesEnriched.MODID, "nuclear_furnace"),
            NUCLEAR_FURNACE, new EmiTexture(MY_SPRITE_SHEET, 0, 0, 16, 16));
    public static final EmiRecipeCategory ACE_NUCLEAR_TRANSMUTATION_CATEGORY
            = new EmiRecipeCategory(Identifier.fromNamespaceAndPath(AlexsCavesEnriched.MODID, "nuclear_transmutation"),
            NUCLEAR_BOMB, new EmiTexture(MY_SPRITE_SHEET, 0, 0, 16, 16));
    public static final EmiRecipeCategory ACE_NEUTRON_KILL_CATEGORY
            = new EmiRecipeCategory(Identifier.fromNamespaceAndPath(AlexsCavesEnriched.MODID, "neutron_kill"),
            NEUTRON_BOMB, new EmiTexture(MY_SPRITE_SHEET, 0, 0, 16, 16));

    @Override
    public void register(EmiRegistry registry) {
        registry.addCategory(ACE_NUCLEAR_FURNACE_CATEGORY);
        registry.addWorkstation(ACE_NUCLEAR_FURNACE_CATEGORY, NUCLEAR_FURNACE);
        registry.addCategory(ACE_NUCLEAR_TRANSMUTATION_CATEGORY);
        registry.addWorkstation(ACE_NUCLEAR_TRANSMUTATION_CATEGORY, NUCLEAR_BOMB);
        registry.addCategory(ACE_NEUTRON_KILL_CATEGORY);
        registry.addWorkstation(ACE_NEUTRON_KILL_CATEGORY, GAMMA_FLASHLIGHT);
        registry.addWorkstation(ACE_NEUTRON_KILL_CATEGORY, NEUTRON_BOMB);

        {
            RecipeManager manager = registry.getRecipeManager();
            for (NuclearFurnanceRecipeAdditional recipe : manager.listAllOfType(ACERecipeRegistry.NUCLEAR_FURNACE_TYPE.get()))
                registry.addRecipe(new EMINuclearFurnaceRecipe(recipe));
        }
        {
            HashSet<Text> alreadyVisited = new HashSet<>();
            RecipeManager manager = registry.getRecipeManager();
            for (NuclearTransmutationRecipe recipe : manager.listAllOfType(ACERecipeRegistry.NUCLEAR_TRANSMUTATION_TYPE.get())) {
                if (recipe.getIsTag()) {
                    var tag = TagKey.of(RegistryKeys.BLOCK, recipe.getInputLocation());
                    Set<ItemStack> blocksInTag = ForgeRegistries.BLOCKS.getEntries().stream()
                            .filter((entry) -> entry.getValue().getDefaultState().isIn(tag))
                            .map((entry) -> entry.getValue().asItem().getDefaultStack())
                            .collect(Collectors.toSet());
                    for (var blockItem : blocksInTag) {
                        if (alreadyVisited.contains(blockItem.getItem().getName()))
                            continue;
                        registry.addRecipe(new EMINuclearTransmutationRecipe(recipe, blockItem));
                        alreadyVisited.add(blockItem.getItem().getName());
                    }
                } else {
                    if (!alreadyVisited.contains(recipe.getInput().getItem().getName()))
                        registry.addRecipe(new EMINuclearTransmutationRecipe(recipe));
                    alreadyVisited.add(recipe.getInput().getItem().getName());
                }
            }
        }
        {
            HashSet<Text> alreadyVisited = new HashSet<>();
            RecipeManager manager = registry.getRecipeManager();
            for (NeutronKillRecipe recipe : manager.listAllOfType(ACERecipeRegistry.NEUTRON_KILL_TYPE.get())) {
                if (recipe.getIsTag()) {
                    var tag = TagKey.of(RegistryKeys.BLOCK, recipe.getInputLocation());
                    Set<ItemStack> blocksInTag = ForgeRegistries.BLOCKS.getEntries().stream()
                            .filter((entry) -> entry.getValue().getDefaultState().isIn(tag))
                            .map((entry) -> entry.getValue().asItem().getDefaultStack())
                            .collect(Collectors.toSet());
                    for (var blockItem : blocksInTag) {
                        if (alreadyVisited.contains(blockItem.getItem().getName()))
                            continue;
                        registry.addRecipe(new EMINeutronKillRecipe(recipe, blockItem));
                        alreadyVisited.add(blockItem.getItem().getName());
                    }
                } else {
                    if (!alreadyVisited.contains(recipe.getInput().getItem().getName()))
                        registry.addRecipe(new EMINeutronKillRecipe(recipe));
                    alreadyVisited.add(recipe.getInput().getItem().getName());
                }
            }
        }
    }
}
