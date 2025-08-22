package net.hellomouse.alexscavesenriched.block.block_entity;

import net.hellomouse.alexscavesenriched.ACEBlockEntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Set;
import java.util.function.Predicate;

public class CentrifugeInventoryProxyBlockEntity extends BaseContainerBlockEntity implements WorldlyContainer {
    private BlockPos targetPos = null;

    public CentrifugeInventoryProxyBlockEntity(BlockPos pos, BlockState state) {
        super(ACEBlockEntityRegistry.CENTRIFUGE_PROXY.get(), pos, state);
    }

    public static void tick(Level level, BlockPos blockPos, BlockState state, CentrifugeInventoryProxyBlockEntity entity) {
    }

    public void setTargetPos(BlockPos pos) {
        this.targetPos = pos;
        setChanged();
    }

    public BlockPos getTargetPos() {
        return targetPos;
    }

    private Container getTargetInventory() {
        if (level == null || targetPos == null) return null;
        BlockEntity be = level.getBlockEntity(targetPos);
        if (be instanceof Container container)
            return container;
        return null;
    }

    @Override
    public int getContainerSize() {
        Container target = getTargetInventory();
        return target != null ? target.getContainerSize() : 0;
    }

    @Override
    public boolean isEmpty() {
        Container target = getTargetInventory();
        return target == null || target.isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        Container target = getTargetInventory();
        return target != null ? target.getItem(slot) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int slot, int count) {
        Container target = getTargetInventory();
        if (target != null) {
            setChanged();
            return target.removeItem(slot, count);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        Container target = getTargetInventory();
        if (target != null) {
            setChanged();
            return target.removeItemNoUpdate(slot);
        }
        return ItemStack.EMPTY;
    }

    @Override
    protected Component getDefaultName() {
        return Component.literal("Proxy");
    }

    @Override
    protected AbstractContainerMenu createMenu(int syncId, Inventory playerInventory) {
        return null;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        Container target = getTargetInventory();
        if (target != null) {
            target.setItem(slot, stack);
            setChanged();
        }
    }

    @Override
    public void clearContent() {
        Container target = getTargetInventory();
        if (target != null) target.clearContent();
    }

    @Override
    public int getMaxStackSize() {
        Container target = getTargetInventory();
        if (target == null)
            return 0;
        return target.getMaxStackSize();
    }

    @Override
    public void setChanged() {
        Container target = getTargetInventory();
        if (target != null && level != null) {
            target.setChanged();
            level.updateNeighbourForOutputSignal(this.getBlockPos(), level.getBlockState(this.getBlockPos()).getBlock());
        }
    }

    @Override
    public boolean stillValid(Player player) {
        Container target = getTargetInventory();
        return target != null && target.stillValid(player);
    }

    @Override
    public void startOpen(Player player) {
        Container target = getTargetInventory();
        if (target != null) target.startOpen(player);
    }

    @Override
    public void stopOpen(Player player) {
        Container target = getTargetInventory();
        if (target != null) target.stopOpen(player);
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        Container target = getTargetInventory();
        return target != null && target.canPlaceItem(slot, stack);
    }

    @Override
    public boolean canTakeItem(Container hopperInventory, int slot, ItemStack stack) {
        Container target = getTargetInventory();
        return target != null && target.canTakeItem(hopperInventory, slot, stack);
    }

    @Override
    public int countItem(Item item) {
        Container target = getTargetInventory();
        if (target == null) return 0;
        return target.countItem(item);
    }

    @Override
    public boolean hasAnyOf(Set<Item> items) {
        Container target = getTargetInventory();
        return target != null && target.hasAnyOf(items);
    }

    @Override
    public boolean hasAnyMatching(Predicate<ItemStack> predicate) {
        Container target = getTargetInventory();
        return target != null && target.hasAnyMatching(predicate);
    }

    // SidedInventory

    @Override
    public int[] getSlotsForFace(Direction side) {
        BlockEntity be = getTargetBlockEntity();
        if (be instanceof WorldlyContainer sided)
            return sided.getSlotsForFace(side);
        return new int[0];
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction dir) {
        BlockEntity be = getTargetBlockEntity();
        if (be instanceof WorldlyContainer sided)
            return sided.canPlaceItemThroughFace(slot, stack, dir);
        return false;
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction dir) {
        BlockEntity be = getTargetBlockEntity();
        if (be instanceof WorldlyContainer sided)
            return sided.canTakeItemThroughFace(slot, stack, dir);
        return false;
    }

    private BlockEntity getTargetBlockEntity() {
        if (level == null || targetPos == null) return null;
        return level.getBlockEntity(targetPos);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (targetPos != null) {
            tag.putInt("TargetX", targetPos.getX());
            tag.putInt("TargetY", targetPos.getY());
            tag.putInt("TargetZ", targetPos.getZ());
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("TargetX") && tag.contains("TargetY") && tag.contains("TargetZ"))
            targetPos = new BlockPos(tag.getInt("TargetX"), tag.getInt("TargetY"), tag.getInt("TargetZ"));
    }

    public @NotNull CompoundTag getUpdateTag() {
        var tag = super.getUpdateTag();
        saveAdditional(tag);
        return tag;
    }

    @Override
    public @org.jetbrains.annotations.Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        var nbt = new CompoundTag();
        saveAdditional(nbt);
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        super.onDataPacket(net, pkt);
        assert pkt.getTag() != null;
        load(pkt.getTag());
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
        load(tag);
    }
}
