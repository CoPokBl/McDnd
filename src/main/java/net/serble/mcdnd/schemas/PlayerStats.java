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

    public PlayerStats randomise() {
        setDexterity((int) (Math.random() * 19) + 1);
        setCharisma((int) (Math.random() * 19) + 1);
        setStrength((int) (Math.random() * 19) + 1);
        setIntelligence((int) (Math.random() * 19) + 1);
        setWisdom((int) (Math.random() * 19) + 1);
        setConstitution((int) (Math.random() * 19) + 1);
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

    public void incrementLevel() {
        setLevel(level + 1);
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
