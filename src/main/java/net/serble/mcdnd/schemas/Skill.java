package net.serble.mcdnd.schemas;

public enum Skill {
    Athletics(AbilityScore.Dexterity),
    Acrobatics(AbilityScore.Dexterity),
    SleightOfHand(AbilityScore.Dexterity),
    Stealth(AbilityScore.Dexterity),
    AnimalHandling(AbilityScore.Wisdom),
    Insight(AbilityScore.Wisdom),
    Medicine(AbilityScore.Wisdom),
    Perception(AbilityScore.Wisdom),
    Survival(AbilityScore.Wisdom),
    Arcana(AbilityScore.Intelligence),
    History(AbilityScore.Intelligence),
    Investigation(AbilityScore.Intelligence),
    Nature(AbilityScore.Intelligence),
    Religion(AbilityScore.Intelligence),
    Deception(AbilityScore.Charisma),
    Intimidation(AbilityScore.Charisma),
    Performance(AbilityScore.Charisma),
    Persuasion(AbilityScore.Charisma);

    private final AbilityScore rollType;

    Skill(AbilityScore type) {
        rollType = type;
    }

    public AbilityScore getRollType() {
        return rollType;
    }
}
