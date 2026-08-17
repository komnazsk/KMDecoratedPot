package com.github.komnazsk.kmdecoratedpot;

import org.bukkit.entity.Player;

/**
 * Supported UI languages for player-facing plugin text.
 */
enum UiLanguage {
    JAPANESE,
    ENGLISH;

    /**
     * Chooses a supported UI language from the player's locale.
     *
     * @param player player whose locale will be inspected
     * @return Japanese when the locale language is ja; otherwise English
     */
    static UiLanguage of(Player player) {
        return player.locale().getLanguage().equalsIgnoreCase(UiText.JAPANESE_LANGUAGE_CODE) ? JAPANESE : ENGLISH;
    }

    /**
     * Selects one of two localized text values.
     *
     * @param japanese Japanese text
     * @param english English fallback text
     * @return text for this language
     */
    String text(String japanese, String english) {
        return this == JAPANESE ? japanese : english;
    }
}
