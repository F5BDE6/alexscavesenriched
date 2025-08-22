package net.hellomouse.alexscavesenriched;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class ACEDamageSources {
    public static final ResourceKey<DamageType> BLACKHOLE = ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath(AlexsCavesEnriched.MODID, "black_hole"));
    public static final ResourceKey<DamageType> RAILGUN = ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath(AlexsCavesEnriched.MODID, "railgun"));

    public ACEDamageSources() {}

    public static DamageSource causeBlackHoleDamage(RegistryAccess registryAccess) {
        return new DamageSourceRandomMessages(
                ((Registry) registryAccess.registry(Registries.DAMAGE_TYPE).get()).getHolderOrThrow(BLACKHOLE), 4);
    }

    public static DamageSource causeRailgunDamage(RegistryAccess registryAccess, Entity source) {
        return new DamageSourceRandomMessages(
                ((Registry) registryAccess.registry(Registries.DAMAGE_TYPE).get()).getHolderOrThrow(RAILGUN), source, 3);
    }

    private static class DamageSourceRandomMessages extends DamageSource {
        private final int messageCount;

        public DamageSourceRandomMessages(Holder.Reference<DamageType> message, int messageCount) {
            super(message);
            this.messageCount = messageCount;
        }

        public DamageSourceRandomMessages(Holder.Reference<DamageType> message, Entity source, int messageCount) {
            super(message, source);
            this.messageCount = messageCount;
        }

        public Component getLocalizedDeathMessage(LivingEntity attacked) {
            int type = attacked.getRandom().nextInt(this.messageCount);
            String var10000 = this.getMsgId();
            String s = "death.attack." + var10000 + "_" + type;
            Entity entity = this.getDirectEntity() == null ? this.getEntity() : this.getDirectEntity();
            return entity != null ?
                    Component.translatable(s + ".entity", attacked.getDisplayName(), entity.getDisplayName()) :
                    Component.translatable(s, attacked.getDisplayName());
        }
    }
}
