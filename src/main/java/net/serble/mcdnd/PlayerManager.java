package net.serble.mcdnd;

import net.serble.mcdnd.mobsheets.CowSheet;
import net.serble.mcdnd.mobsheets.DefaultSheet;
import net.serble.mcdnd.schemas.AbilityScore;
import net.serble.mcdnd.schemas.Combatant;
import net.serble.mcdnd.schemas.Conflict;
import net.serble.mcdnd.schemas.PlayerStats;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.*;

import java.util.*;

public class PlayerManager implements Listener {
    private final HashMap<UUID, PlayerStats> playerStats = new HashMap<>();
    private final HashMap<UUID, Inventory> openInvs = new HashMap<>();

    // Inv items
    private final ItemStack emptySlotCombat = Utils.makeItem(Material.RED_STAINED_GLASS_PANE, "&c");
    private final ItemStack emptySlotPeace = Utils.makeItem(Material.GREEN_STAINED_GLASS_PANE, "&c");
    private final ItemStack endTurn = Utils.makeItem(Material.BARRIER, "&cEnd Turn");
    private final ItemStack dash = Utils.makeItem(Material.LEATHER_BOOTS, "&bDash");
    private final ItemStack friendsMenu = Utils.makeItem(Material.CORNFLOWER, "&aAllies");
    private final ItemStack beFriends = Utils.makeItem(Material.SUNFLOWER, "&eMake Peace");

    public PlayerManager() {
        Bukkit.getScheduler().runTaskTimer(Main.getInstance(), () -> {
            for (Player p : Bukkit.getOnlinePlayers()) {
                updateScoreboard(p);
            }
        }, 20L, 20L);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();

        PlayerStats randomStats = PlayerStats.newRandom();
        playerStats.put(p.getUniqueId(), randomStats);

        p.sendMessage(Utils.t("Dexterity: " + randomStats.getDexterity()));
        p.sendMessage(Utils.t("Strength: " + randomStats.getStrength()));
        p.sendMessage(Utils.t("Charisma: " + randomStats.getCharisma()));
        p.sendMessage(Utils.t("Intelligence: " + randomStats.getIntelligence()));
        p.sendMessage(Utils.t("Wisdom: " + randomStats.getWisdom()));
        p.sendMessage(Utils.t("Constitution: " + randomStats.getConstitution()));

        updatePlayer(p);
    }

