package net.serble.mcdnd;

import org.bukkit.entity.LivingEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class TeamManager {
    private final HashMap<Tuple<UUID, UUID>, Integer> relationships = new HashMap<>();
    private final HashMap<UUID, List<LivingEntity>> teams = new HashMap<>();
    private final HashMap<LivingEntity, LivingEntity> teamInvites = new HashMap<>();

    public Integer getRelationship(UUID t1, UUID t2) {
        if (t1 == t2) {
            return 1;
        }

        for (Tuple<UUID, UUID> relation : relationships.keySet()) {
            if ((relation.a().equals(t1) && relation.b().equals(t2)) || (relation.a().equals(t2) && relation.b().equals(t1))) {
                return relationships.get(relation);
            }
        }

        return 0;
    }

    public Integer getRelationship(LivingEntity e1, LivingEntity e2) {
        return getRelationship(getTeam(e1), getTeam(e2));
    }

    public UUID getTeam(LivingEntity entity) {
        for (UUID team : teams.keySet()) {
            if (teams.get(team).contains(entity)) {
                return team;
            }
        }

        return entity.getUniqueId();
    }

    public List<LivingEntity> getTeamMembers(LivingEntity e) {
        return teams.get(getTeam(e));
    }

    public void setRelationship(UUID t1, UUID t2, Integer state) {
        Tuple<UUID, UUID> relation = new Tuple<>(t1, t2);
        relationships.put(relation, state);
    }

    public boolean isInTeam(LivingEntity e) {
        for (UUID team : teams.keySet()) {
            if (teams.get(team).contains(e)) {
                return true;
            }
        }
        return false;
    }

    public void setPlayersInvite(LivingEntity e1, LivingEntity e2) {
        teamInvites.put(e1, e2);
    }

    public boolean isInvited(LivingEntity e1, LivingEntity e2) {
        return teamInvites.get(e1) == e2;
    }

    public void addPlayerToTeam(LivingEntity owner, LivingEntity target) {
        UUID team = getTeam(owner);
        if (!teams.containsKey(team)) {
            teams.put(team, new ArrayList<>());
            teams.get(team).add(owner);
        }
        teams.get(team).add(target);
    }
}
