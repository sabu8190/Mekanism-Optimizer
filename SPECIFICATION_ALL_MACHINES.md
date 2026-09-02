仕様書：Mekanism全通常機械＆アドオンレシピ照会・搬出O(1)最適化
1. 概要・背景
1.1 実測ボトルネック分析 (Spark プロファイル実測値)
Spark プロファイル（spark.lucko.me/AiNHyKEkkA / spark.lucko.me/JbrMjzybP7）において、大規模工場環境での重度の Tick 遅延（MSPT 45ms〜70ms、TPS 12〜16 への低下）が発生している。


■ 主要ボトルネック内訳:


1. レシピ全件走査 (Recipe Lookup O(N) Overhead):
BEGreenHouse（温室）、電動製錬機（Energized Smelter）、濃縮室（Enrichment Chamber）、粉砕機（Crusher）、冶金注入機（Metallurgic Infuser）、加圧反応室（PRC）、化学機械、Astral Mekanism / Extras 等の全機械において、各 Tick ごとにスロット内のアイテム/流体/ガスとレシピ一覧（Ingredient.test、CropSoilRecipe、CachedRecipe）を全件直列走査。
2. ゼロアロケーション違反 (ItemStack.copy() 多発):
スロットの上限チェックやコンパレーターのレッドストーン出力計算において、毎 Tick ItemStack.copy() が大量に生成され、ヒープメモリ圧迫とマイナー GC スパイクを誘発。
3. 自動搬出の冗長走査 (TileComponentEjector.getEjectItemMap):
排出設定された機械が毎 Tick 隣接 6 面のブロックエンティティおよび IItemHandler / IGasHandler キャパビリティを走査し、インベントリ探索・挿入シミュレーションを実行。
1.2 目的とスコープ
本最適化では、Mekanism 本体および全主要アドオンのレシピ照会・搬出処理を根本から刷新し、工場稼働時の機械処理負荷を 80%〜90% 削減、常に 20.0 TPS (MSPT < 25ms) を維持することを目的とする。
2. システムアーキテクチャ・詳細設計
2.1 モジュール1: 全通常機械レシピ照会の O(1) ハッシュキャッシュ化 (FastRecipeLookupCache)
   * 対象機械:
   * バニラ Mekanism: 製錬機、粉砕機、濃縮室、オスミウム圧縮機、冶金注入機、PRC、アイソトープ遠心機、電解分離機、化学酸化・注入・溶解器等
   * アドオン: BEGreenHouse（温室）、Astral Mekanism、Mekanism Extras 全ティア機械
   * キャッシュ構造:
   * Map<Item, CachedRecipe> / Map<Pair<Item, GasStack>, CachedRecipe> / Map<Pair<Fluid, Gas>, CachedRecipe> による多段階層ハッシュマップ。
   * レシピ照会処理（getRecipeType().findFirst(...) 等）を Mixin でインターセプトし、スロット内入力アイテムの Item / ResourceLocation をキーに O(1) で直接キャッシュ照会。
   * キャッシュミス時のみ初回バニラ探索を行い、結果（不成立含むネガティブキャッシュ）を登録。
2.2 モジュール2: スロット上限＆コンパレーター判定のゼロアロケーション化
   * ItemStack.copy() 排除:
   * スロット容量や搬出可否の判定時に行われる一時コピー（slot.getStack().copy()）を、非破壊的軽量チェッカー（isSameItemSameTags(stackA, stackB)）へリプレイス。
   * コンパレーター出力計算（ItemHandlerHelper.calcRedstoneFromInventory）におけるアロケーションをゼロ化し、GC 頻度を大幅低減。
2.3 モジュール3: 自動搬出パイプライン最適化 (TileComponentEjector 探索キャッシュ)
   * 搬出先キャパビリティのキャッシュ化:
   * 隣接ブロックの更新（neighborChanged）時のみ接続先インベントリ（IItemHandler 等）の参照を更新し、毎 Tick の level.getBlockEntity(pos.relative(side)) および getCapability 探索を排除。
   * インベントリ満杯状態（挿入失敗）を検知した場合、次回搬出試行まで数 Tick のバックオフ待機（指数バックオフまたは 10 tick スロットリング）を導入。
2.4 モジュール4: 倍速加速器 (Torcherino 等) 互換性 & 動的レシピ変更検知
   * 倍速動作（Time Acceleration）完全保証:
   * 状態キャッシュは機械インスタンスまたはスレッドセーフな静的キャッシュで保持され、Torcherino や Tick Warp 等で 1 Tick 内に複数回 tick() が呼ばれても状態不整合が発生しない設計。
   * 動的リロード対応:
   * /reload コマンドやデータパック再読み込み（OnDatapackSyncEvent）を検知し、全機械レシピキャッシュを安全に即座に無効化・再構築。
3. 設定仕様 (config/fastlaunch/mekanism_optimizer.json)
{


  "$schema": "https://fastlaunch.mod/schemas/mekanism_optimizer.v1.json",


  "recipeOptimization": {


    "enableO1RecipeCache": true,


    "enableNegativeCache": true,


    "maxCacheSizePerMachine": 2048,


    "optimizeBEGreenHouse": true,


    "optimizeAddonMachines": true


  },


  "ejectionOptimization": {


    "enableCapabilityCaching": true,


    "fullInventoryBackoffTicks": 10


  },


  "memoryOptimization": {


    "eliminateItemStackCopy": true


  },


  "compatibility": {


    "supportTimeAccelerators": true


  }


}
4. 性能目標と検証基準
評価指標
	改善前（Spark 実測値）
	目標値（最適化適用後）
	削減率 / 合否判定
	全体 Tick 時間 (MSPT)
	45.0 ms 〜 70.0 ms
	18.0 ms 以下
	約 60%〜75% 短縮
	BEGreenHouse Tick 時間
	8.5 ms 〜 14.0 ms
	0.8 ms 以下
	90% 以上削減
	通常機械群 レシピ走査
	18.0 ms 〜 26.0 ms
	2.0 ms 以下
	88% 以上削減
	自動搬出 (Ejector) 処理
	7.5 ms 〜 11.0 ms
	1.2 ms 以下
	84% 以上削減
	毎秒メモリ割り当て量
	約 450 MB/s
	90 MB/s 以下
	アロケーション 80% 削減
	レシピ出力整合性
	-
	100% 正常加工
	全機械動作テスト合格
	5. ロールバック・フォールバック計画
   * 万が一レシピ判定に不整合が生じた場合は、対象機械種別のみ config からバニラ探索ロジックへ即時フォールバック可能。