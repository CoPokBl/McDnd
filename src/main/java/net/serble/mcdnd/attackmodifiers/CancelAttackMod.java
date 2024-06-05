package net.serble.mcdnd.attackmodifiers;

public class CancelAttackMod extends AttackModifier {

    @Override
    public boolean shouldCancelAttack() {
        return true;
    }
}
