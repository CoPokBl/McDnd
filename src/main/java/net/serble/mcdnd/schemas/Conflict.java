package net.serble.mcdnd.schemas;

import net.serble.mcdnd.Main;
import net.serble.mcdnd.Utils;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;

public class Conflict {
    private static final int BaseMovement = 18;

    private final List<LivingEntity> participants = new ArrayList<>();
    private Combatant[] turns;
    private int currentTurn = 0;
    private List<LivingEntity> peopleWantingPeace = new ArrayList<>();

    public double currentTurnMovementRemaining = BaseMovement;
    public int currentTurnActionsRemaining = 1;
    public int currentTurnBonusActionsRemaining = 1;

    private BukkitTask currentMovementTask = null;

    public Conflict(List<LivingEntity> combatants) {
        for (LivingEntity e : combatants) {
            addParticipant(e);
        }
    }

    public void votePeace(LivingEntity e) {
        if (!peopleWantingPeace.contains(e)) {
            peopleWantingPeace.add(e);
        }
    }

    public int countPeopleWantingPeace() {
        return peopleWantingPeace.size();
    }

    public int countAlive() {
        int alive = 0;
        for (Combatant c : turns) {
            if (!c.isDead()) {
                alive++;
            }
        }
        return alive;
    }

    public void end() {
        if (currentMovementTask != null) {
            currentMovementTask.cancel();
        }
        updateParticipants();
    }

    public void updateParticipants() {
        for (LivingEntity e : getParticipants()) {
            if (e instanceof Player) {
                Main.getInstance().getPlayerManager().updatePlayer((Player) e);
            }
        }
    }

    public List<LivingEntity> getParticipants() {
        return participants;
    }

    public void addParticipant(LivingEntity entity) {
        participants.add(entity);
    }

    public void removeParticipant(LivingEntity entity) {
        participants.remove(entity);
    }

    public Combatant[] getTurns() {
        return turns;
    }

    public void setTurns(Combatant[] t) {
        turns = t;
    }

    public void setTurns(LivingEntity[] t) {
        Combatant[] c = new Combatant[t.length];
        for (int i = 0; i < t.length; i++) {
            c[i] = new Combatant(t[i]);
        }
        setTurns(c);
    }

    public void announce(String msg) {
        msg = Utils.t(msg);
        for (LivingEntity e : participants) {
            if (e instanceof Player) {
                e.sendMessage(msg);
            }
        }
    }

    public Combatant getCombatant(LivingEntity e) {
        for (Combatant c : turns) {
            if (c.getEntity().equals(e)) {
                return c;
            }
        }
        return null;
    }

    public LivingEntity getCurrentTurnEntity() {
        return turns[currentTurn].getEntity();
    }

    public Combatant getCurrentTurnCombatant() {
        return turns[currentTurn];
    }

    public void incrementTurn() {
        do {  // Change turn to first non-dead combatant
            currentTurn++;
            if (currentTurn >= turns.length) {
                currentTurn = 0;
            }
        } while (turns[currentTurn].isDead());
        currentTurnMovementRemaining = BaseMovement;
        currentTurnActionsRemaining = 1;
        currentTurnBonusActionsRemaining = 1;
    }

    public boolean isCurrentTurn(LivingEntity p) {
        return p.equals(getCurrentTurnEntity());
    }

    public boolean isCurrentTurn(Combatant p) {
        return p.getEntity().equals(getCurrentTurnEntity());
    }

    public BukkitTask getCurrentMovementTask() {
        return currentMovementTask;
    }

    public void setCurrentMovementTask(BukkitTask currentMovementTask) {
        this.currentMovementTask = currentMovementTask;
    }

    public boolean isAnyoneMad() {  // Check the relationships of everyone, if there is a -1 then return true
        for (LivingEntity e1 : participants) {
            for (LivingEntity e2 : participants) {
                if (e2 == e1) {
                    continue;
                }
                if (Main.getInstance().getTeamManager().getRelationship(e1, e2) == -1) {
                    return true;
                }
            }
        }
        return false;
    }

}
