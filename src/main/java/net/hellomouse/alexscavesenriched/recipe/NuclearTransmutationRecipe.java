package net.hellomouse.alexscavesenriched.recipe;

import com.google.gson.JsonObject;
import net.hellomouse.alexscavesenriched.ACERecipeRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.inventory.SimpleInventory;
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

import java.util.Objects;
import java.util.function.Predicate;

// For in-world nuclear detonation transmutation
public class NuclearTransmutationRecipe implements Recipe<SimpleInventory> {
    private final Identifier id;
    private final Predicate<BlockState> inputPredicate;
    private final Identifier inputLocation;
    private final Block outputBlock;
    private final Block inputBlock;
    private final boolean isTag;
    private final float chance;

    public NuclearTransmutationRecipe(Identifier id, Identifier inputLocation, boolean isTag, Block output, float chance) {
        this.id = id;
        this.inputLocation = inputLocation;
        this.outputBlock = output;
        this.isTag = isTag;
        this.chance = chance;

        if (isTag) {
            TagKey<Block> tag = TagKey.of(RegistryKeys.BLOCK, inputLocation);
            inputPredicate = state -> state.isIn(tag);
            inputBlock = null;
        } else {
            Block block = ForgeRegistries.BLOCKS.getValue(inputLocation);
            inputPredicate = state -> {
                assert block != null;
                return state.isOf(block);
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
        return outputBlock.asItem().getDefaultStack();
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
        return inputBlock.asItem().getDefaultStack();
    }

    public static class NuclearTransmutationRecipeSerializer implements RecipeSerializer<NuclearTransmutationRecipe> {
        @Override
        public @NotNull NuclearTransmutationRecipe read(Identifier id, JsonObject json) {
            String inputStr = json.get("input").getAsString();
            Block output = ForgeRegistries.BLOCKS.getValue(Identifier.parse(json.get("output").getAsString()));
            float chance = json.has("chance") ? JsonHelper.getFloat(json, "chance") : 1.0f;
            return new NuclearTransmutationRecipe(id,
                    inputStr.startsWith("#") ?
                            Identifier.parse(inputStr.substring(1)) :
                            Identifier.parse(inputStr),
                    inputStr.startsWith("#"),
                    output, chance);
        }

        @Override
        public NuclearTransmutationRecipe read(Identifier id, PacketByteBuf buf) {
            Identifier input = buf.readIdentifier();
            Block output = ForgeRegistries.BLOCKS.getValue(buf.readIdentifier());
            float chance = buf.readFloat();
            boolean isTag = buf.readBoolean();
            return new NuclearTransmutationRecipe(id, input, isTag, output, chance);
        }

        @Override
        public void write(PacketByteBuf buf, NuclearTransmutationRecipe recipe) {
            buf.writeIdentifier(recipe.getInputLocation());
            buf.writeIdentifier(Objects.requireNonNull(ForgeRegistries.BLOCKS.getKey(recipe.getOutput())));
            buf.writeFloat(recipe.getChance());
            buf.writeBoolean(recipe.getIsTag());
        }

    }
}
