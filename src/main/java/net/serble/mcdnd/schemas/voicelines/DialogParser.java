package net.serble.mcdnd.schemas.voicelines;

import net.serble.mcdnd.Main;
import net.serble.mcdnd.Tuple;
import net.serble.mcdnd.schemas.Skill;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;

@SuppressWarnings("unchecked")
public class DialogParser {

    public static Map<String, Dialog> parseAll() {
        // List all files in the speech dir
        File folder = new File(Main.getInstance().getDataFolder(), "speech");
        File[] files = folder.listFiles();
        Map<String, Dialog> dialogs = new HashMap<>();
        if (files == null) {
            return dialogs;
        }
        for (File file : files) {
            try {
                YamlConfiguration config = new YamlConfiguration();
                config.load(file);

                ConfigurationSection data = config.getConfigurationSection("data");
                if (data == null) {
                    continue;
                }

                Map<String, VoiceLineSection> sections = new HashMap<>();

                for (String lineKey : data.getKeys(false)) {
                    ConfigurationSection line = data.getConfigurationSection(lineKey);
                    if (line == null) {
                        continue;
                    }

                    String[] speech = line.getStringList("lines").toArray(new String[0]);

                    ConfigurationSection choices = line.getConfigurationSection("choices");
                    if (choices == null) {
                        continue;
                    }

                    List<VoiceLineChoice> choicesList = new ArrayList<>();

                    for (String choiceKey : choices.getKeys(false)) {
                        Bukkit.getLogger().info("Choice key: " + choiceKey);
                        ConfigurationSection choice = choices.getConfigurationSection(choiceKey);
                        if (choice == null) {
                            continue;
                        }

                        String text = choice.getString("text", "FAILED TO LOAD");
                        String skillStr = choice.getString("skill");
                        Skill skill = Objects.equals(skillStr, "none") ? null : Skill.valueOf(skillStr);
                        int dc = choice.getInt("dc", 0);

                        // Actions
                        Tuple<String, String[]>[] success = new Tuple[0];
                        Tuple<String, String[]>[] failure = new Tuple[0];

                        ConfigurationSection successSec = choice.getConfigurationSection("success");
                        if (successSec != null) {
                            success = getActionsFromSection(successSec);
                        }

                        ConfigurationSection failureSec = choice.getConfigurationSection("failure");
                        if (failureSec != null) {
                            failure = getActionsFromSection(failureSec);
                        }

                        choicesList.add(new VoiceLineChoice(text, skill, dc, success, failure));
                    }

                    VoiceLineSection sec = new VoiceLineSection(speech, choicesList.toArray(new VoiceLineChoice[0]));
                    sections.put(lineKey, sec);
                }

                dialogs.put(file.getName(), new Dialog(sections));
            } catch (Exception e) {
                Main.getInstance().getLogger().warning("Error parsing speech file: " + file.getName());
            }
        }

        return dialogs;
    }

    private static Tuple<String, String[]>[] getActionsFromSection(ConfigurationSection section) {
        List<Tuple<String, String[]>> actions = new ArrayList<>();
        for (String key : section.getKeys(false)) {
            ConfigurationSection action = section.getConfigurationSection(key);
            if (action == null) {
                continue;
            }

            String type = action.getString("type", "");
            List<String> args = new ArrayList<>();

            switch (type) {
                case "leave":
                    break;

                case "jump":
                    args.add(action.getString("target"));
                    break;

                default:  // Idk man
                    break;
            }

            actions.add(new Tuple<>(type, args.toArray(new String[0])));
        }

        // Literally look up, I'm right
        // noinspection unchecked
        return actions.toArray(new Tuple[0]);
    }
}
