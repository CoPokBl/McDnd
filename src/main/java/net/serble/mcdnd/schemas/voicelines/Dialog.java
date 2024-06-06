package net.serble.mcdnd.schemas.voicelines;

import java.util.Map;

public class Dialog {
    private final Map<String, VoiceLineSection> sections;
    private final String startSection;

    public Dialog(Map<String, VoiceLineSection> sections, String startSection) {
        this.sections = sections;
        this.startSection = startSection;
    }

     public VoiceLineSection getSection(String name) {
        return sections.get(name);
     }

     /** Actually returns the last section because they are added in reverse order for some reason */
     public String getFirstSection() {  // Get the last section key
        return sections.keySet().toArray(new String[0])[sections.size() - 1];
     }

    public String getStartSection() {
        return startSection;
    }
}
