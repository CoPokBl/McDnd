package net.serble.mcdnd;

import net.serble.mcdnd.ai.*;
import net.serble.mcdnd.schemas.AbilityScore;
import net.serble.mcdnd.schemas.Conflict;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Objects;

public class EnvironmentManager implements Listener {

    public EnvironmentManager() {
        Bukkit.getScheduler().runTaskTimer(Main.getInstance(), this::applyEnvironmentalDamageToAllEntities, 20 * 6L, 20 * 6L);
        Bukkit.getScheduler().runTaskTimer(Main.getInstance(), this::prolongEffectsForAllEntities, 10L, 10L);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof LivingEntity)) {
            return;
        }
        LivingEntity entity = (LivingEntity) e.getEntity();

        switch (e.getCause()) {

            case BLOCK_EXPLOSION:
            case ENTITY_EXPLOSION:
                boolean saved = Main.getInstance().getPlayerManager().abilityCheck(entity, AbilityScore.DEXTERITY, 15, 0);
                if (saved) {
                    e.setDamage(e.getDamage() / 2);
                    entity.sendMessage(Utils.t("&aYou successfully saved against explosion"));
                }
                break;

            case FIRE_TICK:
            case FIRE:
            case WITHER:
            case POISON:
                e.setCancelled(true);
                break;

        }
    }

    private void applyEnvironmentalDamageToAllEntities() {
        for (LivingEntity entity : Objects.requireNonNull(Bukkit.getWorld("world")).getLivingEntities()) {
            if (Main.getInstance().getConflictManager().isInCombat(entity)) {
                continue;
            }
            applyEnvironmentalDamages(entity);
        }
    }

    private void prolongEffectsForAllEntities() {
        for (LivingEntity entity : Objects.requireNonNull(Bukkit.getWorld("world")).getLivingEntities()) {
            prolongEffects(entity);
        }
    }

    private void prolongEffects(LivingEntity entity) {
        infiniteEffect(entity, PotionEffectType.POISON);
        infiniteEffect(entity, PotionEffectType.WITHER);
        if (entity.getFireTicks() > 0) {
            entity.setFireTicks(Short.MAX_VALUE);
        }
    }

    public void applyEnvironmentalDamages(LivingEntity entity) {
        if (entity.hasPotionEffect(PotionEffectType.POISON)) {
            int damage = Utils.roll("1d4");
            entity.damage(damage);
            trySave(entity, PotionEffectType.POISON, AbilityScore.CONSTITUTION, 15);
        }

        if (entity.hasPotionEffect(PotionEffectType.WITHER)) {
            int damage = Utils.roll("1d6");
            entity.damage(damage);
            trySave(entity, PotionEffectType.WITHER, AbilityScore.CONSTITUTION, 17);
        }

        if (entity.getFireTicks() > 0) {
            int damage = Utils.roll("1d6");
            entity.damage(damage);
            if (Main.getInstance().getPlayerManager().abilityCheck(entity, AbilityScore.CONSTITUTION, 10, 0)) {
                entity.setFireTicks(0);
                entity.sendMessage(Utils.t("&7You passed check and lost fire"));
            }
        }
    }

    private void infiniteEffect(LivingEntity entity, PotionEffectType type) {
        if (entity.hasPotionEffect(type)) {
            entity.removePotionEffect(type);
            entity.addPotionEffect(type.createEffect(-1, 0));
        }
    }

    private void trySave(LivingEntity entity, PotionEffectType effectType, AbilityScore ability, int dc) {
        if (!Main.getInstance().getPlayerManager().abilityCheck(entity, ability, dc, 0)) {
            // Fail
            return;
        }
        entity.removePotionEffect(effectType);
        entity.sendMessage(Utils.t("&7You passed check and lost " + effectType.getTranslationKey().toLowerCase()));
    }

    @EventHandler
    public void onMobSpawn(EntitySpawnEvent e) {  // A mob spawned
        patchMob(e.getEntity());
    }

    public void patchAllMobs() {
        for (LivingEntity entity : Objects.requireNonNull(Bukkit.getWorld("world")).getLivingEntities()) {
            if (entity == null) {
                Bukkit.getLogger().warning("Got null entity while patching");
                continue;
            }
            try {
                patchMob(entity);
            } catch (AssertionError | Exception e) {
                Bukkit.getLogger().info("Failed to patch entity: " + entity.getName());
                continue;
            }
            Bukkit.getLogger().info("[McDnd Mob Patcher] Patched " + entity.getName());
        }
    }

    private void patchMob(Entity entity) {
        if (!(entity instanceof Mob)) {
            return;
        }
        Mob mob = (Mob) entity;

        switch (mob.getType()) {
            case ZOMBIE:
                SpeedyZombie.patchMob(mob);
                break;

            case SKELETON:
                AggressiveArcherMob.patchMob(mob, false);
                break;

            case CREEPER:
                BoomBoomMob.patchMob(mob);
                break;

            default:
                NeutralMob.patchMob(mob);
                break;
        }

        mob.setAI(true);
    }

    @EventHandler
    public void onFoodLose(FoodLevelChangeEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void onEat(PlayerItemConsumeEvent e) {
        if (e.getItem().getType() == Material.POTION) {
            return;
        }

        e.setCancelled(true);
    }

    @EventHandler
    public void onDespawn(@SuppressWarnings("UnstableApiUsage") EntityRemoveEvent e) {
        if (!(e.getEntity() instanceof LivingEntity)) {
            return;
        }

        Conflict conflict = Main.getInstance().getConflictManager().getConflict((LivingEntity) e.getEntity());
        if (conflict == null) {
            return;
        }

        EntityDeathEvent deathEvent = new EntityDeathEvent((LivingEntity) e.getEntity(), new ArrayList<>());
        Main.getInstance().getConflictManager().onDeath(deathEvent);
    }

}
