package net.serble.mcdnd.schemas;

public enum DamageType {
    Bludgeoning("BL"),
    Piecing("PI"),
    Slashing("SL"),
    Cold("CO"),
    Fire("FI"),
    Lightning("LI"),
    Thunder("TH"),
    Acid("AC"),
    Poison("PO"),
    Radiant("RA"),
    Necrotic("NE"),
    Force("FO"),
    Psychic("PS");

    private final String prefix;

    DamageType(String prefix) {
        this.prefix = prefix.toUpperCase();
    }

    public String getPrefix() {
        return prefix;
    }

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

        throw new RuntimeException("Invalid damage type: " + pre);
    }
}
