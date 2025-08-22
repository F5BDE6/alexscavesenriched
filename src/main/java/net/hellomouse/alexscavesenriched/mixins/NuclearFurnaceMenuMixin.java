package net.hellomouse.alexscavesenriched.mixins;

import com.github.alexmodguy.alexscaves.server.block.blockentity.NuclearFurnaceBlockEntity;
import com.github.alexmodguy.alexscaves.server.inventory.ACMenuRegistry;
import net.hellomouse.alexscavesenriched.nuclear_furnace.NuclearFurnaceUtil;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(com.github.alexmodguy.alexscaves.server.inventory.NuclearFurnaceMenu.class)
public abstract class NuclearFurnaceMenuMixin extends AbstractContainerMenu {
    @Final
    @Mutable
    protected final Level level;

    public NuclearFurnaceMenuMixin(int id, Inventory inventory) {
        super(ACMenuRegistry.NUCLEAR_FURNACE_MENU.get(), id);
        this.level = inventory.player.getCommandSenderWorld();
    }

    @Inject(at = @At(value = "HEAD"), method = {"canSmelt"}, cancellable = true, remap = false)
    private void canSmelt(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        var container = new SimpleContainer(stack);
        cir.setReturnValue(this.level.getRecipeManager().getRecipeFor(NuclearFurnaceBlockEntity.getRecipeType(), container, this.level).isPresent() ||
                this.level.getRecipeManager().getRecipeFor(NuclearFurnaceUtil.getRecipeTypeAdditional(), container, this.level).isPresent());
    }
}
