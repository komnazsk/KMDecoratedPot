# KMDecoratedPot

KMDecoratedPot は Paper 26.2 向けのプラグインです。プレイヤーは GUI から壺の欠片の模様を選び、好みの飾り壺を作成できます。

## 日本語

### 必要環境

- Paper 26.2
- Java 25 以上

Spigot での動作は保証していません。

### インストール

1. GitHub の Releases から KMDecoratedPot プラグインの `.jar` ファイルをダウンロードします。
2. `.jar` ファイルをサーバーの `plugins` ディレクトリに配置します。
3. サーバーを再起動します。

### 使い方

`/pot` を実行すると、プリセットと柄を順番に選択するGUIが開きます。
アイテムのレンガ（`minecraft:brick`）を手に持って右クリックしても、同じGUIが開きます。レンガは消費されません。

- `same`: 全面同じ（柄1個）
- `front`: 正面のみ。残りは無地（柄1個）
- `symmetric`: 正面、左右共通、背面（柄3個）
- `individual`: 正面、右、背面、左（柄4個）

引数だけで直接作成することも、途中まで指定して残りをGUIで選ぶこともできます。

```text
/pot same flow
/pot front brick
/pot symmetric flow arms_up brick
/pot individual flow arms_up brick heart
```

権限ノードは `kmdecoratedpot.command.pot`（デフォルトで全員に許可）です。

## English

KMDecoratedPot is a Paper 26.2 plugin for getting decorated pots by choosing patterns from a GUI.

### Requirements

- Paper 26.2
- Java 25 or later

Spigot is not supported.

### Installation

1. Download the KMDecoratedPot plugin `.jar` file from GitHub Releases.
2. Place the `.jar` file in your server's `plugins` directory.
3. Restart your server.

### Usage

Run `/pot` to open a GUI where you choose a preset and then select the required patterns in order.
You can also right-click while holding a brick (`minecraft:brick`) to open the same GUI. The brick is not consumed.

- `same`: Same pattern on every side (1 pattern)
- `front`: Front side only, with the other sides left blank (1 pattern)
- `symmetric`: Front, shared left/right sides, and back (3 patterns)
- `individual`: Front, right, back, and left sides individually (4 patterns)

You can create a pot directly with command arguments, or provide only part of the selection and choose the rest in the GUI.

```text
/pot same flow
/pot front brick
/pot symmetric flow arms_up brick
/pot individual flow arms_up brick heart
```

Permission node: `kmdecoratedpot.command.pot` (allowed for everyone by default).
