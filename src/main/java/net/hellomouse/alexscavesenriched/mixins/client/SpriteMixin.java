package net.hellomouse.alexscavesenriched.mixins.client;

import net.hellomouse.alexscavesenriched.client.particle.RadiationGlowTexture;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteContents;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(Sprite.class)
public class SpriteMixin {
    @Shadow
    @Final
    private SpriteContents contents;

    @Inject(method = "createAnimation", at = @At("HEAD"), cancellable = true)
    public void animation(CallbackInfoReturnable<Sprite.TickableAnimation> cir) {
        if (this.contents.getId().equals(RadiationGlowTexture.ID)) {
            Sprite sprite = (Sprite) (Object) this;
            cir.setReturnValue(
                    new Sprite.TickableAnimation() {
                        @Override
                        public void tick() {
                            RadiationGlowTexture.tick();
                            contents.upload(sprite.getX(), sprite.getY());
                        }

                        @Override
                        public void close() {

                        }
                    }
            );
        }
    }
}
