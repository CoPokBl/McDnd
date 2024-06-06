package net.serble.mcdnd.schemas.voicelines;

import net.serble.mcdnd.Tuple;
import net.serble.mcdnd.schemas.Skill;

public class VoiceLineChoice {
    private final String text;
    private final Skill skill;
    private final int difficultClass;
    private final Tuple<String, String[]>[] successActions;
    private final Tuple<String, String[]>[] failActions;

    public VoiceLineChoice(String text, Skill skill, int difficultClass, Tuple<String, String[]>[] successActions, Tuple<String, String[]>[] failActions) {
        this.text = text;
        this.skill = skill;
        this.difficultClass = difficultClass;
        this.successActions = successActions;
        this.failActions = failActions;
    }

    public Skill getSkill() {
        return skill;
    }

    public int getDifficultClass() {
        return difficultClass;
    }

    public String getText() {
        return text;
    }

    public Tuple<String, String[]>[] getFailActions() {
        return failActions;
    }

    public Tuple<String, String[]>[] getSuccessActions() {
        return successActions;
    }
}
