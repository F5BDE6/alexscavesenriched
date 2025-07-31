package net.hellomouse.alexscavesenriched;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ACEDamageSources {
    public static final RegistryKey<DamageType> BLACKHOLE = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, Identifier.fromNamespaceAndPath(AlexsCavesEnriched.MODID, "black_hole"));;

    public ACEDamageSources() {}

    public static DamageSource causeBlackHoleDamage(DynamicRegistryManager registryAccess) {
        return new DamageSourceRandomMessages(
                ((Registry)registryAccess.getOptional(RegistryKeys.DAMAGE_TYPE).get()).entryOf(BLACKHOLE), 4);
    }

    private static class DamageSourceRandomMessages extends DamageSource {
        private final int messageCount;

        public DamageSourceRandomMessages(RegistryEntry.Reference<DamageType> message, int messageCount) {
            super(message);
            this.messageCount = messageCount;
        }

        public DamageSourceRandomMessages(RegistryEntry.Reference<DamageType> message, Entity source, int messageCount) {
            super(message, source);
            this.messageCount = messageCount;
        }

        public Text getDeathMessage(LivingEntity attacked) {
            int type = attacked.getRandom().nextInt(this.messageCount);
            String var10000 = this.getName();
            String s = "death.attack." + var10000 + "_" + type;
            Entity entity = this.getSource() == null ? this.getAttacker() : this.getSource();
            return entity != null ?
                    Text.translatable(s + ".entity", attacked.getDisplayName(), entity.getDisplayName()) :
                    Text.translatable(s, attacked.getDisplayName());
        }
    }
}
