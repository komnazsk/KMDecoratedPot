package com.github.komnazsk.kmdecoratedpot;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.BlockState;
import org.bukkit.block.DecoratedPot;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Main plugin entry point for creating customized decorated pot items through a command and GUI menus.
 */
public final class KMDecoratedPotPlugin extends JavaPlugin implements Listener, TabExecutor {
    private static final int PATTERNS_PER_PAGE = 45;
    private static final int PRESET_MENU_SIZE = 27;
    private static final int PATTERN_MENU_SIZE = 54;
    private static final int PRESET_SLOT_SAME = 10;
    private static final int PRESET_SLOT_FRONT = 12;
    private static final int PRESET_SLOT_SYMMETRIC = 14;
    private static final int PRESET_SLOT_INDIVIDUAL = 16;
    private static final int BACK_SLOT = 45;
    private static final int PREVIOUS_PAGE_SLOT = 48;
    private static final int NEXT_PAGE_SLOT = 50;
    private static final int CANCEL_SLOT = 53;
    private static final List<Integer> PRESET_SLOTS = List.of(PRESET_SLOT_SAME, PRESET_SLOT_FRONT,
            PRESET_SLOT_SYMMETRIC, PRESET_SLOT_INDIVIDUAL);
    private static final List<Material> PRESET_ICONS = List.of(Material.DECORATED_POT, Material.BRICK,
            Material.ARMS_UP_POTTERY_SHERD, Material.FLOW_POTTERY_SHERD);
    private static final List<String> PRESET_COMMANDS = List.of(UiText.PRESET_SAME_COMMAND, UiText.PRESET_FRONT_COMMAND,
            UiText.PRESET_SYMMETRIC_COMMAND, UiText.PRESET_INDIVIDUAL_COMMAND);

    private final Map<UUID, Selection> selections = new HashMap<>();
    private List<Material> patterns;

    /**
     * Initializes selectable pot patterns and registers the command and event listeners.
     */
    @Override
    public void onEnable() {
        patterns = new ArrayList<>(Tag.ITEMS_DECORATED_POT_SHERDS.getValues());
        patterns.add(Material.BRICK);
        patterns.sort(Comparator.comparing(Material::getKey));
        getServer().getPluginManager().registerEvents(this, this);
        var command = getCommand(UiText.COMMAND_NAME);
        if (command == null) {
            throw new IllegalStateException(UiText.MISSING_COMMAND_ERROR);
        }
        command.setExecutor(this);
        command.setTabCompleter(this);
    }

