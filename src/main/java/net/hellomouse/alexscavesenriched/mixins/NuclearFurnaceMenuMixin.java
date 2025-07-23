package net.hellomouse.alexscavesenriched.mixins;

import com.github.alexmodguy.alexscaves.server.block.blockentity.NuclearFurnaceBlockEntity;
import com.github.alexmodguy.alexscaves.server.inventory.ACMenuRegistry;
import net.hellomouse.alexscavesenriched.nuclear_furnace.NuclearFurnaceUtil;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(com.github.alexmodguy.alexscaves.server.inventory.NuclearFurnaceMenu.class)
public abstract class NuclearFurnaceMenuMixin extends ScreenHandler {
    @Final
    @Mutable
    protected final World level;

    public NuclearFurnaceMenuMixin(int id, PlayerInventory inventory) {
        super(ACMenuRegistry.NUCLEAR_FURNACE_MENU.get(), id);
        this.level = inventory.player.getEntityWorld();
    }

    @Inject(at = @At(value = "HEAD"), method = {"canSmelt"}, cancellable = true)
    private void canSmelt(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        var container = new SimpleInventory(stack);
        cir.setReturnValue(this.level.getRecipeManager().getFirstMatch(NuclearFurnaceBlockEntity.getRecipeType(), container, this.level).isPresent() ||
                this.level.getRecipeManager().getFirstMatch(NuclearFurnaceUtil.getRecipeTypeAdditional(), container, this.level).isPresent());
    }
}
