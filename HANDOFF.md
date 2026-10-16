# Project Handoff State

## 1. 現在の到達点（Current Status）
- **完了した機能・タスク：**
  - 万能ファクトリー自動分割エンジン（`UniversalFactoryAutoSorter`）の実装：Mekanism 本家、More Machine、EMExtras、および Astral Mekanism（通常・高Tier）の全入力スロット間における完全均等アイテム分配（ゼロアロケーションバランシング）。
  - 高信頼丸石生成供給エンジン（`CobblestoneSupplyEngine`）の実装：Astral Mekanism の丸石生成アップグレード（`astral_mekanism:cobblestone_supply_upgrade`）装着時、ワールド再開時の再計算漏れに関係なくリアルタイムに装着数を検出し、内部自動化（`AutomationType.INTERNAL`）で機械入力スロットへ直接供給。
  - ファクトリー向け Mixin 群の整備：`TileEntityFactoryMixin`（本家・アドオンファクトリー用）、`AMEProgressFactoryMixin`（Astral 通常ファクトリー用）、`AMERecipeFactoryMixin`（Astral 高Tierファクトリー用）、`TileEntityMekanismMixin`（丸石供給 tick）。
  - 不具合要因となっていた旧スロット Mixin（`EMExtraMoreMachineFactoryInputSlotMixin`, `EMExtraMoreMachineFactoryOutputSlotMixin`）の完全削除。
  - Mekanism Optimizer v1.3.4 のビルド完了（`mekanism_optimizer-1.20.1-1.3.4.jar`）および EWF（`The Eternal World of Fantasy`）・MAI（`Mystical Astral Infinity`）両環境の `mods` フォルダへのデプロイ完了。
  - Git コミット完了（`main` ブランチ、working tree clean）。
- **ビルド・テスト状態：**
  - `gradlew.bat build`：正常終了（BUILD SUCCESSFUL, exit code 0）。
  - JAR 実体：`build/libs/mekanism_optimizer-1.20.1-1.3.4.jar`（838,157 bytes）生成済・デプロイ済。

## 2. 確定したアーキテクチャ・変更ファイルマップ（Modified & Key Files）
- `src/main/java/com/mekanismoptimizer/core/UniversalFactoryAutoSorter.java` : 全ファクトリー共通のゼロアロケーション自動分割エンジン。主要関数: `sortMekanismFactory(TileEntityFactory<?>)`, `sortAMEFactory(Object)`, `balanceSlots(List<IInventorySlot>)`。
- `src/main/java/com/mekanismoptimizer/core/CobblestoneSupplyEngine.java` : 丸石生成アップグレード供給エンジン。主要関数: `tick(TileEntityMekanism)`, `produceAndSupply(TileEntityMekanism, int)`。
- `src/main/java/com/mekanismoptimizer/mixin/TileEntityFactoryMixin.java` : Mekanism 本家およびアドオンファクトリーの `sortInventory` をインターセプトし、`UniversalFactoryAutoSorter` を実行。
- `src/main/java/com/mekanismoptimizer/mixin/TileEntityMekanismMixin.java` : `TileEntityMekanism.onUpdateServer` の HEAD で `CobblestoneSupplyEngine.tick` を呼び出し。
- `src/main/java/com/mekanismoptimizer/mixin/addons/AMEProgressFactoryMixin.java` : Astral Mekanism の `BEEnergizedSmeltingFactory.sort()` の欠陥ロジックをバイパスし `UniversalFactoryAutoSorter` を実行。
- `src/main/java/com/mekanismoptimizer/mixin/addons/AMERecipeFactoryMixin.java` : Astral Mekanism の `BlockEntityRecipeFactory.onUpdateServer` にて高Tierファクトリーへの定期的な自動分割を実行。
- `src/main/resources/mekanism_optimizer.mixins.json` : 上記 Mixin の登録設定。

## 3. コアの型定義・インターフェース契約（I/O Contracts）
```java
// UniversalFactoryAutoSorter
public final class UniversalFactoryAutoSorter {
    public static boolean sortMekanismFactory(TileEntityFactory<?> factory);
    public static boolean sortAMEFactory(Object ameFactory);
    public static boolean balanceSlots(List<IInventorySlot> slots);
    public static List<IInventorySlot> getAMEInputSlots(Object factory);
}

// CobblestoneSupplyEngine
public final class CobblestoneSupplyEngine {
    public static void tick(TileEntityMekanism tile);
}

// Mixin Injection Points
// TileEntityFactory: sortInventory() -> Cancellable Inject
// TileEntityMekanism: onUpdateServer() -> Non-cancellable Inject
// BEEnergizedSmeltingFactory: sort() -> Cancellable Inject
// BlockEntityRecipeFactory: onUpdateServer() -> Non-cancellable Inject
```

## 4. 残課題・未解決事項（Remaining Tasks & Known Issues）
- **ユーザー実機テスト待ち**:
  - 外出中のユーザーによる実機（EWF / MAI）でのファクトリー自動分割挙動および丸石生成アップグレード動作確認待ち。
  - 実機確認で問題がなければ、CurseForge / GitHub リリース（v1.3.4）の打診または実行へ進む。

## 5. 次のセッションで最初に実行すべきコマンド（Next Action Commands）
```powershell
# Git 状態の確認
git status

# デプロイ済み JAR の存在確認
python -c "import os; print('EWF:', os.path.exists(r'D:\Minecraft\GDLauncher\instances\The Eternal World of Fantasy-Tale of the End--0.b5.2.zip\instance\mods\mekanism_optimizer-1.20.1-1.3.4.jar')); print('MAI:', os.path.exists(r'D:\Minecraft\GDLauncher\instances\Mystical Astral Infinity0.3.2hotfix.zip\instance\mods\mekanism_optimizer-1.20.1-1.3.4.jar'))"
# ※ 実行時はスクラッチスクリプトを作成して実行すること
```
