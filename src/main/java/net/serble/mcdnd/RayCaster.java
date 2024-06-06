package net.serble.mcdnd;

import net.serble.mcdnd.schemas.RayCastCallback;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;

public class RayCaster implements Listener {
    private final HashMap<UUID, RayCastCallback> runningCasts = new HashMap<>();

    /** Please don't use this I beg of you, this is the worst function ever, it throws a visible snowball as a ray */
    public void complexRayCast(LivingEntity entity, RayCastCallback callback) {
        // Shoot a snowball in the direction they are looking very quickly
        Snowball snowball = entity.launchProjectile(Snowball.class);
        snowball.addScoreboardTag("raycast");
        snowball.setGravity(false);
        snowball.setVelocity(entity.getLocation().getDirection().multiply(10));
        runningCasts.put(snowball.getUniqueId(), callback);
    }

    public Entity entityRayCast(final LivingEntity entity, double range) {
        Collection<Entity> ens = entity.getWorld().getNearbyEntities(entity.getLocation(), 5, 5, 5);
        return getTarget(entity, ens);
    }

    public static Entity getTarget(final Entity entity, final Iterable<Entity> entities) {
        Entity target = null;
        final double threshold = 1;

        Location enLocation = entity.getLocation();
        Vector enDirNormalised = enLocation.getDirection().normalize();

        for (final Entity other : entities) {
            final Vector direction = other.getLocation().toVector().subtract(enLocation.toVector());

            if (enDirNormalised.crossProduct(direction).lengthSquared() < threshold &&
                    direction.normalize().dot(enDirNormalised) >= 0 && (
                    target == null || // don't compare to previous target if there is no previous target
                            target.getLocation().distanceSquared(enLocation) >
                                    other.getLocation().distanceSquared(enLocation))) {

                target = other;
            }
        }
        return target;
    }

    public boolean isRayCast(Entity e) {
        return runningCasts.containsKey(e.getUniqueId());
    }

    @EventHandler
    public void onProjLand(ProjectileHitEvent e) {
        if (!runningCasts.containsKey(e.getEntity().getUniqueId())) {
            return;
        }

        runningCasts.get(e.getEntity().getUniqueId()).run(e.getHitEntity(), e.getHitBlock(), e.getHitBlockFace());
        runningCasts.remove(e.getEntity().getUniqueId());
        e.setCancelled(true);
    }
}
