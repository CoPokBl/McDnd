package net.serble.mcdnd;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CustomItemManager implements Listener {

    @EventHandler
    public void onPickup(EntityPickupItemEvent e) {
        Bukkit.getLogger().info("Inv pickup event");
        if (migrateIfVanilla(e.getItem().getItemStack())) {
            Bukkit.getLogger().info("[McDnd] Migrated vanilla item");
        }
    }

    public boolean hasAtLeastFood(Player p, int required) {
        return countSupplies(p) >= required;
    }

    public int countSupplies(Player p) {
        int total = 0;
        for (ItemStack item : p.getInventory().getContents()) {
            if (item == null) {
                continue;
            }

            if (NbtHandler.itemStackHasTag(item, "customitem", PersistentDataType.STRING) &&
                    Objects.equals(NbtHandler.itemStackGetTag(item, "customitem", PersistentDataType.STRING), "supplies")) {
                total += NbtHandler.itemStackGetTag(item, "supplyvalue", PersistentDataType.INTEGER) * item.getAmount();
            }
        }
        return total;
    }

    public void removeFood(Player p, int amount) {
        List<ItemStack> toRemove = new ArrayList<>();
        for (ItemStack item : p.getInventory().getContents()) {
            if (item == null) {
                continue;
            }

            if (NbtHandler.itemStackHasTag(item, "customitem", PersistentDataType.STRING) &&
                    Objects.equals(NbtHandler.itemStackGetTag(item, "customitem", PersistentDataType.STRING), "supplies")) {
                int value = NbtHandler.itemStackGetTag(item, "supplyvalue", PersistentDataType.INTEGER);
                if (value * item.getAmount() >= amount) {
                    int remaining = amount % value;
                    int toRemoveAmount = (amount - remaining) / value;
                    if (toRemoveAmount == item.getAmount()) {
                        toRemove.add(item);
                    } else {
                        item.setAmount(item.getAmount() - toRemoveAmount);
                    }
                    amount = remaining;
                }
            }
        }

        for (ItemStack item : toRemove) {
            p.getInventory().remove(item);
        }
    }

    public boolean migrateIfVanilla(ItemStack item) {
        if (item == null) {
            return false;
        }

        if (NbtHandler.itemStackHasTag(item, "customitem", PersistentDataType.STRING)) {
            return false;
        }

        boolean migratedAny = false;

        int swordType = getSwordType(item);
        if (swordType != 0) {
            String roll = "sl" + swordType + "d4";
            NbtHandler.itemStackSetTag(item, "customitem", PersistentDataType.STRING, "melee");
            NbtHandler.itemStackSetTag(item, "damageroll", PersistentDataType.STRING, roll);
            NbtHandler.itemStackSetTag(item, "weapontype", PersistentDataType.STRING, "SimpleMelee");
            Utils.setLore(item,
                    "&6Melee weapon",
                    "&7Damage: &6" + Utils.getRollDisplayRange(roll));
            migratedAny = true;
        }

        int armorType = getItemArmorBonus(item);
        if (armorType != 0) {
            NbtHandler.itemStackSetTag(item, "customitem", PersistentDataType.STRING, "armor");
            NbtHandler.itemStackSetTag(item, "armorbonus", PersistentDataType.INTEGER, armorType);
            Utils.setLore(item,
                    "&6Armor",
                    "&7Armor Class: &6" + (10 + armorType));
            migratedAny = true;
        }

        int rangedType = getRangedWeaponType(item);
        if (rangedType != 0) {
            String roll = "pi1d" + (4 + (rangedType*2));
            NbtHandler.itemStackSetTag(item, "customitem", PersistentDataType.STRING, "ranged");
            NbtHandler.itemStackSetTag(item, "damageroll", PersistentDataType.STRING, roll);
            NbtHandler.itemStackSetTag(item, "weapontype", PersistentDataType.STRING, "SimpleRanged");
            Utils.setLore(item,
                    "&6Ranged weapon",
                    "&7Damage: &6" + Utils.getRollDisplayRange(roll));
            migratedAny = true;
        }

        int foodValue = getFoodValue(item);
        if (foodValue != 0) {
            NbtHandler.itemStackSetTag(item, "customitem", PersistentDataType.STRING, "supplies");
            NbtHandler.itemStackSetTag(item, "supplyvalue", PersistentDataType.INTEGER, foodValue);
            Utils.setLore(item, "&6Camp Supplies: " + foodValue);
            migratedAny = true;
        }

        return migratedAny;
    }

    private int getSwordType(ItemStack item) {
        switch (item.getType()) {
            case WOODEN_SWORD:
                return 1;
            case STONE_SWORD:
                return 2;
            case IRON_SWORD:
                return 3;
            case DIAMOND_SWORD:
                return 4;
            case NETHERITE_SWORD:
                return 5;
        }

        return 0;
    }

    private int getRangedWeaponType(ItemStack item) {
        switch (item.getType()) {
            case BOW:
                return 1;
            case CROSSBOW:
                return 2;
        }

        return 0;
    }

    private int getItemArmorBonus(ItemStack item) {
        String[] parts = item.getType().name().split("_");
        String secondPart = parts.length > 1 ? parts[1] : "";

        int pieceModifier = 0;

        switch (secondPart) {
            case "HELMET":
                pieceModifier = -1;
                break;

            case "CHESTPLATE":
                break;

            case "LEGGINGS":
            case "BOOTS":
                pieceModifier = -2;
                break;

            default:
                return 0;
        }

        // It's armor
        String type = parts[0];
        int typeValue = 0;

        switch (type) {
            case "LEATHER":
                typeValue = 1;
                break;
            case "CHAINMAIL":
                typeValue = 1;
                break;
            case "GOLD":
                typeValue = 1;
                break;
            case "IRON":
                typeValue = 2;
                break;
            case "DIAMOND":
                typeValue = 3;
                break;
            case "NETHERITE":
                typeValue = 4;
                break;
        }

        return Utils.clamp(typeValue + pieceModifier, 1, 10);
    }

    private int getFoodValue(ItemStack item) {
        switch (item.getType()) {
            case COOKED_BEEF:
            case COOKED_CHICKEN:
            case COOKED_COD:
            case COOKED_MUTTON:
            case COOKED_PORKCHOP:
            case COOKED_RABBIT:
            case COOKED_SALMON:
                return 5;

            case SUSPICIOUS_STEW:
            case COOKIE:
            case BEETROOT:
            case POTATO:
            case CARROT:
            case APPLE:
                return 2;

            case GOLDEN_CARROT:
            case GOLDEN_APPLE:
                return 10;

            case ENCHANTED_GOLDEN_APPLE:
                return 20;

            case HONEY_BOTTLE:
            case MILK_BUCKET:
            case BEETROOT_SOUP:
            case RABBIT_STEW:
            case MUSHROOM_STEW:
            case PUMPKIN_PIE:
            case PUMPKIN:
            case CAKE:
            case BREAD:
            case DRIED_KELP_BLOCK:
            case BAKED_POTATO:
            case MELON:
                return 4;

            case BROWN_MUSHROOM:
            case RED_MUSHROOM:
            case SPIDER_EYE:
            case ROTTEN_FLESH:
            case PUFFERFISH:
            case DRIED_KELP:
            case POISONOUS_POTATO:
            case CHORUS_FRUIT:
            case MELON_SLICE:
            case SWEET_BERRIES:
            case GLOW_BERRIES:
                return 1;

            case BEEF:
            case PORKCHOP:
            case MUTTON:
            case CHICKEN:
            case RABBIT:
            case COD:
            case SALMON:
            case TROPICAL_FISH:
                return 3;
        }

        return 0;
    }
}
