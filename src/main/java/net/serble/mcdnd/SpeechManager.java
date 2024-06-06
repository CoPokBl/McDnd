package net.serble.mcdnd;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Text;
import net.serble.mcdnd.schemas.voicelines.Dialog;
import net.serble.mcdnd.schemas.voicelines.DialogParser;
import net.serble.mcdnd.schemas.voicelines.VoiceLineChoice;
import net.serble.mcdnd.schemas.voicelines.VoiceLineSection;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.*;

public class SpeechManager implements Listener {
    private final Map<String, Dialog> dialogs;
    private final Map<UUID, String> speechStatuses = new HashMap<>();  // <EntityID, DialogKey>
    private final Map<UUID, String> activeChoice = new HashMap<>();

    public SpeechManager() {
        dialogs = DialogParser.parseAll();
        for (String key : dialogs.keySet()) {
            Bukkit.getLogger().info("Loaded dialog: " + key);
        }
    }

    public boolean isInDialog(LivingEntity e) {
        return speechStatuses.containsKey(e.getUniqueId());
    }

    public void startDialog(LivingEntity e, String dialogKey) {
        if (!(e instanceof Player)) {
            return;
        }

        Player p = (Player) e;
        if (isInDialog(p)) {
            throw new RuntimeException("Entity is already in dialog");
        }

        Dialog dialog = dialogs.get(dialogKey);
        if (dialog == null) {
            throw new RuntimeException("Dialog does not exist: " + dialogKey);
        }

        speechStatuses.put(e.getUniqueId(), dialogKey);
        triggerDialogOption(p, dialog.getStartSection());
    }

    public void triggerDialogOption(Player p, String key) {
        Dialog dialog = dialogs.get(speechStatuses.get(p.getUniqueId()));
        if (dialog == null) {
            throw new RuntimeException("Dialog does not exist");
        }

        VoiceLineSection sec = dialog.getSection(key);

        if (sec == null) {
            throw new RuntimeException("Voice line section doesn't exist for key: " + key);
        }

        int cDelay = 0;
        for (String line : sec.getLines()) {
            sayTextLater(p, line, cDelay);
            cDelay += 20;
        }

        activeChoice.put(p.getUniqueId(), key);

        ComponentBuilder componentBuilder = new ComponentBuilder()
                .append(Utils.t("&3------------------------------\n&6Dialog Options:\n"));

        int cIndex = 0;
        for (VoiceLineChoice choice : sec.getChoices()) {
            componentBuilder.append(Utils.t("&7> "));
            if (choice.getSkill() != null) {
                componentBuilder
                        .append(Utils.t("&r[" + choice.getSkill().name() + "] "))
                        .event(new HoverEvent(
                                HoverEvent.Action.SHOW_TEXT,
                                new Text(Utils.t("&6Bonus: &7" + Main.getInstance().getPlayerManager().getStatMod(p, choice.getSkill().getRollType())))));
            }
            componentBuilder
                    .append(Utils.t("&7" + choice.getText() + "\n"))
                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Click to select")))
                    .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/dnd selectchoice " + cIndex));
            cIndex++;
        }
        componentBuilder.append(Utils.t("&3------------------------------"));

        Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> p.spigot().sendMessage(componentBuilder.build()), cDelay);
    }

    public void selectChoice(Player p, int choice) {
        if (!isInDialog(p)) {
            p.sendMessage(Utils.t("&cYou are not in dialog"));
            return;
        }

        Dialog dialog = dialogs.get(speechStatuses.get(p.getUniqueId()));
        if (dialog == null) {
            throw new RuntimeException("Dialog does not exist");
        }

        String currentChoiceKey = activeChoice.get(p.getUniqueId());
        VoiceLineSection section = dialog.getSection(currentChoiceKey);
        VoiceLineChoice choiceObj = section.getChoices()[choice];

        boolean success = true;
        if (choiceObj.getSkill() != null) {
            // Ability Check
            success = Main.getInstance().getPlayerManager().abilityCheck(
                    p,
                    choiceObj.getSkill(),
                    choiceObj.getDifficultClass(),
                    0);

            p.sendMessage(Utils.t("&7Ability Check Result (" + choiceObj.getSkill() + " [DC " + choiceObj.getDifficultClass() + "]): " + Utils.successFailStr(success)));
        }

        Tuple<String, String[]>[] actions = success ? choiceObj.getSuccessActions() : choiceObj.getFailActions();
        for (Tuple<String, String[]> action : actions) {
            switch (action.a()) {  // Type
                case "leave": {
                    p.sendMessage(Utils.t("&7&oConversation ended."));
                    speechStatuses.remove(p.getUniqueId());
                    break;
                }

                case "jump": {  // Jump ends all other actions
                    String target = action.b()[0];
                    triggerDialogOption(p, target);
                    return;
                }
            }
        }
    }

    public void sayTextLater(LivingEntity e, String text, int delay) {
        Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
            e.sendMessage(Utils.t(text));
            Utils.playSound(Sound.ENTITY_EXPERIENCE_ORB_PICKUP, e);
        }, delay);
    }

    public void triggerDialogOptionLater(Player e, String option, int delay) {
        Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> triggerDialogOption(e, option), delay);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR) {
            return;
        }

        // Check if it was on an entity
        Entity hitEntity = Main.getInstance().getRayCaster().entityRayCast(event.getPlayer(), 100);

        if (hitEntity == null) {
            return;
        }

        if (isInDialog(event.getPlayer())) {  // Only check if they clicked an entity
            event.getPlayer().sendMessage(Utils.t("&cYou are already in conversation"));
            return;
        }

        // They interacted with entity, can we talk to them
        startDialog(event.getPlayer(), "test.yml");
    }
}