    @EventHandler
    public void onInvClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) {
            return;
        }

        Player p = (Player) e.getWhoClicked();

        if (e.getClickedInventory() == openInvs.get(p.getUniqueId())) {
            e.setCancelled(true);
            ItemStack clicked = e.getCurrentItem();
            if (clicked == null) {
                return;
            }
            String playerName = ChatColor.stripColor(Objects.requireNonNull(clicked.getItemMeta()).getDisplayName());
            Player other = Bukkit.getPlayer(playerName);
            if (other == null) {
                p.sendMessage(Utils.t("&cThat player doesn't exist"));
                return;
            }
            if (e.isLeftClick()) {  // Join team
                if (!Main.getInstance().getTeamManager().isInvited(other, p)) {
                    p.sendMessage(Utils.t("&cYou are not invited"));
                    return;
                }
                Main.getInstance().getTeamManager().addPlayerToTeam(other, p);
                other.sendMessage(Utils.t("&6" + p.getName() + "&a joined your team!"));
                p.sendMessage(Utils.t("&aYou joined &6" + playerName + "'s&a team!"));
            } else if (e.isRightClick()) {  // Invite to team
                Main.getInstance().getTeamManager().setPlayersInvite(p, other);
                p.sendMessage(Utils.t("&aInvited &6" + playerName + "&a to your team"));
                other.sendMessage(Utils.t("&aYou were invited to join &6" + p.getName() + "'s&a team"));
            }
            return;
        }

        if (e.getClickedInventory() == null) {
            return;
        }

        if (e.getClickedInventory().getType() != InventoryType.PLAYER) {
            return;
        }

        updateScoreboard(p);

        if (e.getSlot() < 9 || e.getSlot() > 17) {
            return;
        }

        // It's the top inventory raw
        e.setCancelled(true);

        ItemStack stack = e.getClickedInventory().getItem(e.getSlot());

        if (stack == null) {
            return;
        }

        Conflict conflict = Main.getInstance().getConflictManager().getConflict(p);
        boolean inConflict = conflict != null;
        if (stack.isSimilar(endTurn)) {
            Main.getInstance().getConflictManager().endPlayersTurn(p);
        }
        if (stack.isSimilar(dash) && inConflict) {
            if (conflict.currentTurnActionsRemaining < 1) {
                p.sendMessage(Utils.t("&cYou have no more actions"));
                return;
            }
            conflict.currentTurnMovementRemaining += 18;
            conflict.announce("&6" + p.getName() + "&a used dash");
            conflict.currentTurnActionsRemaining--;
            Main.getInstance().getConflictManager().trackMovement(conflict);
        }
        if (stack.isSimilar(friendsMenu)) {
            Inventory inv = createTeamInv(p);
            openInvs.put(p.getUniqueId(), inv);
            p.openInventory(inv);
        }
        if (stack.isSimilar(beFriends) && inConflict) {
            conflict.votePeace(p);
            conflict.announce("&6" + p.getName() + "&e has voted for peace");
            if (conflict.countAlive() <= conflict.countPeopleWantingPeace()) {
                Main.getInstance().getConflictManager().endConflict(conflict);
            }
        }
    }

    public void updatePlayer(Player p) {
        // Disable shields
        p.setCooldown(Material.SHIELD, 999999);

        updateScoreboard(p);
        Inventory inv = p.getInventory();

        boolean inCombat = Main.getInstance().getConflictManager().isInCombat(p);

        for (int i = 9; i < 18; i++) {
            inv.setItem(i, inCombat ? emptySlotCombat : emptySlotPeace);
        }

        inv.setItem(9, friendsMenu);
        if (inCombat) {
            inv.setItem(10, beFriends);
            Conflict conflict = Main.getInstance().getConflictManager().getConflict(p);

            // Actual slots and glowing
            if (Main.getInstance().getConflictManager().isTurn(p)) {
                inv.setItem(17, endTurn);
                if (conflict.currentTurnActionsRemaining > 0) {
                    inv.setItem(16, dash);
                }
            }
        }

    }

    private Inventory createTeamInv(Player p) {
        Inventory inv = Bukkit.createInventory(null, 9, Utils.t("&aAllies"));

//        if (Main.getInstance().getTeamManager().isInTeam(p)) {
        if (false) {
            // Nothing for now
        }
        else {
            int cIndex = 0;
            for (Player p2 : Bukkit.getOnlinePlayers()) {
                if (p2 == p) {
                    continue;
                }

                ItemStack item = Utils.getPlayerHead(p2);
                Utils.setLore(item, "&aClick to join team", "&aRight click to invite to team");

                inv.setItem(cIndex, item);
                cIndex++;
            }
        }

        return inv;
    }

    private PlayerStats getStatsFor(LivingEntity e) {
        if (playerStats.containsKey(e.getUniqueId())) {
            return playerStats.get(e.getUniqueId());
        }

        // Try to get mob types
        switch (e.getType()) {
            case COW:
                return CowSheet.stats;
        }

        Bukkit.getLogger().warning("Could not get stats for entity type: " + e.getType());
        return DefaultSheet.stats;
    }

    public boolean abilityCheck(LivingEntity p, AbilityScore ability, int passValue, int adv) {
        return abilityRoll(p, ability, adv) >= passValue;
    }

    public int getStatMod(LivingEntity p, AbilityScore ability) {
        PlayerStats stats = getStatsFor(p);
        int stat = stats.get(ability);

        return (int) (double) ((stat - 10) / 2);
    }

    public int abilityRoll(LivingEntity p, AbilityScore ability, int adv) {
        int mod = getStatMod(p, ability);

        int roll = Utils.roll(20);
        if (adv != 0) {
            int roll2 = Utils.roll(20);
            if (adv == 1) {
                roll = Math.max(roll, roll2);
            } else {
                roll = Math.min(roll, roll2);
            }
        }

        if (roll == 20) {  // Nat 20
            return 999999;
        }
        if (roll == 1) {  // Nat 1
            return -99999;
        }

        return roll + mod;
    }

    private void updateScoreboard(Player player) {
        // scoreboard
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        assert manager != null;
        Scoreboard main = manager.getNewScoreboard();

        // title
        String text = "&6Dungeons &7and &bDiamonds";
        Objective obj = main.registerNewObjective("dnd", Criteria.DUMMY, Utils.t(text));
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        List<String> lines = new ArrayList<>();
        lines.add("&6Armor Class: &7" + Main.getInstance().getCombatManager().calculateArmorClass(player));

        Conflict conflict = Main.getInstance().getConflictManager().getConflict(player);
        if (conflict == null) {
            lines.add("&aNot in combat");
        } else {
            lines.add("&cIn combat");
            lines.add("&r");
            lines.add("&7Turn Movement: " + (int)conflict.currentTurnMovementRemaining);
            lines.add("&7Remaining Actions: " + conflict.currentTurnActionsRemaining);
            lines.add("&7Bonus Actions: " + conflict.currentTurnBonusActionsRemaining);
            lines.add("");

            for (Combatant enemy : conflict.getTurns()) {  // Turns gives the order
                if (enemy.isDead()) {
                    lines.add("&7&m> &6&m" + enemy.getName());
                    continue;
                }
                AttributeInstance maxHealthAtt = enemy.getEntity().getAttribute(Attribute.GENERIC_MAX_HEALTH);
                assert maxHealthAtt != null;
                String healthInfo = " &7(" + ((int) enemy.getEntity().getHealth()) + "/" + ((int) maxHealthAtt.getValue()) + ")";

                int relationship = Main.getInstance().getTeamManager().getRelationship(enemy.getEntity(), player);
                String relColour = relationship == -1 ? "&c" : relationship == 0 ? "&e" : "&a";

                if (conflict.isCurrentTurn(enemy)) {
                    lines.add("&7&l> " + relColour + "&l" + enemy.getName() + healthInfo);
                } else {
                    lines.add("&7> " + relColour + enemy.getName() + healthInfo);
                }
            }
        }

        int cScore = lines.size();
        for (String line : lines) {
            Score score = obj.getScore(Utils.t(line));
            score.setScore(cScore);
            cScore--;
        }

        player.setScoreboard(main);
    }

}
