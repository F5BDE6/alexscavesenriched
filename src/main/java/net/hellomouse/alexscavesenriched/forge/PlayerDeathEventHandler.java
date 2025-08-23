package net.hellomouse.alexscavesenriched.forge;

import net.hellomouse.alexscavesenriched.AlexsCavesEnriched;
import net.hellomouse.alexscavesenriched.item.DeadmanSwitchItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = AlexsCavesEnriched.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PlayerDeathEventHandler {
    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        for (var item : player.getInventory().items)
            if (item.getItem() instanceof DeadmanSwitchItem && (DeadmanSwitchItem.isActive(item)))
                DeadmanSwitchItem.detonate(player.level(), player, item);
        for (var item : player.getInventory().offhand)
            if (item.getItem() instanceof DeadmanSwitchItem && (DeadmanSwitchItem.isActive(item)))
                DeadmanSwitchItem.detonate(player.level(), player, item);
    }
}
