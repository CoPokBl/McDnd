package net.serble.mcdnd.commands;

import net.serble.mcdnd.Main;
import net.serble.mcdnd.NbtHandler;
import net.serble.mcdnd.Utils;
import net.serble.mcdnd.ai.SpeedyZombie;
import net.serble.mcdnd.classes.Rogue;
import net.serble.mcdnd.schemas.PlayerStats;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.Objects;

public class DndCommand implements CommandExecutor {
    private void s(CommandSender s, String msg) {
        s.sendMessage(Utils.t(msg));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            s(sender, "&aMcDnd by CoPokBl");
            return false;
        }

        if (Objects.equals(args[0], "roll")) {
            if (args.length < 2) {
                s(sender, "&c/dnd roll <roll>");
                return false;
            }
            s(sender, "&aRolled: " + Utils.roll(args[1]));
            return true;
        }

        if (!(sender instanceof Player)) {
            s(sender, "&cMust be a player");
            return false;
        }
        Player p = (Player) sender;

        if (Objects.equals(args[0], "zombie")) {
            SpeedyZombie.spawn(p.getLocation());
            s(p, "&aSpawned speedy zombie");
            return true;
        }

        if (Objects.equals(args[0], "isrouge")) {
            s(p, "Is rouge: " + (Main.getInstance().getPlayerManager().getStatsFor(p) instanceof Rogue));
            return true;
        }

        if (Objects.equals(args[0], "maxhealth")) {
            PlayerStats stats = Main.getInstance().getPlayerManager().getStatsFor(p);
            s(p, String.valueOf(stats.getMaxHealth()));
            return true;
        }

        if (Objects.equals(args[0], "setlevel")) {
            if (args.length < 2) {
                s(p, "&c/dnd setlevel <level>");
                return false;
            }
            PlayerStats stats = Main.getInstance().getPlayerManager().getStatsFor(p);
            stats.setLevel(Integer.parseInt(args[1]));
            s(p, "&aSet level to " + args[1]);
            return true;
        }

        if (Objects.equals(args[0], "genlore")) {
            ItemStack held = p.getInventory().getItemInMainHand();
            Main.getInstance().getItemManager().generateLore(held);
            s(p, "&aDone!");
            return true;
        }

        if (Objects.equals(args[0], "setdmg")) {
            if (args.length < 2) {
                s(p, "&c/dnd setdmg <damage>");
                return false;
            }

            ItemStack held = p.getInventory().getItemInMainHand();
            NbtHandler.itemStackSetTag(held, "damageroll", PersistentDataType.STRING, args[1]);
            Main.getInstance().getItemManager().generateLore(held);
            s(p, "&aDone!");
            return true;
        }

        if (Objects.equals(args[0], "selectchoice")) {
            if (args.length < 2) {
                s(p, "&c/dnd selectchoice <choiceIndex>");
                return false;
            }

            int choice = Integer.parseInt(args[1]);
            Main.getInstance().getSpeech().selectChoice(p, choice);
            return true;
        }

        s(p, "&cInvalid subcommand.");
        return false;
    }
}
