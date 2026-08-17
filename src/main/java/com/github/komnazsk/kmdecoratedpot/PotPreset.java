package com.github.komnazsk.kmdecoratedpot;

import org.bukkit.Material;
import org.bukkit.block.DecoratedPot;

import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Preset strategies that determine how selected patterns are applied to the four decorated pot sides.
 */
enum PotPreset {
    SAME(UiText.PRESET_SAME, List.of(UiText.SIDE_ALL)),
    FRONT(UiText.PRESET_FRONT, List.of(UiText.SIDE_FRONT)),
    SYMMETRIC(UiText.PRESET_SYMMETRIC, List.of(UiText.SIDE_FRONT, UiText.SIDE_RIGHT_AND_LEFT, UiText.SIDE_BACK)),
    INDIVIDUAL(UiText.PRESET_INDIVIDUAL, List.of(UiText.SIDE_FRONT, UiText.SIDE_RIGHT, UiText.SIDE_BACK,
            UiText.SIDE_LEFT));

    private static final int FIRST_CHOICE_INDEX = 0;
    private static final int RIGHT_CHOICE_INDEX = 1;
    private static final int BACK_CHOICE_INDEX = 2;
    private static final int LEFT_CHOICE_INDEX = 3;

    private final UiText.LocalizedText displayName;
    private final List<UiText.LocalizedText> selectionLabels;

    /**
     * Creates a preset definition.
     *
     * @param displayName localized preset display name
     * @param selectionLabels labels for the pattern choices required from the player
     */
    PotPreset(UiText.LocalizedText displayName, List<UiText.LocalizedText> selectionLabels) {
        this.displayName = displayName;
        this.selectionLabels = selectionLabels;
    }

    /**
     * Returns the localized display name shown in the preset menu.
     *
     * @param language UI language for the player
     * @return localized preset name
     */
    String displayName(UiLanguage language) {
        return displayName.text(language);
    }

    /**
     * Returns the number of pattern choices this preset needs.
     *
     * @return required choice count
     */
    int selectionCount() {
        return selectionLabels.size();
    }

    /**
     * Returns the label for the pattern choice currently being requested.
     *
     * @param index zero-based choice index
     * @param language UI language for the player
     * @return localized side or side-group label
     */
    String selectionLabel(int index, UiLanguage language) {
        return selectionLabels.get(index).text(language);
    }

    /**
     * Expands the compact user choices into the four decorated pot sides.
     *
     * @param choices selected pattern materials in the order requested by this preset
     * @return mapping from each pot side to the material to set
     */
    Map<DecoratedPot.Side, Material> expand(List<Material> choices) {
        EnumMap<DecoratedPot.Side, Material> result = new EnumMap<>(DecoratedPot.Side.class);
        Material brick = Material.BRICK;
        switch (this) {
            case SAME -> {
                for (DecoratedPot.Side side : DecoratedPot.Side.values()) {
                    result.put(side, choices.get(FIRST_CHOICE_INDEX));
                }
            }
            case FRONT -> {
                // A plain brick sherd keeps non-front sides visually unpatterned.
                for (DecoratedPot.Side side : DecoratedPot.Side.values()) {
                    result.put(side, brick);
                }
                result.put(DecoratedPot.Side.FRONT, choices.get(FIRST_CHOICE_INDEX));
            }
            case SYMMETRIC -> {
                result.put(DecoratedPot.Side.FRONT, choices.get(FIRST_CHOICE_INDEX));
                result.put(DecoratedPot.Side.RIGHT, choices.get(RIGHT_CHOICE_INDEX));
                result.put(DecoratedPot.Side.BACK, choices.get(BACK_CHOICE_INDEX));
                result.put(DecoratedPot.Side.LEFT, choices.get(RIGHT_CHOICE_INDEX));
            }
            case INDIVIDUAL -> {
                result.put(DecoratedPot.Side.FRONT, choices.get(FIRST_CHOICE_INDEX));
                result.put(DecoratedPot.Side.RIGHT, choices.get(RIGHT_CHOICE_INDEX));
                result.put(DecoratedPot.Side.BACK, choices.get(BACK_CHOICE_INDEX));
                result.put(DecoratedPot.Side.LEFT, choices.get(LEFT_CHOICE_INDEX));
            }
        }
        return result;
    }

    /**
     * Parses a preset from a command argument.
     *
     * @param value raw command argument
     * @return matching preset, or null when the value is unknown
     */
    static PotPreset parse(String value) {
        try {
            return valueOf(value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return switch (value) {
                case UiText.PRESET_SAME_JAPANESE -> SAME;
                case UiText.PRESET_FRONT_JAPANESE -> FRONT;
                case UiText.PRESET_SYMMETRIC_JAPANESE -> SYMMETRIC;
                case UiText.PRESET_INDIVIDUAL_JAPANESE -> INDIVIDUAL;
                default -> null;
            };
        }
    }
}
