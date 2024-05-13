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
import org.bukkit.Sound;
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
    private final ItemStack shortRest = Utils.makeItem(Material.BREAD, "&bShort Rest");
    private final ItemStack longRest = Utils.makeItem(Material.RED_BED, "&bLong Rest");
    private final ItemStack spells = Utils.makeItem(Material.BLAZE_ROD, "&eSpells");

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
        if (stack.isSimilar(endTurn) && inConflict) {
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
            if (!conflict.doesWantPeace(p)) {
                conflict.votePeace(p);
                // Make person neutral to everyone else who has made peace
                for (LivingEntity other : conflict.getParticipants()) {
                    if (other == p) {
                        continue;
                    }
                    if (conflict.doesWantPeace(other)) {
                        Main.getInstance().getTeamManager().setRelationship(p, other, 0);
                    }
                }

                conflict.announce("&6" + p.getName() + "&e has voted for peace");
                if (conflict.countAlive() <= conflict.countPeopleWantingPeace()) {
                    Main.getInstance().getConflictManager().endConflict(conflict);
                } else {
                    Main.getInstance().getConflictManager().checkEndConflict(conflict);
                }
            } else {
                conflict.unvotePeace(p);
                conflict.announce("&6" + p.getName() + "&c has removed their vote for peace");
            }
        }
        if (stack.isSimilar(shortRest) && !inConflict) {
            if (Main.getInstance().getTeamManager().getRemainingShortRests(p) > 0) {
                shortRest(p);
            } else {
                p.sendMessage(Utils.t("&cYou have no remaining short rests"));
            }
        }
        if (stack.isSimilar(longRest) && !inConflict) {
            if (Main.getInstance().getItemManager().hasAtLeastFood(p, 20)) {
                longRest(p);
            } else {
                p.sendMessage(Utils.t("&cYou don't have enough camp supplies"));
            }
        }
        if (stack.isSimilar(spells)) {
            // Spells
        }

        updatePlayer(p);
    }

    public void shortRest(Player p) {
        List<LivingEntity> members = Main.getInstance().getTeamManager().getTeamMembers(p);
        int remainingRests = Main.getInstance().getTeamManager().getRemainingShortRests(p);
        int newFoodLevel = remainingRests == 1 ? 7 : 15;  // We haven't decremented it yet
        for (LivingEntity m : members) {
            double healAmount = Utils.getMaxHealth(m) / 2;
            Utils.healEntity(m, healAmount);
            Utils.playSound(Sound.ENTITY_PLAYER_LEVELUP, m);
            if (m instanceof Player) {
                ((Player) m).setFoodLevel(newFoodLevel);
            }
            m.sendMessage(Utils.t("&aSuccessfully short rested!"));
        }
        Main.getInstance().getTeamManager().decrementRemainingShortRests(p);
    }

    public void longRest(Player p) {
        List<LivingEntity> members = Main.getInstance().getTeamManager().getTeamMembers(p);
        for (LivingEntity m : members) {
            Utils.healEntity(m);
            Utils.playSound(Sound.ITEM_GOAT_HORN_SOUND_0, m);
            if (m instanceof Player) {
                ((Player) m).setFoodLevel(20);
            }
            m.sendMessage(Utils.t("&aSuccessfully long rested!"));
        }
        Main.getInstance().getItemManager().removeFood(p, 20);
        Main.getInstance().getTeamManager().replenishShortRests(p);
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
        inv.setItem(10, spells);
        if (inCombat) {
            inv.setItem(11, beFriends);
            Conflict conflict = Main.getInstance().getConflictManager().getConflict(p);

            // Actual slots and glowing
            if (Main.getInstance().getConflictManager().isTurn(p)) {
                inv.setItem(17, endTurn);
                if (conflict.currentTurnActionsRemaining > 0) {
                    inv.setItem(16, dash);
                }
            }
        } else {
            inv.setItem(16, shortRest);
            inv.setItem(17, longRest);
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

        //Bukkit.getLogger().warning("Could not get stats for entity type: " + e.getType());
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

        int roll = Utils.roll("1d20");
        if (adv != 0) {
            int roll2 = Utils.roll("1d20");
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
        lines.add("&6Short Rests: &7" + Main.getInstance().getTeamManager().getRemainingShortRests(player));
        lines.add("&6Camp Supplies: &7" + Main.getInstance().getItemManager().countSupplies(player));

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

            HashMap<String, Integer> conflictingNames = new HashMap<>();
            for (Combatant enemy : conflict.getTurns()) {  // Turns gives the order
                String name = enemy.getName();
                if (conflictingNames.containsKey(name)) {
                    int amount = conflictingNames.get(name);
                    conflictingNames.put(name, amount + 1);
                    name += " " + (amount + 1);
                } else {
                    conflictingNames.put(name, 1);
                }

                if (enemy.isDead()) {
                    lines.add("&7&m> &6&m" + name);
                    continue;
                }

                String healthInfo = " &7(" + ((int) enemy.getEntity().getHealth()) + "/" + ((int) Utils.getMaxHealth(enemy.getEntity())) + ")";

                int relationship = Main.getInstance().getTeamManager().getRelationship(enemy.getEntity(), player);
                String relColour = relationship == -1 ? "&c" : relationship == 0 ? "&e" : "&a";

                if (conflict.isCurrentTurn(enemy)) {
                    lines.add("&7&l> " + relColour + "&l" + name + healthInfo);
                } else {
                    lines.add("&7> " + relColour + name + healthInfo);
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
