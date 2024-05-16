package net.serble.mcdnd.actions;

import net.serble.mcdnd.Main;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;

public abstract class Action {

    public abstract void use(LivingEntity e);
    public abstract boolean canUse(LivingEntity e);
    public abstract Material getIcon();
    public abstract String getName();

    protected void waitForTarget(LivingEntity e) {
        Main.getInstance().getCombatManager().registerWaitingAction(e, this);
    }

    public void runWithTarget(LivingEntity e, LivingEntity target) {
        // Override to actually use
    }
}
