package net.serble.mcdnd.schemas.voicelines;

public class VoiceLineSection {
    private final String[] lines;
    private final VoiceLineChoice[] choices;

    public VoiceLineSection(String[] lines, VoiceLineChoice[] choices) {
        this.lines = lines;
        this.choices = choices;
    }

    public String[] getLines() {
        return lines;
    }

    public VoiceLineChoice[] getChoices() {
        return choices;
    }
}
