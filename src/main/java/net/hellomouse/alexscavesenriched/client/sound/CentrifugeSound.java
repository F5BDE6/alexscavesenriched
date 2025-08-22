package net.hellomouse.alexscavesenriched.client.sound;

import D;
import net.hellomouse.alexscavesenriched.block.block_entity.CentrifugeBlockEntity;
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
public class CentrifugeSound extends AbstractTickableSoundInstance {
    protected final CentrifugeBlockEntity centrifugeBlockEntity;

    public CentrifugeSound(CentrifugeBlockEntity centrifuge, SoundEvent soundEvent, SoundSource soundSource) {
        super(soundEvent, soundSource, SoundInstance.createUnseededRandom());
        this.centrifugeBlockEntity = centrifuge;
        this.x = (float) centrifuge.getBlockPos().getX();
        this.y = (float) centrifuge.getBlockPos().getY();
        this.z = (float) centrifuge.getBlockPos().getZ();
        this.looping = true;
        this.delay = 0;
        this.volume = 0.0F;
    }

    public void tick() {
        if (!this.centrifugeBlockEntity.isRemoved()) {
            this.x = (float) this.centrifugeBlockEntity.getBlockPos().getX();
            this.y = (float) this.centrifugeBlockEntity.getBlockPos().getY();
            this.z = (float) this.centrifugeBlockEntity.getBlockPos().getZ();

            final double soundRange = 32.0F;
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
            float spinPercent = (float)this.centrifugeBlockEntity.getSpinSpeed() / CentrifugeBlockEntity.MAX_SPIN_SPEED;
            this.volume = (float)(1.0 - Math.pow(playerDistance / soundRange, 2)) * spinPercent;
            this.pitch = 1.0F + spinPercent * 0.8F;

        } else {
            this.stop();
        }
    }

    public boolean canStartSilent() {
        return true;
    }
}