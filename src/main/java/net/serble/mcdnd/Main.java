package net.serble.mcdnd;

import net.serble.mcdnd.commands.DndCommand;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class Main extends JavaPlugin {
    private static Main instance;
    private ConflictManager conflict;
    private TeamManager teams;
    private PlayerManager players;
    private CombatManager combat;
    private EnvironmentManager environment;
    private CustomItemManager items;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        instance = this;
        conflict = new ConflictManager();
        teams = new TeamManager();
        players = new PlayerManager();
        combat = new CombatManager();
        environment = new EnvironmentManager();
        items = new CustomItemManager();

        Bukkit.getPluginManager().registerEvents(conflict, this);
        Bukkit.getPluginManager().registerEvents(players, this);
        Bukkit.getPluginManager().registerEvents(combat, this);
        Bukkit.getPluginManager().registerEvents(environment, this);
        Bukkit.getPluginManager().registerEvents(items, this);

        Objects.requireNonNull(getCommand("dnd")).setExecutor(new DndCommand());

        // Enable the AI of all LivingEntities in the world
        getEnvironmentManager().patchAllMobs();

        Bukkit.getLogger().info("McDnd has been enabled!");
    }

    @Override
    public void onDisable() {
        getConflictManager().endAllConflicts();
    }

    public static Main getInstance() {
        return instance;
    }

    public ConflictManager getConflictManager() {
        return conflict;
    }

    public TeamManager getTeamManager() {
        return teams;
    }

    public PlayerManager getPlayerManager() {
        return players;
    }

    public CombatManager getCombatManager() {
        return combat;
    }

    public EnvironmentManager getEnvironmentManager() {
        return environment;
    }

    public CustomItemManager getItemManager() {
        return items;
    }

}
