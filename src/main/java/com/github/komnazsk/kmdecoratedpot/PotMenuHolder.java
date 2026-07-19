package com.github.komnazsk.kmdecoratedpot;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

final class PotMenuHolder implements InventoryHolder {
    enum Type { PRESET, PATTERN }

    private final Type type;
    private final int page;

    PotMenuHolder(Type type, int page) {
        this.type = type;
        this.page = page;
    }

    Type type() { return type; }
    int page() { return page; }

    @Override
    public @NotNull Inventory getInventory() {
        throw new UnsupportedOperationException("This holder only identifies KMDecoratedPot menus");
    }
}
