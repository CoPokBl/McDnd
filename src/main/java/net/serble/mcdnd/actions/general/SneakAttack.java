package net.serble.mcdnd.actions.general;

import net.serble.mcdnd.Main;
import net.serble.mcdnd.Utils;
import net.serble.mcdnd.actions.Action;
import net.serble.mcdnd.attackmodifiers.AttackModifier;
import net.serble.mcdnd.attackmodifiers.CancelAttackMod;
import net.serble.mcdnd.attackmodifiers.IncreaseWeaponDamage;
import net.serble.mcdnd.schemas.Damage;
import net.serble.mcdnd.schemas.WeaponProfile;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;

public class SneakAttack extends Action {

    @Override
    public void use(LivingEntity e) {
        waitAndApplyToAttack(e);
        e.sendMessage(Utils.t("&7Attack with a weapon to sneak attack"));
    }

    @Override
    public AttackModifier runWithAttack(LivingEntity attacker, LivingEntity defender, WeaponProfile weapon, Damage damage) {
        if (Main.getInstance().getCombatManager().getAdvantage(attacker, defender, weapon.isRanged()) != 1) {
            attacker.sendMessage(Utils.t("&cYou must have advantage against target"));
            Utils.playSound(Sound.BLOCK_ANVIL_BREAK, attacker);
            return new CancelAttackMod();
        }

        Utils.particlePoint(attacker.getLocation(), Particle.CRIT_MAGIC, 10);
        return new IncreaseWeaponDamage("1d20");
    }

    @Override
    public boolean canUse(LivingEntity e) {
        return hasAction(e);
    }

    @Override
    public Material getIcon() {
        return Material.IRON_SWORD;
    }

    @Override
    public String getName() {
        return "&cSneak Attack";
    }

    @Override
    public String[] getDescription() {
        return new String[] {
                "&7Exploit an enemy's distraction to deal extra damage to their vulnerable points."
        };
    }
}
