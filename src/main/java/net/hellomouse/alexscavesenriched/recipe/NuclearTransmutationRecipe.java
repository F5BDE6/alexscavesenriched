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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Predicate;

// For in-world nuclear detonation transmutation
public class NuclearTransmutationRecipe implements Recipe<SimpleContainer> {
    private final ResourceLocation id;
    private final Predicate<BlockState> inputPredicate;
    private final ResourceLocation inputLocation;
    private final Block outputBlock;
    private final Block inputBlock;
    private final boolean isTag;
    private final float chance;

    public NuclearTransmutationRecipe(ResourceLocation id, ResourceLocation inputLocation, boolean isTag, Block output, float chance) {
        this.id = id;
        this.inputLocation = inputLocation;
        this.outputBlock = output;
        this.isTag = isTag;
        this.chance = chance;

        if (isTag) {
            TagKey<Block> tag = TagKey.create(Registries.BLOCK, inputLocation);
            inputPredicate = state -> state.is(tag);
            inputBlock = null;
        } else {
            Block block = ForgeRegistries.BLOCKS.getValue(inputLocation);
            inputPredicate = state -> {
                assert block != null;
                return state.is(block);
            };
            inputBlock = block;
        }
    }

    public boolean matches(@NotNull BlockState state) {
        return inputPredicate.test(state);
    }

    public Block getOutput() {
        return outputBlock;
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
        return outputBlock.asItem().getDefaultInstance();
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return ACERecipeRegistry.NUCLEAR_TRANSMUTATION.get();
    }

    @Override
    public @NotNull RecipeType<?> getType() {
        return ACERecipeRegistry.NUCLEAR_TRANSMUTATION_TYPE.get();
    }

    public @Nullable ItemStack getInput() {
        if (inputBlock == null)
            return null;
        return inputBlock.asItem().getDefaultInstance();
    }

    public static class NuclearTransmutationRecipeSerializer implements RecipeSerializer<NuclearTransmutationRecipe> {
        @Override
        public @NotNull NuclearTransmutationRecipe fromJson(ResourceLocation id, JsonObject json) {
            String inputStr = json.get("input").getAsString();
            Block output = ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse(json.get("output").getAsString()));
            float chance = json.has("chance") ? GsonHelper.getAsFloat(json, "chance") : 1.0f;
            return new NuclearTransmutationRecipe(id,
                    inputStr.startsWith("#") ?
                            ResourceLocation.parse(inputStr.substring(1)) :
                            ResourceLocation.parse(inputStr),
                    inputStr.startsWith("#"),
                    output, chance);
        }

        @Override
        public NuclearTransmutationRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            ResourceLocation input = buf.readResourceLocation();
            Block output = ForgeRegistries.BLOCKS.getValue(buf.readResourceLocation());
            float chance = buf.readFloat();
            boolean isTag = buf.readBoolean();
            return new NuclearTransmutationRecipe(id, input, isTag, output, chance);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, NuclearTransmutationRecipe recipe) {
            buf.writeResourceLocation(recipe.getInputLocation());
            buf.writeResourceLocation(Objects.requireNonNull(ForgeRegistries.BLOCKS.getKey(recipe.getOutput())));
            buf.writeFloat(recipe.getChance());
            buf.writeBoolean(recipe.getIsTag());
        }

    }
}
