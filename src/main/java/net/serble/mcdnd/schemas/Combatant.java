package net.serble.mcdnd.schemas;

import org.bukkit.entity.LivingEntity;

public class Combatant {
    private final LivingEntity entity;
    private final String name;
    private boolean dead = false;

    public Combatant(LivingEntity e) {
        entity = e;
        name = e.getName();
    }

    public boolean isDead() {
        return dead;
    }

    public void setDead(boolean dead) {
        this.dead = dead;
    }

    public LivingEntity getEntity() {
        return entity;
    }

    public String getName() {
        return name;
    }
}
