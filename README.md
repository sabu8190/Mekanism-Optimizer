# ⚡ Mekanism Optimizer v1.3 (Closed Beta)

[![Minecraft](https://img.shields.io/badge/Minecraft-1.20.1-brightgreen.svg)](https://minecraft.net/)
[![Forge](https://img.shields.io/badge/Forge-47.3.0+-orange.svg)](https://files.minecraftforge.net/)
[![Release](https://img.shields.io/badge/Release-v1.3-blue.svg)](https://github.com/sabu8190/Mekanism-Optimizer)

**Mekanism Optimizer** は、大規模工業環境・超巨大ファクトリーにおける Mekanism（および AME / Mekanism Extras 等のアドオン群）の Tick スパイク、GC 負荷、ネットワーク遅延、送電・単位変換オーバーヘッドを抜本的に解消する次世代ハイパフォーマンス最適化 MOD です。

---

## 🚀 主な機能とアーキテクチャ (v1.3)

### 1. ⚡ 送電ネットワーク・セーブ計算の劇的最適化 (`EnergyNetworkMixin`)
- 巨大なケーブル網（数百〜数千本）における `EnergyTransmitterSaveTarget` の再計算を最適化。定常状態（送電量が不変）における毎 Tick の全ケーブルへの再保存・TileEntity マーク処理をスキップ。

### 2. ⚡ 単位変換・HUD 文字列生成の高速キャッシュ (`UnitDisplayUtilsMixin`)
- `UnitDisplayUtils` に軽量な LRU キャッシュを導入。Jade や GUI、ツールチップによる 1 Tick あたり数万回もの文字列生成・結合処理をバイパス。

### 3. 🛠️ AE2 クラフトシミュレーションとの完全整合 (`FastSlotIndexer`)
- インベントリの空きスロット状況をリアルタイム・オンデマンドで高速判定するように改良。AE2 のクラフト発注シミュレーションが誤ってブロックされる問題を解消。

### 4. ☢️ 放射線計算ゼロ負荷ファストパス (`RadiationManagerMixin`)
- 放射線源が存在しないクリーンな環境下において、ワールド内の全エンティティ（プレイヤー・Mob）に対する毎 Tick の放射線レベル探索・オブジェクト生成を定数時間 \(O(1)\) で完全バイパス。

### 5. ⚡ ケーブル送電アダプティブ・バックオフ (`CableUtilsMixin`)
- 周囲の接続先機械や蓄電器が満杯（充電 100%）の際、毎 Tick の不要な送電シミュレーション・Capability ルックアップを適応型に休止。

### 6. ⚡ ゼロアロケーション・ファクトリー爆速均等分配 (`FastFactorySorter`)
- AME / Mekanism Extras の巨大アドバンスドファクトリー（10〜15スロット以上）における Auto-Sort 処理を再構築。ゼロ GC 負荷で最大 **8〜15倍** 高速化。

---

## 📦 動作環境

- **Minecraft**: 1.20.1
- **Forge**: 47.3.0+ (47.4.21 / 47.4.22 推奨)
- **Java**: 17+
- **前提 MOD**: Mekanism 10.4.x
- **完全互換**: Applied Energistics 2 (AE2), Astral Mekanism (AME), Mekanism Extras, Evolved Mekanism, FastLaunch, ModernFix, FerriteCore 等
