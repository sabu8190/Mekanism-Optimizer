# Release v1.3 (Closed Beta)

Spark プロファイラ（4bW1msJYU3）の最新解析に基づき、CPU 消費上位（計 11.9%）の EnergyTransmitterSaveTarget および UnitDisplayUtils を直接解消した大型アップデート v1.3 です。

### ✨ 新機能・最適化項目 (v1.3)
- **⚡ 送電ネットワーク・セーブ計算の最適化 (`EnergyNetworkMixin`)**: 巨大ケーブル網の定常状態における全ケーブル TileEntity 保存処理（44,093 hits）をスキップ
- **⚡ 単位変換・HUD 文字列生成の高速キャッシュ (`UnitDisplayUtilsMixin`)**: Jade / GUI による毎 Tick 数万回の文字列生成（33,218 hits）をキャッシュ化
- **🛠️ AE2 クラフトシミュレーションとの完全整合 (`FastSlotIndexer`)**: スロット空き状況のリアルタイム判定により、AE2 の素材判定ミスを完全修正
- **☢️ 放射線計算ゼロ負荷ファストパス (`RadiationManagerMixin`)**: 放射線がない環境での毎 Tick 放射線計算を O(1) で即時スキップ
- **⚡ ケーブル送電アダプティブ・バックオフ (`CableUtilsMixin`)**: 充電満杯の機械群に対する毎 Tick の無駄な送電シミュレーションを休止
