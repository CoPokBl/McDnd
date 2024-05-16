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
        return Material.FIRE_CHARGE;
    }

    @Override
    public String getName() {
        return "&6Fire Bolt";
    }

    @Override
    public String[] getDescription() {
        return new String[] {
                "&7Shoot a ball of fire at a target.",
                "&71d10 &6Fire Damage"
        };
    }

    @Override
    public void runWithTarget(LivingEntity e, LivingEntity target) {
        super.runWithTarget(e, target);
        Utils.particleStream(e.getLocation().add(0, 1, 0), target.getLocation().add(0, 1, 0), Particle.FLAME, 20);
        target.damage(Utils.roll("1d10"), e);
        target.setFireTicks(100);
    }
}
