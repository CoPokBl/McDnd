package net.serble.mcdnd.schemas;

public class WeaponProfile {
    private final WeaponType type;
    private final String damageRoll;
    private final boolean isRanged;

    public WeaponProfile(WeaponType t, String dr, boolean ranged) {
        type = t;
        damageRoll = dr;
        isRanged = ranged;
    }

    public String getDamageRoll() {
        return damageRoll;
    }

    public WeaponType getType() {
        return type;
    }

    public boolean isRanged() {
        return isRanged;
    }

    public static WeaponProfile getFist() {
        return new WeaponProfile(WeaponType.Fist, "1d4", false);
    }
}
