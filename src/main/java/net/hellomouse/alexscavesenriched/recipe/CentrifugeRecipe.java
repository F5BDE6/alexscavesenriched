package net.hellomouse.alexscavesenriched.recipe;

import com.google.gson.JsonObject;
import net.hellomouse.alexscavesenriched.ACERecipeRegistry;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.function.Predicate;

// For centrifuge
public class CentrifugeRecipe implements Recipe<SimpleInventory> {
    private final Identifier id;
    private final Predicate<ItemStack> inputPredicate;
    private final Identifier inputLocation;
    private final Item inputItem;
    private final ArrayList<Item> outputItems;
    private final boolean isTag;
    private final float chance;

    public CentrifugeRecipe(Identifier id, Identifier inputLocation, boolean isTag, ArrayList<Item> output, float chance) {
        this.id = id;
        this.inputLocation = inputLocation;
        this.outputItems = output;
        this.isTag = isTag;
        this.chance = chance;

        if (isTag) {
            TagKey<Item> tag = TagKey.of(RegistryKeys.ITEM, inputLocation);
            inputPredicate = stack -> stack.isIn(tag);
            inputItem = null;
        } else {
            Item item = ForgeRegistries.ITEMS.getValue(inputLocation);
            inputPredicate = state -> {
                assert item != null;
                return state.isOf(item);
            };
            inputItem = item;
        }
    }

    public boolean matches(@NotNull ItemStack stack) {
        return inputPredicate.test(stack);
    }

    public ArrayList<Item> getOutputs() {
        return outputItems;
    }

    public float getChance() {
        return chance;
    }

    @Override
    public @NotNull Identifier getId() {
        return id;
    }

    public boolean getIsTag() {
        return isTag;
    }

    public @NotNull Identifier getInputLocation() {
        return inputLocation;
    }

    // These methods aren't used here but must be implemented
    @Override
    public boolean matches(SimpleInventory inv, World level) {
        return false;
    }

    @Override
    public ItemStack craft(SimpleInventory inventory, DynamicRegistryManager registryManager) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean fits(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getOutput(DynamicRegistryManager p_267052_) {
        return outputItems.get(0).getDefaultStack();
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return ACERecipeRegistry.CENTRIFUGE.get();
    }

    @Override
    public @NotNull RecipeType<?> getType() {
        return ACERecipeRegistry.CENTRIFUGE_TYPE.get();
    }

    public @Nullable ItemStack getInput() {
        if (inputItem == null)
            return null;
        return inputItem.asItem().getDefaultStack();
    }

    public static class CentrifugeRecipeSerializer implements RecipeSerializer<CentrifugeRecipe> {
        @Override
        public @NotNull CentrifugeRecipe read(Identifier id, JsonObject json) {
            String inputStr = json.get("input").getAsString();
            var outputsJSON = json.get("outputs").getAsJsonArray();
            ArrayList<Item> outputs = new ArrayList<>();
            for (var item : outputsJSON) {
                Item output = ForgeRegistries.ITEMS.getValue(Identifier.parse(item.getAsString()));
                outputs.add(output);
            }

            float chance = json.has("chance") ? JsonHelper.getFloat(json, "chance") : 1.0f;
            return new CentrifugeRecipe(id,
                    inputStr.startsWith("#") ?
                            Identifier.parse(inputStr.substring(1)) :
                            Identifier.parse(inputStr),
                    inputStr.startsWith("#"),
                    outputs, chance);
        }

        @Override
        public CentrifugeRecipe read(Identifier id, PacketByteBuf buf) {
            Identifier input = buf.readIdentifier();
            ArrayList<Item> outputs = new ArrayList<>();
            String outputStrings = buf.readString();
            for (var item : outputStrings.split("\\|"))
                outputs.add(ForgeRegistries.ITEMS.getValue(Identifier.parse(item)));

            float chance = buf.readFloat();
            boolean isTag = buf.readBoolean();
            return new CentrifugeRecipe(id, input, isTag, outputs, chance);
        }

        @Override
        public void write(PacketByteBuf buf, CentrifugeRecipe recipe) {
            buf.writeIdentifier(recipe.getInputLocation());
            StringJoiner allOutputs = new StringJoiner("|");
            for (var item : recipe.outputItems)
                allOutputs.add(Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(item)).toString());

            buf.writeString(allOutputs.toString());
            buf.writeFloat(recipe.getChance());
            buf.writeBoolean(recipe.getIsTag());
        }
    }
}
