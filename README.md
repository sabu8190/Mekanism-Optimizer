# ⚡ Mekanism Optimizer v1.8 (Closed Beta)

[![Minecraft](https://img.shields.io/badge/Minecraft-1.20.1-brightgreen.svg)](https://minecraft.net/)
[![Forge](https://img.shields.io/badge/Forge-47.3.0+-orange.svg)](https://files.minecraftforge.net/)
[![Release](https://img.shields.io/badge/Release-v1.8-blue.svg)](https://github.com/sabu8190/Mekanism-Optimizer)

**Mekanism Optimizer** は、大規模工業環境・超巨大ファクトリー・天体倍速加速環境における Mekanism（および AME / Mekanism Extras / Generators 等のアドオン群）の Tick スパイク、GC 負荷、流体レシピバリデーション・スロット探索オーバーヘッドを抜本的に解消する次世代ハイパフォーマンス最適化 MOD です。

---

## 🚀 主な機能とアーキテクチャ (v1.8)

### 1. 💧 流体タンク挿入バリデーションの高速キャッシュ化 (`BasicFluidTankMixin`)
- **解消された負荷**: AME（Astral Mekanism）の温室やファクトリーにおいて、液体挿入（`insert`）時に毎 Tick 走っていた Java Stream による全レシピ探索（53.58% の負荷）。
- **実装内容**: 流体（Fluid）ごとのバリデーション結果を $O(1)$ キャッシュ化。重い Stream 探索を完全バイパス。

### 2. ⚡ 流体スロット空チェック即時バイパス (`IFluidHandlerSlotMixin`)
- **解消された負荷**: スロットが空にもかかわらず毎 Tick 行われていた `fillTank` / `drainTank` の Capability 照会処理（55.39% の負荷）。
- **実装内容**: スロットが空の場合、Capability 探索を $O(1)$ で即座にスキップ。

### 3. 🌐 完全日本語化コンフィグ＆ツールチップ対応
- ゲーム内コンフィグ画面で全項目の分かりやすい日本語説明を表示。

### 4. ⚡ 隣接ブロック探索の完全 O(1) キャッシュ化 (`AdjacentTargetCache` ＆ `WorldUtilsMixin`)
- 256 倍速加速時やバースト搬出時に、周囲 6 方向のブロック探索・ハッシュマップ検索をキャッシュ化し、直参照で即時リターン。

---

## 📦 動作環境

- **Minecraft**: 1.20.1
- **Forge**: 47.3.0+ (47.4.21 / 47.4.22 推奨)
- **Java**: 17+
- **前提 MOD**: Mekanism 10.4.x
