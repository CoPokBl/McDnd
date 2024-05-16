package net.serble.mcdnd.classes;

public enum DndClass {
    DEFAULT("Default"),
    ROGUE("Rogue");

    private final String name;

    DndClass(String n) {
        name = n;
    }

    public String getName() {
        return name;
    }
}
