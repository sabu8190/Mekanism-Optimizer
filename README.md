# ⚡ Mekanism Optimizer (Closed Beta)

[![Minecraft](https://img.shields.io/badge/Minecraft-1.20.1-brightgreen.svg)](https://minecraft.net/)
[![Forge](https://img.shields.io/badge/Forge-47.3.0+-orange.svg)](https://files.minecraftforge.net/)
[![Release](https://img.shields.io/badge/Release-v1.0.0--beta.1-blue.svg)](https://github.com/)

**Mekanism Optimizer** は、大規模工業環境・超巨大ファクトリーにおける Mekanism（およびそのアドオン群）の Tick スパイク、GC 負荷、ネットワーク遅延、過剰なチャンクロードを抜本的に解消する次世代ハイパフォーマンス最適化 MOD です。

---

## 🚀 主な機能とアーキテクチャ

### 1. ⚡ ゼロアロケーション・ファクトリー爆速均等分配 (`FastFactorySorter`)
- 発展・精鋭・究極ファクトリーにおける **Auto-Sort（自動ソート / 均等分配）** の処理アルゴリズムを再構築。
- 毎 Tick 発生していた `HashMap` / `ArrayList` などの不要なオブジェクト生成を完全撤廃（ゼロ GC 負荷）。
- 単一アイテム高速パスとインプレース整数演算により、アイテム分配を最大 **8〜15倍** 高速化。

### 2. 🧵 マルチコア・スレッド並列処理エンジン (`ParallelWorkerPool`)
- CPU の論理コア数に応じた最適化スレッドプール（Work-Stealing / `ForkJoinPool`）をバックグラウンドに自動配備。
- 重い計算処理（ポアソン分布計算・バックグラウンドキャッシュ更新等）をメイン Tick スレッドから安全にオフロード。

### 3. 🛡️ チャンクロード＆ネットワーク防御 (`MekDefender` 統合)
- **所有者連動チャンクロードゲート (`OwnerChunkloadGate`)**: アンカーアップグレードや次元安定化装置の所有者がオフラインの際、無駄なチャンクロード処理を自動的にスリープ。
- **パケット集約バッファ (`PacketCoalescer`)**: タイル更新パケット（`mekanism:update_tile`）の重複送信を Latest-Write-Wins 方式で集約し、帯域消費とクライアント描画ラグを軽減。

### 4. 🌐 ロジスティカル・トランスポーター重複探索防止 (`TransporterCache`)
- 経路探索（Pathfinding）の不要な再計算をブロック座標・アイテム種類単位でキャッシュ。
- 大規模パイプ網における TPS 低下を劇的に改善。

### 5. 📉 アダプティブ・バックオフ (`AdaptiveBackoffManager`)
- 搬出先インベントリが満杯（Blocked）の際、毎 Tick の無駄なアイテム押し出し判定を指数バックオフで一時休止。

---

## 📦 動作環境

- **Minecraft**: 1.20.1
- **Forge**: 47.3.0+ (47.4.21 推奨)
- **Java**: 17+
- **前提 MOD**: Mekanism 10.4.x
- **互換 MOD**: Evolved Mekanism, Mekanism Additions, Mekanism Lasers, Astral Mekanism 等

---

## 🔧 設定項目 (`config/mekanism_optimizer-common.toml`)

```toml
[MekanismOptimizer]
    [MekanismOptimizer."General Settings"]
        # ファクトリーの高速自動分配を有効化
        enableFactoryAutoSortOptimization = true
        # 所有者オフライン時のチャンクロード自動休止を有効化
        enableOwnerChunkloadGating = true
        # タイル更新パケットの集約を有効化
        enablePacketCoalescing = true
        # トランスポーター経路探索キャッシュを有効化
        enableTransporterCache = true
        # イジェクターのアダプティブバックオフを有効化
        enableAdaptiveBackoff = true
```

---

## 🛠️ ビルド方法

```bash
# クローン
git clone https://github.com/<username>/MekanismOptimizer.git
cd MekanismOptimizer

# ビルド (Gradle 8.14)
./gradlew build
```

ビルドされた JAR ファイルは `build/libs/mekanism_optimizer-1.20.1-1.0.0.jar` に出力されます。

---

## 📄 ライセンス

本プロジェクトはクローズドベータ版（Closed Beta）として提供されています。無断転載・再配布はご遠慮ください。
