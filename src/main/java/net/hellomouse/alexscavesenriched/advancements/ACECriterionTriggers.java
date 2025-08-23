package net.hellomouse.alexscavesenriched.advancements;

import com.github.alexmodguy.alexscaves.server.misc.ACAdvancementTrigger;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.resources.ResourceLocation;

import static net.hellomouse.alexscavesenriched.AlexsCavesEnriched.MODID;

public class ACECriterionTriggers {
    public static final ACAdvancementTrigger KILL_MOB_WITH_BACKBLAST = new ACAdvancementTrigger(ResourceLocation.fromNamespaceAndPath(MODID, "kill_mob_with_backblast"));
    public static final ACAdvancementTrigger FIRE_NUKE_THROUGH_PORTAL = new ACAdvancementTrigger(ResourceLocation.fromNamespaceAndPath(MODID, "fire_nuke_through_portal"));
    public static final ACAdvancementTrigger KILL_SKELETON_WITH_RAILGUN = new ACAdvancementTrigger(ResourceLocation.fromNamespaceAndPath(MODID, "kill_skeleton_with_railgun"));

    public static void init() {
        CriteriaTriggers.register(KILL_MOB_WITH_BACKBLAST);
        CriteriaTriggers.register(FIRE_NUKE_THROUGH_PORTAL);
        CriteriaTriggers.register(KILL_SKELETON_WITH_RAILGUN);
    }
}
