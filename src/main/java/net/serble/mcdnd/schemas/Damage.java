package net.serble.mcdnd.schemas;

import net.serble.mcdnd.Tuple;
import net.serble.mcdnd.Utils;

import java.util.List;

public class Damage {
    private final Tuple<DamageType, String>[] damages;

    @SafeVarargs
    public Damage(Tuple<DamageType, String>... dmgs) {
        damages = dmgs;
    }

    public static Damage parse(String str) {
        return Utils.parseDamage(str);
    }

    public Damage(List<Tuple<DamageType, String>> dmgs) {
        //noinspection unchecked  trust me bro
        damages = dmgs.toArray(new Tuple[0]);
    }

    public Damage(DamageType type, String str) {
        //noinspection unchecked  trust me bro
        damages = new Tuple[] {
                new Tuple<>(type, str)
        };
    }

    public Tuple<DamageType, String>[] getDamages() {
        return damages;
    }
}
