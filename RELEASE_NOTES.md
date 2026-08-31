# Release v1.5 (Closed Beta)

自動搬出の 21 億（Integer.MAX_VALUE）制限を突破し、Tick 単位での搬出頻度・バースト倍率（64/tick 等）を自由に調整可能にした大型アップデート v1.5 です。

### ✨ 新機能・最適化項目 (v1.5)
- **🚀 自動搬出 21億制限の突破（マルチバースト搬出）**: 1 Tick 内で複数回の連続 emit を実行し、100億〜数兆超の液体・気体・エネルギーを 1 Tick で一括搬出
- **⚙️ Tick 単位での搬出レート自由調整（Config）**:
  - `autoEjectBurstMultiplier`: 流体・ケミカル・エネルギーのバースト倍率（デフォルト 64/tick）
  - `itemEjectTickDelay`: アイテム搬出インターバル（デフォルト 0 = 毎 Tick 搬出、標準の 10 Tick 制限を解除）
  - `itemEjectMaxStacksPerTick`: 1 Tick 内の最大搬出スタック数（デフォルト 64スタック/tick）
- **☀️ 太陽光・風力・全発電機群の軽量化 (`TileEntityGeneratorMixin` 系列)**: 夜間スカイライトバイパスおよび満杯時アダプティブ・スリープ
- **⚡ 送電ネットワーク・セーブ計算の最適化 (`EnergyNetworkMixin`)**: 巨大ケーブル網の定常状態における全ケーブル TileEntity 保存処理（44,093 hits）をスキップ
- **⚡ 単位変換・HUD 文字列生成の高速キャッシュ (`UnitDisplayUtilsMixin`)**: Jade / GUI による毎 Tick 数万回の文字列生成（33,218 hits）をキャッシュ化
