package net.serble.mcdnd.actions;

import net.serble.mcdnd.Main;
import net.serble.mcdnd.schemas.PlayerStats;
import org.bukkit.entity.LivingEntity;

public abstract class SpellSlotSpellBase extends SpellBase {
    protected abstract int getSpellSlotLevel();
    protected abstract void cast(LivingEntity e);

    @Override
    public void runWithTarget(LivingEntity e, LivingEntity target) {
        PlayerStats stats = Main.getInstance().getPlayerManager().getStatsFor(e);
        stats.consumeSpellSlot(getSpellSlotLevel());
        cast(e);
    }

    @Override
    public boolean canUse(LivingEntity e) {
        PlayerStats stats = Main.getInstance().getPlayerManager().getStatsFor(e);
        int remaining = stats.getRemainingSpellSlots().getOrDefault(getSpellSlotLevel(), 0);
        return remaining > 0;
    }
}
