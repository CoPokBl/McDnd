package net.serble.mcdnd.commands;

import net.serble.mcdnd.Utils;
import net.serble.mcdnd.ai.SpeedyZombie;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Objects;

public class DndCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(Utils.t("&aMcDnd by CoPokBl"));
            return false;
        }

        if (Objects.equals(args[0], "roll")) {
            if (args.length < 2) {
                sender.sendMessage(Utils.t("&c/dnd roll <roll>"));
                return false;
            }
            sender.sendMessage(Utils.t("&aRolled: " + Utils.roll(args[1])));
            return true;
        }

        if (Objects.equals(args[0], "zombie")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(Utils.t("&cMust be a player"));
                return false;
            }

            SpeedyZombie.spawn(((Player) sender).getLocation());
            sender.sendMessage(Utils.t("&aSpawned speedy zombie"));
            return true;
        }

        sender.sendMessage(Utils.t("&cInvalid subcommand."));
        return false;
    }
}
