# ⚡ Mekanism Optimizer v1.5 (Closed Beta)

[![Minecraft](https://img.shields.io/badge/Minecraft-1.20.1-brightgreen.svg)](https://minecraft.net/)
[![Forge](https://img.shields.io/badge/Forge-47.3.0+-orange.svg)](https://files.minecraftforge.net/)
[![Release](https://img.shields.io/badge/Release-v1.5-blue.svg)](https://github.com/sabu8190/Mekanism-Optimizer)

**Mekanism Optimizer** は、大規模工業環境・超巨大ファクトリーにおける Mekanism（および AME / Mekanism Extras / Generators 等のアドオン群）の Tick スパイク、GC 負荷、送電・自動搬出制限（21億制限/アイテム遅延）を抜本的に解消する次世代ハイパフォーマンス最適化 MOD です。

---

## 🚀 主な機能とアーキテクチャ (v1.5)

### 1. 🚀 自動搬出 21億制限の完全突破 ＆ Tick 単位搬出レート調整 (`TileComponentEjectorMixin`)
- **21.4億制限（Integer.MAX_VALUE）の完全解除**: 1 Tick 内でのマルチバースト搬出エンジンにより、100億〜数兆超の液体・気体・エネルギーを 1 Tick で一括搬出。
- **Tick 単位でのレート調整（コンフィグ自由調整）**:
  - `autoEjectBurstMultiplier` (デフォルト: 64回/tick): 流体・気体・エネルギーの 1 Tick あたり搬出バースト倍率。
  - `itemEjectTickDelay` (デフォルト: 0 = 毎 Tick 搬出): Mekanism 標準の 10 Tick 制限（1秒に2回）を撤廃し、毎 Tick 爆速搬出。
  - `itemEjectMaxStacksPerTick` (デフォルト: 64スタック/tick): 1 Tick 内に最大 64 スタック（4,096 個）のアイテムを一瞬で搬出。

### 2. ☀️ 太陽光・風力・発電機群の劇的軽量化 (`TileEntityGeneratorMixin` 系列)
- **夜間スカイライト完全バイパス**: 太陽光発電機が夜間に無駄なスカイライト探索を行うのを O(1) で即時スキップ。
- **満杯時アダプティブ・スリープ**: 発電機内部が満杯の際、毎 Tick の発電・送電計算を 4 Tick に 1 回へ適応型に休止。

### 3. ⚡ 送電ネットワーク・セーブ計算の最適化 (`EnergyNetworkMixin`)
- 巨大なケーブル網（数百〜数千本）における定常状態の毎 Tick 全ケーブル TileEntity 再保存処理（44,093 hits）をスキップ。

### 4. ⚡ 単位変換・HUD 文字列生成の高速キャッシュ (`UnitDisplayUtilsMixin`)
- `UnitDisplayUtils` に軽量な LRU キャッシュを導入。Jade や GUI、ツールチップによる毎 Tick 数万回の文字列生成をバイパス。

### 5. 🛠️ AE2 クラフトシミュレーションとの完全整合 (`FastSlotIndexer`)
- スロット空き状況のリアルタイム判定により、AE2 の素材判定ミスを完全修正。

---

## 📦 動作環境

- **Minecraft**: 1.20.1
- **Forge**: 47.3.0+ (47.4.21 / 47.4.22 推奨)
- **Java**: 17+
- **前提 MOD**: Mekanism 10.4.x
