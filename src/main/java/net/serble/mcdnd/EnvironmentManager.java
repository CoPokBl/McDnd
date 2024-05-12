package net.serble.mcdnd;

import net.serble.mcdnd.schemas.AbilityScore;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffectType;

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

}
