package net.serble.mcdnd;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.serble.mcdnd.schemas.Combatant;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Utils {

    public static String t(String msg) {
        return ChatColor.translateAlternateColorCodes('&', msg);
    }

    public static int roll(int sides) {
        return (int) (Math.random() * sides) + 1;
    }

    public static int roll(int dice, int sides) {
        int total = 0;
        for (int i = 0; i < dice; i++) {
            total += roll(sides);
        }
        return total;
    }

    public static int roll(Tuple<Integer, Integer> tuple) {
        return roll(tuple.a(), tuple.b());
    }

    public static int roll(String str) {
        return roll(Objects.requireNonNull(parseRollString(str)));
    }

    // Parse things like 1d4 or 2d20
    public static Tuple<Integer, Integer> parseRollString(String str) {
        String[] parts = str.split("d");
        if (parts.length != 2) {
            return null;
        }

        try {
            int dice = Integer.parseInt(parts[0]);
            int sides = Integer.parseInt(parts[1]);
            return new Tuple<>(dice, sides);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static void setActionBar(Player p, String msg) {
        BaseComponent[] message2 = {
                new TextComponent(t(msg)),
        };
        p.spigot().sendMessage(ChatMessageType.ACTION_BAR, message2);
    }

    public static ItemStack makeItem(Material mat, String name) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        assert meta != null;
        meta.setDisplayName(t(name));
        item.setItemMeta(meta);
        return item;
    }

    public static int clamp(int val, int min, int max) {
        return Math.min(Math.max(val, min), max);
    }

    public static List<String> toStringList(String... arr) {
        List<String> ls = new ArrayList<>();
        for (String s : arr) {
            ls.add(t(s));
        }
        return ls;
    }

    public static void setLore(ItemStack item, String... lines) {
        ItemMeta meta = item.getItemMeta();
        assert meta != null;
        meta.setLore(toStringList(lines));
        item.setItemMeta(meta);
    }

    public static Tuple<Integer, Integer> calculateRollRange(String roll) {
        Tuple<Integer, Integer> parsedRoll = parseRollString(roll);
        if (parsedRoll == null) {
            return null;
        }
        return new Tuple<>(parsedRoll.a(), parsedRoll.a() * parsedRoll.b());
    }

    public static String getRollDisplayRange(String roll) {
        Tuple<Integer, Integer> range = calculateRollRange(roll);
        assert range != null;
        return range.a() + "~" + range.b();
    }

    public static void playSound(Sound sound, LivingEntity... players) {
        for (LivingEntity e : players) {
            if (e instanceof Player) {
                ((Player) e).playSound(e, sound, 1, 1);
            }
        }
    }

    public static Combatant[] addCombatant(Combatant[] arr, Combatant combatant) {
        Combatant[] newArr = new Combatant[arr.length + 1];
        System.arraycopy(arr, 0, newArr, 0, arr.length);
        newArr[arr.length] = combatant;
        return newArr;
    }

    public static ItemStack getPlayerHead(Player p) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta meta = head.getItemMeta();
        assert meta != null;
        meta.setDisplayName(t("&6" + p.getName()));
        head.setItemMeta(meta);
        return head;
    }

}
