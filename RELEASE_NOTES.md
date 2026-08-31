# Release v2.0 (Closed Beta)

Mekanism Optimizer 専用の非同期ログファイル出力機能（`logs/mekanism_optimizer.log`）を搭載した大型メジャーアップデート v2.0 です。

### ✨ 新機能・改善項目 (v2.0)
- **🪵 専用ログファイル生成システム (`logs/mekanism_optimizer.log`)**: 最適化メトリクスや統計ログを非同期に専用ファイルへ記録。Tick スレッドへの負荷ゼロで追跡可能
- **🛡️ Greenhouse（温室）等マシンの GUI 開放の完全動作担保**: 全マシンの GUI が快適に動作
- **🇯🇵 完全日本語コンフィグ＆マウスホバー説明対応 (`ja_jp.json` / `en_us.json`)**: Configured やコンフィグ画面でマウスホバーした際、日本語環境で詳細説明を表示
- **🔒 21億制限突破のデフォルトオフ化**: 安全な初期設定
- **☀️ 太陽光・風力・全発電機群の軽量化 (`TileEntityGeneratorMixin` 系列)**: 夜間スカイライトバイパスおよび満杯時アダプティブ・スリープ
