package net.hellomouse.alexscavesenriched.item;

import com.github.alexmodguy.alexscaves.server.item.ACItemRegistry;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

public class EnrichedUraniumNuggetItem extends Item {
    public EnrichedUraniumNuggetItem() {
        super(new Properties()
                .stacksTo(64)
                .rarity(Rarity.UNCOMMON)
                .fireResistant()
                .rarity(ACItemRegistry.RARITY_NUCLEAR));
    }
}
