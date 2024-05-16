package net.serble.mcdnd.schemas.events;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import javax.annotation.Nullable;

public class PreAttackEvent extends Event implements Cancellable {
    private final LivingEntity attacker;
    private final LivingEntity defender;
    private final Projectile projectile;
    private boolean cancelled;
    private final boolean ranged;

    @Override
    public HandlerList getHandlers() {
        return new HandlerList();
    }

    public PreAttackEvent(LivingEntity a, LivingEntity d, boolean ranged, Projectile proj) {
        attacker = a;
        defender = d;
        this.ranged = ranged;
        projectile = proj;
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
