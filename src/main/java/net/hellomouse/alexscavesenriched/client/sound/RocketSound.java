package net.hellomouse.alexscavesenriched.client.sound;

import net.hellomouse.alexscavesenriched.entity.RocketEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.MovingSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RocketSound extends MovingSoundInstance {
    protected final RocketEntity rocket;

    public RocketSound(RocketEntity rocket, SoundEvent soundEvent, SoundCategory soundSource) {
        super(soundEvent, soundSource, SoundInstance.createRandom());
        this.rocket = rocket;
        this.x = (float)rocket.getX();
        this.y = (float)rocket.getY();
        this.z = (float)rocket.getZ();
        this.repeat = true;
        this.repeatDelay = 0;
        this.volume = 0.0F;
    }

    public void tick() {
        if (!this.rocket.isRemoved()) {
            this.x = (float)this.rocket.getX();
            this.y = (float)this.rocket.getY();
            this.z = (float)this.rocket.getZ();

            final double soundRange = 32.0F;
            var player = MinecraftClient.getInstance().player;
            if (player == null)
                return;
            if (this.rocket.getVelocity().length() < 0.5) {
                this.volume = 0.0F;
                return;
            }

            var thisPos = new Vec3d(this.x, this.y, this.z);
            var playerDistance = player.getLerpedPos(1.0F).distanceTo(thisPos);
            if (playerDistance > soundRange) {
                this.pitch = 0.0F;
                this.volume = 0.0F;
                return;
            }

            Vec3d thisFuturePos = thisPos.add(this.rocket.getVelocity());
            Vec3d playerFuturePos = player.getLerpedPos(1.0F).add(player.getVelocity());
            final double deltaDistance = thisFuturePos.distanceTo(playerFuturePos) - thisPos.distanceTo(player.getLerpedPos(1.0F));
            float pitchDelta = MathHelper.getLerpProgress((float)deltaDistance / 3.0F, -1.0F, 1.0F);

            this.pitch = MathHelper.lerp(
                    MathHelper.clamp(1.0F - pitchDelta, 0.0F, 1.0F),
                    this.getMinPitch(), this.getMaxPitch()
            );
            this.volume = 1.0F;
        } else {
            this.setDone();
        }
    }

    private float getMinPitch() { return 0.5F; }
    private float getMaxPitch() { return 1.5F; }
    public boolean shouldAlwaysPlay() { return true; }

    public boolean canPlay() {
        return !this.rocket.isSilent();
    }
}