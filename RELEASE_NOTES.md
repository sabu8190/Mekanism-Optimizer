# Release v1.9 (Closed Beta)

温室（Greenhouse）等のアドオンマシンの GUI が開けなくなる問題を完全に修正したホットフィックス v1.9 です。

### 🛠️ 修正・改善項目 (v1.9)
- **🛡️ Greenhouse（温室）等マシンの GUI 開放バグの完全修正**: クライアント側のコンテナ初期化に影響を与えていた過剰なキャッシュ（WorldUtilsMixin / BasicFluidTankMixin）を完全撤廃し、100% 正常な GUI 動作を復元
- **🇯🇵 完全日本語コンフィグ＆マウスホバー説明対応 (`ja_jp.json` / `en_us.json`)**: Configured やコンフィグ画面でマウスホバーした際、日本語環境で詳細説明を表示
- **🔒 21億制限突破のデフォルトオフ化**: 安全な初期設定
- **☀️ 太陽光・風力・全発電機群の軽量化 (`TileEntityGeneratorMixin` 系列)**: 夜間スカイライトバイパスおよび満杯時アダプティブ・スリープ
