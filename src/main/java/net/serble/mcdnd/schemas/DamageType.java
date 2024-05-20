package net.serble.mcdnd.schemas;

public enum DamageType {
    Bludgeoning("BL", "8"),
    Piecing("PI", "8"),
    Slashing("SL", "8"),
    Cold("CO", "b"),
    Fire("FI", "6"),
    Lightning("LI", "9"),
    Thunder("TH", "5"),
    Acid("AC", "a"),
    Poison("PO", "2"),
    Radiant("RA", "e"),
    Necrotic("NE", "a"),
    Force("FO", "c"),
    Psychic("PS", "d");

    private final String prefix;
    private final String colour;

    DamageType(String prefix, String colourCode) {
        this.prefix = prefix.toUpperCase();
        colour = "&" + colourCode;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getColour() {
        return colour;
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
