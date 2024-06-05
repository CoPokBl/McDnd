package net.serble.mcdnd;

import net.serble.mcdnd.actions.Action;
import net.serble.mcdnd.attackmodifiers.AttackModifier;
import net.serble.mcdnd.schemas.*;
import net.serble.mcdnd.schemas.events.AttackEvent;
import net.serble.mcdnd.schemas.events.PreAttackEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;

import java.util.*;

public class CombatManager implements Listener {
    private final HashMap<UUID, Action> actionsWaitingForTarget = new HashMap<>();
    private final HashMap<UUID, Action> actionsWaitingForAttack = new HashMap<>();
    private final HashMap<UUID, List<AttackModifier>> attackModifiers = new HashMap<>();

    private void conditionalSend(LivingEntity p, String msg) {
        if (!Main.getInstance().getConflictManager().isInCombat(p)) {
            p.sendMessage(Utils.t(msg));
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onHit(EntityDamageByEntityEvent e) {
        if (e.isCancelled() || Main.getInstance().getRayCaster().isRayCast(e.getDamager())) {
            return;
        }

        if (e.getCause() == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) {
            return;  // Not a melee attack
        }

        if (e.getDamager() instanceof LivingEntity && getWaitingAction((LivingEntity) e.getDamager()) != null) {
            return;
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

        PreAttackEvent preAttackEvent = new PreAttackEvent(damager, damagee, false, null);
        Bukkit.getPluginManager().callEvent(preAttackEvent);
        if (preAttackEvent.isCancelled()) {
            e.setCancelled(true);
            return;
        }

        applyBadKarma(damagee, damager);

        ItemStack heldItem = Objects.requireNonNull(damager.getEquipment()).getItemInMainHand();
        WeaponProfile weapon = getWeaponProfile(heldItem);

        if (weapon.isRanged()) {  // You can't use it for melee
            weapon = WeaponProfile.getFist();
        }

        boolean miss = false;
        String sendMsg = null;
        HitResult hitResult = rollForHit(damager, damagee, weapon);
        if (!hitResult.isSuccessfulHit()) {
            e.setCancelled(true);
            miss = true;
            if (damagee instanceof Mob) {
                Mob mob = (Mob) damagee;
                mob.setTarget(damager);
            }
            Utils.playSound(Sound.ENTITY_PLAYER_ATTACK_NODAMAGE, damager, damagee);
            sendMsg = "&cYou missed!";
        }

        if (!miss) {
            Damage finalDamage = weapon.getDamage();

            ensurePlayerAmExists(damager);
            for (AttackModifier mod : attackModifiers.get(damager.getUniqueId())) {
                finalDamage = mod.modifyDamage(weapon, finalDamage);
            }
            Action attackWaitingAction = getWaitingAttackAction(damager);
            if (attackWaitingAction != null) {
                damager.sendMessage(Utils.t("&aUsed &6" + attackWaitingAction.getName()));
                cancelWaitingAttackAction(damager);
                AttackModifier mod = attackWaitingAction.runWithAttack(damager, damagee, weapon, finalDamage);

                if (mod.shouldCancelAttack()) {
                    e.setCancelled(true);
                    return;
                }

                finalDamage = mod.modifyDamage(weapon, finalDamage);
            }

            int roll = rollDamage(damager, damagee, finalDamage);
            String rollMsg = hitResult.isCritical() ? roll * 2 + " (Critical Hit)" : String.valueOf(roll);
            if (hitResult.isCritical()) {
                roll *= 2;
            }
            e.setDamage(roll);
            sendMsg = "&aRolled &6" + weapon.getDamage().getDamageString() + "&a and dealt &6" + rollMsg + "&a damage";
        }

        AttackEvent event = new AttackEvent(damager, damagee, !miss, e.getDamage(), false, null);
        Bukkit.getPluginManager().callEvent(event);

        if (!miss) {
            e.setDamage(event.getDamage());

            if (e.getDamage() >= damagee.getHealth()) {  // Fatal
                Utils.grantBasicKillExp(damager);
            }
        }

        if (event.isCancelled()) {
            e.setCancelled(true);
        } else {
            damager.sendMessage(Utils.t(sendMsg));
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    private int rollDamage(LivingEntity attacker, LivingEntity defender, Damage d) {
        DamageSource source = DamageSource.builder(DamageType.PLAYER_ATTACK).withDirectEntity(attacker).build();
        return Main.getInstance().getEnvironmentManager().getDamageAmount(source, defender, d).a();
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

        WeaponProfile weapon = getWeaponProfile(heldItem);

        PlayerStats stats = Main.getInstance().getPlayerManager().getStatsFor(entity);
        int prof = stats.isProficient(weapon.getType()) ? stats.getProficiencyBonus() : 0;

        e.getEntity().addScoreboardTag("mcdndproj" + weapon.getDamage().getDamageString());
        e.getEntity().addScoreboardTag("mcdndprof" + prof);
    }

    public void onProjHit(EntityDamageByEntityEvent e) {
        Projectile projectile = (Projectile) e.getDamager();
        LivingEntity victim = (LivingEntity) e.getEntity();
        LivingEntity attacker = (LivingEntity) projectile.getShooter();

        PreAttackEvent preAttackEvent = new PreAttackEvent(attacker, victim, true, projectile);
        Bukkit.getPluginManager().callEvent(preAttackEvent);
        if (preAttackEvent.isCancelled()) {
            e.setCancelled(true);
            return;
        }

        applyBadKarma(victim, attacker);

        String damageTag = null;
        String profTag = null;
        for (String tag : projectile.getScoreboardTags()) {
            if (tag.startsWith("mcdndproj")) {
                damageTag = tag;
            }
            if (tag.startsWith("mcdndprof")) {
                profTag = tag;
            }
        }

        if (damageTag == null || profTag == null) {
            return;
        }

        String damageStr = damageTag.replace("mcdndproj", "");
        Damage damage = Damage.parse(damageStr);
        int prof = Integer.parseInt(profTag.replace("mcdndprof", ""));

        boolean miss = false;
        HitResult hitResult = rollForHit(attacker, victim, prof, true);
        if (!hitResult.isSuccessfulHit()) {
            miss = true;
            e.setCancelled(true);
            if (victim instanceof Mob) {
                Mob mob = (Mob) victim;
                mob.setTarget(attacker);
            }
            Utils.playSound(Sound.ENTITY_ARROW_HIT, attacker, victim);
        }

        if (!miss) {
            int roll = rollDamage(attacker, victim, damage);
            String rollMsg = hitResult.isCritical() ? "&c" + roll * 2 + " (Critical Hit)" : String.valueOf(roll);
            if (hitResult.isCritical()) {
                roll *= 2;
            }
            conditionalSend(attacker, "&aRolled &6" + damageStr + "&a and dealt &6" + rollMsg + "&a damage");
            e.setDamage(roll);
        }

        AttackEvent event = new AttackEvent(attacker, victim, !miss, e.getDamage(), true, projectile);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            e.setCancelled(true);
        }
    }

    private WeaponProfile getWeaponProfile(ItemStack item) {
        Main.getInstance().getItemManager().migrateIfVanilla(item);

        String type = NbtHandler.itemStackGetTag(item, "customitem", PersistentDataType.STRING);
        if (type == null) {
            type = "fist";
        }
        boolean ranged = true;
        switch (type) {
            case "melee":
                ranged = false;
            case "ranged":
                WeaponType wt = WeaponType.valueOf(NbtHandler.itemStackGetTag(item, "weapontype", PersistentDataType.STRING));
                String dmgStr = NbtHandler.itemStackGetTag(item, "damageroll", PersistentDataType.STRING);
                assert dmgStr != null;
                Damage dmg = Utils.parseDamage(dmgStr);
                return new WeaponProfile(wt, dmg, ranged);

            default:
                return WeaponProfile.getFist();
        }
    }

    private int getArmorBonus(ItemStack item) {
        boolean isCustom = NbtHandler.itemStackHasTag(item, "customitem", PersistentDataType.STRING);

        if (isCustom && Objects.equals(NbtHandler.itemStackGetTag(item, "customitem", PersistentDataType.STRING), "armor")) {
            return Objects.requireNonNull(NbtHandler.itemStackGetTag(item, "armorbonus", PersistentDataType.INTEGER));
        }

        return 0;
    }

    private HitResult rollForHit(LivingEntity attacker, LivingEntity defender, WeaponProfile weapon) {
        PlayerStats stats = Main.getInstance().getPlayerManager().getStatsFor(attacker);
        int profBonus = stats.isProficient(weapon.getType()) ? stats.getProficiencyBonus() : 0;

        return rollForHit(attacker, defender, profBonus, weapon.isRanged());
    }

    private HitResult rollForHit(LivingEntity attacker, LivingEntity defender, int prof, boolean ranged) {
        int enemyArmorClass = calculateArmorClass(defender);
        int attackRollBonus = calculateAttackBonus(attacker, prof, ranged);

        int adv = getAdvantage(attacker, defender, ranged);

        int roll = Utils.roll("1d20", adv);
        if (roll == 20) {
            return new HitResult(true, true);
        }
        roll += attackRollBonus;

        conditionalSend(attacker, "&oEnemy AC: " + enemyArmorClass + ", Roll Bonus: " + attackRollBonus + ", Roll: " + roll);
        return new HitResult(roll >= enemyArmorClass, false);
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

        return current + Main.getInstance().getPlayerManager().getStatMod(e, AbilityScore.Dexterity);
    }

    private int calculateAttackBonus(LivingEntity e, int prof, boolean ranged) {
        if (ranged) {
            return Main.getInstance().getPlayerManager().getStatMod(e, AbilityScore.Dexterity) + prof;
        }
        return Main.getInstance().getPlayerManager().getStatMod(e, AbilityScore.Strength) + prof;
    }

    public int getAdvantage(LivingEntity attacker, LivingEntity defender, boolean ranged) {
        boolean advantage = hasAdvantage(attacker, defender, ranged);
        boolean disadvantage = hasDisadvantage(attacker, defender, ranged);

        if (advantage == disadvantage) {
            return 0;
        }

        return advantage ? 1 : -1;
    }

    private boolean hasAdvantage(LivingEntity attacker, LivingEntity defender, boolean ranged) {
        if (attacker.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
            return true;
        }

        if (defender.hasPotionEffect(PotionEffectType.BLINDNESS)) {
            return true;
        }

        if (defender.hasPotionEffect(PotionEffectType.SLOW)) {
            return true;
        }

        return false;
    }

    private boolean hasDisadvantage(LivingEntity attacker, LivingEntity defender, boolean ranged) {
        if (defender.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
            return true;
        }

        if (attacker.hasPotionEffect(PotionEffectType.BLINDNESS)) {
            return true;
        }

        double distance = attacker.getLocation().distance(defender.getLocation());
        if (ranged && distance < 5) {
            return true;
        }

        return false;
    }

    public void registerWaitingAction(LivingEntity entity, Action action) {
        actionsWaitingForTarget.put(entity.getUniqueId(), action);
    }

    public Action getWaitingAction(LivingEntity entity) {
        return actionsWaitingForTarget.get(entity.getUniqueId());
    }

    public void cancelWaitingAction(LivingEntity entity) {
        actionsWaitingForTarget.remove(entity.getUniqueId());
    }

    public Action getWaitingAttackAction(LivingEntity entity) {
        return actionsWaitingForAttack.get(entity.getUniqueId());
    }

    public void registerWaitingAttackAction(LivingEntity entity, Action action) {
        actionsWaitingForAttack.put(entity.getUniqueId(), action);
    }

    public void cancelWaitingAttackAction(LivingEntity entity) {
        actionsWaitingForAttack.remove(entity.getUniqueId());
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        Action action = getWaitingAction(e.getPlayer());
        if (action == null) {
            return;
        }

        // Check if they are looking at an entity and get that entity
        Main.getInstance().getRayCaster().rayCast(e.getPlayer(), new RayCastCallback() {
            @Override
            public void run(Entity hitEntity, Block hitBlock, BlockFace hitBlockFace) {
                Action waitingAction = getWaitingAction(e.getPlayer());
                cancelWaitingAction(e.getPlayer());
                if (waitingAction == null || hitEntity == null) {
                    return;
                }
                if (!(hitEntity instanceof LivingEntity)) {
                    return;
                }
                waitingAction.runWithTarget(e.getPlayer(), (LivingEntity) hitEntity);
            }
        });

        e.setCancelled(true);
    }

    private void ensurePlayerAmExists(LivingEntity e) {
        if (!attackModifiers.containsKey(e.getUniqueId())) {
            attackModifiers.put(e.getUniqueId(), new ArrayList<>());
        }
    }

    public void addPlayerAttackMod(LivingEntity e, AttackModifier mod) {
        ensurePlayerAmExists(e);
        attackModifiers.get(e.getUniqueId()).add(mod);
    }
}
