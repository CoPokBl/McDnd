package net.serble.mcdnd.attackmodifiers;

import net.serble.mcdnd.schemas.Damage;
import net.serble.mcdnd.schemas.WeaponProfile;

public class IncreaseWeaponDamage extends AttackModifier {
    private final String amount;

    public IncreaseWeaponDamage(String amount) {
        this.amount = amount;
    }

    @Override
    public Damage modifyDamage(WeaponProfile weapon, Damage damage) {
        Damage newDmg = new Damage(weapon.getDamage().getDamages()[0].a(), amount);
        return damage.add(newDmg);
    }
}
