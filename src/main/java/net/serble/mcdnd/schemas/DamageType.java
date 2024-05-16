package net.serble.mcdnd.schemas;

public enum DamageType {
    Bludgeoning,
    Piecing,
    Slashing,
    Cold,
    Fire,
    Lightning,
    Thunder,
    Acid,
    Poison,
    Radiant,
    Necrotic,
    Force,
    Psychic;

    public static DamageType getFromPrefix(String prefix) {
        String pre = prefix.substring(0, 2).toUpperCase();

        switch (pre) {
            case "BL":
                return Bludgeoning;
            case "PI":
                return Piecing;
            case "SL":
                return Slashing;
            case "CO":
                return Cold;
            case "FI":
                return Fire;
            case "LI":
                return Lightning;
            case "TH":
                return Thunder;
            case "AC":
                return Acid;
            case "PO":
                return Poison;
            case "RA":
                return Radiant;
            case "NE":
                return Necrotic;
            case "FO":
                return Force;
            case "PS":
                return Psychic;
        }

        throw new RuntimeException("Invalid damage type");
    }
}
