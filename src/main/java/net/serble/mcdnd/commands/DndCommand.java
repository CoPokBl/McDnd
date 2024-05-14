package net.serble.mcdnd.commands;

import net.serble.mcdnd.Main;
import net.serble.mcdnd.Utils;
import net.serble.mcdnd.ai.SpeedyZombie;
import net.serble.mcdnd.classes.Rouge;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
            s(p, "Is rouge: " + (Main.getInstance().getPlayerManager().getStatsFor(p) instanceof Rouge));
            return true;
        }

        s(p, "&cInvalid subcommand.");
        return false;
    }
}
