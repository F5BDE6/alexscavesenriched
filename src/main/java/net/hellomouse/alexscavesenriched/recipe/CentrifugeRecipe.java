package net.hellomouse.alexscavesenriched.recipe;

import com.google.gson.JsonObject;
import net.hellomouse.alexscavesenriched.ACERecipeRegistry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.function.Predicate;

// For centrifuge
public class CentrifugeRecipe implements Recipe<SimpleContainer> {
    private final ResourceLocation id;
    private final Predicate<ItemStack> inputPredicate;
    private final ResourceLocation inputLocation;
    private final Item inputItem;
    private final ArrayList<Item> outputItems;
    private final boolean isTag;
    private final float chance;

    public CentrifugeRecipe(ResourceLocation id, ResourceLocation inputLocation, boolean isTag, ArrayList<Item> output, float chance) {
        this.id = id;
        this.inputLocation = inputLocation;
        this.outputItems = output;
        this.isTag = isTag;
        this.chance = chance;

        if (isTag) {
            TagKey<Item> tag = TagKey.create(Registries.ITEM, inputLocation);
            inputPredicate = stack -> stack.is(tag);
            inputItem = null;
        } else {
            Item item = ForgeRegistries.ITEMS.getValue(inputLocation);
            inputPredicate = state -> {
                assert item != null;
                return state.is(item);
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
    public @NotNull ResourceLocation getId() {
        return id;
    }

    public boolean getIsTag() {
        return isTag;
    }

    public @NotNull ResourceLocation getInputLocation() {
        return inputLocation;
    }

    // These methods aren't used here but must be implemented
    @Override
    public boolean matches(SimpleContainer inv, Level level) {
        return false;
    }

    @Override
    public ItemStack assemble(SimpleContainer inventory, RegistryAccess registryManager) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess p_267052_) {
        return outputItems.get(0).getDefaultInstance();
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
        return inputItem.asItem().getDefaultInstance();
    }

    public static class CentrifugeRecipeSerializer implements RecipeSerializer<CentrifugeRecipe> {
        @Override
        public @NotNull CentrifugeRecipe fromJson(ResourceLocation id, JsonObject json) {
            String inputStr = json.get("input").getAsString();
            var outputsJSON = json.get("outputs").getAsJsonArray();
            ArrayList<Item> outputs = new ArrayList<>();
            for (var item : outputsJSON) {
                Item output = ForgeRegistries.ITEMS.getValue(ResourceLocation.parse(item.getAsString()));
                outputs.add(output);
            }

            float chance = json.has("chance") ? GsonHelper.getAsFloat(json, "chance") : 1.0f;
            return new CentrifugeRecipe(id,
                    inputStr.startsWith("#") ?
                            ResourceLocation.parse(inputStr.substring(1)) :
                            ResourceLocation.parse(inputStr),
                    inputStr.startsWith("#"),
                    outputs, chance);
        }

        @Override
        public CentrifugeRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            ResourceLocation input = buf.readResourceLocation();
            ArrayList<Item> outputs = new ArrayList<>();
            String outputStrings = buf.readUtf();
            for (var item : outputStrings.split("\\|"))
                outputs.add(ForgeRegistries.ITEMS.getValue(ResourceLocation.parse(item)));

            float chance = buf.readFloat();
            boolean isTag = buf.readBoolean();
            return new CentrifugeRecipe(id, input, isTag, outputs, chance);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, CentrifugeRecipe recipe) {
            buf.writeResourceLocation(recipe.getInputLocation());
            StringJoiner allOutputs = new StringJoiner("|");
            for (var item : recipe.outputItems)
                allOutputs.add(Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(item)).toString());

            buf.writeUtf(allOutputs.toString());
            buf.writeFloat(recipe.getChance());
            buf.writeBoolean(recipe.getIsTag());
        }
    }
}
