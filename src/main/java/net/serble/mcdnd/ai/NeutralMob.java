package net.serble.mcdnd.ai;

import me.gamercoder215.mobchip.EntityBrain;
import me.gamercoder215.mobchip.bukkit.BukkitBrain;
import org.bukkit.Location;
import org.bukkit.entity.*;

import java.util.Objects;

public class NeutralMob extends BaseNpc {
    @Override
    protected EntityType[] getEnemies() {
        return new EntityType[] {
                EntityType.SPIDER,
//                EntityType.CREEPER,
//                EntityType.ZOMBIE,
//                EntityType.SKELETON,
//                EntityType.ENDERMAN
        };
    }

    protected NeutralMob(Mob m) {
        super(m);
    }

    public static void spawn(Location loc) {
        Cow cow = Objects.requireNonNull(loc.getWorld()).spawn(loc, Cow.class);
        patchMob(cow);
    }

    public static void patchMob(Mob entity) {
        EntityBrain brain = BukkitBrain.getBrain(entity);
        NeutralMob pathFind = new NeutralMob(entity);
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

    protected void aggro() {
        brain.getController().moveTo(target, 1);
        brain.getController().lookAt(target);
        meleeIfPossible(target);
    }
}
