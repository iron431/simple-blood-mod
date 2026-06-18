package io.redspace.simpleblood;

import io.redspace.simpleblood.client.particles.BloodParticleOptions;
import io.redspace.simpleblood.data.BloodConfig;
import io.redspace.simpleblood.registry.BloodRegistry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class ServerEvents {

    @SubscribeEvent
    public static void onFinalDamage(LivingDamageEvent event) {
        var entity = event.getEntity();
        var level = entity.level();
        if (level.isClientSide()) {
            return;
        }

        for (BloodConfig config : BloodRegistry.getAll()) {
            if (!entity.getType().is(config.entityTag())) {
                continue;
            }
            AABB aabb = entity.isMultipartEntity() ? entity.getParts()[entity.getRandom().nextInt(entity.getParts().length)].getBoundingBox() : entity.getBoundingBox();
            Vec3 vec = aabb.getCenter();
            float damage = event.getAmount();
            if (damage <= config.minDamage()) {
                return;
            }
            if (damage == Float.MAX_VALUE) {
                // kill command
                return;
            }

            damage = Math.min(damage, 2000);
            int count = (int) (damage / config.minDamage())
                    + level.random.nextIntBetweenInclusive(0, (int) (2 * (damage - config.minDamage()) / config.maxDamage()));
            double speed = config.scaledBaseSpeed() + count * config.scaledSpeedPerParticle();
            double bbShove = Math.max(aabb.getXsize() * 0.5 - 0.5, 0);
            double scale = (aabb.getXsize() + 2) / 3f;
            spawnParticles(
                    level,
                    new BloodParticleOptions(config.color(), (float) scale, config.isGraphic()),
                    vec.x,
                    vec.y + aabb.getYsize() * 0.5,
                    vec.z,
                    count,
                    0.05 + bbShove,
                    0.1,
                    0.05 + bbShove,
                    speed,
                    true
            );
            return;
        }
    }

    public static void spawnParticles(Level level, ParticleOptions particle, double x, double y, double z, int count, double deltaX, double deltaY, double deltaZ, double speed, boolean force) {
        level.getServer().getPlayerList().getPlayers().forEach(player -> ((ServerLevel) level).sendParticles(player, particle, force, x, y, z, count, deltaX, deltaY, deltaZ, speed));
    }
}
