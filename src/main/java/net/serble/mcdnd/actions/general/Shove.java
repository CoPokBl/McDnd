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

        deductAction(e);

        Vector direction = e.getLocation().getDirection();
        target.setVelocity(direction.multiply(1.5).add(new Vector(0, 1, 0)));

        e.sendMessage(Utils.t("&aYou have shoved!"));
    }

    @Override
    public boolean canUse(LivingEntity e) {
        return hasAction(e);
    }

    @Override
    public Material getIcon() {
        return Material.LEATHER_BOOTS;
    }

    @Override
    public String getName() {
        return "&2Dash";
    }

    @Override
    public String[] getDescription() {
        return new String[] {
                "&7Double your movement speed for this turn."
        };
    }
}
