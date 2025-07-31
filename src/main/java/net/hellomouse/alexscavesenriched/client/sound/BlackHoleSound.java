package net.hellomouse.alexscavesenriched.client.sound;

import net.hellomouse.alexscavesenriched.entity.BlackHoleEntity;
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
public class BlackHoleSound extends MovingSoundInstance {
    protected final BlackHoleEntity blackHole;

    public BlackHoleSound(BlackHoleEntity blackhole, SoundEvent soundEvent, SoundCategory soundSource) {
        super(soundEvent, soundSource, SoundInstance.createRandom());
        this.blackHole = blackhole;
        this.x = (float)blackhole.getX();
        this.y = (float)blackhole.getY();
        this.z = (float)blackhole.getZ();
        this.repeat = true;
        this.repeatDelay = 0;
        this.volume = 0.0F;
    }

    public void tick() {
        if (!this.blackHole.isRemoved()) {
            this.x = (float)this.blackHole.getX();
            this.y = (float)this.blackHole.getY();
            this.z = (float)this.blackHole.getZ();

            final double soundRange = 96.0F;
            var player = MinecraftClient.getInstance().player;
            if (player == null)
                return;

            var thisPos = new Vec3d(this.x, this.y, this.z);
            var playerDistance = player.getLerpedPos(1.0F).distanceTo(thisPos);
            if (playerDistance > soundRange) {
                this.pitch = 0.0F;
                this.volume = 0.0F;
                return;
            }
            this.volume = (float)(1.0 - Math.pow(playerDistance / soundRange, 2));
        } else {
            this.setDone();
        }
    }

    public boolean shouldAlwaysPlay() { return true; }
    public boolean canPlay() {
        return !this.blackHole.isSilent();
    }
}