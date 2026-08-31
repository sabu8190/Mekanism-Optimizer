# ⚡ Mekanism Optimizer v1.7 (Closed Beta)

[![Minecraft](https://img.shields.io/badge/Minecraft-1.20.1-brightgreen.svg)](https://minecraft.net/)
[![Forge](https://img.shields.io/badge/Forge-47.3.0+-orange.svg)](https://files.minecraftforge.net/)
[![Release](https://img.shields.io/badge/Release-v1.7-blue.svg)](https://github.com/sabu8190/Mekanism-Optimizer)

**Mekanism Optimizer** は、大規模工業環境・超巨大ファクトリー・天体倍速加速環境における Mekanism（および AME / Mekanism Extras / Generators 等のアドオン群）の Tick スパイク、GC 負荷、ブロック探索オーバーヘッドを抜本的に解消する次世代ハイパフォーマンス最適化 MOD です。

---

## 🚀 主な機能とアーキテクチャ (v1.7)

### 1. 🌐 完全日本語化コンフィグ＆ツールチップ対応
- ゲーム内コンフィグ画面（Configured 等）において、言語が日本語の際に全項目の分かりやすい日本語名・詳細な説明ツールチップを表示。
- `enableUnlimitedAutoEject` をデフォルトで **`false`（オフ）** に設定。

### 2. ⚡ 隣接ブロック探索の完全 O(1) キャッシュ化 (`AdjacentTargetCache` ＆ `WorldUtilsMixin`)
- 256 倍速加速時やバースト搬出時に、周囲 6 方向のブロック探索・ハッシュマップ検索をキャッシュ化し、直参照で即時リターン。

### 3. 🚀 自動搬出 21億制限突破 ＆ Tick 単位搬出レート調整 (Config で自由有効化)
- `enableUnlimitedAutoEject` を有効化することで、1 Tick 内で 100億〜数兆超の液体・気体・エネルギーを一括搬出可能。
- `autoEjectBurstMultiplier` (64/tick), `itemEjectTickDelay` (0), `itemEjectMaxStacksPerTick` (64) を自由に設定可能。

### 4. ☀️ 太陽光・風力・全発電機群の劇的軽量化 (`TileEntityGeneratorMixin` 系列)
- 夜間スカイライト完全バイパスおよび満杯時アダプティブ・スリープ。

---

## 📦 動作環境

- **Minecraft**: 1.20.1
- **Forge**: 47.3.0+ (47.4.21 / 47.4.22 推奨)
- **Java**: 17+
- **前提 MOD**: Mekanism 10.4.x
