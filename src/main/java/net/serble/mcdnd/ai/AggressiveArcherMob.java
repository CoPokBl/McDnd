package net.serble.mcdnd.ai;

import me.gamercoder215.mobchip.EntityBrain;
import me.gamercoder215.mobchip.bukkit.BukkitBrain;
import net.serble.mcdnd.Main;
import net.serble.mcdnd.Utils;
import net.serble.mcdnd.schemas.Conflict;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mob;
import org.bukkit.inventory.EntityEquipment;

public class AggressiveArcherMob extends NeutralMob {
    protected final int preferredDistance = 10;

    @Override
    protected EntityType[] getEnemies() {
        return new EntityType[] {
                EntityType.PLAYER,
                EntityType.VILLAGER,
                EntityType.IRON_GOLEM
        };
    }

    protected AggressiveArcherMob(Mob m) {
        super(m);
    }

    public static void patchMob(Mob entity, boolean crossbow) {
        EntityEquipment equipment = entity.getEquipment();
        assert equipment != null;
        equipment.setItemInMainHand(Utils.makeItem(crossbow ? Material.CROSSBOW : Material.BOW, "Skeleton Bow"));
        EntityBrain brain = BukkitBrain.getBrain(entity);
        AggressiveArcherMob pathFind = new AggressiveArcherMob(entity);
        brain.getTargetAI().put(pathFind, 0);
    }

    @Override
    protected void aggro() {  // Maintain 10 blocks from target and shoot
        brain.getController().lookAt(target);
        Conflict conflict = Main.getInstance().getConflictManager().getConflict(entity);
        boolean fighting = conflict != null;

        double distance = entity.getLocation().distance(target.getLocation());
        if (distance < preferredDistance) {
            shootIfPossible(target);

            // Move away from target
            if (fighting) {
                brain.getController().moveTo(entity.getLocation().add(target.getLocation().subtract(entity.getLocation()).toVector().normalize().multiply(-10)), 1.5);
            }
            return;
        }

        shootIfPossible(target);
        brain.getController().moveTo(target, 0.5);
    }
}
