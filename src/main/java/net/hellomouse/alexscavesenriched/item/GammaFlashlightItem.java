package net.hellomouse.alexscavesenriched.item;

import com.github.alexmodguy.alexscaves.server.item.ACItemRegistry;
import com.github.alexmodguy.alexscaves.server.potion.ACEffectRegistry;
import net.hellomouse.alexscavesenriched.AlexsCavesEnriched;
import net.hellomouse.alexscavesenriched.entity.NeutronExplosionEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

public class GammaFlashlightItem extends Item {
    public GammaFlashlightItem() {
        super(new Settings()
                .maxCount(1)
                .rarity(ACItemRegistry.RARITY_NUCLEAR));
    }
    public static boolean isOn(ItemStack itemStack) {
        return itemStack.getNbt() != null && itemStack.getNbt().contains("on");
    }
    @Override
    public void inventoryTick(ItemStack itemStack, World level, Entity entity, int i, boolean b) {
        if (!level.isClient && isOn(itemStack)) {
            boolean irradiate = true;
            if (entity instanceof PlayerEntity player)
                irradiate = player.getInventory().getMainHandStack() == itemStack || player.getInventory().offHand.get(0) == itemStack;
            if (irradiate) {
                Vec3d launchPos = entity.getEyePos();
                Vec3d launchDir = entity.getRotationVec(1.0F);

                // Mutate blocks
                var hit = level.raycast(new RaycastContext(
                        launchPos,
                        launchPos.add(launchDir.add(new Vec3d(
                                level.random.nextFloat() - 0.5,
                                level.random.nextFloat() - 0.5,
                                level.random.nextFloat() - 0.5
                            ).multiply(AlexsCavesEnriched.CONFIG.gammaFlashlightConfig.spread))
                                .multiply(AlexsCavesEnriched.CONFIG.gammaFlashlightConfig.range)),
                        RaycastContext.ShapeType.OUTLINE,
                        RaycastContext.FluidHandling.NONE,
                        entity));
                NeutronExplosionEntity.tryTransmuteBlock(entity.getEntityWorld(), hit.getBlockPos());

                // Irradiate mobs
                Box bashBox = new Box(new BlockPos((int) launchPos.getX(), (int) launchPos.getY(), (int) launchPos.getZ()))
                        .stretch(launchDir.multiply(AlexsCavesEnriched.CONFIG.gammaFlashlightConfig.range));
                for (LivingEntity otherEntity : level.getNonSpectatingEntities(LivingEntity.class, bashBox)) {
                    if (otherEntity == entity || !otherEntity.canSee(entity))
                        continue;
                    otherEntity.addStatusEffect(new StatusEffectInstance(ACEffectRegistry.IRRADIATED.get(),
                    200, 0,
                    false, false, true));
                }
            }
        }
    }

    @Override
    public TypedActionResult<ItemStack> use(World level, PlayerEntity player, Hand hand) {
        ItemStack itemStack = player.getStackInHand(hand);
        NbtCompound tag = itemStack.getOrCreateNbt();

        if (isOn(itemStack)) {
            level.playSound(null, player.getBlockPos(), SoundEvents.BLOCK_WOODEN_BUTTON_CLICK_ON, SoundCategory.PLAYERS, 1.0F, 1.0F);
            tag.remove("on");
            itemStack.setNbt(tag);
        } else {
            level.playSound(null, player.getBlockPos(), SoundEvents.BLOCK_WOODEN_BUTTON_CLICK_OFF, SoundCategory.PLAYERS, 1.0F, 1.0F);
            tag.putBoolean("on", true);
            itemStack.setNbt(tag);
        }
        return TypedActionResult.success(itemStack);
    }
}
