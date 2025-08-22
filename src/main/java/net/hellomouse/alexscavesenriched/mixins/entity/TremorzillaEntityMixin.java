package net.hellomouse.alexscavesenriched.mixins.entity;

import com.github.alexmodguy.alexscaves.server.entity.living.DinosaurEntity;
import com.github.alexmodguy.alexscaves.server.entity.living.TremorzillaEntity;
import com.github.alexmodguy.alexscaves.server.entity.util.ActivatesSirens;
import com.github.alexmodguy.alexscaves.server.entity.util.KaijuMob;
import com.github.alexmodguy.alexscaves.server.entity.util.KeybindUsingMount;
import com.github.alexmodguy.alexscaves.server.entity.util.ShakesScreen;
import com.github.alexthe666.citadel.animation.IAnimatedEntity;
import com.github.alexthe666.citadel.server.entity.pathfinding.raycoms.ITallWalker;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.hellomouse.alexscavesenriched.ACEBlockRegistry;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.github.alexmodguy.alexscaves.server.entity.living.GrottoceratopsEntity.ANIMATION_CHEW;

@Mixin(TremorzillaEntity.class)
public abstract class TremorzillaEntityMixin extends DinosaurEntity implements KeybindUsingMount, IAnimatedEntity, ShakesScreen, KaijuMob, ActivatesSirens, ITallWalker {
    @Shadow
    private Player lastFedPlayer = null;

    public TremorzillaEntityMixin(EntityType entityType, Level level) {
        super(entityType, level);
    }

    @Inject(
            method = {"mobInteract"},
            at = @At(
                    value = "INVOKE_ASSIGN",
                    target = "Lnet/minecraft/world/entity/player/Player;getItemInHand(Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/item/ItemStack;",
                    shift = At.Shift.AFTER
            ),
            cancellable = true
    )
    private void allowInstantTamingWithEnrichedUraniumRods(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        var itemStack = player.getItemInHand(hand);
        if (!this.isTame() && (itemStack.is(ACEBlockRegistry.ENRICHED_URANIUM_ROD.get().asItem())) && this.getAnimation() == NO_ANIMATION) {
            this.usePlayerItem(player, hand, itemStack);
            this.setAnimation(ANIMATION_CHEW);
            this.lastFedPlayer = player;

            this.tame(lastFedPlayer);
            this.clearRestriction();
            this.getCommandSenderWorld().broadcastEntityEvent(this, (byte) 7);
            cir.setReturnValue(InteractionResult.SUCCESS);
        }
    }

    @ModifyReturnValue(method = "isFood", at = @At("RETURN"))
    public boolean isFood(boolean original, ItemStack stack) {
        return original || stack.is(ACEBlockRegistry.ENRICHED_URANIUM_ROD.get().asItem());
    }
}
