package net.serble.mcdnd.schemas.events;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import javax.annotation.Nullable;

public class AttackEvent extends Event implements Cancellable {
    private final LivingEntity attacker;
    private final LivingEntity defender;
    private final Projectile projectile;
    private final boolean didHit;
    private double damage;
    private boolean cancelled;
    private final boolean ranged;

    @Override
    public HandlerList getHandlers() {
        return new HandlerList();
    }

    public AttackEvent(LivingEntity a, LivingEntity d, boolean hit, double dmg, boolean ranged, Projectile proj) {
        attacker = a;
        defender = d;
        didHit = hit;
        damage = dmg;
        this.ranged = ranged;
        projectile = proj;
    }

    public double getDamage() {
        return damage;
    }

    public void setDamage(double damage) {
        this.damage = damage;
    }

    public LivingEntity getAttacker() {
        return attacker;
    }

    public LivingEntity getDefender() {
        return defender;
    }

    public boolean isRanged() {
        return ranged;
    }

    public boolean didHit() {
        return didHit;
    }

    public @Nullable Projectile getProjectile() {
        return projectile;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }
}
