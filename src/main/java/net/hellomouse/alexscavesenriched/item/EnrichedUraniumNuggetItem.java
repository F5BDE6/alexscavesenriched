package net.hellomouse.alexscavesenriched.item;

import com.github.alexmodguy.alexscaves.server.item.ACItemRegistry;
import net.minecraft.item.Item;
import net.minecraft.util.Rarity;

public class EnrichedUraniumNuggetItem extends Item {
    public EnrichedUraniumNuggetItem() {
        super(new Settings()
                .maxCount(64)
                .rarity(Rarity.UNCOMMON)
                .fireproof()
                .rarity(ACItemRegistry.RARITY_NUCLEAR));
    }
}
