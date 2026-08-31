# Release v1.6 (Closed Beta)

ユーザー様のご提案に基づき、搬出時の周囲ブロック探索（チャンク走査・ハッシュマップ検索）を完全 O(1) キャッシュ化し、256 倍速加速環境でも TPS 20 を維持可能にした大型アップデート v1.6 です。

### ✨ 新機能・最適化項目 (v1.6)
- **⚡ 隣接ブロック探索の完全 O(1) キャッシュ化 (`AdjacentTargetCache`)**: 毎 Tick 何万回も行われていた周囲 6 方向のブロック探索をキャッシュ化し、直参照で即時リターン
- **🚀 自動搬出 21億制限の突破（マルチバースト搬出）**: 1 Tick 内で 100億〜数兆超の液体・気体・エネルギーを一括搬出
- **⚙️ Tick 単位での搬出レート自由調整（Config）**: `autoEjectBurstMultiplier` (64/tick), `itemEjectTickDelay` (0), `itemEjectMaxStacksPerTick` (64)
- **☀️ 太陽光・風力・全発電機群の軽量化 (`TileEntityGeneratorMixin` 系列)**: 夜間スカイライトバイパスおよび満杯時アダプティブ・スリープ
