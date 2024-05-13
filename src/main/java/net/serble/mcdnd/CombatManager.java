package net.serble.mcdnd;

import net.serble.mcdnd.schemas.AbilityScore;
import net.serble.mcdnd.schemas.Conflict;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.projectiles.ProjectileSource;

import java.util.Objects;
import java.util.UUID;

public class CombatManager implements Listener {

    private void conditionalSend(LivingEntity p, String msg) {
        if (!Main.getInstance().getConflictManager().isInCombat(p)) {
            p.sendMessage(Utils.t(msg));
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onHit(EntityDamageByEntityEvent e) {
        if (e.getCause() == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) {
            return;  // Not a melee attack
        }

        if (e.getDamager() instanceof Projectile && e.getEntity() instanceof LivingEntity) {
            onProjHit(e);
            return;
        }

        if (!(e.getDamager() instanceof LivingEntity)) {
            return;
        }

        LivingEntity damager = (LivingEntity) e.getDamager();
        LivingEntity damagee = (LivingEntity) e.getEntity();

        applyBadKarma(damagee, damager);

        boolean isPlayer = damager instanceof Player;
        ItemStack heldItem = Objects.requireNonNull(damager.getEquipment()).getItemInMainHand();
        String itemType = "fist";
        if (heldItem != null) {
            itemType = getWeaponType(heldItem);
        }

        if (!Objects.equals(itemType, "melee")) {  // You can't use it for melee
            itemType = "fist";
        }

        if (!rollForHit(damager, damagee, false)) {
            e.setCancelled(true);
            if (damagee instanceof Mob) {
                Mob mob = (Mob) damagee;
                mob.setTarget(damager);
            }
            Utils.playSound(Sound.ENTITY_PLAYER_ATTACK_NODAMAGE, damager, damagee);
            conditionalSend(damager, "&cYou missed!");
            return;
        }

        String damageRoll = "";
        if (Objects.equals(itemType, "fist")) {
            damageRoll = "1d4";
        } else {
            damageRoll = NbtHandler.itemStackGetTag(heldItem, "damageroll", PersistentDataType.STRING);
        }

        int roll = Utils.roll(damageRoll);
        e.setDamage(roll);
        if (damager instanceof Player) {
            conditionalSend(damager, "&aRolled &6" + roll + "&a and dealt &6" + roll + "&a damage");
        }
    }

    private void applyBadKarma(LivingEntity e1, LivingEntity e2) {
        UUID t1 = Main.getInstance().getTeamManager().getTeam(e1);
        UUID t2 = Main.getInstance().getTeamManager().getTeam(e2);

        if (t1 == t2) {
            return;
        }

        Integer relation = Main.getInstance().getTeamManager().getRelationship(t1, t2);

        if (relation == 0) {
            Main.getInstance().getTeamManager().setRelationship(t1, t2, -1);
        }
    }

    @EventHandler
    public void onProjLaunch(ProjectileLaunchEvent e) {
        ProjectileSource source = e.getEntity().getShooter();
        if (!(source instanceof LivingEntity)) {
            return;
        }

        LivingEntity entity = (LivingEntity) source;

        Conflict conflict = Main.getInstance().getConflictManager().getConflict(entity);
        if (conflict != null && !conflict.isCurrentTurn(entity)) {
            e.setCancelled(true);
            return;
        }

        ItemStack heldItem = Objects.requireNonNull(entity.getEquipment()).getItemInMainHand();

        // Potions
        if (e.getEntity() instanceof ThrownPotion) {
            if (conflict != null) {
                if (conflict.currentTurnBonusActionsRemaining < 1) {
                    e.setCancelled(true);
                    entity.sendMessage(Utils.t("&cYou don't have a bonus action"));
                    return;
                } else {
                    conflict.currentTurnBonusActionsRemaining--;
                }
            }
        }

        String itemType = "fist";
        itemType = getWeaponType(heldItem);

        String damageRoll = "";
        if (Objects.equals(itemType, "fist")) {
            damageRoll = "1d4";
        } else {
            damageRoll = NbtHandler.itemStackGetTag(heldItem, "damageroll", PersistentDataType.STRING);
        }

        e.getEntity().addScoreboardTag("mcdndproj" + damageRoll);
    }

    public void onProjHit(EntityDamageByEntityEvent e) {
        Projectile projectile = (Projectile) e.getDamager();
        LivingEntity victim = (LivingEntity) e.getEntity();
        LivingEntity attacker = (LivingEntity) projectile.getShooter();

        applyBadKarma(victim, attacker);

        String cTag = null;
        for (String tag : projectile.getScoreboardTags()) {
            if (tag.startsWith("mcdndproj")) {
                cTag = tag;
                break;
            }
        }

        if (cTag == null) {
            return;
        }

        String dice = cTag.replace("mcdndproj", "");

        if (!rollForHit(attacker, victim, true)) {
            e.setCancelled(true);
            if (victim instanceof Mob) {
                Mob mob = (Mob) victim;
                mob.setTarget(attacker);
            }
            Utils.playSound(Sound.ENTITY_ARROW_HIT, attacker, victim);
            return;
        }

        int roll = Utils.roll(dice);
        if (attacker instanceof Player) {
            conditionalSend(attacker, "&aRolled &6" + dice + "&a and dealt &6" + roll + "&a damage");
        }

        e.setDamage(roll);
    }

    private String getWeaponType(ItemStack item) {
        boolean isCustom = NbtHandler.itemStackHasTag(item, "customitem", PersistentDataType.STRING);

        if (isCustom) {
            String type = NbtHandler.itemStackGetTag(item, "customitem", PersistentDataType.STRING);
            switch (Objects.requireNonNull(type)) {
                case "melee":
                case "ranged":
                    return type;

                default:
                    return "fist";
            }
        }

        if (item.getType() == Material.BOW) {
            NbtHandler.itemStackSetTag(item, "customitem", PersistentDataType.STRING, "ranged");
            NbtHandler.itemStackSetTag(item, "damageroll", PersistentDataType.STRING, "1d6");
        }

        return "fist";
    }

    private int getArmorBonus(ItemStack item) {
        boolean isCustom = NbtHandler.itemStackHasTag(item, "customitem", PersistentDataType.STRING);

        if (isCustom && Objects.equals(NbtHandler.itemStackGetTag(item, "customitem", PersistentDataType.STRING), "armor")) {
            return NbtHandler.itemStackGetTag(item, "armorbonus", PersistentDataType.INTEGER);
        }

        return 0;
    }

    private boolean rollForHit(LivingEntity attacker, LivingEntity defender, boolean ranged) {
        int enemyArmorClass = calculateArmorClass(defender);
        int attackRollBonus = calculateAttackBonus(attacker, ranged);

        int roll = Utils.roll("1d20");
        roll += attackRollBonus;

        conditionalSend(attacker, "&oEnemy AC: " + enemyArmorClass + ", Roll Bonus: " + attackRollBonus + ", Roll: " + roll);
        return roll >= enemyArmorClass;
    }

    public int calculateArmorClass(LivingEntity e) {
        int current = 10;

        EntityEquipment equipment = e.getEquipment();

        ItemStack helmet = null;
        ItemStack chestplate = null;
        ItemStack leggings = null;
        ItemStack boots = null;
        if (equipment != null) {
            helmet = equipment.getHelmet();
            chestplate = equipment.getChestplate();
            leggings = equipment.getLeggings();
            boots = equipment.getBoots();
        }

        boolean hasShield = false;
        if (equipment != null) {
            hasShield = equipment.getItemInOffHand().getType() == Material.SHIELD;
        }

        if (helmet != null) {
            current += getArmorBonus(helmet);
        }
        if (chestplate != null) {
            current += getArmorBonus(chestplate);
        }
        if (leggings != null) {
            current += getArmorBonus(leggings);
        }
        if (boots != null) {
            current += getArmorBonus(boots);
        }

        if (hasShield) {
            current += 2;
        }

        return current + Main.getInstance().getPlayerManager().getStatMod(e, AbilityScore.DEXTERITY);
    }

    private int calculateAttackBonus(LivingEntity e, boolean isRanged) {
        if (isRanged) {
            return Main.getInstance().getPlayerManager().getStatMod(e, AbilityScore.DEXTERITY);
        }
        return Main.getInstance().getPlayerManager().getStatMod(e, AbilityScore.STRENGTH);
    }



}
