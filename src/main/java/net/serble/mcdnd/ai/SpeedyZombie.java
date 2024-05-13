package net.serble.mcdnd.ai;

import me.gamercoder215.mobchip.EntityBrain;
import me.gamercoder215.mobchip.bukkit.BukkitBrain;
import net.serble.mcdnd.Main;
import net.serble.mcdnd.Utils;
import net.serble.mcdnd.schemas.Conflict;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

public class SpeedyZombie extends BaseNpc {
    @Override
    protected EntityType[] getEnemies() {
        return new EntityType[] {
                EntityType.SPIDER
        };
    }

    protected SpeedyZombie(Mob m) {
        super(m);
    }

    public static void spawn(Location loc) {
        Zombie zombie = Objects.requireNonNull(loc.getWorld()).spawn(loc, Zombie.class);
        patchMob(zombie);
    }

    public static void patchMob(Mob zombie) {
        EntityBrain brain = BukkitBrain.getBrain(zombie);
        BaseNpc pathFind = new SpeedyZombie(zombie);
        brain.getTargetAI().put(pathFind, 1);
        zombie.addScoreboardTag("noautoendturn");

        ItemStack bow = Utils.makeItem(Material.BOW, "Zombie Bow");
        ItemStack sword = Utils.makeItem(Material.IRON_SWORD, "Zombie Sword");
        Objects.requireNonNull(zombie.getEquipment()).setItemInOffHand(bow);
        zombie.getEquipment().setItemInMainHand(sword);
    }

    @Override
    protected void update() {
        if (target == null) {
            return;
        }
        boolean canSee = brain.canSee(target);
        double distance = target.getLocation().distance(entity.getLocation());
        brain.getController().moveTo(target, 1.5);
        brain.getController().lookAt(target);

        meleeIfPossible(target);
        if (canSee && canAttack && distance > 10) {
            // Launch an arrow towards target
            shootIfPossible(target);
        }

        Conflict conflict = Main.getInstance().getConflictManager().getConflict(entity);
        if (conflict != null && conflict.currentTurnActionsRemaining < 1) {
            Main.getInstance().getConflictManager().endPlayersTurn(entity);
        }
    }
}
