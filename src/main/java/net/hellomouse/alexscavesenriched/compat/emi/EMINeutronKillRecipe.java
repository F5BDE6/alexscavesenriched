package net.hellomouse.alexscavesenriched.compat.emi;

import dev.emi.emi.EmiPort;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.hellomouse.alexscavesenriched.recipe.NeutronKillRecipe;
import net.minecraft.world.item.ItemStack;

public class EMINeutronKillRecipe extends BasicEmiRecipe {
    protected NeutronKillRecipe recipe;

    public EMINeutronKillRecipe(NeutronKillRecipe recipe) {
        super(ACEEMIPlugin.ACE_NEUTRON_KILL_CATEGORY, recipe.getId(), 70, 18);
        this.recipe = recipe;

        if (recipe.getInput() != null)
            this.inputs.add(EmiStack.of(recipe.getInput()));
        else
            throw new RuntimeException("EMINeutronKillRecipe does not expect tag");
        this.outputs.add(EmiStack.of(recipe.getResultItem(null)));
    }

    public EMINeutronKillRecipe(NeutronKillRecipe recipe, ItemStack inputsOverride) {
        super(ACEEMIPlugin.ACE_NEUTRON_KILL_CATEGORY, recipe.getId().withPrefix("/" + inputsOverride.getDescriptionId()), 70, 18);
        this.recipe = recipe;
        this.inputs.add(EmiStack.of(inputsOverride));
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
        widgets.addTexture(EmiTexture.FULL_ARROW, 24, 5);
        widgets.addText(EmiPort.ordered(EmiPort.translatable("emi.transmutation_chance", recipe.getChance() * 100)),
                0, 28, -1, true);
        widgets.addSlot(inputs.get(0), 0, 4);
        widgets.addSlot(outputs.get(0), 56, 0).large(true).recipeContext(this);
    }
}