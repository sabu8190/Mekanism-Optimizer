# ⚡ Mekanism Optimizer v1.4 (Closed Beta)

[![Minecraft](https://img.shields.io/badge/Minecraft-1.20.1-brightgreen.svg)](https://minecraft.net/)
[![Forge](https://img.shields.io/badge/Forge-47.3.0+-orange.svg)](https://files.minecraftforge.net/)
[![Release](https://img.shields.io/badge/Release-v1.4-blue.svg)](https://github.com/sabu8190/Mekanism-Optimizer)

**Mekanism Optimizer** は、大規模工業環境・超巨大ファクトリーにおける Mekanism（および AME / Mekanism Extras / Generators 等のアドオン群）の Tick スパイク、GC 負荷、送電・発電機群（風力/太陽光/ガス）オーバーヘッドを抜本的に解消する次世代ハイパフォーマンス最適化 MOD です。

---

## 🚀 主な機能とアーキテクチャ (v1.4)

### 1. ☀️ 太陽光・風力・発電機群の劇的軽量化 (`TileEntityGeneratorMixin` 系列)
- **夜間スカイライト完全バイパス**: 太陽光発電機が夜間に無駄なスカイライト探索（Y=320 までのブロック走査）を行うのを O(1) で即時スキップ。
- **満杯時アダプティブ・スリープ**: 発電機内部が満杯（充電 100%）かつ送電先が詰まっている際、毎 Tick の発電・送電計算を 4 Tick に 1 回へ適応型に休止。
- **ゼロアロケーション送電方向管理**: 発電機から周囲への送電時に発生していた毎 Tick の `EnumSet` 生成を排除。

### 2. ⚡ 送電ネットワーク・セーブ計算の最適化 (`EnergyNetworkMixin`)
- 巨大なケーブル網（数百〜数千本）における `EnergyTransmitterSaveTarget` の再計算を最適化。定常状態における毎 Tick の全ケーブルへの再保存・TileEntity マーク処理をスキップ。

### 3. ⚡ 単位変換・HUD 文字列生成の高速キャッシュ (`UnitDisplayUtilsMixin`)
- `UnitDisplayUtils` に軽量な LRU キャッシュを導入。Jade や GUI、ツールチップによる 1 Tick あたり数万回もの文字列生成・結合処理をバイパス。

### 4. 🛠️ AE2 クラフトシミュレーションとの完全整合 (`FastSlotIndexer`)
- インベントリの空きスロット状況をリアルタイム・オンデマンドで高速判定するように改良。AE2 のクラフト発注シミュレーションが誤ってブロックされる問題を完全修正。

### 5. ☢️ 放射線計算ゼロ負荷ファストパス (`RadiationManagerMixin`)
- 放射線源が存在しないクリーンな環境下において、ワールド内の全エンティティに対する毎 Tick の放射線計算を定数時間 \(O(1)\) で完全バイパス。

### 6. ⚡ ゼロアロケーション・ファクトリー爆速均等分配 (`FastFactorySorter`)
- AME / Mekanism Extras の巨大アドバンスドファクトリー（10〜15スロット以上）における Auto-Sort 処理を再構築。ゼロ GC 負荷で最大 **8〜15倍** 高速化。

---

## 📦 動作環境

- **Minecraft**: 1.20.1
- **Forge**: 47.3.0+ (47.4.21 / 47.4.22 推奨)
- **Java**: 17+
- **前提 MOD**: Mekanism 10.4.x
- **完全互換**: Mekanism Generators, Applied Energistics 2 (AE2), Astral Mekanism (AME), Mekanism Extras, Evolved Mekanism, FastLaunch, ModernFix, FerriteCore 等
