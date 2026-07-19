# KMDecoratedPot

Paper 26.2 向けの、GUIから模様を選んで飾り壺を入手できるプラグインです。

## 必要環境

- Paper 26.2
- Java 25 以上

## ビルド

Gradle で `build` タスクを実行してください。生成物は `build/libs/` に出力されます。

```shell
./gradlew build
```

## 使い方

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
