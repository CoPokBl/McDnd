package net.serble.mcdnd;

import net.serble.mcdnd.ai.*;
import net.serble.mcdnd.schemas.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.damage.DamageSource;
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
        //noinspection UnstableApiUsage
        if (e.getDamageSource() instanceof ProcessedDamageSource) {
            return;  // We have already processed it, return
        }

        if (!(e.getEntity() instanceof LivingEntity)) {
            return;
        }
        LivingEntity entity = (LivingEntity) e.getEntity();

        switch (e.getCause()) {

            case BLOCK_EXPLOSION:
            case ENTITY_EXPLOSION:
                boolean saved = Main.getInstance().getPlayerManager().savingThrow(entity, AbilityScore.Dexterity, 15);
                if (saved) {
                    recastDamage(e, new Damage(DamageType.Fire, "1d10"));
                    entity.sendMessage(Utils.t("&aYou successfully saved against explosion"));
                }
                break;

            case FIRE_TICK:
            case FIRE:
            case WITHER:
            case POISON:
                e.setCancelled(true);  // Status effects are handled manually
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
            damage(entity, new Damage(DamageType.Poison, "1d4"));
            trySave(entity, PotionEffectType.POISON, AbilityScore.Constitution, 15);
        }

        if (entity.hasPotionEffect(PotionEffectType.WITHER)) {
            damage(entity, new Damage(DamageType.Necrotic, "1d6"));
            trySave(entity, PotionEffectType.WITHER, AbilityScore.Constitution, 17);
        }

        if (entity.getFireTicks() > 0) {
            damage(entity, new Damage(DamageType.Fire, "1d6"));
            if (Main.getInstance().getPlayerManager().savingThrow(entity, AbilityScore.Constitution, 10)) {
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
        if (!Main.getInstance().getPlayerManager().savingThrow(entity, ability, dc)) {
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

    @SuppressWarnings("UnstableApiUsage")
    public Tuple<DamageType, Integer>[] damage(DamageSource source, LivingEntity target, Damage damage) {
        Tuple<Integer, Tuple<DamageType, Integer>[]> dmgInfo = getDamageAmount(source, target, damage);
        ProcessedDamageSource processedDamageSource = new ProcessedDamageSource(source);
        target.damage(dmgInfo.a(), processedDamageSource);
        return dmgInfo.b();
    }

    @SuppressWarnings("UnstableApiUsage")
    public Tuple<Integer, Tuple<DamageType, Integer>[]> getDamageAmount(DamageSource source, LivingEntity target, Damage damage) {
        int totalDmg = 0;
        @SuppressWarnings("unchecked")  // Please just believe me
        Tuple<DamageType, Integer>[] appliedDamage = new Tuple[damage.getDamages().length];
        int cDmgIndex = 0;
        for (Tuple<DamageType, String> dmg : damage.getDamages()) {
            int roll = Utils.roll(dmg.b());
            totalDmg += roll;
            appliedDamage[cDmgIndex] = new Tuple<>(dmg.a(), roll);
            cDmgIndex++;
        }
        return new Tuple<>(totalDmg, appliedDamage);
    }

    public void damage(LivingEntity target, Damage damage) {
        damage(null, target, damage);
    }

    public void recastDamage(EntityDamageEvent e, Damage newDmg) {
        e.setCancelled(true);
        LivingEntity entity = (LivingEntity) e.getEntity();
        //noinspection UnstableApiUsage
        damage(e.getDamageSource(), entity, newDmg);
    }
}
