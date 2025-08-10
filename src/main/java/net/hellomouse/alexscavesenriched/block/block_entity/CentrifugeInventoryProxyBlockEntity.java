package net.hellomouse.alexscavesenriched.block.block_entity;

import net.hellomouse.alexscavesenriched.ACEBlockEntityRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Set;
import java.util.function.Predicate;

public class CentrifugeInventoryProxyBlockEntity extends LockableContainerBlockEntity implements SidedInventory {
    private BlockPos targetPos = null;

    public CentrifugeInventoryProxyBlockEntity(BlockPos pos, BlockState state) {
        super(ACEBlockEntityRegistry.CENTRIFUGE_PROXY.get(), pos, state);
    }

    public static void tick(World level, BlockPos blockPos, BlockState state, CentrifugeInventoryProxyBlockEntity entity) {}

    public void setTargetPos(BlockPos pos) {
        this.targetPos = pos;
        markDirty();
    }

    public BlockPos getTargetPos() {
        return targetPos;
    }

    private Inventory getTargetInventory() {
        if (world == null || targetPos == null) return null;
        BlockEntity be = world.getBlockEntity(targetPos);
        if (be instanceof Inventory container)
            return container;
        return null;
    }

    @Override
    public int size() {
        Inventory target = getTargetInventory();
        return target != null ? target.size() : 0;
    }

    @Override
    public boolean isEmpty() {
        Inventory target = getTargetInventory();
        return target == null || target.isEmpty();
    }

    @Override
    public ItemStack getStack(int slot) {
        Inventory target = getTargetInventory();
        return target != null ? target.getStack(slot) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeStack(int slot, int count) {
        Inventory target = getTargetInventory();
        if (target != null) {
            markDirty();
            return target.removeStack(slot, count);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeStack(int slot) {
        Inventory target = getTargetInventory();
        if (target != null) {
            markDirty();
            return target.removeStack(slot);
        }
        return ItemStack.EMPTY;
    }

    @Override
    protected Text getContainerName() {
        return Text.literal("Proxy");
    }

    @Override
    protected ScreenHandler createScreenHandler(int syncId, PlayerInventory playerInventory) {
        return null;
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        Inventory target = getTargetInventory();
        if (target != null) {
            target.setStack(slot, stack);
            markDirty();
        }
    }

    @Override
    public void clear() {
        Inventory target = getTargetInventory();
        if (target != null) target.clear();
    }

    @Override
    public int getMaxCountPerStack() {
        Inventory target = getTargetInventory();
        if (target == null)
            return 0;
        return target.getMaxCountPerStack();
    }

    @Override
    public void markDirty() {
        Inventory target = getTargetInventory();
        if (target != null && world != null) {
            target.markDirty();
            world.updateComparators(this.getPos(), world.getBlockState(this.getPos()).getBlock());
        }
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        Inventory target = getTargetInventory();
        return target != null && target.canPlayerUse(player);
    }

    @Override
    public void onOpen(PlayerEntity player) {
        Inventory target = getTargetInventory();
        if (target != null)  target.onOpen(player);
    }

    @Override
    public void onClose(PlayerEntity player) {
        Inventory target = getTargetInventory();
        if (target != null) target.onClose(player);
    }

    @Override
    public boolean isValid(int slot, ItemStack stack) {
        Inventory target = getTargetInventory();
        return target != null && target.isValid(slot, stack);
    }

    @Override
    public boolean canTransferTo(Inventory hopperInventory, int slot, ItemStack stack) {
        Inventory target = getTargetInventory();
        return target != null && target.canTransferTo(hopperInventory, slot, stack);
    }

    @Override
    public int count(Item item) {
        Inventory target = getTargetInventory();
        if (target == null) return 0;
        return target.count(item);
    }

    @Override
    public boolean containsAny(Set<Item> items) {
        Inventory target = getTargetInventory();
        return target != null && target.containsAny(items);
    }

    @Override
    public boolean containsAny(Predicate<ItemStack> predicate) {
        Inventory target = getTargetInventory();
        return target != null && target.containsAny(predicate);
    }

    // SidedInventory

    @Override
    public int[] getAvailableSlots(Direction side) {
        BlockEntity be = getTargetBlockEntity();
        if (be instanceof SidedInventory sided)
            return sided.getAvailableSlots(side);
        return new int[0];
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
        BlockEntity be = getTargetBlockEntity();
        if (be instanceof SidedInventory sided)
            return sided.canInsert(slot, stack, dir);
        return false;
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        BlockEntity be = getTargetBlockEntity();
        if (be instanceof SidedInventory sided)
            return sided.canExtract(slot, stack, dir);
        return false;
    }

    private BlockEntity getTargetBlockEntity() {
        if (world == null || targetPos == null) return null;
        return world.getBlockEntity(targetPos);
    }

    @Override
    protected void writeNbt(NbtCompound tag) {
        super.writeNbt(tag);
        if (targetPos != null) {
            tag.putInt("TargetX", targetPos.getX());
            tag.putInt("TargetY", targetPos.getY());
            tag.putInt("TargetZ", targetPos.getZ());
        }
    }

    @Override
    public void readNbt(NbtCompound tag) {
        super.readNbt(tag);
        if (tag.contains("TargetX") && tag.contains("TargetY") && tag.contains("TargetZ"))
            targetPos = new BlockPos(tag.getInt("TargetX"), tag.getInt("TargetY"), tag.getInt("TargetZ"));
    }

    public @NotNull NbtCompound toInitialChunkDataNbt() {
        var tag = super.toInitialChunkDataNbt();
        writeNbt(tag);
        return tag;
    }

    @Override
    public @org.jetbrains.annotations.Nullable Packet<ClientPlayPacketListener> toUpdatePacket() {
        var nbt = new NbtCompound();
        writeNbt(nbt);
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public void onDataPacket(ClientConnection net, BlockEntityUpdateS2CPacket pkt) {
        super.onDataPacket(net, pkt);
        assert pkt.getNbt() != null;
        readNbt(pkt.getNbt());
    }

    @Override
    public void handleUpdateTag(NbtCompound tag) {
        super.handleUpdateTag(tag);
        readNbt(tag);
    }
}
