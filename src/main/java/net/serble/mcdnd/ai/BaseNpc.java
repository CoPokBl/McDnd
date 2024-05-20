package net.serble.mcdnd.ai;

import me.gamercoder215.mobchip.EntityBrain;
import me.gamercoder215.mobchip.ai.goal.CustomPathfinder;
import me.gamercoder215.mobchip.bukkit.BukkitBrain;
import net.serble.mcdnd.Main;
import net.serble.mcdnd.Utils;
import net.serble.mcdnd.schemas.Conflict;
import org.bukkit.Bukkit;
import org.bukkit.entity.*;
import org.bukkit.util.Vector;

public abstract class BaseNpc extends CustomPathfinder {
    protected EntityBrain brain;
    protected LivingEntity target;
    protected boolean canAttack = true;

    protected abstract EntityType[] getEnemies();

    protected BaseNpc(Mob m) {
        super(m);
    }

    private boolean isEnemy(LivingEntity e) {
        for (EntityType type : getEnemies()) {
            if (e.getType() == type) {
                return true;
            }
        }
        return false;
    }

    @Override
    public PathfinderFlag[] getFlags() {
        return new PathfinderFlag[] {
                PathfinderFlag.JUMPING,
                PathfinderFlag.MOVEMENT,
                PathfinderFlag.TARGETING
        };
    }

    @Override
    public boolean canStart() {
        return true;
    }

    @Override
    public void start() {
        brain = BukkitBrain.getBrain(entity);
    }

    protected void ensureTarget() {
        // Find nearby entities
        if (target == null || target.isDead()) {
            LivingEntity closest = null;
            double closestDistance = 999999;
            for (LivingEntity e : entity.getWorld().getLivingEntities()) {
                Integer relationship = Main.getInstance().getTeamManager().getRelationship(e, entity);
                if (!isEnemy(e) && relationship != -1) {  // Fight natural enemies and people you hate
                    continue;
                }
                if (!brain.canSee(e)) {
                    continue;
                }
                double distance = e.getLocation().distance(entity.getLocation());
                if (distance > closestDistance) {
                    continue;
                }
                closestDistance = distance;
                closest = e;
            }
            target = closest;
        }
    }

    protected boolean meleeIfPossible(LivingEntity target) {
        boolean canSee = brain.canSee(target);
        double distance = target.getLocation().distance(entity.getLocation());
        if (!canAttack || !canSee || distance > 3) {
            return false;
        }

        target.damage(1, entity);  // Damage is overridden by combat manager
        resetAttack();
        return true;
    }

    protected boolean shootIfPossible(LivingEntity target) {
        if (!canAttack) {
            return false;
        }
        Arrow arrow = entity.launchProjectile(Arrow.class);
        Vector vec = target.getLocation().toVector().subtract(entity.getLocation().toVector()).normalize().multiply(2);
        if (!Utils.isFinite(vec)) {  // I guess we can't shoot
            return false;
        }
        arrow.setVelocity(vec);
        resetAttack();
        return true;
    }

    protected boolean endTurnIfFighting() {
        Conflict conflict = Main.getInstance().getConflictManager().getConflict(entity);
        if (conflict == null) {
            return false;
        }

        if (!conflict.isCurrentTurn(entity)) {
            return false;
        }

        Main.getInstance().getConflictManager().endPlayersTurn(entity);
        return true;
    }

    @Override
    public void tick() {
        if (!Main.getInstance().isEnabled()) {
            return;  // The plugin is disabled
        }

        Conflict conflict = Main.getInstance().getConflictManager().getConflict(entity);
        if (conflict != null && !conflict.isCurrentTurn(entity)) {
            Bukkit.getLogger().info("NO MOVE");
            brain.getController().moveTo(entity);
            return;
        }

        ensureTarget();
        update();
    }

    protected abstract void update();

    private void resetAttack() {
        if (!Main.getInstance().isEnabled()) {
            Bukkit.getLogger().severe("Tried to do mob tick while disabled");
            return;
        }
        canAttack = false;
        try {
            Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> canAttack = true, 40L);
        } catch (Exception e) {
            Bukkit.getLogger().warning("Could not schedule attack task, enabled: " + Main.getInstance().isEnabled());
            canAttack = true;  // I guess
        }
    }
}
