package net.hellomouse.alexscavesenriched.forge;

import net.hellomouse.alexscavesenriched.AlexsCavesEnriched;
import net.hellomouse.alexscavesenriched.item.DeadmanSwitchItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = AlexsCavesEnriched.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PlayerDeathEventHandler {
    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof PlayerEntity player)) return;
        for (var item : player.getInventory().main)
            if (item.getItem() instanceof DeadmanSwitchItem && (DeadmanSwitchItem.isActive(item)))
                DeadmanSwitchItem.detonate(player.getWorld(), player, item);
        for (var item : player.getInventory().offHand)
            if (item.getItem() instanceof DeadmanSwitchItem && (DeadmanSwitchItem.isActive(item)))
                DeadmanSwitchItem.detonate(player.getWorld(), player, item);
    }
}
