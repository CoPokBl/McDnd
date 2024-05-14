package net.serble.mcdnd.classes;

import net.serble.mcdnd.schemas.PlayerStats;

public class Default extends PlayerStats {

    @Override
    public DndClass getDndClass() {
        return DndClass.DEFAULT;
    }

    @Override
    public void setLevel(int lvl) {

    }

    public Default(int dex, int cha, int str, int inte, int wis, int con) {
        setDexterity(dex);
        setCharisma(cha);
        setStrength(str);
        setIntelligence(inte);
        setWisdom(wis);
        setConstitution(con);
    }

    public Default() { }
}
