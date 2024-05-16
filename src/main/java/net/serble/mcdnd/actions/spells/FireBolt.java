package net.serble.mcdnd.actions.spells;

import net.serble.mcdnd.Utils;
import net.serble.mcdnd.actions.SpellBase;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;

public class FireBolt extends SpellBase {

    @Override
    public void use(LivingEntity e) {
        waitForTarget(e);
    }

    @Override
    public boolean canUse(LivingEntity e) {
        return true;
    }

    @Override
    public Material getIcon() {
        return Material.FIRE;
    }

    @Override
    public String getName() {
        return "Fire Bolt";
    }

    @Override
    public void runWithTarget(LivingEntity e, LivingEntity target) {
        Utils.particleStream(e.getLocation().add(0, 1, 0), target.getLocation().add(0, 1, 0), Particle.FLAME, 20);
        target.damage(Utils.roll("1d10"), e);
        target.setFireTicks(100);
    }
}
