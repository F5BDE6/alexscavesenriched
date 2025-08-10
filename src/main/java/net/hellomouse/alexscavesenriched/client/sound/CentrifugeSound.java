package net.hellomouse.alexscavesenriched.client.sound;

import net.hellomouse.alexscavesenriched.block.block_entity.CentrifugeBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.MovingSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CentrifugeSound extends MovingSoundInstance {
    protected final CentrifugeBlockEntity centrifugeBlockEntity;

    public CentrifugeSound(CentrifugeBlockEntity centrifuge, SoundEvent soundEvent, SoundCategory soundSource) {
        super(soundEvent, soundSource, SoundInstance.createRandom());
        this.centrifugeBlockEntity = centrifuge;
        this.x = (float)centrifuge.getPos().getX();
        this.y = (float)centrifuge.getPos().getY();
        this.z = (float)centrifuge.getPos().getZ();
        this.repeat = true;
        this.repeatDelay = 0;
        this.volume = 0.0F;
    }

    public void tick() {
        if (!this.centrifugeBlockEntity.isRemoved()) {
            this.x = (float)this.centrifugeBlockEntity.getPos().getX();
            this.y = (float)this.centrifugeBlockEntity.getPos().getY();
            this.z = (float)this.centrifugeBlockEntity.getPos().getZ();

            final double soundRange = 32.0F;
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
            float spinPercent = (float)this.centrifugeBlockEntity.getSpinSpeed() / CentrifugeBlockEntity.MAX_SPIN_SPEED;
            this.volume = (float)(1.0 - Math.pow(playerDistance / soundRange, 2)) * spinPercent;
            this.pitch = 1.0F + spinPercent * 0.8F;

        } else {
            this.setDone();
        }
    }

    public boolean shouldAlwaysPlay() { return true; }
}