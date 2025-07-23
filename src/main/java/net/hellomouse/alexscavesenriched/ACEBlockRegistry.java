package net.hellomouse.alexscavesenriched;

import com.github.alexmodguy.alexscaves.server.item.*;

import com.github.alexthe666.citadel.item.BlockItemWithSupplier;
import net.hellomouse.alexscavesenriched.block.EnrichedUraniumBlock;
import net.hellomouse.alexscavesenriched.block.EnrichedUraniumRodBlock;
import net.hellomouse.alexscavesenriched.block.NukeGlowingAir;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.Rarity;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class ACEBlockRegistry {
    public static final DeferredRegister<Block> DEF_REG = DeferredRegister.create(ForgeRegistries.BLOCKS, AlexsCavesEnriched.MODID);
    public static final RegistryObject<Block> ENRICHED_URANIUM = registerBlockAndItem("block_of_enriched_uranium", EnrichedUraniumBlock::new, 9);
    public static final RegistryObject<Block> ENRICHED_URANIUM_ROD = registerBlockAndItem("enriched_uranium_rod", EnrichedUraniumRodBlock::new, 9);
    public static final RegistryObject<Block> NUKE_GLOWING_AIR = DEF_REG.register("nuke_glowing_air", NukeGlowingAir::new);

    private static RegistryObject<Block> registerBlockAndItem(String name, Supplier<Block> block) {
        return registerBlockAndItem(name, block, 0);
    }

    private static RegistryObject<Block> registerBlockAndItem(String name, Supplier<Block> block, int itemType) {
        RegistryObject<Block> blockObj = DEF_REG.register(name, block);
        ACEItemRegistry.DEF_REG.register(name, getBlockSupplier(itemType, blockObj));
        return blockObj;
    }

    private static Supplier<? extends BlockItemWithSupplier> getBlockSupplier(int itemType, RegistryObject<Block> blockObj) {
        return switch (itemType) {
            case 1 -> () -> new BlockItemWithSupplierLore(blockObj, new Item.Settings());
            case 2 -> () -> new BlockItemWithScaffolding(blockObj, new Item.Settings());
            case 4 -> () -> new RadioactiveBlockItem(blockObj, new Item.Settings(), 0.001F);
            case 5 -> () -> new RadioactiveOnDestroyedBlockItem(blockObj, new Item.Settings(), 0.01F);
            case 6 -> () -> new BlockItemWithSupplier(blockObj, (new Item.Settings()).rarity(Rarity.UNCOMMON));
            case 7 -> () -> new BlockItemWithSupplier(blockObj, (new Item.Settings()).rarity(Rarity.UNCOMMON).fireproof());
            case 8 -> () -> new BlockItemWithSupplier(blockObj, (new Item.Settings()).rarity(Rarity.UNCOMMON).fireproof().rarity(ACItemRegistry.RARITY_NUCLEAR));
            case 9 -> () -> new RadioactiveBlockItem(blockObj, (new Item.Settings()).rarity(Rarity.UNCOMMON).fireproof().rarity(ACItemRegistry.RARITY_NUCLEAR), 0.001F);
            default -> () -> new BlockItemWithSupplier(blockObj, new Item.Settings());
        };
    }
}
