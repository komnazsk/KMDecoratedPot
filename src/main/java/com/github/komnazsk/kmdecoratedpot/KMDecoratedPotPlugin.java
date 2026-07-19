package com.github.komnazsk.kmdecoratedpot;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.DecoratedPot;
import org.bukkit.block.BlockState;
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

public final class KMDecoratedPotPlugin extends JavaPlugin implements Listener, TabExecutor {
    private static final int PATTERNS_PER_PAGE = 45;
    private static final List<Integer> PRESET_SLOTS = List.of(10, 12, 14, 16);

    private final Map<UUID, Selection> selections = new HashMap<>();
    private List<Material> patterns;

    @Override
    public void onEnable() {
        patterns = new ArrayList<>(Tag.ITEMS_DECORATED_POT_SHERDS.getValues());
        patterns.add(Material.BRICK);
        patterns.sort(Comparator.comparing(Material::getKey));
        getServer().getPluginManager().registerEvents(this, this);
        var command = getCommand("pot");
        if (command == null) throw new IllegalStateException("pot command is missing from plugin.yml");
        command.setExecutor(this);
        command.setTabCompleter(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("このコマンドはプレイヤーのみ使用できます。");
            return true;
        }
        if (args.length == 0) {
            openPresetMenu(player);
            return true;
        }

        PotPreset preset = PotPreset.parse(args[0]);
        if (preset == null) {
            player.sendMessage(Component.text("不明なプリセットです: " + args[0], NamedTextColor.RED));
            return true;
        }
        if (args.length - 1 > preset.selectionCount()) {
            player.sendMessage(Component.text("柄の指定が多すぎます。必要数: " + preset.selectionCount(), NamedTextColor.RED));
            return true;
        }

        Selection selection = new Selection(preset);
        for (int i = 1; i < args.length; i++) {
            Material pattern = parsePattern(args[i]);
            if (pattern == null) {
                player.sendMessage(Component.text("不明な壺の柄です: " + args[i], NamedTextColor.RED));
                return true;
            }
            selection.choices.add(pattern);
        }
        selections.put(player.getUniqueId(), selection);
        if (selection.complete()) finish(player, selection);
        else openPatternMenu(player, 0);
        return true;
    }

    private void openPresetMenu(Player player) {
        selections.remove(player.getUniqueId());
        Inventory inventory = Bukkit.createInventory(new PotMenuHolder(PotMenuHolder.Type.PRESET, 0), 27,
                Component.text("飾り壺のプリセット"));
        PotPreset[] presets = PotPreset.values();
        Material[] icons = {Material.DECORATED_POT, Material.BRICK,
                Material.ARMS_UP_POTTERY_SHERD, Material.FLOW_POTTERY_SHERD};
        for (int i = 0; i < presets.length; i++) {
            PotPreset preset = presets[i];
            inventory.setItem(PRESET_SLOTS.get(i), namedItem(icons[i], preset.displayName(),
                    List.of(Component.text("必要な柄: " + preset.selectionCount(), NamedTextColor.GRAY))));
        }
        player.openInventory(inventory);
    }

    @EventHandler
    public void onBrickUse(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        ItemStack item = event.getItem();
        if (item == null || item.getType() != Material.BRICK) return;
        Player player = event.getPlayer();
        if (!player.hasPermission("kmdecoratedpot.command.pot")) return;
        if (player.getOpenInventory().getTopInventory().getHolder(false) instanceof PotMenuHolder) return;

        event.setCancelled(true);
        openPresetMenu(player);
    }

