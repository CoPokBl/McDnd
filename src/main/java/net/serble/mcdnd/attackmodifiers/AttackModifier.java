package net.serble.mcdnd.attackmodifiers;

import net.serble.mcdnd.schemas.Damage;
import net.serble.mcdnd.schemas.WeaponProfile;

public class AttackModifier {

    public Damage modifyDamage(WeaponProfile weapon, Damage damage) {
        return damage;
    }

    public void tickEffects() {

    }
}
