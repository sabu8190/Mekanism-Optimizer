# Release v1.8 (Closed Beta)

Spark 解析で特定された流体系のボトルネック（AME の液体挿入時における Java Stream 全レシピ探索 53.58% および空スロット Capability 照会 55.39%）を直接解消した大型アップデート v1.8 です。

### ✨ 新機能・改善項目 (v1.8)
- **💧 流体タンク挿入バリデーションの O(1) キャッシュ化 (`BasicFluidTankMixin`)**: AME（Astral Mekanism）等の温室・ファクトリーで発生していた毎回の Java Stream 全レシピ走査（53.58%）をキャッシュ化し即時判定
- **⚡ 流体スロット空チェック即時バイパス (`IFluidHandlerSlotMixin`)**: 空のアイテムスロットに対する毎 Tick の無駄な `fillTank` Capability 探索（55.39%）をスキップ
- **🇯🇵 完全日本語コンフィグ＆マウスホバー説明対応 (`ja_jp.json` / `en_us.json`)**: Configured やコンフィグ画面でマウスホバーした際、日本語環境で詳細説明を表示
- **🔒 21億制限突破のデフォルトオフ化**: 安全な初期設定
- **⚡ 隣接ブロック探索の完全 O(1) キャッシュ化 (`AdjacentTargetCache` ＆ `WorldUtilsMixin`)**: 256倍速加速時の探索コストをゼロ化