    private void openPatternMenu(Player player, int requestedPage) {
        Selection selection = selections.get(player.getUniqueId());
        if (selection == null) return;
        int maxPage = Math.max(0, (patterns.size() - 1) / PATTERNS_PER_PAGE);
        int page = Math.max(0, Math.min(requestedPage, maxPage));
        String label = selection.preset.selectionLabel(selection.choices.size());
        Inventory inventory = Bukkit.createInventory(new PotMenuHolder(PotMenuHolder.Type.PATTERN, page), 54,
                Component.text(label + "の柄を選択"));
        int start = page * PATTERNS_PER_PAGE;
        for (int slot = 0; slot < PATTERNS_PER_PAGE && start + slot < patterns.size(); slot++) {
            Material material = patterns.get(start + slot);
            inventory.setItem(slot, namedItem(material, patternName(material), List.of()));
        }
        inventory.setItem(45, namedItem(Material.ARROW, "ひとつ前に戻る", List.of()));
        if (page > 0) inventory.setItem(48, namedItem(Material.SPECTRAL_ARROW, "前のページ", List.of()));
        if (page < maxPage) inventory.setItem(50, namedItem(Material.SPECTRAL_ARROW, "次のページ", List.of()));
        inventory.setItem(53, namedItem(Material.BARRIER, "キャンセル", List.of()));
        player.openInventory(inventory);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getView().getTopInventory().getHolder(false) instanceof PotMenuHolder holder)) return;
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) return;
        int slot = event.getRawSlot();
        if (slot < 0 || slot >= event.getView().getTopInventory().getSize()) return;

        if (holder.type() == PotMenuHolder.Type.PRESET) {
            int index = PRESET_SLOTS.indexOf(slot);
            if (index < 0) return;
            selections.put(player.getUniqueId(), new Selection(PotPreset.values()[index]));
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
            if (patternIndex >= patterns.size()) return;
            selection.choices.add(patterns.get(patternIndex));
            Bukkit.getScheduler().runTask(this, () -> {
                if (selection.complete()) finish(player, selection);
                else openPatternMenu(player, 0);
            });
        } else if (slot == 45) {
            if (!selection.choices.isEmpty()) selection.choices.removeLast();
            Bukkit.getScheduler().runTask(this, () -> {
                if (selection.choices.isEmpty()) openPresetMenu(player);
                else openPatternMenu(player, 0);
            });
        } else if (slot == 48 && holder.page() > 0) {
            Bukkit.getScheduler().runTask(this, () -> openPatternMenu(player, holder.page() - 1));
        } else if (slot == 50) {
            Bukkit.getScheduler().runTask(this, () -> openPatternMenu(player, holder.page() + 1));
        } else if (slot == 53) {
            selections.remove(player.getUniqueId());
            player.closeInventory();
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getView().getTopInventory().getHolder(false) instanceof PotMenuHolder) event.setCancelled(true);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getInventory().getHolder(false) instanceof PotMenuHolder)) return;
        UUID id = event.getPlayer().getUniqueId();
        Bukkit.getScheduler().runTask(this, () -> {
            if (!(event.getPlayer().getOpenInventory().getTopInventory().getHolder(false) instanceof PotMenuHolder)) {
                selections.remove(id);
            }
        });
    }

    private void finish(Player player, Selection selection) {
        ItemStack pot = new ItemStack(Material.DECORATED_POT);
        BlockStateMeta meta = (BlockStateMeta) pot.getItemMeta();
        BlockState state = meta.getBlockState();
        if (!(state instanceof DecoratedPot decoratedPot)) {
            throw new IllegalStateException("Decorated pot item did not provide DecoratedPot block state");
        }
        selection.preset.expand(selection.choices).forEach(decoratedPot::setSherd);
        meta.setBlockState(decoratedPot);
        meta.displayName(Component.text("カスタマイズした飾り壺", NamedTextColor.GOLD));
        pot.setItemMeta(meta);

        selections.remove(player.getUniqueId());
        player.closeInventory();
        Map<Integer, ItemStack> overflow = player.getInventory().addItem(pot);
        overflow.values().forEach(item -> player.getWorld().dropItemNaturally(player.getLocation(), item));
        player.sendMessage(Component.text(overflow.isEmpty()
                ? "飾り壺をインベントリに追加しました。"
                : "インベントリが満杯のため、飾り壺を足元に落としました。", NamedTextColor.GREEN));
    }

    private Material parsePattern(String input) {
        String normalized = input.toUpperCase(Locale.ROOT).replace('-', '_');
        Material material = Material.matchMaterial(normalized);
        if (material == null && !normalized.endsWith("_POTTERY_SHERD")) {
            material = Material.matchMaterial(normalized + "_POTTERY_SHERD");
        }
        return material != null && patterns.contains(material) ? material : null;
    }

    private String patternName(Material material) {
        return material == Material.BRICK ? "無地（レンガ）" : material.getKey().getKey();
    }

    private ItemStack namedItem(Material material, String name, List<Component> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(name, NamedTextColor.YELLOW));
        if (!lore.isEmpty()) meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) return filter(List.of("same", "front", "symmetric", "individual"), args[0]);
        PotPreset preset = PotPreset.parse(args[0]);
        if (preset == null || args.length - 1 > preset.selectionCount()) return List.of();
        return filter(patterns.stream().map(this::commandPatternName).toList(), args[args.length - 1]);
    }

    private String commandPatternName(Material material) {
        String key = material.getKey().getKey();
        return key.endsWith("_pottery_sherd") ? key.substring(0, key.length() - "_pottery_sherd".length()) : key;
    }

    private List<String> filter(List<String> values, String prefix) {
        String lower = prefix.toLowerCase(Locale.ROOT);
        return values.stream().filter(value -> value.startsWith(lower)).toList();
    }

    private static final class Selection {
        private final PotPreset preset;
        private final List<Material> choices = new ArrayList<>();

        private Selection(PotPreset preset) { this.preset = preset; }
        private boolean complete() { return choices.size() == preset.selectionCount(); }
    }
}
