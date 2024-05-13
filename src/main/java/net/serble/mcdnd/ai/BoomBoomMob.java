package net.serble.mcdnd.ai;

import me.gamercoder215.mobchip.EntityBrain;
import me.gamercoder215.mobchip.bukkit.BukkitBrain;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;

public class BoomBoomMob extends NeutralMob {
    @Override
    protected EntityType[] getEnemies() {
        return new EntityType[] {
                EntityType.PLAYER,
                EntityType.VILLAGER,
                EntityType.IRON_GOLEM
        };
    }

    protected BoomBoomMob(Mob m) {
        super(m);
    }

    public static void patchMob(Mob entity) {
        EntityBrain brain = BukkitBrain.getBrain(entity);
        BoomBoomMob pathFind = new BoomBoomMob(entity);
        brain.getTargetAI().put(pathFind, 0);
    }

    @Override
    protected void update() {
        if (target != null) {
            aggro();
            return;
        }

        // Passive, pick random position around entity
        if (Math.random() < 0.01) {
            Location loc = entity.getLocation().clone();
            loc.add(Math.random() * 10 - 5, 0, Math.random() * 10 - 5);
            brain.getController().moveTo(loc, 0.8);
        }
    }

    protected boolean meleeIfPossible(LivingEntity target) {
        boolean canSee = brain.canSee(target);
        double distance = target.getLocation().distance(entity.getLocation());
        if (!canAttack || !canSee || distance > 3) {
            return false;
        }

        // Explode
        entity.getWorld().createExplosion(entity.getLocation(), 4F, false, false, entity);
        entity.remove();
        return true;
    }

    protected void aggro() {
        brain.getController().moveTo(target, 1);
        brain.getController().lookAt(target);
        meleeIfPossible(target);
    }
}
