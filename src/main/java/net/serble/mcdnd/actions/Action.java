package net.serble.mcdnd.actions;

import net.serble.mcdnd.Main;
import net.serble.mcdnd.attackmodifiers.AttackModifier;
import net.serble.mcdnd.schemas.Conflict;
import net.serble.mcdnd.schemas.Damage;
import net.serble.mcdnd.schemas.WeaponProfile;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;

public abstract class Action {

    public abstract void use(LivingEntity e);
    public abstract boolean canUse(LivingEntity e);
    public abstract Material getIcon();
    public abstract String getName();
    public abstract String[] getDescription();

    protected void waitForTarget(LivingEntity e) {
        Main.getInstance().getCombatManager().registerWaitingAction(e, this);
    }

    protected void waitAndApplyToAttack(LivingEntity e) {
        Main.getInstance().getCombatManager().registerWaitingAttackAction(e, this);
    }

    public void runWithTarget(LivingEntity e, LivingEntity target) {
        // Override to actually use
    }

    public AttackModifier runWithAttack(LivingEntity e, WeaponProfile weapon, Damage damage) {
        return new AttackModifier();
    }

    protected boolean hasAction(LivingEntity e) {
        Conflict conflict = Main.getInstance().getConflictManager().getConflict(e);
        if (conflict == null) {
            return true;
        }

        if (!conflict.isCurrentTurn(e)) {
            return false;
        }

        return conflict.currentTurnActionsRemaining > 0;
    }

    protected boolean hasBonusAction(LivingEntity e) {
        Conflict conflict = Main.getInstance().getConflictManager().getConflict(e);
        if (conflict == null) {
            return true;
        }

        if (!conflict.isCurrentTurn(e)) {
            return false;
        }

        return conflict.currentTurnBonusActionsRemaining > 0;
    }

    protected void deductAction(LivingEntity e) {
        Conflict conflict = Main.getInstance().getConflictManager().getConflict(e);
        if (conflict == null) {
            return;
        }

        conflict.currentTurnActionsRemaining--;
    }

    protected void deductBonusAction(LivingEntity e) {
        Conflict conflict = Main.getInstance().getConflictManager().getConflict(e);
        if (conflict == null) {
            return;
        }

        conflict.currentTurnBonusActionsRemaining--;
    }

    protected void addAttackMod(LivingEntity e, AttackModifier mod) {
        Main.getInstance().getCombatManager().addPlayerAttackMod(e, mod);
    }
}
