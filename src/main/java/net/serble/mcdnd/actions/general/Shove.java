package net.serble.mcdnd.actions.general;

import net.serble.mcdnd.Main;
import net.serble.mcdnd.Utils;
import net.serble.mcdnd.actions.Action;
import net.serble.mcdnd.schemas.PlayerStats;
import net.serble.mcdnd.schemas.Skill;
import net.serble.mcdnd.schemas.events.AttackEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

public class Shove extends Action {

    @Override
    public void use(LivingEntity e) {
        waitForTarget(e);
    }

    @Override
    public void runWithTarget(LivingEntity e, LivingEntity target) {
        PlayerStats stats = Main.getInstance().getPlayerManager().getStatsFor(e);

        boolean success = Main.getInstance().getPlayerManager().abilityCheck(e, Skill.Athletics, 10, 0);

        AttackEvent event = new AttackEvent(e, target, success, 0, false, null);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return;
        }

        deductBonusAction(e);

        Vector direction = e.getLocation().getDirection();
        Vector push = direction.multiply(1).setY(0.5);
        target.setVelocity(push);

        e.sendMessage(Utils.t("&aYou have shoved! " + direction.getX() + " " + direction.getY() + " " + direction.getZ()));
    }

    @Override
    public boolean canUse(LivingEntity e) {
        return hasBonusAction(e);
    }

    @Override
    public Material getIcon() {
        return Material.LEATHER_CHESTPLATE;
    }

    @Override
    public String getName() {
        return "&2Shove";
    }

    @Override
    public String[] getDescription() {
        return new String[] {
                "&7Push someone away from you."
        };
    }
}
