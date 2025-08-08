package net.hellomouse.alexscavesenriched.forge;

import com.github.alexmodguy.alexscaves.server.block.ACBlockRegistry;
import net.hellomouse.alexscavesenriched.ACEBlockRegistry;
import net.hellomouse.alexscavesenriched.AlexsCavesEnriched;
import net.minecraft.block.BlockState;
import net.minecraft.block.PistonBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.PistonBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = AlexsCavesEnriched.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SheepFumoEventHandler {
    @SubscribeEvent
    public static void onNeighborNotify(BlockEvent.NeighborNotifyEvent event) {
        BlockPos pos = event.getPos();
        World level = (World) event.getLevel();
        BlockState state = level.getBlockState(pos);

        if (state.getBlock() instanceof PistonBlock && state.get(PistonBlock.EXTENDED)) {
            var pistonDir = state.get(PistonBlock.FACING);
            BlockEntity blockEntity = level.getBlockEntity(pos.offset(pistonDir, 2));
            if (blockEntity instanceof PistonBlockEntity pistonBlockEntity && pistonBlockEntity.getPushedBlock().getBlock() == ACBlockRegistry.URANIUM_ROD.get()) {
                List<SheepEntity> sheepList = level.getEntitiesByClass(SheepEntity.class, new Box(pos.offset(pistonDir, 2)).expand(0.5), (Entity x) -> true);
                if (!sheepList.isEmpty()) {
                    SheepEntity unfortunateSheep = sheepList.get(0);
                    level.playSound(null, unfortunateSheep.getBlockPos(), SoundEvents.ENTITY_SHEEP_HURT, SoundCategory.NEUTRAL, 1.0F, 1.0F);

                    if (level.random.nextInt() % 64 == 0)
                        explodeSheep(unfortunateSheep, level, pos.offset(pistonDir, 2));
                }
            }
        }
    }

    public static void explodeSheep(SheepEntity sheep, World level, BlockPos pos) {
        if (!level.isClient) {
            level.createExplosion(sheep, sheep.getX(), sheep.getY(), sheep.getZ(), 0.0F, World.ExplosionSourceType.MOB);
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
            level.spawnEntity(itemEntity);
        }
    }
}
