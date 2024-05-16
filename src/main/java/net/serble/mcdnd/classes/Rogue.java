package net.serble.mcdnd.classes;

import net.serble.mcdnd.Utils;
import net.serble.mcdnd.actions.spells.FireBolt;
import net.serble.mcdnd.schemas.AbilityScore;
import net.serble.mcdnd.schemas.PlayerStats;
import org.bukkit.Bukkit;

public class Rogue extends PlayerStats {

    @Override
    public DndClass getDndClass() {
        return DndClass.ROGUE;
    }

    public Rogue(int dex, int cha, int str, int inte, int wis, int con) {
        setDexterity(dex);
        setCharisma(cha);
        setStrength(str);
        setIntelligence(inte);
        setWisdom(wis);
        setConstitution(con);
    }

    public Rogue() {
        setLevel(1);
    }

    @Override
    public void setLevel(int lvl) {
        level = lvl;
        savingThrowProficiencies.clear();
        weaponProficiencies.clear();

        savingThrowProficiencies.add(AbilityScore.Dexterity);
        savingThrowProficiencies.add(AbilityScore.Intelligence);

        switch (lvl) {
            case 1:
                maxHealth = 8 + Utils.getStatMod(constitution);
                Bukkit.getLogger().info("Max Health: " + maxHealth + ", const mod: " + Utils.getStatMod(constitution) + ", const: " + constitution);
                break;

            default:
            case 12:
            case 10:
            case 11:
            case 9:
            case 8:
            case 7:
            case 6:
            case 5:
            case 4:
            case 3:
            case 2:
                maxHealth = (5 * lvl) + Utils.getStatMod(constitution);
                break;
        }

        switch (lvl) {
            case 1:
            case 2:
                actions.add(new FireBolt());
            case 3:
            case 4:
                proficiencyBonus = 2;
                break;

            case 5:
            case 6:
            case 7:
            case 8:
                proficiencyBonus = 3;
                break;

            case 9:
            case 10:
            case 11:
            case 12:
            default:
                proficiencyBonus = 4;
                break;
        }
    }
}
