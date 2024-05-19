package net.serble.mcdnd.schemas;

import net.serble.mcdnd.Tuple;
import net.serble.mcdnd.Utils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Damage {
    private Tuple<DamageType, String>[] damages;

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

    public String getDamageString() {
        return Utils.serialiseDamage(this);
    }

    public Damage add(Damage dmg) {
        List<Tuple<DamageType, String>> ourDmgs = Arrays.stream(damages).collect(Collectors.toList());
        List<Tuple<DamageType, String>> newDmgs = Arrays.stream(dmg.getDamages()).collect(Collectors.toList());
        ourDmgs.addAll(newDmgs);
        //noinspection unchecked  trust me bro
        damages = ourDmgs.toArray(new Tuple[0]);
        return this;
    }
}
