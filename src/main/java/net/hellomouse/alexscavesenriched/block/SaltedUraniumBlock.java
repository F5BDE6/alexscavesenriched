package net.hellomouse.alexscavesenriched.block;


import com.github.alexmodguy.alexscaves.client.particle.ACParticleRegistry;
import com.github.alexmodguy.alexscaves.server.block.ACSoundTypes;
import com.github.alexmodguy.alexscaves.server.misc.ACSoundRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.MapColor;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class SaltedUraniumBlock extends Block {
    public SaltedUraniumBlock() {
        super(Settings.create()
                .mapColor(MapColor.LIME)
                .strength(3.5F)
                .luminance((state) -> 4)
                .emissiveLighting((state, level, pos) -> true).sounds(ACSoundTypes.URANIUM));
    }

    public void randomDisplayTick(BlockState state, World level, BlockPos pos, Random randomSource) {
        if (randomSource.nextInt(80) == 0) {
            level.playSound((double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5,
                    ACSoundRegistry.URANIUM_HUM.get(), SoundCategory.BLOCKS, 0.5F, randomSource.nextFloat() * 0.4F + 0.8F, false);
        }
        if (randomSource.nextInt(13) == 0) {
            Vec3d center = Vec3d.ofCenter(pos, 0.5);
            level.addParticle(ACParticleRegistry.PROTON.get(), center.x, center.y, center.z, center.x, center.y, center.z);
        }
    }
}
