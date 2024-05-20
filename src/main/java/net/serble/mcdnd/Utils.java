package net.serble.mcdnd;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.serble.mcdnd.schemas.Combatant;
import net.serble.mcdnd.schemas.Damage;
import net.serble.mcdnd.schemas.DamageType;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.NumberConversions;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class Utils {

    public static String t(String msg) {
        return ChatColor.translateAlternateColorCodes('&', msg);
    }

    public static int roll(String str) {
        Integer[] parsedRoll = parseRollString(str);
        int dice = parsedRoll[0];
        int sides = parsedRoll[1];
        int addition = parsedRoll[2];

        int total = 0;
        for (int i = 0; i < dice; i++) {
            total += (int) (Math.random() * sides) + 1;
        }
        return total + addition;
    }

    public static int roll(String str, int adv) {
        int roll1 = roll(str);

        if (adv == 0) {
            return roll1;
        }

        int roll2 = roll(str);

        return adv == 1 ? Math.max(roll1, roll2) : Math.min(roll1, roll2);
    }

    // Parse things like 1d4 or 2d20
    public static Integer[] parseRollString(String str) {
        String[] parts1 = str.split("d");
        if (parts1.length != 2) {
            throw new RuntimeException("Invalid die format: " + str);
        }

        int dice = Integer.parseInt(parts1[0]);

        int sides;
        int addition = 0;

        String[] partsAdd = parts1[1].split("\\+");
        String[] partsSub = parts1[1].split("-");
        if (partsAdd.length > 1) {
            addition = Integer.parseInt(partsAdd[1]);
            sides = Integer.parseInt(partsAdd[1]);
        } else if (partsSub.length > 1) {
            addition = -Integer.parseInt(partsSub[1]);
            sides = Integer.parseInt(partsSub[1]);
        } else {
            sides = Integer.parseInt(parts1[1]);
        }

        return new Integer[] {
                dice,
                sides,
                addition
        };
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
        Integer[] parsedRoll = parseRollString(roll);
        int dice = parsedRoll[0];
        int sides = parsedRoll[1];
        int add = parsedRoll[2];
        return new Tuple<>(dice + add, dice * sides + add);
    }

    public static String getRollDisplayRange(String roll) {
        Tuple<Integer, Integer> range = calculateRollRange(roll);
        return range.a() + "~" + range.b();
    }

    public static String getDamageDisplayRange(Damage damage) {
        Tuple<Integer, Integer> range = calculateDamageRange(damage);
        return range.a() + "~" + range.b();
    }

    public static Tuple<Integer, Integer> calculateDamageRange(Damage damage) {
        List<Tuple<Integer, Integer>> individualRanges = new ArrayList<>();
        for (Tuple<DamageType, String> dmg : damage.getDamages()) {
            Tuple<Integer, Integer> rollRange = calculateRollRange(dmg.b());
            individualRanges.add(rollRange);
        }

        // Add up the values
        int min = 0;
        int max = 0;
        for (Tuple<Integer, Integer> range : individualRanges) {
            min += range.a();
            max += range.b();
        }

        return new Tuple<>(min, max);
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

    public static double getMaxHealth(LivingEntity entity) {
        AttributeInstance maxHealthAtt = entity.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        assert maxHealthAtt != null;
        return maxHealthAtt.getValue();
    }

    public static void setMaxHealth(LivingEntity entity, double amount) {
        AttributeInstance maxHealthAtt = entity.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        assert maxHealthAtt != null;
        maxHealthAtt.setBaseValue(amount);
    }

    public static void healEntity(LivingEntity entity, double amount) {
        double health = entity.getHealth();
        double maxHealth = getMaxHealth(entity);
        double newHealth = Math.min(health + amount, maxHealth);
        entity.setHealth(newHealth);
    }

    public static void healEntity(LivingEntity entity) {
        healEntity(entity, 99999);
    }

    public static void setTarget(LivingEntity entity, LivingEntity target) {
        if (entity instanceof Mob) {
            ((Mob) entity).setTarget(target);
        }
    }

    public static boolean isFinite(Vector vec) {
        return NumberConversions.isFinite(vec.getX()) && NumberConversions.isFinite(vec.getY()) && NumberConversions.isFinite(vec.getZ());
    }

    public static int getStatMod(int stat) {
        return (int) (double) ((stat - 10) / 2);
    }

    public static void particleStream(Location from, Location to, Particle particle, int ticks) {
        Vector direction = to.toVector().subtract(from.toVector()).normalize().multiply(0.1);

        AtomicReference<Integer> timesRan = new AtomicReference<>(0);
        AtomicReference<BukkitTask> runningTask = new AtomicReference<>();
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(Main.getInstance(), () -> {
            Location current = from.clone();
            while (current.distance(to) > 0.5) {
                current.add(direction);
                Objects.requireNonNull(from.getWorld()).spawnParticle(particle, current, 1);
            }
            timesRan.set(timesRan.get() + 1);
            if (timesRan.get() >= ticks) {
                runningTask.get().cancel();
            }
        }, 0, 1);
        runningTask.set(task);
    }

    public static void particlePoint(Location loc, Particle particle, int ticks) {
        AtomicReference<Integer> timesRan = new AtomicReference<>(0);
        AtomicReference<BukkitTask> runningTask = new AtomicReference<>();
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(Main.getInstance(), () -> {
            Objects.requireNonNull(loc.getWorld()).spawnParticle(particle, loc, 1);
            timesRan.set(timesRan.get() + 1);
            if (timesRan.get() >= ticks) {
                runningTask.get().cancel();
            }
        }, 0, 1);
        runningTask.set(task);
    }

    public static String fromPascal(String pascalName) {
        List<String> words = new ArrayList<>();
        StringBuilder cWord = new StringBuilder();
        for (char c : pascalName.toCharArray()) {
            if (Character.isUpperCase(c) && !cWord.toString().isEmpty()) {
                words.add(cWord.toString());
                cWord = new StringBuilder();
            }
            cWord.append(c);
        }

        if (!cWord.toString().isEmpty()) {
            words.add(cWord.toString());
        }

        StringBuilder finalText = new StringBuilder();
        for (String word : words) {
            finalText.append(word).append(" ");
        }

        finalText.deleteCharAt(finalText.length()-1);
        return finalText.toString();
    }

    public static Damage parseDamage(String dmgStr) {
        String[] damages = dmgStr.split(",");
        List<Tuple<DamageType, String>> dmgs = new ArrayList<>();
        for (String dmg : damages) {
            String typeStr = dmg.substring(0, 2);
            DamageType type = DamageType.getFromPrefix(typeStr);
            dmgs.add(new Tuple<>(type, dmg.substring(2)));
        }
        return new Damage(dmgs);
    }

    public static String serialiseDamage(Damage damage) {
        StringBuilder str = new StringBuilder();
        for (Tuple<DamageType, String> dmg : damage.getDamages()) {
            str.append(dmg.a().getPrefix()).append(dmg.b()).append(",");
        }
        return str.substring(0, str.length() - 1);
    }
}
