package net.hellomouse.alexscavesenriched.forge;

import com.github.alexmodguy.alexscaves.server.block.ACBlockRegistry;
import net.hellomouse.alexscavesenriched.ACEBlockRegistry;
import net.hellomouse.alexscavesenriched.AlexsCavesEnriched;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.piston.PistonMovingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = AlexsCavesEnriched.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SheepFumoEventHandler {
    @SubscribeEvent
    public static void onNeighborNotify(BlockEvent.NeighborNotifyEvent event) {
        BlockPos pos = event.getPos();
        Level level = (Level) event.getLevel();
        BlockState state = level.getBlockState(pos);

        if (state.getBlock() instanceof PistonBaseBlock && state.getValue(PistonBaseBlock.EXTENDED)) {
            var pistonDir = state.getValue(PistonBaseBlock.FACING);
            BlockEntity blockEntity = level.getBlockEntity(pos.relative(pistonDir, 2));
            if (blockEntity instanceof PistonMovingBlockEntity pistonBlockEntity && pistonBlockEntity.getMovedState().getBlock() == ACBlockRegistry.URANIUM_ROD.get()) {
                List<Sheep> sheepList = level.getEntitiesOfClass(Sheep.class, new AABB(pos.relative(pistonDir, 2)).inflate(0.5), (Entity x) -> true);
                if (!sheepList.isEmpty()) {
                    Sheep unfortunateSheep = sheepList.get(0);
                    level.playSound(null, unfortunateSheep.blockPosition(), SoundEvents.SHEEP_HURT, SoundSource.NEUTRAL, 1.0F, 1.0F);

                    if (level.random.nextInt() % 64 == 0)
                        explodeSheep(unfortunateSheep, level, pos.relative(pistonDir, 2));
                }
            }
        }
    }

    public static void explodeSheep(Sheep sheep, Level level, BlockPos pos) {
        if (!level.isClientSide) {
            level.explode(sheep, sheep.getX(), sheep.getY(), sheep.getZ(), 0.0F, Level.ExplosionInteraction.MOB);
            sheep.remove(Entity.RemovalReason.KILLED);

            ItemStack stack = level.getRandom().nextInt() % 2 == 0 ?
                    new ItemStack(ACEBlockRegistry.XIAOYU_FUMO.get()) :
                    new ItemStack(ACEBlockRegistry.XENO_FUMO.get());
            ItemEntity itemEntity = new ItemEntity(
                    level,
                    pos.getX() + 0.5,
                    pos.getY() + 0.5,
                    pos.getZ() + 0.5,
                    stack
            );
            level.addFreshEntity(itemEntity);
        }
    }
}
