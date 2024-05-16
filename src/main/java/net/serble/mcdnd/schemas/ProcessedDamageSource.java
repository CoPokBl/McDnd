package net.serble.mcdnd.schemas;

import org.bukkit.Location;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Entity;

@SuppressWarnings("UnstableApiUsage")
public class ProcessedDamageSource implements DamageSource {
    private final DamageSource child;

    public ProcessedDamageSource(DamageSource s) {
        child = s;
    }

    @Override
    public DamageType getDamageType() {
        return child.getDamageType();
    }

    @Override
    public Entity getCausingEntity() {
        return child.getCausingEntity();
    }

    @Override
    public Entity getDirectEntity() {
        return child.getDirectEntity();
    }

    @Override
    public Location getDamageLocation() {
        return child.getDamageLocation();
    }

    @Override
    public Location getSourceLocation() {
        return child.getSourceLocation();
    }

    @Override
    public boolean isIndirect() {
        return child.isIndirect();
    }

    @Override
    public float getFoodExhaustion() {
        return child.getFoodExhaustion();
    }

    @Override
    public boolean scalesWithDifficulty() {
        return child.scalesWithDifficulty();
    }
}
