package net.hellomouse.alexscavesenriched.compat.emi;

import dev.emi.emi.EmiPort;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.hellomouse.alexscavesenriched.recipe.NuclearFurnanceRecipeAdditional;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import java.util.List;

public class EMINuclearFurnaceRecipe extends BasicEmiRecipe {
    protected NuclearFurnanceRecipeAdditional recipe;

    public EMINuclearFurnaceRecipe(NuclearFurnanceRecipeAdditional recipe) {
        super(ACEEMIPlugin.ACE_NUCLEAR_FURNACE_CATEGORY, recipe.getId(), 70, 18);
        this.recipe = recipe;
        this.inputs.add(EmiIngredient.of(recipe.getIngredients().get(0)));
        this.outputs.add(EmiStack.of(recipe.getResultItem(null)));
    }

    @Override
    public int getDisplayWidth() {
        return 82;
    }

    @Override
    public int getDisplayHeight() {
        return 38;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addFillingArrow(24, 5, 50 * recipe.getCookingTime()).tooltip((mx, my)
                -> List.of(ClientTooltipComponent.create(
                EmiPort.ordered(EmiPort.translatable("emi.cooking.time", recipe.getCookingTime() / 20f))
                )
        ));
        widgets.addTexture(EmiTexture.FULL_FLAME, 1, 24);
        widgets.addText(EmiPort.ordered(EmiPort.translatable("emi.cooking.experience", recipe.getExperience())), 26, 28, -1, true);
        widgets.addSlot(inputs.get(0), 0, 4);
        widgets.addSlot(outputs.get(0), 56, 0).large(true).recipeContext(this);
    }
}