package net.hellomouse.alexscavesenriched.block.centrifuge;

import net.hellomouse.alexscavesenriched.ACEBlockRegistry;
import net.hellomouse.alexscavesenriched.block.block_entity.CentrifugeInventoryProxyBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Optional;

public class CentrifugeUtil {
    public static int CENTRIFUGE_HEIGHT = 5; // Height including base

    // Try to assemble the multi block
    public static Optional<ArrayList<BlockPos>> getMultiBlockPositions(World world, BlockPos pos, boolean isTop) {
        BlockPos.Mutable carve = new BlockPos.Mutable();
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

    public static void assembleMultiBlock(World world, BlockPos pos, boolean isTop) {
        var blocks = getMultiBlockPositions(world, pos, isTop);
        if (blocks.isEmpty()) return;

        world.setBlockState(blocks.get().get(0), ACEBlockRegistry.CENTRIFUGE.get().getDefaultState());
        for (int i = 1; i < blocks.get().size(); i++) {
            var carve = blocks.get().get(i);
            world.setBlockState(carve, i < blocks.get().size() - 1 ?
                    ACEBlockRegistry.CENTRIFUGE_PROXY.get().getDefaultState() :
                    ACEBlockRegistry.CENTRIFUGE_PROXY.get().getDefaultState().with(CentrifugeMultiBlockProxyBlock.IS_TOP, true));
            var be = world.getBlockEntity(carve);
            if (be instanceof CentrifugeInventoryProxyBlockEntity proxy)
                proxy.setTargetPos(blocks.get().get(0));
        }
    }

    public static void breakMultiBlockFromBase(World world, BlockPos pos, boolean forceDrop) {
        // Spawn multiblock components
        if (forceDrop || world.getBlockState(pos).getBlock() instanceof CentrifugeMultiBlockBaseBlock) {
            ItemScatterer.spawn(world, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(ACEBlockRegistry.CENTRIFUGE_BASE.get()));
            for (int i = 0; i < CENTRIFUGE_HEIGHT - 1; i++)
                ItemScatterer.spawn(world, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(ACEBlockRegistry.CENTRIFUGE_TOP.get()));
        }

        BlockPos.Mutable carve = new BlockPos.Mutable();
        for (int dy = 0; dy < CENTRIFUGE_HEIGHT; dy++) {
            carve.set(pos.getX(), pos.getY() + dy, pos.getZ());
            BlockState state = world.getBlockState(carve);
            if (state.getBlock() instanceof CentrifugeMultiBlockBaseBlock || state.getBlock() instanceof CentrifugeMultiBlockProxyBlock)
                world.removeBlock(carve, false);
        }
    }
}
