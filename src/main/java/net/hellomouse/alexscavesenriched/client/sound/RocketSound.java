package net.hellomouse.alexscavesenriched.client.sound;

import D;
import net.hellomouse.alexscavesenriched.entity.RocketEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RocketSound extends AbstractTickableSoundInstance {
    protected final RocketEntity rocket;

    public RocketSound(RocketEntity rocket, SoundEvent soundEvent, SoundSource soundSource) {
        super(soundEvent, soundSource, SoundInstance.createUnseededRandom());
        this.rocket = rocket;
        this.x = (float)rocket.getX();
        this.y = (float)rocket.getY();
        this.z = (float)rocket.getZ();
        this.looping = true;
        this.delay = 0;
        this.volume = 0.0F;
    }

    public void tick() {
        if (!this.rocket.isRemoved()) {
            this.x = (float)this.rocket.getX();
            this.y = (float)this.rocket.getY();
            this.z = (float)this.rocket.getZ();

            final double soundRange = 32.0F;
            var player = Minecraft.getInstance().player;
            if (player == null)
                return;
            if (this.rocket.getDeltaMovement().length() < 0.5) {
                this.volume = 0.0F;
                return;
            }

            var thisPos = new Vec3(this.x, this.y, this.z);
            var playerDistance = player.getPosition(1.0F).distanceTo(thisPos);
            if (playerDistance > soundRange) {
                this.pitch = 0.0F;
                this.volume = 0.0F;
                return;
            }

            Vec3 thisFuturePos = thisPos.add(this.rocket.getDeltaMovement());
            Vec3 playerFuturePos = player.getPosition(1.0F).add(player.getDeltaMovement());
            final double deltaDistance = thisFuturePos.distanceTo(playerFuturePos) - thisPos.distanceTo(player.getPosition(1.0F));
            float pitchDelta = Mth.inverseLerp((float) deltaDistance / 3.0F, -1.0F, 1.0F);

            this.pitch = Mth.lerp(
                    Mth.clamp(1.0F - pitchDelta, 0.0F, 1.0F),
                    this.getMinPitch(), this.getMaxPitch()
            );
            this.volume = 1.0F;
        } else {
            this.stop();
        }
    }

    private float getMinPitch() { return 0.5F; }
    private float getMaxPitch() { return 1.5F; }

    public boolean canStartSilent() {
        return true;
    }

    public boolean canPlaySound() {
        return !this.rocket.isSilent();
    }
}