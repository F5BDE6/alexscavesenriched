package net.hellomouse.alexscavesenriched.block.centrifuge;

import com.github.alexmodguy.alexscaves.server.block.ACSoundTypes;
import net.hellomouse.alexscavesenriched.ACEBlockRegistry;
import net.hellomouse.alexscavesenriched.block.block_entity.CentrifugeInventoryProxyBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import java.util.ArrayList;
import java.util.Optional;

public class CentrifugeUtil {
    public static int CENTRIFUGE_HEIGHT = 5; // Height including base

    public static BlockBehaviour.Properties getBlockSettings() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .requiresCorrectToolForDrops()
                .noOcclusion()
                .forceSolidOn()
                .strength(5, 3)
                .sound(ACSoundTypes.METAL_SCAFFOLDING);
    }

    // Try to assemble the multi block
    public static Optional<ArrayList<BlockPos>> getMultiBlockPositions(Level world, BlockPos pos, boolean isTop) {
        BlockPos.MutableBlockPos carve = new BlockPos.MutableBlockPos();
        BlockPos baseBlockPos = isTop ? null : pos;

        for (int dy = 0; dy < CENTRIFUGE_HEIGHT; dy++) {
            carve.set(pos.getX(), pos.getY() - dy, pos.getZ());
            BlockState state = world.getBlockState(carve);
            if (state.getBlock() instanceof CentrifugeBaseBlock) {
                baseBlockPos = new BlockPos(carve.getX(), carve.getY(), carve.getZ());
                break;
            }
        }

        if (baseBlockPos == null) // Failed to find base block
            return Optional.empty();
        for (int dy = 1; dy < CENTRIFUGE_HEIGHT; dy++) {
            carve.set(pos.getX(), baseBlockPos.getY() + dy, pos.getZ());
            if (isTop && carve.equals(pos))
                continue;
            BlockState state = world.getBlockState(carve);
            if (!(state.getBlock() instanceof CentrifugeTopBlock))
                return Optional.empty();
        }

        ArrayList<BlockPos> out = new ArrayList<>();
        out.add(baseBlockPos);
        for (int dy = 1; dy < CENTRIFUGE_HEIGHT; dy++)
            out.add(new BlockPos(pos.getX(), baseBlockPos.getY() + dy, pos.getZ()));
        return Optional.of(out);
    }

    public static void assembleMultiBlock(Level world, BlockPos pos, boolean isTop) {
        var blocks = getMultiBlockPositions(world, pos, isTop);
        if (blocks.isEmpty()) return;

        world.setBlockAndUpdate(blocks.get().get(0), ACEBlockRegistry.CENTRIFUGE.get().defaultBlockState());
        for (int i = 1; i < blocks.get().size(); i++) {
            var carve = blocks.get().get(i);
            world.setBlockAndUpdate(carve, i < blocks.get().size() - 1 ?
                    ACEBlockRegistry.CENTRIFUGE_PROXY.get().defaultBlockState() :
                    ACEBlockRegistry.CENTRIFUGE_PROXY.get().defaultBlockState().setValue(CentrifugeMultiBlockProxyBlock.IS_TOP, true));
            var be = world.getBlockEntity(carve);
            if (be instanceof CentrifugeInventoryProxyBlockEntity proxy)
                proxy.setTargetPos(blocks.get().get(0));
        }
    }

    public static void breakMultiBlockFromBase(Level world, BlockPos pos, boolean forceDrop) {
        // Spawn multiblock components
        if (forceDrop || world.getBlockState(pos).getBlock() instanceof CentrifugeMultiBlockBaseBlock) {
            Containers.dropItemStack(world, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(ACEBlockRegistry.CENTRIFUGE_BASE.get()));
            for (int i = 0; i < CENTRIFUGE_HEIGHT - 1; i++)
                Containers.dropItemStack(world, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(ACEBlockRegistry.CENTRIFUGE_TOP.get()));
        }

        BlockPos.MutableBlockPos carve = new BlockPos.MutableBlockPos();
        for (int dy = 0; dy < CENTRIFUGE_HEIGHT; dy++) {
            carve.set(pos.getX(), pos.getY() + dy, pos.getZ());
            BlockState state = world.getBlockState(carve);
            if (state.getBlock() instanceof CentrifugeMultiBlockBaseBlock || state.getBlock() instanceof CentrifugeMultiBlockProxyBlock)
                world.removeBlock(carve, false);
        }
    }
}
