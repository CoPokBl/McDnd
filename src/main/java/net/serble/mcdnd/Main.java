package net.serble.mcdnd;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {
    private static Main instance;
    private ConflictManager conflict;
    private TeamManager teams;
    private PlayerManager players;
    private CombatManager combat;
    private EnvironmentManager environment;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        instance = this;
        conflict = new ConflictManager();
        teams = new TeamManager();
        players = new PlayerManager();
        combat = new CombatManager();
        environment = new EnvironmentManager();

        Bukkit.getPluginManager().registerEvents(conflict, this);
        Bukkit.getPluginManager().registerEvents(players, this);
        Bukkit.getPluginManager().registerEvents(combat, this);
        Bukkit.getPluginManager().registerEvents(environment, this);

        Bukkit.getLogger().info("McDnd has been enabled!");
    }

    @Override
    public void onDisable() {

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

}
