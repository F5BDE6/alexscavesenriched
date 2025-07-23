package net.hellomouse.alexscavesenriched.mixins;

import com.github.alexmodguy.alexscaves.server.block.blockentity.ACBlockEntityRegistry;
import com.github.alexmodguy.alexscaves.server.block.blockentity.NuclearFurnaceBlockEntity;
import net.hellomouse.alexscavesenriched.ACEBlockRegistry;
import net.hellomouse.alexscavesenriched.ACERecipeRegistry;
import net.hellomouse.alexscavesenriched.AlexsCavesEnriched;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.AbstractCookingRecipe;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Optional;

@Mixin(NuclearFurnaceBlockEntity.class)
public abstract class NuclearFurnaceBlockEntityMixin extends LockableContainerBlockEntity implements SidedInventory {
    @Shadow
    private int fissionTime;
    @Final
    private RecipeManager.MatchGetter<Inventory, ? extends AbstractCookingRecipe> quickCheck;
    @Final
    @Unique
    public final RecipeManager.MatchGetter<Inventory, ? extends AbstractCookingRecipe> quickCheckAdditional
            = RecipeManager.createCachedMatchGetter(ACERecipeRegistry.NUCLEAR_FURNACE_TYPE.get());

    public NuclearFurnaceBlockEntityMixin(BlockPos pos, BlockState state) {
        super(ACBlockEntityRegistry.NUCLEAR_FURNACE.get(), pos, state);
    }

    @Inject(
            method = {"tick"},
            at = @At(
                    value = "INVOKE_ASSIGN",
                    target = "Lcom/github/alexmodguy/alexscaves/server/block/blockentity/NuclearFurnaceBlockEntity;getMaxFissionTime()I",
                    shift = At.Shift.AFTER
            ),
            locals = LocalCapture.CAPTURE_FAILSOFT
    )
    private static void makeEnrichedRodsBurnLonger(World level, BlockPos blockPos, BlockState state, NuclearFurnaceBlockEntity entity, CallbackInfo ci, boolean flag, ItemStack cookStack, ItemStack rodStack, ItemStack barrelStack, ItemStack cookResult) {
        if (rodStack.isOf(Item.BLOCK_ITEMS.get(ACEBlockRegistry.ENRICHED_URANIUM_ROD.get())))
            ((NuclearFurnaceBlockEntityMixin) (Object) entity).fissionTime *= (int) AlexsCavesEnriched.CONFIG.enrichedRodFuelMultiplier;
    }

    @Inject(at = @At(value = "HEAD"), method = {"getRecipeFor"}, cancellable = true)
    private void getRecipeFor(ItemStack itemStack, CallbackInfoReturnable<Optional<? extends AbstractCookingRecipe>> cir) {
        var container = new SimpleInventory(itemStack);
        var out = this.quickCheck.getFirstMatch(container, this.world);
        if (out.isPresent()) {
            cir.setReturnValue(Optional.of(out.get()));
        } else {
            cir.setReturnValue(this.quickCheckAdditional.getFirstMatch(container, this.world));
        }
    }
}
