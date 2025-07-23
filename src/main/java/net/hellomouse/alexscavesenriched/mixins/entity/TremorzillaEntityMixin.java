package net.hellomouse.alexscavesenriched.mixins.entity;

import com.github.alexmodguy.alexscaves.server.block.ACBlockRegistry;
import com.github.alexmodguy.alexscaves.server.entity.living.DinosaurEntity;
import com.github.alexmodguy.alexscaves.server.entity.living.TremorzillaEntity;
import com.github.alexmodguy.alexscaves.server.entity.util.ActivatesSirens;
import com.github.alexmodguy.alexscaves.server.entity.util.KaijuMob;
import com.github.alexmodguy.alexscaves.server.entity.util.KeybindUsingMount;
import com.github.alexmodguy.alexscaves.server.entity.util.ShakesScreen;
import com.github.alexthe666.citadel.animation.IAnimatedEntity;
import com.github.alexthe666.citadel.server.entity.pathfinding.raycoms.ITallWalker;
import net.hellomouse.alexscavesenriched.ACEBlockRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import static com.github.alexmodguy.alexscaves.server.entity.living.GrottoceratopsEntity.ANIMATION_CHEW;

@Mixin(TremorzillaEntity.class)
public abstract class TremorzillaEntityMixin extends DinosaurEntity implements KeybindUsingMount, IAnimatedEntity, ShakesScreen, KaijuMob, ActivatesSirens, ITallWalker {
    @Shadow
    private PlayerEntity lastFedPlayer = null;

    public TremorzillaEntityMixin(EntityType entityType, World level) {
        super(entityType, level);
    }

    @Inject(
            method = {"interactMob"},
            at = @At(
                    value = "INVOKE_ASSIGN",
                    target = "Lnet/minecraft/entity/player/PlayerEntity;getStackInHand(Lnet/minecraft/util/Hand;)Lnet/minecraft/item/ItemStack;",
                    shift = At.Shift.AFTER
            ),
            cancellable = true
    )
    private void allowInstantTamingWithEnrichedUraniumRods(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        var itemStack = player.getStackInHand(hand);
        if (!this.isTamed() && (itemStack.isOf(ACEBlockRegistry.ENRICHED_URANIUM_ROD.get().asItem())) && this.getAnimation() == NO_ANIMATION) {
            this.eat(player, hand, itemStack);
            this.setAnimation(ANIMATION_CHEW);
            this.lastFedPlayer = player;

            this.setOwner(lastFedPlayer);
            this.clearPositionTarget();
            this.getEntityWorld().sendEntityStatus(this, (byte) 7);
            cir.setReturnValue(ActionResult.SUCCESS);
        }
    }

    @Inject(at = @At(value = "HEAD"), method = {"isBreedingItem"}, cancellable = true)
    public void isFood(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(stack.isOf(ACBlockRegistry.NUCLEAR_BOMB.get().asItem()) || stack.isOf(ACEBlockRegistry.ENRICHED_URANIUM_ROD.get().asItem()));
    }
}
