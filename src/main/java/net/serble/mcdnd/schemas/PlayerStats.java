package net.serble.mcdnd.schemas;

public class PlayerStats {
    private int Dexterity;
    private int Charisma;
    private int Strength;
    private int Intelligence;
    private int Wisdom;
    private int Constitution;

    public PlayerStats() { }

    public PlayerStats(int dex, int cha, int str, int inte, int wis, int con) {
        setDexterity(dex);
        setCharisma(cha);
        setStrength(str);
        setIntelligence(inte);
        setWisdom(wis);
        setConstitution(con);
    }

    private int checkValue(int val) {
        if (val < 0 || val > 20) {
            throw new RuntimeException("Invalid value");
        }
        return val;
    }

    public static PlayerStats newRandom() {
        PlayerStats stats = new PlayerStats();
        stats.setDexterity((int) (Math.random() * 19) + 1);
        stats.setCharisma((int) (Math.random() * 19) + 1);
        stats.setStrength((int) (Math.random() * 19) + 1);
        stats.setIntelligence((int) (Math.random() * 19) + 1);
        stats.setWisdom((int) (Math.random() * 19) + 1);
        stats.setConstitution((int) (Math.random() * 19) + 1);
        return stats;
    }

    public int get(AbilityScore score) {
        switch (score) {
            case DEXTERITY:
                return getDexterity();
            case WISDOM:
                return getWisdom();
            case CHARISMA:
                return getCharisma();
            case STRENGTH:
                return getStrength();
            case CONSTITUTION:
                return getConstitution();
            case INTELLIGENCE:
                return getIntelligence();
        }
        throw new RuntimeException("Invalid ability score");
    }

    public int getDexterity() {
        return Dexterity;
    }

    public void setDexterity(int dexterity) {
        Dexterity = checkValue(dexterity);
    }

    public int getCharisma() {
        return Charisma;
    }

    public void setCharisma(int charisma) {
        Charisma = checkValue(charisma);
    }

    public int getStrength() {
        return Strength;
    }

    public void setStrength(int strength) {
        Strength = checkValue(strength);
    }

    public int getIntelligence() {
        return Intelligence;
    }

    public void setIntelligence(int intelligence) {
        Intelligence = checkValue(intelligence);
    }

    public int getWisdom() {
        return Wisdom;
    }

    public void setWisdom(int wisdom) {
        Wisdom = checkValue(wisdom);
    }

    public int getConstitution() {
        return Constitution;
    }

    public void setConstitution(int constitution) {
        Constitution = checkValue(constitution);
    }
}
