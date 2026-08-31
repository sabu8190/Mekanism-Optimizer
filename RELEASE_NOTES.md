# Release v1.4 (Closed Beta)

Spark プロファイラ解析で特定された、BlockEntity Tick の主たる負荷（約 16% 以上）を占めていた Mekanism Generators（風力・太陽光・発電機群）を極限まで軽量化した大型アップデート v1.4 です。

### ✨ 新機能・最適化項目 (v1.4)
- **☀️ 太陽光発電機の夜間・スカイライト即時バイパス (`TileEntitySolarGeneratorMixin`)**: 夜間に発生する毎 Tick の空の探索・レイキャスト計算を O(1) で完全スキップ
- **💨 風力・全発電機の満杯時アダプティブ・スリープ (`TileEntityWindGeneratorMixin` / `TileEntityGeneratorMixin`)**: 内部ストレージ満杯時の無駄な毎 Tick 発電計算を休止
- **⚡ 送電ネットワーク・セーブ計算の最適化 (`EnergyNetworkMixin`)**: 巨大ケーブル網の定常状態における全ケーブル TileEntity 保存処理（44,093 hits）をスキップ
- **⚡ 単位変換・HUD 文字列生成の高速キャッシュ (`UnitDisplayUtilsMixin`)**: Jade / GUI による毎 Tick 数万回の文字列生成（33,218 hits）をキャッシュ化
- **🛠️ AE2 クラフトシミュレーションとの完全整合 (`FastSlotIndexer`)**: スロット空き状況のリアルタイム判定により、AE2 の素材判定ミスを完全修正