    /**
     * Handles the /pot command, either opening the preset GUI or creating a pot from command arguments.
     * <p>
     * Flow:
     * <ul>
     *     <li>Reject non-player senders.</li>
     *     <li>Open the preset menu when no arguments are supplied.</li>
     *     <li>Validate the requested preset and pattern arguments.</li>
     *     <li>Store the in-progress selection.</li>
     *     <li>Finish immediately when complete, or continue in the pattern selection menu.</li>
     * </ul>
     *
     * @param sender command sender
     * @param command executed Bukkit command
     * @param label command label used by the sender
     * @param args preset name followed by optional pattern names
     * @return true when the command was handled
     */
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(UiText.COMMAND_PLAYER_ONLY.text(UiLanguage.ENGLISH));
            return true;
        }

        UiLanguage language = UiLanguage.of(player);
        if (args.length == 0) {
            openPresetMenu(player);
            return true;
        }

        PotPreset preset = PotPreset.parse(args[0]);
        if (preset == null) {
            player.sendMessage(Component.text(UiText.UNKNOWN_PRESET.text(language) + args[0], NamedTextColor.RED));
            return true;
        }
        if (args.length - 1 > preset.selectionCount()) {
            player.sendMessage(Component.text(UiText.TOO_MANY_PATTERNS.text(language) + preset.selectionCount(),
                    NamedTextColor.RED));
            return true;
        }

        Selection selection = new Selection(preset);
        for (int i = 1; i < args.length; i++) {
            Material pattern = parsePattern(args[i]);
            if (pattern == null) {
                player.sendMessage(Component.text(UiText.UNKNOWN_PATTERN.text(language) + args[i],
                        NamedTextColor.RED));
                return true;
            }
            selection.choices.add(pattern);
        }
        selections.put(player.getUniqueId(), selection);
        if (selection.complete()) {
            finish(player, selection);
        } else {
            openPatternMenu(player, 0);
        }
        return true;
    }

    /**
     * Opens the preset selection menu and clears any in-progress selection for the player.
     *
     * @param player player who will receive the menu
     */
    private void openPresetMenu(Player player) {
        selections.remove(player.getUniqueId());
        UiLanguage language = UiLanguage.of(player);
        Inventory inventory = Bukkit.createInventory(new PotMenuHolder(PotMenuHolder.Type.PRESET, 0),
                PRESET_MENU_SIZE, Component.text(UiText.PRESET_MENU_TITLE.text(language)));
        PotPreset[] presets = PotPreset.values();
        for (int i = 0; i < presets.length; i++) {
            PotPreset preset = presets[i];
            inventory.setItem(PRESET_SLOTS.get(i), namedItem(PRESET_ICONS.get(i),
                    Component.text(preset.displayName(language), NamedTextColor.YELLOW),
                    List.of(Component.text(UiText.PATTERNS_REQUIRED.text(language) + preset.selectionCount(),
                            NamedTextColor.GRAY))));
        }
        player.openInventory(inventory);
    }

    /**
     * Opens the preset menu when a permitted player right-clicks with a brick.
     *
     * @param event player interaction event
     */
    @EventHandler
    public void onBrickUse(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        ItemStack item = event.getItem();
        if (item == null || item.getType() != Material.BRICK) {
            return;
        }
        Player player = event.getPlayer();
        if (!player.hasPermission(UiText.COMMAND_PERMISSION)) {
            return;
        }
        if (player.getOpenInventory().getTopInventory().getHolder(false) instanceof PotMenuHolder) {
            return;
        }

        event.setCancelled(true);
        openPresetMenu(player);
    }

    /**
     * Opens a paged pattern selection menu for the next required side group.
     *
     * @param player player who will receive the menu
     * @param requestedPage requested zero-based page number
     */
    private void openPatternMenu(Player player, int requestedPage) {
        Selection selection = selections.get(player.getUniqueId());
        if (selection == null) {
            return;
        }
        int maxPage = Math.max(0, (patterns.size() - 1) / PATTERNS_PER_PAGE);
        int page = Math.max(0, Math.min(requestedPage, maxPage));
        UiLanguage language = UiLanguage.of(player);
        String label = selection.preset.selectionLabel(selection.choices.size(), language);
        Inventory inventory = Bukkit.createInventory(new PotMenuHolder(PotMenuHolder.Type.PATTERN, page),
                PATTERN_MENU_SIZE, Component.text(patternMenuTitle(label, language)));
        int start = page * PATTERNS_PER_PAGE;
        for (int slot = 0; slot < PATTERNS_PER_PAGE && start + slot < patterns.size(); slot++) {
            Material material = patterns.get(start + slot);
            inventory.setItem(slot, namedItem(material, Component.translatable(material.translationKey())
                    .color(NamedTextColor.YELLOW), List.of(Component.text(material.getKey().asString(),
                            NamedTextColor.DARK_GRAY))));
        }
        inventory.setItem(BACK_SLOT, namedItem(Material.ARROW,
                Component.text(UiText.GO_BACK.text(language), NamedTextColor.YELLOW), List.of()));
        if (page > 0) {
            inventory.setItem(PREVIOUS_PAGE_SLOT, namedItem(Material.SPECTRAL_ARROW,
                    Component.text(UiText.PREVIOUS_PAGE.text(language), NamedTextColor.YELLOW), List.of()));
        }
        if (page < maxPage) {
            inventory.setItem(NEXT_PAGE_SLOT, namedItem(Material.SPECTRAL_ARROW,
                    Component.text(UiText.NEXT_PAGE.text(language), NamedTextColor.YELLOW), List.of()));
        }
        inventory.setItem(CANCEL_SLOT, namedItem(Material.BARRIER,
                Component.text(UiText.CANCEL.text(language), NamedTextColor.YELLOW), List.of()));
        player.openInventory(inventory);
    }

    /**
     * Builds the localized title for a pattern selection menu.
     *
     * @param label localized side or side-group label
     * @param language UI language for the player
     * @return localized inventory title
     */
    private String patternMenuTitle(String label, UiLanguage language) {
        return language == UiLanguage.JAPANESE
                ? label + UiText.PATTERN_MENU_TITLE_SUFFIX.text(language)
                : UiText.PATTERN_MENU_TITLE_SUFFIX.text(language) + label;
    }

    /**
     * Handles all clicks inside plugin-owned menus.
     * <p>
     * Flow:
     * <ul>
     *     <li>Ignore clicks outside plugin menus.</li>
     *     <li>Cancel Bukkit's default inventory behavior.</li>
     *     <li>Handle preset menu slots.</li>
     *     <li>Handle pattern selection slots, back, page navigation, and cancellation for pattern menus.</li>
     *     <li>Schedule menu transitions for the next server tick to avoid replacing the active inventory during its
     *     own click event.</li>
     * </ul>
     *
     * @param event inventory click event
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getView().getTopInventory().getHolder(false) instanceof PotMenuHolder holder)) {
            return;
        }
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        int slot = event.getRawSlot();
        if (slot < 0 || slot >= event.getView().getTopInventory().getSize()) {
            return;
        }

        if (holder.type() == PotMenuHolder.Type.PRESET) {
            int index = PRESET_SLOTS.indexOf(slot);
            if (index < 0) {
                return;
            }
            selections.put(player.getUniqueId(), new Selection(PotPreset.values()[index]));
            // Reopen menus on the next server tick to avoid mutating the active inventory during its click event.
            Bukkit.getScheduler().runTask(this, () -> openPatternMenu(player, 0));
            return;
        }

        Selection selection = selections.get(player.getUniqueId());
        if (selection == null) {
            player.closeInventory();
            return;
        }
        if (slot < PATTERNS_PER_PAGE) {
            int patternIndex = holder.page() * PATTERNS_PER_PAGE + slot;
            if (patternIndex >= patterns.size()) {
                return;
            }
            selection.choices.add(patterns.get(patternIndex));
            Bukkit.getScheduler().runTask(this, () -> {
                if (selection.complete()) {
                    finish(player, selection);
                } else {
                    openPatternMenu(player, 0);
                }
            });
        } else if (slot == BACK_SLOT) {
            if (!selection.choices.isEmpty()) {
                selection.choices.removeLast();
            }
            Bukkit.getScheduler().runTask(this, () -> {
                if (selection.choices.isEmpty()) {
                    openPresetMenu(player);
                } else {
                    openPatternMenu(player, 0);
                }
            });
        } else if (slot == PREVIOUS_PAGE_SLOT && holder.page() > 0) {
            Bukkit.getScheduler().runTask(this, () -> openPatternMenu(player, holder.page() - 1));
        } else if (slot == NEXT_PAGE_SLOT) {
            Bukkit.getScheduler().runTask(this, () -> openPatternMenu(player, holder.page() + 1));
        } else if (slot == CANCEL_SLOT) {
            selections.remove(player.getUniqueId());
            player.closeInventory();
        }
    }

    /**
     * Prevents dragging items into or out of plugin-owned menus.
     *
     * @param event inventory drag event
     */
    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getView().getTopInventory().getHolder(false) instanceof PotMenuHolder) {
            event.setCancelled(true);
        }
    }

    /**
     * Clears a player's in-progress selection when they leave the plugin menu flow.
     *
     * @param event inventory close event
     */
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getInventory().getHolder(false) instanceof PotMenuHolder)) {
            return;
        }
        UUID id = event.getPlayer().getUniqueId();
        // InventoryCloseEvent also fires while switching between our menus, so cleanup is delayed until the
        // replacement inventory has had a chance to open.
        Bukkit.getScheduler().runTask(this, () -> {
            if (!(event.getPlayer().getOpenInventory().getTopInventory().getHolder(false) instanceof PotMenuHolder)) {
                selections.remove(id);
            }
        });
    }

    /**
     * Creates the decorated pot item from a completed selection and gives it to the player.
     *
     * @param player player receiving the item
     * @param selection completed pattern selection
     */
    private void finish(Player player, Selection selection) {
        ItemStack pot = new ItemStack(Material.DECORATED_POT);
        BlockStateMeta meta = (BlockStateMeta) pot.getItemMeta();
        BlockState state = meta.getBlockState();
        if (!(state instanceof DecoratedPot decoratedPot)) {
            throw new IllegalStateException(UiText.INVALID_POT_STATE_ERROR);
        }
        selection.preset.expand(selection.choices).forEach(decoratedPot::setSherd);
        meta.setBlockState(decoratedPot);
        UiLanguage language = UiLanguage.of(player);
        meta.displayName(Component.text(UiText.CUSTOMIZED_POT.text(language), NamedTextColor.GOLD));
        pot.setItemMeta(meta);

        selections.remove(player.getUniqueId());
        player.closeInventory();
        Map<Integer, ItemStack> overflow = player.getInventory().addItem(pot);
        overflow.values().forEach(item -> player.getWorld().dropItemNaturally(player.getLocation(), item));
        player.sendMessage(Component.text(overflow.isEmpty()
                ? UiText.POT_ADDED.text(language) : UiText.POT_DROPPED.text(language), NamedTextColor.GREEN));
    }

    /**
     * Parses a command argument as a selectable pot pattern.
     *
     * @param input raw command argument
     * @return matching material, or null when the argument is not selectable
     */
    private Material parsePattern(String input) {
        String normalized = input.toUpperCase(Locale.ROOT).replace('-', '_');
        Material material = Material.matchMaterial(normalized);
        if (material == null && !normalized.endsWith(UiText.POTTERY_SHERD_SUFFIX)) {
            material = Material.matchMaterial(normalized + UiText.POTTERY_SHERD_SUFFIX);
        }
        return material != null && patterns.contains(material) ? material : null;
    }

    /**
     * Creates a named item stack for use as an inventory button.
     *
     * @param material item material
     * @param name display name component
     * @param lore lore lines
     * @return item stack with display metadata applied
     */
    private ItemStack namedItem(Material material, Component name, List<Component> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(name);
        if (!lore.isEmpty()) {
            meta.lore(lore);
        }
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Provides command completions for preset names and pattern names.
     *
     * @param sender command sender
     * @param command command being completed
     * @param alias alias used for completion
     * @param args current command arguments
     * @return matching completion candidates
     */
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return filter(PRESET_COMMANDS, args[0]);
        }
        PotPreset preset = PotPreset.parse(args[0]);
        if (preset == null || args.length - 1 > preset.selectionCount()) {
            return List.of();
        }
        return filter(patterns.stream().map(this::commandPatternName).toList(), args[args.length - 1]);
    }

    /**
     * Converts a material key to the shorter command-facing pattern name.
     *
     * @param material selectable pattern material
     * @return command argument name
     */
    private String commandPatternName(Material material) {
        String key = material.getKey().getKey();
        return key.endsWith(UiText.COMMAND_POTTERY_SHERD_SUFFIX)
                ? key.substring(0, key.length() - UiText.COMMAND_POTTERY_SHERD_SUFFIX.length()) : key;
    }

    /**
     * Filters completion candidates by a lowercase prefix.
     *
     * @param values candidates to filter
     * @param prefix typed prefix
     * @return candidates that start with the prefix
     */
    private List<String> filter(List<String> values, String prefix) {
        String lower = prefix.toLowerCase(Locale.ROOT);
        return values.stream().filter(value -> value.startsWith(lower)).toList();
    }

    /**
     * Tracks one player's in-progress preset and pattern choices while they move through the GUI.
     */
    private static final class Selection {
        private final PotPreset preset;
        private final List<Material> choices = new ArrayList<>();

        /**
         * Creates a new in-progress selection for the given preset.
         *
         * @param preset selected preset
         */
        private Selection(PotPreset preset) {
            this.preset = preset;
        }

        /**
         * Checks whether all required pattern choices have been selected.
         *
         * @return true when the selection has enough choices for its preset
         */
        private boolean complete() {
            return choices.size() == preset.selectionCount();
        }
    }
}
