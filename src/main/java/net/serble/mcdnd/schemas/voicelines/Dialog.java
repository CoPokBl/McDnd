package net.serble.mcdnd.schemas.voicelines;

import java.util.HashMap;
import java.util.Map;

public class Dialog {
    private Map<String, VoiceLineSection> sections;

    public Dialog(Map<String, VoiceLineSection> sections) {
        this.sections = sections;
    }

     public VoiceLineSection getSection(String name) {
        return sections.get(name);
     }

     public String getFirstSection() {
        return sections.keySet().iterator().next();
     }
}
