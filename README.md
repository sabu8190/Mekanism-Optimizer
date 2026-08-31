# ⚡ Mekanism Optimizer v1.6 (Closed Beta)

[![Minecraft](https://img.shields.io/badge/Minecraft-1.20.1-brightgreen.svg)](https://minecraft.net/)
[![Forge](https://img.shields.io/badge/Forge-47.3.0+-orange.svg)](https://files.minecraftforge.net/)
[![Release](https://img.shields.io/badge/Release-v1.6-blue.svg)](https://github.com/sabu8190/Mekanism-Optimizer)

**Mekanism Optimizer** は、大規模工業環境・超巨大ファクトリー・天体倍速加速環境における Mekanism（および AME / Mekanism Extras / Generators 等のアドオン群）の Tick スパイク、GC 負荷、ブロック探索オーバーヘッドを抜本的に解消する次世代ハイパフォーマンス最適化 MOD です。

---

## 🚀 主な機能とアーキテクチャ (v1.6)

### 1. ⚡ 隣接ブロック探索の完全キャッシュ化 (`AdjacentTargetCache`)
- **解消された負荷**: 256 倍速加速時やバースト搬出時に毎 Tick 何万回も行われていた周囲 6 方向のチャンク探索・ハッシュマップ走査。
- **実装内容**: 各マシンの周囲 6 方向にある接続先 `BlockEntity` をメモリ上で $O(1)$ キャッシュ。ブロックが破壊・変更された時のみ安全に再取得。

### 2. 🚀 自動搬出 21億制限の完全突破 ＆ Tick 単位搬出レート調整 (`TileComponentEjectorMixin`)
- **21.4億制限（Integer.MAX_VALUE）の完全解除**: 1 Tick 内でのマルチバースト搬出エンジンにより、100億〜数兆超の液体・気体・エネルギーを一括搬出。
- **Tick 単位でのレート調整（Config）**:
  - `autoEjectBurstMultiplier` (デフォルト: 64回/tick)
  - `itemEjectTickDelay` (デフォルト: 0 = 毎 Tick 搬出)
  - `itemEjectMaxStacksPerTick` (デフォルト: 64スタック/tick)

### 3. ☀️ 太陽光・風力・発電機群の劇的軽量化 (`TileEntityGeneratorMixin` 系列)
- 夜間スカイライト完全バイパスおよび満杯時アダプティブ・スリープ。

### 4. ⚡ 送電ネットワーク・セーブ計算の最適化 (`EnergyNetworkMixin`)
- 巨大ケーブル網の定常状態における全ケーブル TileEntity 保存処理（44,093 hits）をスキップ。

### 5. ⚡ 単位変換・HUD 文字列生成の高速キャッシュ (`UnitDisplayUtilsMixin`)
- Jade / GUI による毎 Tick 数万回の文字列生成（33,218 hits）をキャッシュ化。

---

## 📦 動作環境

- **Minecraft**: 1.20.1
- **Forge**: 47.3.0+ (47.4.21 / 47.4.22 推奨)
- **Java**: 17+
- **前提 MOD**: Mekanism 10.4.x
