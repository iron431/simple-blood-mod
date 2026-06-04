package io.redspace.simpleblood;

import io.redspace.simpleblood.registry.ParticleRegistry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import java.util.Arrays;

@EventBusSubscriber
public class ServerEvents {

    @SubscribeEvent
    public static void onFinalDamage(LivingDamageEvent.Pre event) {
        var entity = event.getEntity();
        if (entity.getType().is(ModTags.BLEEDS) && event.getSource().getEntity() instanceof Player) {
            var level = entity.level();
            if (!level.isClientSide()) {
                Vec3 vec = entity.getBoundingBox().getCenter();
                float damage = event.getContainer().getNewDamage();
                float minDamage = 3f;
                float highDamage = 12f;
                if (damage <= minDamage) {
                    return;
                }
                int count = (int) (damage / minDamage) + level.random.nextIntBetweenInclusive(0, (int) (2 * damage / highDamage));
                float speed = 0.06f + count * .01f;
                    spawnParticles(level, ParticleRegistry.BLOOD_EMITTER.get(), vec.x, vec.y + .5, vec.z, count, 0.05, 0.05, 0.05, speed, true);
            }
        }
    }

    @SubscribeEvent
    public static void joinLevelEvent(EntityJoinLevelEvent event) {
        if (event.getEntity().getType().equals(EntityType.EXPERIENCE_ORB)) {
            event.setCanceled(true);
        } else if (event.getEntity() instanceof Zombie zombie) {
            if (event.getLevel().getBrightness(LightLayer.BLOCK, zombie.blockPosition()) > 0) {
                event.setCanceled(true);
            } else {
                Arrays.stream(EquipmentSlot.values()).forEach(slot -> {
                    zombie.setItemSlot(slot, ItemStack.EMPTY);
                    zombie.setDropChance(slot, 0);
                });
            }
        }
    }

    public static void spawnParticles(Level level, ParticleOptions particle, double x, double y, double z, int count, double deltaX, double deltaY, double deltaZ, double speed, boolean force) {
        level.getServer().getPlayerList().getPlayers().forEach(player -> ((ServerLevel) level).sendParticles(player, particle, force, x, y, z, count, deltaX, deltaY, deltaZ, speed));
    }
}
