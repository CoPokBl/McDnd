package net.serble.mcdnd.schemas;

import net.serble.mcdnd.actions.Action;
import net.serble.mcdnd.actions.general.Dash;
import net.serble.mcdnd.actions.general.Shove;
import net.serble.mcdnd.classes.DndClass;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public abstract class PlayerStats {
    protected int level = 1;
    protected int exp = 1;
    protected int maxHealth = 20;
    protected int movementSpeed = 18;

    protected int dexterity;
    protected int charisma;
    protected int strength;
    protected int intelligence;
    protected int wisdom;
    protected int constitution;

    protected final HashMap<Integer, Integer> spellSlots = new HashMap<>();
    protected final HashMap<Integer, Integer> usedSpellSlots = new HashMap<>();
    protected final List<WeaponType> weaponProficiencies = new ArrayList<>();
    protected final List<Skill> skillProficiencies = new ArrayList<>();
    protected final List<AbilityScore> savingThrowProficiencies = new ArrayList<>();
    protected final List<Action> actions = new ArrayList<>();
    protected int bonusActions = 1;
    protected int proficiencyBonus = 2;

    public PlayerStats() {

    }

    private int checkValue(int val) {  // Ensure value is within bounds
        return Math.min(20, Math.max(0, val));
    }

    public static int getExpToLevel(int level) {
        switch (level) {
            case 1:
                return 0;
            case 2:
                return 300;
            case 3:
                return 900;
            case 4:
                return 2700;
            case 5:
                return 6500;
            case 6:
                return 14000;
            case 7:
                return 23000;
            case 8:
                return 34000;
            case 9:
                return 48000;
            case 10:
                return 64000;
            case 11:
                return 85000;
            default:  // Not implemented yet
            case 12:
                return 100_000;
        }
    }

    public int getBasicKillExp() {
        switch (level) {
            case 1:
                return 10;
            case 2:
                return 15;
            case 3:
                return 20;
            case 4:
                return 40;
            case 5:
                return 75;
            case 6:
                return 90;
            case 7:
                return 110;
            case 8:
                return 140;
            case 9:
                return 200;
            case 10:
                return 250;
            case 11:
                return 320;
            default:  // Not implemented yet
            case 12:
                return 400;
        }
    }

    public PlayerStats randomise() {  // Randomise each stat such that they all start at 8 and 30 points are distributed so that nothing is higher than 15
        int[] stats = new int[6];
        for (int i = 0; i < 6; i++) {
            stats[i] = 8;
        }
        int points = 30;
        while (points > 0) {
            int index = (int) (Math.random() * 6);
            if (stats[index] < 15) {
                stats[index]++;
                points--;
            }
        }
        dexterity = stats[0];
        charisma = stats[1];
        strength = stats[2];
        intelligence = stats[3];
        wisdom = stats[4];
        constitution = stats[5];
        return this;
    }

    public void addBaseActions() {
        actions.add(new Dash());
        actions.add(new Shove());
    }

    public int get(AbilityScore score) {
        switch (score) {
            case Dexterity:
                return dexterity;
            case Wisdom:
                return wisdom;
            case Charisma:
                return charisma;
            case Strength:
                return strength;
            case Constitution:
                return constitution;
            case Intelligence:
                return intelligence;
        }
        throw new RuntimeException("Invalid ability score");
    }

    public abstract DndClass getDndClass();

    public abstract void setLevel(int lvl);

    public void incrementExp(int amount) {
        exp += amount;
    }

    public boolean canLevelUp() {
        return exp >= getExpToLevel(level + 1);
    }

    public void levelUp() {
        level++;
    }

    public void setDexterity(int dexterity) {
        this.dexterity = checkValue(dexterity);
    }

    public void setCharisma(int charisma) {
        this.charisma = checkValue(charisma);
    }

    public void setStrength(int strength) {
        this.strength = checkValue(strength);
    }

    public void setIntelligence(int intelligence) {
        this.intelligence = checkValue(intelligence);
    }

    public void setWisdom(int wisdom) {
        this.wisdom = checkValue(wisdom);
    }

    public void setConstitution(int constitution) {
        this.constitution = checkValue(constitution);
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public boolean isProficient(WeaponType type) {
        return weaponProficiencies.contains(type);
    }

    public boolean isProficient(Skill type) {
        return skillProficiencies.contains(type);
    }

    public boolean isProficient(AbilityScore type) {  // Saving throw
        return savingThrowProficiencies.contains(type);
    }

    public int getProficiencyBonus() {
        return proficiencyBonus;
    }

    public void consumeSpellSlot(int lvl) {
        if (usedSpellSlots.containsKey(lvl)) {
            usedSpellSlots.put(lvl, usedSpellSlots.get(lvl) + 1);
        } else {
            usedSpellSlots.put(lvl, 1);
        }
    }

    public void refillSpellSlots() {
        usedSpellSlots.clear();
    }

    public HashMap<Integer, Integer> getRemainingSpellSlots() {
        HashMap<Integer, Integer> remaining = new HashMap<>();
        for (int i = 1; i <= 6; i++) {
            int remainingSlots = spellSlots.getOrDefault(i, 0) - usedSpellSlots.getOrDefault(i, 0);
            if (remainingSlots > 0) {
                remaining.put(i, remainingSlots);
            }
        }
        return remaining;
    }

    public List<Action> getActions() {
        return actions;
    }

    public int getLevel() {
        return level;
    }

    public int getMovementSpeed() {
        return movementSpeed;
    }
}
