package net.serble.mcdnd.schemas;

public class WeaponProfile {
    private final WeaponType type;
    private final Damage damage;
    private final boolean isRanged;

    public WeaponProfile(WeaponType t, Damage dr, boolean ranged) {
        type = t;
        damage = dr;
        isRanged = ranged;
    }

    public Damage getDamage() {
        return damage;
    }

    public WeaponType getType() {
        return type;
    }

    public boolean isRanged() {
        return isRanged;
    }

    public static WeaponProfile getFist() {
        return new WeaponProfile(WeaponType.Fist, new Damage(DamageType.Bludgeoning, "1d4-1"), false);
    }
}
