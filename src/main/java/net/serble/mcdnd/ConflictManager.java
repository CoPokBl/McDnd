package net.serble.mcdnd;

import net.serble.mcdnd.schemas.AbilityScore;
import net.serble.mcdnd.schemas.Combatant;
import net.serble.mcdnd.schemas.Conflict;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class ConflictManager implements Listener {
    private final List<Conflict> conflicts = new ArrayList<>();

    public boolean isInCombat(LivingEntity entity) {
        return conflicts.stream().anyMatch(c -> c.getParticipants().contains(entity));
    }

    public boolean isTurn(LivingEntity entity) {
        Conflict conflict = getConflict(entity);
        if (conflict == null) {
            throw new RuntimeException("Entity not in combat");
        }

        return conflict.getCurrentTurnEntity() == entity;
    }

    public Conflict getConflict(LivingEntity entity) {
        for (Conflict conflict : conflicts) {
            if (conflict.getParticipants().contains(entity)) {
                return conflict;
            }
        }
        return null;
    }

    public void startConflict(LivingEntity... members) {
        List<LivingEntity> actualMembers = new ArrayList<>();

        for (LivingEntity m : members) {
            if (isInCombat(m)) {
                throw new RuntimeException("An entity cannot be in 2 conflicts at one time");
            }
            actualMembers.add(m);

            List<LivingEntity> teamMembers = Main.getInstance().getTeamManager().getTeamMembers(m);
            if (teamMembers == null) {
                continue;
            }
            for (LivingEntity tm : teamMembers) {
                if (actualMembers.contains(tm)) {
                    continue;
                }

                if (tm.getLocation().distance(m.getLocation()) > 18) {
                    tm.sendMessage(Utils.t("&6" + m.getName() + "&c is in combat but you are too far to help them"));
                    continue;
                }

                actualMembers.add(tm);
            }
        }

        Conflict conflict = new Conflict(actualMembers);
        conflict.announce(Utils.t("&cYou are now in combat"));

        // Roll for initiative
        HashMap<LivingEntity, Integer> initiatives = new HashMap<>();
        for (LivingEntity m : actualMembers) {
            int in = Main.getInstance().getPlayerManager().abilityRoll(m, AbilityScore.Dexterity, 0);
            initiatives.put(m, in);
            String inName = in == Integer.MAX_VALUE ? "20 (Critical Success)" : in == Integer.MIN_VALUE ? "1 (Critical Failure)" : String.valueOf(in);
            conflict.announce(Utils.t("&6" + m.getName() + "&a rolled &6" + inName + "&a for initiative"));

            if (!(m instanceof Player)) {
                m.setAI(false);
            }
        }

        // Sort the list of people based on their initiatives
        LivingEntity[] order = initiatives
                .entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .toArray(LivingEntity[]::new);

        LivingEntity[] reversedOrder = new LivingEntity[order.length];
        for (int i = 0; i < order.length; i++) {
            reversedOrder[i] = order[order.length - i - 1];
        }

        conflict.setTurns(reversedOrder);  // Reverse otherwise it is lowest first
        conflicts.add(conflict);

        conflict.announce(Utils.t("&aIt is now &6" + conflict.getCurrentTurnEntity().getName() + "'s &aturn"));

        newTurn(conflict);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDamage(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Projectile && e.getEntity() instanceof LivingEntity) {
            onProjHit(e);
            return;
        }

        if (!(e.getDamager() instanceof LivingEntity) || !(e.getEntity() instanceof LivingEntity)) {
            return;
        }

        LivingEntity aggressor = (LivingEntity) e.getDamager();
        LivingEntity defender = (LivingEntity) e.getEntity();

        if (processHit(aggressor, defender, e.getDamage(), e.isCancelled(), false)) {
            e.setCancelled(true);
        }
    }

    private void onProjHit(EntityDamageByEntityEvent e) {
        LivingEntity target = (LivingEntity) e.getEntity();
        Projectile damager = (Projectile) e.getDamager();

        String cTag = null;
        for (String tag : damager.getScoreboardTags()) {
            if (tag.startsWith("mcdndproj")) {
                cTag = tag;
                break;
            }
        }

        if (cTag == null) {
            return;
        }

        LivingEntity aggressor = (LivingEntity) damager.getShooter();

        if (processHit(aggressor, target, e.getDamage(), e.isCancelled(), damager instanceof ThrownPotion)) {
            e.setCancelled(true);
        }
    }

    private boolean processHit(LivingEntity aggressor, LivingEntity defender, double damage, boolean missed, boolean isFree) {
        boolean aInCombat = isInCombat(aggressor);
        boolean dInCombat = isInCombat(defender);

        if (aInCombat != dInCombat) {  // Join the fight
            LivingEntity fighter = aInCombat ? aggressor : defender;
            if (aggressor == fighter && !isTurn(fighter)) {  // Must be current turn to engage in combat
                aggressor.sendMessage(Utils.t("&cIt is not your turn"));
                return true;
            }
            LivingEntity newComer = aInCombat ? defender : aggressor;
            Conflict conflict = getConflict(fighter);

            conflict.addParticipant(newComer);
            conflict.setTurns(Utils.addCombatant(conflict.getTurns(), new Combatant(newComer)));
            conflict.updateParticipants();
            newComer.setAI(false);
            return true;
        }

        Conflict conflict = getConflict(aggressor);
        if (conflict == null || !conflict.getParticipants().contains(defender)) {
            if (aInCombat) {
                return true;
            }

            // Neither are in combat

            if (aggressor == defender) {  // Can't fight yourself
                return false;
            }

            Integer relation = Main.getInstance().getTeamManager().getRelationship(aggressor, defender);
            if (relation == 1) {  // Allies can't fight each other, but they can friendly fire
                return false;
            }

            Utils.setTarget(defender, aggressor);
            Utils.setTarget(aggressor, defender);
            startConflict(aggressor, defender);
            return false;
        }

        // They are fighting
        LivingEntity turnEntity = conflict.getCurrentTurnEntity();
        if (turnEntity != aggressor) {
            aggressor.sendMessage(Utils.t("&cIt is not your turn, it is &6" + turnEntity.getName() + "'s&c turn"));
            return true;
        }

        // Check to see if they have action left
        if (conflict.currentTurnActionsRemaining < 1 && !isFree) {
            aggressor.sendMessage(Utils.t("&cYou have no actions remaining"));
            return true;
        }

        // Damage success
        String msg = "&6" + aggressor.getName() + "&a hit &6" + defender.getName() + "&a for &6" + damage + "&a damage";
        if (missed) {
            msg = "&6" + aggressor.getName() + "&a tried to attack &6" + defender.getName() + "&a but missed";
        }

        conflict.announce(Utils.t(msg));
        if (!isFree) conflict.currentTurnActionsRemaining--;

        if (!(turnEntity instanceof Player) && !turnEntity.getScoreboardTags().contains("noautoendturn")) {
            endTurn(conflict);
        }
        return false;
    }

    public void endAllConflicts() {
        List<Conflict> dupe = new ArrayList<>(conflicts);
        for (Conflict conflict : dupe) {
            conflict.announce("&ePeace across the lands!!!");
            endConflict(conflict);
        }
    }

    public void endPlayersTurn(LivingEntity p) {
        Conflict conflict = getConflict(p);
        if (conflict == null) {
            p.sendMessage(Utils.t("&cYou are not in combat"));
            return;
        }

        if (!conflict.isCurrentTurn(p)) {
            p.sendMessage(Utils.t("&cIt is not your turn"));
            return;
        }

        endTurn(conflict);
    }

    private void endTurn(Conflict conflict) {
        if (!(conflict.getCurrentTurnEntity() instanceof Player)) {
            conflict.getCurrentTurnEntity().setAI(false);
        }

        if (conflict.getCurrentMovementTask() != null) {
            conflict.getCurrentMovementTask().cancel();
            conflict.setCurrentMovementTask(null);
        }

        conflict.incrementTurn();
        conflict.announce(Utils.t("&6" + conflict.getCurrentTurnEntity().getName() + "'s &7turn"));
        newTurn(conflict);
    }

    private void newTurn(Conflict conflict) {
        final boolean isPlayer = conflict.getCurrentTurnEntity() instanceof Player;
        if (!isPlayer) {
            conflict.getCurrentTurnEntity().setAI(true);
        }

        trackMovement(conflict);

        // Update invs and scoreboards
        for (LivingEntity en : conflict.getParticipants()) {
            if (en instanceof Player) {
                Main.getInstance().getPlayerManager().updatePlayer((Player) en);
            }
        }

        Main.getInstance().getEnvironmentManager().applyEnvironmentalDamages(conflict.getCurrentTurnEntity());
    }

    public void trackMovement(Conflict conflict) {  // Calculate movement
        if (conflict.getCurrentMovementTask() != null) {
            conflict.getCurrentMovementTask().cancel();
            conflict.setCurrentMovementTask(null);
        }

        final boolean isPlayer = conflict.getCurrentTurnEntity() instanceof Player;
        AtomicReference<Location> lastLoc = new AtomicReference<>();
        lastLoc.set(conflict.getCurrentTurnEntity().getLocation());
        AtomicReference<BukkitTask> taskReference = new AtomicReference<>();
        AtomicReference<Boolean> didStop = new AtomicReference<>(false);
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(Main.getInstance(), () -> {
            if (didStop.get()) {
                throw new RuntimeException("ALREADY STOPPED WTF");
            }

            if (conflict.getCurrentTurnEntity().isDead()) {
                endTurn(conflict);
                return;
            }

            Location newPos = conflict.getCurrentTurnEntity().getLocation();
            Vector oldPosVec = lastLoc.get().toVector();
            Vector newPosVec = newPos.toVector();
            double distance = oldPosVec.distance(newPosVec);
            lastLoc.set(newPos);
            conflict.currentTurnMovementRemaining -= distance;
            if (conflict.currentTurnMovementRemaining <= 0) {
                if (!isPlayer) {
                    endTurn(conflict);
                }
                taskReference.get().cancel();
                didStop.set(true);
            }
        }, 2L, 2L);
        taskReference.set(task);
        conflict.setCurrentMovementTask(task);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (!isInCombat(e.getPlayer())) {
            return;
        }

        double distance = e.getFrom().distance(Objects.requireNonNull(e.getTo()));
        boolean onlyYChange = e.getFrom().getBlockX() == e.getTo().getBlockX() && e.getFrom().getBlockZ() == e.getTo().getBlockZ();
        boolean didMove = distance > 0 && !onlyYChange;

        if (!didMove) {
            return;
        }

        Conflict conflict = getConflict(e.getPlayer());
        if (!conflict.isCurrentTurn(e.getPlayer())) {
            e.setCancelled(true);
            Utils.setActionBar(e.getPlayer(), "&cIt is not your turn");
            return;
        }

        String msg;
        if (conflict.currentTurnMovementRemaining < distance) {
            msg = "&cYou cannot move more";
            e.setCancelled(true);
        } else {
            double oneDigit = Math.round(conflict.currentTurnMovementRemaining);
            msg = "&aRemaining Movement: &6" + oneDigit;
        }

        Utils.setActionBar(e.getPlayer(), msg);
    }

    @EventHandler
    public void onDeath(EntityDeathEvent e) {
        Conflict conflict = getConflict(e.getEntity());
        if (conflict == null) {
            return;
        }

        conflict.announce(Utils.t("&6" + e.getEntity().getName() + "&a has been defeated"));
        conflict.removeParticipant(e.getEntity());
        Combatant combatant = conflict.getCombatant(e.getEntity());
        combatant.setDead(true);
        if (conflict.isCurrentTurn(combatant)) {
            endTurn(conflict);
        }

        // Check to see if anyone is enemys
        checkEndConflict(conflict);
    }

    public void checkEndConflict(Conflict conflict) {
        if (!conflict.isAnyoneMad()) {
            endConflict(conflict);
        }
    }

    public void endConflict(Conflict conflict) {
        conflict.announce(Utils.t("&6The conflict is over"));
        playSoundForConflict(conflict, Sound.UI_TOAST_CHALLENGE_COMPLETE);
        conflicts.remove(conflict);
        conflict.end();
    }

    private void playSoundForConflict(Conflict conflict, Sound sound) {
        for (Combatant combatant : conflict.getTurns()) {
            if (combatant.isDead()) {
                continue;
            }
            Utils.playSound(sound, combatant.getEntity());
        }
    }

    @EventHandler
    public void onBoom(EntityExplodeEvent e) {
        if (e.getEntity() instanceof LivingEntity) {
            EntityDeathEvent deathEvent = new EntityDeathEvent((LivingEntity) e.getEntity(), new ArrayList<>());
            onDeath(deathEvent);
        }
    }
}
