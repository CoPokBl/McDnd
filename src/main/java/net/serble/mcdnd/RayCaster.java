package net.serble.mcdnd;

import net.serble.mcdnd.schemas.RayCastCallback;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;

import java.util.HashMap;
import java.util.UUID;

public class RayCaster implements Listener {
    private final HashMap<UUID, RayCastCallback> runningCasts = new HashMap<>();

    public void rayCast(LivingEntity entity, RayCastCallback callback) {
        // Shoot a snowball in the direction they are looking very quickly
        Snowball snowball = entity.launchProjectile(Snowball.class);
        snowball.addScoreboardTag("raycast");
        snowball.setGravity(false);
        snowball.setVelocity(entity.getLocation().getDirection().multiply(10));
        runningCasts.put(snowball.getUniqueId(), callback);
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
