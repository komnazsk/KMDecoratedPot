package com.github.komnazsk.kmdecoratedpot;

/**
 * Centralized constants for command names, permissions, material suffixes, and localized UI text.
 */
final class UiText {
    static final String JAPANESE_LANGUAGE_CODE = "ja";
    static final String COMMAND_NAME = "pot";
    static final String COMMAND_PERMISSION = "kmdecoratedpot.command.pot";
    static final String POTTERY_SHERD_SUFFIX = "_POTTERY_SHERD";
    static final String COMMAND_POTTERY_SHERD_SUFFIX = "_pottery_sherd";

    static final String PRESET_SAME_COMMAND = "same";
    static final String PRESET_FRONT_COMMAND = "front";
    static final String PRESET_SYMMETRIC_COMMAND = "symmetric";
    static final String PRESET_INDIVIDUAL_COMMAND = "individual";

    static final String PRESET_SAME_JAPANESE = "全面同じ";
    static final String PRESET_FRONT_JAPANESE = "正面のみ";
    static final String PRESET_SYMMETRIC_JAPANESE = "左右対称";
    static final String PRESET_INDIVIDUAL_JAPANESE = "4面個別";

    static final LocalizedText COMMAND_PLAYER_ONLY =
            new LocalizedText("このコマンドはプレイヤーのみ使用できます。", "Only players can use this command.");
    static final LocalizedText UNKNOWN_PRESET =
            new LocalizedText("不明なプリセットです: ", "Unknown preset: ");
    static final LocalizedText TOO_MANY_PATTERNS =
            new LocalizedText("柄の指定が多すぎます。必要数: ", "Too many patterns. Required: ");
    static final LocalizedText UNKNOWN_PATTERN =
            new LocalizedText("不明な壺の柄です: ", "Unknown pot pattern: ");
    static final LocalizedText PRESET_MENU_TITLE =
            new LocalizedText("飾り壺のプリセット", "Decorated pot preset");
    static final LocalizedText PATTERNS_REQUIRED =
            new LocalizedText("必要な柄: ", "Patterns required: ");
    static final LocalizedText PATTERN_MENU_TITLE_SUFFIX =
            new LocalizedText("の柄を選択", "Select pattern: ");
    static final LocalizedText GO_BACK =
            new LocalizedText("ひとつ前に戻る", "Go back");
    static final LocalizedText PREVIOUS_PAGE =
            new LocalizedText("前のページ", "Previous page");
    static final LocalizedText NEXT_PAGE =
            new LocalizedText("次のページ", "Next page");
    static final LocalizedText CANCEL =
            new LocalizedText("キャンセル", "Cancel");
    static final LocalizedText CUSTOMIZED_POT =
            new LocalizedText("カスタマイズした飾り壺", "Customized decorated pot");
    static final LocalizedText POT_ADDED =
            new LocalizedText("飾り壺をインベントリに追加しました。",
                    "The decorated pot was added to your inventory.");
    static final LocalizedText POT_DROPPED =
            new LocalizedText("インベントリが満杯のため、飾り壺を足元に落としました。",
                    "Your inventory was full, so the decorated pot was dropped at your feet.");

    static final LocalizedText PRESET_SAME =
            new LocalizedText(PRESET_SAME_JAPANESE, "Same on all sides");
    static final LocalizedText PRESET_FRONT =
            new LocalizedText(PRESET_FRONT_JAPANESE, "Front only");
    static final LocalizedText PRESET_SYMMETRIC =
            new LocalizedText(PRESET_SYMMETRIC_JAPANESE, "Left-right symmetry");
    static final LocalizedText PRESET_INDIVIDUAL =
            new LocalizedText(PRESET_INDIVIDUAL_JAPANESE, "Four individual sides");

    static final LocalizedText SIDE_ALL =
            new LocalizedText("全ての面", "All sides");
    static final LocalizedText SIDE_FRONT =
            new LocalizedText("正面", "Front");
    static final LocalizedText SIDE_RIGHT_AND_LEFT =
            new LocalizedText("右・左側面", "Right and left sides");
    static final LocalizedText SIDE_RIGHT =
            new LocalizedText("右側面", "Right side");
    static final LocalizedText SIDE_BACK =
            new LocalizedText("背面", "Back");
    static final LocalizedText SIDE_LEFT =
            new LocalizedText("左側面", "Left side");

    static final String MISSING_COMMAND_ERROR = "pot command is missing from plugin.yml";
    static final String INVALID_POT_STATE_ERROR = "Decorated pot item did not provide DecoratedPot block state";
    static final String MENU_HOLDER_UNSUPPORTED_ERROR = "This holder only identifies KMDecoratedPot menus";

    /**
     * Prevents instantiation of this constants class.
     */
    private UiText() {
    }

    /**
     * Immutable pair of Japanese and English text values.
     */
    static final class LocalizedText {
        private final String japanese;
        private final String english;

        /**
         * Creates a pair of localized text values.
         *
         * @param japanese Japanese text
         * @param english English text
         */
        private LocalizedText(String japanese, String english) {
            this.japanese = japanese;
            this.english = english;
        }

        /**
         * Returns the text matching the requested UI language.
         *
         * @param language UI language
         * @return localized text
         */
        String text(UiLanguage language) {
            return language.text(japanese, english);
        }
    }
}
