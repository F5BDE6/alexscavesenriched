package net.hellomouse.alexscavesenriched.client.sound;

import D;
import net.hellomouse.alexscavesenriched.entity.BlackHoleEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BlackHoleSound extends AbstractTickableSoundInstance {
    protected final BlackHoleEntity blackHole;

    public BlackHoleSound(BlackHoleEntity blackhole, SoundEvent soundEvent, SoundSource soundSource) {
        super(soundEvent, soundSource, SoundInstance.createUnseededRandom());
        this.blackHole = blackhole;
        this.x = (float)blackhole.getX();
        this.y = (float)blackhole.getY();
        this.z = (float)blackhole.getZ();
        this.looping = true;
        this.delay = 0;
        this.volume = 0.0F;
    }

    public void tick() {
        if (!this.blackHole.isRemoved()) {
            this.x = (float)this.blackHole.getX();
            this.y = (float)this.blackHole.getY();
            this.z = (float)this.blackHole.getZ();

            final double soundRange = 96.0F;
            var player = Minecraft.getInstance().player;
            if (player == null)
                return;

            var thisPos = new Vec3(this.x, this.y, this.z);
            var playerDistance = player.getPosition(1.0F).distanceTo(thisPos);
            if (playerDistance > soundRange) {
                this.pitch = 0.0F;
                this.volume = 0.0F;
                return;
            }
            this.volume = (float)(1.0 - Math.pow(playerDistance / soundRange, 2));
        } else {
            this.stop();
        }
    }

    public boolean canStartSilent() {
        return true;
    }

    public boolean canPlaySound() {
        return !this.blackHole.isSilent();
    }
}