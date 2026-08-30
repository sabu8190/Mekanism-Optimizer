# ⚡ Mekanism Optimizer v1.1 (Closed Beta)

[![Minecraft](https://img.shields.io/badge/Minecraft-1.20.1-brightgreen.svg)](https://minecraft.net/)
[![Forge](https://img.shields.io/badge/Forge-47.3.0+-orange.svg)](https://files.minecraftforge.net/)
[![Release](https://img.shields.io/badge/Release-v1.1-blue.svg)](https://github.com/sabu8190/Mekanism-Optimizer)

**Mekanism Optimizer** は、大規模工業環境・超巨大ファクトリーにおける Mekanism（および AME / Mekanism Extras 等のアドオン群）の Tick スパイク、GC 負荷、ネットワーク遅延、過剰なチャンクロードを抜本的に解消する次世代ハイパフォーマンス最適化 MOD です。

---

## 🚀 主な機能とアーキテクチャ (v1.1)

### 1. ⚡ ゼロアロケーション・ファクトリー爆速均等分配 (`FastFactorySorter`)
- 発展・精鋭・究極ファクトリーおよび **AME / Mekanism Extras の巨大アドバンスドファクトリー（10〜15スロット以上）** における Auto-Sort 処理を再構築。
- 毎 Tick 発生していた `HashMap` / `ArrayList` などの不要なオブジェクト生成を完全撤廃（ゼロ GC 負荷）。
- 単一アイテム高速パスとインプレース整数演算により、アイテム分配を最大 **8〜15倍** 高速化。

### 2. 🧵 マルチコア・スレッド並列処理エンジン (`ParallelWorkerPool`)
- CPU の論理コア数に応じた最適化スレッドプール（Work-Stealing / `ForkJoinPool`）をバックグラウンドに自動配備（最大 24+ スレッド自動検知）。
- 重い計算処理（ポアソン分布計算・バックグラウンドキャッシュ更新等）をメイン Tick スレッドから安全にオフロード。

### 3. ⚛️ 巨大マルチブロック適応型 Tick 最適化 (`MultiblockOptimizer`)
- 工業タービン（Turbine）や原子炉（Reactor）が定常状態（満杯または空）にある際の無駄な毎 Tick 流体・エネルギー再計算をインテリジェントにスキップ。

### 4. 🛡️ チャンクロード＆ネットワーク防御 (`MekDefender` 統合)
- **所有者連動チャンクロードゲート (`OwnerChunkloadGate`)**: アンカーアップグレードや次元安定化装置の所有者がオフラインの際、無駄なチャンクロード処理を自動的にスリープ。
- **パケット集約バッファ (`PacketCoalescer`)**: タイル更新パケット（`mekanism:update_tile`）の重複送信を集約し、帯域消費と描画ラグを軽減。

### 5. 🌐 超高速パイプ対応トランスポーター経路探索キャッシュ (`TransporterCache`)
- AME / Extras の超高速パイプ（8x〜16x）環境下における経路探索（Pathfinding）の重複計算をキャッシュ。

### 6. 📉 アダプティブ・バックオフ (`AdaptiveBackoffManager`)
- 搬出先インベントリが満杯（Blocked）の際、毎 Tick の無駄なアイテム押し出し判定を指数バックオフで一時休止。

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

ビルドされた JAR ファイルは `build/libs/mekanism_optimizer-1.20.1-1.1.0.jar` に出力されます。
