package com.github.komnazsk.kmdecoratedpot;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

/**
 * Marker inventory holder used to identify KMDecoratedPot GUI inventories and their page state.
 */
final class PotMenuHolder implements InventoryHolder {
    /**
     * Menu categories handled by the plugin.
     */
    enum Type { PRESET, PATTERN }

    private final Type type;
    private final int page;

    /**
     * Creates a holder that identifies one plugin menu inventory.
     *
     * @param type menu type
     * @param page zero-based page number for pattern menus
     */
    PotMenuHolder(Type type, int page) {
        this.type = type;
        this.page = page;
    }

    /**
     * Returns the menu type represented by this holder.
     *
     * @return menu type
     */
    Type type() {
        return type;
    }

    /**
     * Returns the page number represented by this holder.
     *
     * @return zero-based page number
     */
    int page() {
        return page;
    }

    /**
     * InventoryHolder requires this method, but the plugin only uses this class as an identity marker.
     *
     * @return never returns normally
     */
    @Override
    public @NotNull Inventory getInventory() {
        throw new UnsupportedOperationException(UiText.MENU_HOLDER_UNSUPPORTED_ERROR);
    }
}
