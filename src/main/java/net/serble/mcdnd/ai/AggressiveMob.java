package net.serble.mcdnd.ai;

import me.gamercoder215.mobchip.EntityBrain;
import me.gamercoder215.mobchip.bukkit.BukkitBrain;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mob;

public class AggressiveMob extends NeutralMob {
    @Override
    protected EntityType[] getEnemies() {
        return new EntityType[] {
                EntityType.PLAYER,
                EntityType.VILLAGER,
                EntityType.IRON_GOLEM
        };
    }

    protected AggressiveMob(Mob m) {
        super(m);
    }

    public static void patchMob(Mob entity) {
        EntityBrain brain = BukkitBrain.getBrain(entity);
        AggressiveMob pathFind = new AggressiveMob(entity);
        brain.getTargetAI().put(pathFind, 0);
    }
}
