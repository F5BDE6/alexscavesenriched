package net.hellomouse.alexscavesenriched.inventory;

import net.hellomouse.alexscavesenriched.ACEMenuRegistry;
import net.hellomouse.alexscavesenriched.block.block_entity.CentrifugeBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

import java.util.Objects;

public class CentrifugeBlockMenu extends ScreenHandler {
    private final Inventory inventory;

    public CentrifugeBlockMenu(int syncId, PlayerInventory playerInv, Inventory blockInv) {
        super(ACEMenuRegistry.CENTRIFUGE.get(), syncId);
        this.inventory = blockInv;
        checkSize(blockInv, 18);
        blockInv.onOpen(playerInv.player);

        // Input slots (0–8)
        for (int row = 0; row < 3; ++row)
            for (int col = 0; col < 3; ++col)
                this.addSlot(new Slot(blockInv, col + row * 3, 8 + col * 18, 16 + row * 18) {
                    @Override
                    public boolean canInsert(ItemStack stack) {
                        return CentrifugeBlockEntity.inputAllowed(stack.getItem(), playerInv.player.getWorld());
                    }
                });

        // Output slots (9–17)
        for (int row = 0; row < 3; ++row)
            for (int col = 0; col < 3; ++col)
                this.addSlot(new Slot(blockInv, 9 + col + row * 3, 97 + col * 18, 16 + row * 18) {
                    @Override
                    public boolean canInsert(ItemStack stack) {
                        return CentrifugeBlockEntity.inputAllowed(stack.getItem(), playerInv.player.getWorld());
                    }
                });

        // Player inventory
        for (int row = 0; row < 3; ++row)
            for (int col = 0; col < 9; ++col)
                this.addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));

        // Hotbar
        for (int col = 0; col < 9; ++col)
            this.addSlot(new Slot(playerInv, col, 8 + col * 18, 142));
    }

    public CentrifugeBlockMenu(int syncId, PlayerInventory playerInv) {
        this(syncId, playerInv, new SimpleInventory(18));
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slotIndex) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(slotIndex);
        if (slot.hasStack()) {
            ItemStack slotItemStack = slot.getStack();
            newStack = slotItemStack.copy();

            if (slotIndex < CentrifugeBlockEntity.N_SLOTS) { // Item -> player inv
                if (!this.insertItem(slotItemStack, CentrifugeBlockEntity.N_SLOTS, this.slots.size(), false))
                    return ItemStack.EMPTY;
            } else { // player inv -> input
                if (!CentrifugeBlockEntity.inputAllowed(slotItemStack.getItem(), Objects.requireNonNull(player.getWorld())))
                    return ItemStack.EMPTY;
                if (!this.insertItem(slotItemStack, 0, CentrifugeBlockEntity.N_INPUT_SLOTS, false))
                    return ItemStack.EMPTY;
            }
            if (slotItemStack.isEmpty()) { slot.setStack(ItemStack.EMPTY); }
            else { slot.markDirty(); }
        }
        return newStack;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return this.inventory.canPlayerUse(player);
    }

    public Inventory getInventory() { return inventory; }
}

