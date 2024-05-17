package net.serble.mcdnd.attackmodifiers;

import net.serble.mcdnd.schemas.Damage;
import net.serble.mcdnd.schemas.WeaponProfile;

public class AddDamage extends AttackModifier {
    private final Damage amount;

    public AddDamage(Damage amount) {
        this.amount = amount;
    }

    @Override
    public Damage modifyDamage(WeaponProfile weapon, Damage damage) {
        return damage.add(amount);
    }
}
