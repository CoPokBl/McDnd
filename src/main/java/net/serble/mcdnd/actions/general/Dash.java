package net.serble.mcdnd.actions.general;

import net.serble.mcdnd.Main;
import net.serble.mcdnd.Utils;
import net.serble.mcdnd.actions.Action;
import net.serble.mcdnd.schemas.Conflict;
import net.serble.mcdnd.schemas.PlayerStats;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;

public class Dash extends Action {

    @Override
    public void use(LivingEntity e) {
        Conflict conflict = Main.getInstance().getConflictManager().getConflict(e);
        if (conflict == null) {
            e.sendMessage(Utils.t("&aYou have dashed!"));
            return;
        }

        if (!conflict.isCurrentTurn(e)) {
            return;
        }

        deductAction(e);

        PlayerStats stats = Main.getInstance().getPlayerManager().getStatsFor(e);
        conflict.currentTurnMovementRemaining += stats.getMovementSpeed();
        e.sendMessage(Utils.t("&aYou have dashed!"));
    }

    @Override
    public boolean canUse(LivingEntity e) {
        Conflict conflict = Main.getInstance().getConflictManager().getConflict(e);
        if (conflict == null) {
            return true;
        }

        if (!hasAction(e)) {
            return false;
        }

        return conflict.isCurrentTurn(e);
    }

    @Override
    public Material getIcon() {
        return Material.RABBIT_FOOT;
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
