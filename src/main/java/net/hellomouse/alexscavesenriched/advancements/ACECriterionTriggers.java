package net.hellomouse.alexscavesenriched.advancements;

import com.github.alexmodguy.alexscaves.server.misc.ACAdvancementTrigger;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.util.Identifier;

import static net.hellomouse.alexscavesenriched.AlexsCavesEnriched.MODID;

public class ACECriterionTriggers {
    public static final ACAdvancementTrigger KILL_MOB_WITH_BACKBLAST = new ACAdvancementTrigger(Identifier.fromNamespaceAndPath(MODID, "kill_mob_with_backblast"));
    public static final ACAdvancementTrigger FIRE_NUKE_THROUGH_PORTAL = new ACAdvancementTrigger(Identifier.fromNamespaceAndPath(MODID, "fire_nuke_through_portal"));
    public static final ACAdvancementTrigger KILL_SKELETON_WITH_RAILGUN = new ACAdvancementTrigger(Identifier.fromNamespaceAndPath(MODID, "kill_skeleton_with_railgun"));

    public static void init() {
        Criteria.register(KILL_MOB_WITH_BACKBLAST);
        Criteria.register(FIRE_NUKE_THROUGH_PORTAL);
        Criteria.register(KILL_SKELETON_WITH_RAILGUN);
    }
}
