# ⚡ Mekanism Optimizer v1.2 (Closed Beta)

[![Minecraft](https://img.shields.io/badge/Minecraft-1.20.1-brightgreen.svg)](https://minecraft.net/)
[![Forge](https://img.shields.io/badge/Forge-47.3.0+-orange.svg)](https://files.minecraftforge.net/)
[![Release](https://img.shields.io/badge/Release-v1.2-blue.svg)](https://github.com/sabu8190/Mekanism-Optimizer)

**Mekanism Optimizer** は、大規模工業環境・超巨大ファクトリーにおける Mekanism（および AME / Mekanism Extras 等のアドオン群）の Tick スパイク、GC 負荷、ネットワーク遅延、過剰なチャンクロード、および放射線・送電オーバーヘッドを抜本的に解消する次世代ハイパフォーマンス最適化 MOD です。

---

## 🚀 主な機能とアーキテクチャ (v1.2)

### 1. ☢️ 放射線計算ゼロ負荷ファストパス (`RadiationManagerMixin`)
- 放射線源が存在しないクリーンな環境下において、ワールド内の全エンティティ（プレイヤー・Mob）に対する毎 Tick の放射線レベル探索・オブジェクト生成を定数時間 \(O(1)\) で完全バイパス。

### 2. ⚡ ケーブル送電アダプティブ・バックオフ (`CableUtilsMixin`)
- 周囲の接続先機械や蓄電器が満杯（充電 100%）の際、毎 Tick の不要な送電シミュレーション・Capability ルックアップを適応型に休止。

### 3. ⚡ ゼロアロケーション・ファクトリー爆速均等分配 (`FastFactorySorter`)
- 発展・精鋭・究極ファクトリーおよび **AME / Mekanism Extras の巨大アドバンスドファクトリー（10〜15スロット以上）** における Auto-Sort 処理を再構築。ゼロ GC 負荷で最大 **8〜15倍** 高速化。

### 4. ⚛️ 巨大マルチブロック適応型 Tick 最適化 (`MultiblockOptimizer`)
- 工業タービン（Turbine）や原子炉（Reactor）、蓄電マトリックスが定常状態にある際の無駄な毎 Tick 流体・エネルギー再計算をインテリジェントにスキップ。

### 5. 🧵 マルチコア・スレッド並列処理エンジン (`ParallelWorkerPool`)
- CPU の論理コア数に応じた最適化スレッドプール（Work-Stealing / `ForkJoinPool`）をバックグラウンドに自動配備（最大 24+ スレッド自動検知）。

### 6. 🛡️ チャンクロード＆ネットワーク防御 (`MekDefender` 統合)
- **所有者連動チャンクロードゲート**: 所有者がオフラインの際、アンカーアップグレードや次元安定化装置の無駄な処理を自動スリープ。
- **パケット集約バッファ**: タイル更新パケットの重複送信を集約し、通信負荷と描画ラグを軽減。

---

## 📦 動作環境

- **Minecraft**: 1.20.1
- **Forge**: 47.3.0+ (47.4.21 / 47.4.22 推奨)
- **Java**: 17+
- **前提 MOD**: Mekanism 10.4.x
- **完全互換アドオン**: Astral Mekanism (AME), Mekanism Extras, Evolved Mekanism, Mekanism More Capacity, FastLaunch, ModernFix, FerriteCore 等

---

## 🛠️ ビルド方法

```bash
git clone https://github.com/sabu8190/Mekanism-Optimizer.git
cd Mekanism-Optimizer
./gradlew build
```

ビルドされた JAR ファイルは `build/libs/mekanism_optimizer-1.20.1-1.2.0.jar` に出力されます。
