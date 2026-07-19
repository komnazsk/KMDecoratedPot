package com.github.komnazsk.kmdecoratedpot;

import org.bukkit.Material;
import org.bukkit.block.DecoratedPot;

import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

enum PotPreset {
    SAME("全面同じ", 1),
    FRONT("正面のみ", 1),
    SYMMETRIC("左右対称", 3),
    INDIVIDUAL("4面個別", 4);

    private final String displayName;
    private final int selectionCount;

    PotPreset(String displayName, int selectionCount) {
        this.displayName = displayName;
        this.selectionCount = selectionCount;
    }

    String displayName() {
        return displayName;
    }

    int selectionCount() {
        return selectionCount;
    }

    String selectionLabel(int index) {
        return switch (this) {
            case SAME -> "全ての面";
            case FRONT -> "正面";
            case SYMMETRIC -> List.of("正面", "右・左側面", "背面").get(index);
            case INDIVIDUAL -> List.of("正面", "右側面", "背面", "左側面").get(index);
        };
    }

    Map<DecoratedPot.Side, Material> expand(List<Material> choices) {
        EnumMap<DecoratedPot.Side, Material> result = new EnumMap<>(DecoratedPot.Side.class);
        Material brick = Material.BRICK;
        switch (this) {
            case SAME -> {
                for (DecoratedPot.Side side : DecoratedPot.Side.values()) result.put(side, choices.getFirst());
            }
            case FRONT -> {
                for (DecoratedPot.Side side : DecoratedPot.Side.values()) result.put(side, brick);
                result.put(DecoratedPot.Side.FRONT, choices.getFirst());
            }
            case SYMMETRIC -> {
                result.put(DecoratedPot.Side.FRONT, choices.get(0));
                result.put(DecoratedPot.Side.RIGHT, choices.get(1));
                result.put(DecoratedPot.Side.BACK, choices.get(2));
                result.put(DecoratedPot.Side.LEFT, choices.get(1));
            }
            case INDIVIDUAL -> {
                result.put(DecoratedPot.Side.FRONT, choices.get(0));
                result.put(DecoratedPot.Side.RIGHT, choices.get(1));
                result.put(DecoratedPot.Side.BACK, choices.get(2));
                result.put(DecoratedPot.Side.LEFT, choices.get(3));
            }
        }
        return result;
    }

    static PotPreset parse(String value) {
        try {
            return valueOf(value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return switch (value) {
                case "全面同じ" -> SAME;
                case "正面のみ" -> FRONT;
                case "左右対称" -> SYMMETRIC;
                case "4面個別" -> INDIVIDUAL;
                default -> null;
            };
        }
    }
}
