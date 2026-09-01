# ⚡ Mekanism Optimizer

![CurseForge Banner](https://raw.githubusercontent.com/sabu8190/Mekanism-Optimizer/main/icon.png)

**Mekanism Optimizer** is a next-generation, high-performance optimization mod engineered specifically for **Mekanism** and its rich addon ecosystem (Astral Mekanism, Mekanism Extras, Mekanism Generators, etc.) on Minecraft 1.20.1 Forge.

It drastically reduces server tick duration, eliminates GC memory pressure, and removes bottlenecks caused by logistical transporters, universal cables, multiblock calculations, and high-frequency ticking environments.

---

## 🚀 Key Features (English)

### ⚡ 1. Logistical Transporter & Pipe Caching ($O(1)$ Lookups)
- Eliminates heavy acceptor capability checks and repetitive pathfinding lookups per tick.
- Reduces item and fluid pipe tick lag by up to **80% - 90%** in dense mega-factories.

### ⚡ 2. Universal Cable Adaptive Backoff
- Intelligently pauses redundant energy transfer calculations when destination machines or energy cubes are fully charged.

### ☀️ 3. Generator & Solar Activator Skylight Bypass
- Bypasses expensive world raycasting and skylight checks for Solar Generators and Solar Neutron Activators during nighttime or when buffers are full.

### 🚀 4. Bypass 2.14B Auto-Eject Limit & Multi-Burst Mode
- Optional config to bypass Forge's `Integer.MAX_VALUE` (2.14 billion) throughput bottleneck per tick.
- Seamlessly transfers trillions of RF/FE, mB of fluids/chemicals, and tens of thousands of items in a single tick (Default: safely `false`).

### 🪵 5. Dedicated Async File Logger
- Automatically logs real-time performance statistics, optimization metrics, and health reports to `.minecraft/logs/mekanism_optimizer.log` with **zero tick overhead** (non-blocking async writer).

### 🌐 6. Full Localization & In-Game Config
- Includes complete English and Japanese (`ja_jp`) tooltips for in-game configuration via **Configured** or `mekanism_optimizer-common.toml`.

---

## 💡 Inspirations & References (参考・リスペクト)

Mekanism Optimizer was inspired by the architectural excellence and optimization paradigms of leading performance mods in the Minecraft ecosystem:

- **[ModernFix](https://www.curseforge.com/minecraft/mc-mods/modernfix)** by embeddedt: Outstanding design for bytecode injection, lazy capability resolution, and structural memory optimizations.
- **[FerriteCore](https://www.curseforge.com/minecraft/mc-mods/ferritecore)** by malte0811: Memory allocation reduction techniques and fast data caching structures.
- **[Lithium](https://www.curseforge.com/minecraft/mc-mods/lithium) / [Canary](https://www.curseforge.com/minecraft/mc-mods/canary)** by CaffeineMC & AbdElAziz308: World ticking, block entity throttling, and $O(1)$ spatial tracking principles.
- **[Mekanism](https://www.curseforge.com/minecraft/mc-mods/mekanism)** by aidancbrady & the Mekanism Team: Deepest respect to the core team for creating one of the most incredible technological mod experiences in Minecraft history.

---

## 📦 Requirements & Compatibility

- **Minecraft**: `1.20.1`
- **Mod Loader**: `Forge 47.3.0+` (`47.4.21`+ recommended)
- **Java**: `17+`
- **Required Dependency**: [Mekanism](https://www.curseforge.com/minecraft/mc-mods/mekanism) (`10.4.x`)
- **Addon Compatibility**: Fully compatible with *Astral Mekanism (AME)*, *Mekanism Extras*, *Mekanism Generators*, *Mekanism Tools*, and major storage mods (*Applied Energistics 2*, *Refined Storage*).
- **Client / Server**: Can be installed on both Client and Dedicated Server (Server-only installation also works).

---

## 🛠️ Configuration

Configure settings in-game via **Configured** or edit `.minecraft/config/mekanism_optimizer-common.toml`:

```toml
[general]
# Output optimization statistics to logs/mekanism_optimizer.log
enableDedicatedLogFile = true

# Bypass 2.14B per-tick eject limits (Default: false)
enableUnlimitedAutoEject = false
maxItemBurstPerTick = 64
```

---

<br>

# 🇯🇵 日本語版説明 (Japanese Description)

**Mekanism Optimizer** は、Minecraft 1.20.1 Forge 環境における **Mekanism** およびそのアドオン群（Astral Mekanism, Mekanism Extras, Mekanism Generators 等）専用の次世代ハイパフォーマンス最適化 MOD です。

大規模工業ファクトリー、複雑に入り組んだパイプライン、天体倍速・加速環境において発生する Tick スパイク、GC（メモリ解放）負荷、および送電・パイプ探索のオーバーヘッドを抜本的に解消します。

---

## 🚀 主な機能と特徴 (日本語)

### ⚡ 1. 物流パイプ＆トランスポーター探索の劇的軽量化 ($O(1)$ キャッシュ)
- 毎 Tick 行われていた搬入先マシンの Capability 照会や経路探索を $O(1)$ で高速キャッシュ化。
- パイプやトランスポーターが密集した超巨大工場における Tick 負荷を **最大 80% 〜 90% 削減** します。

### ⚡ 2. 送電ケーブル適応型バックオフ
- 接続先マシンや蓄電器が満充電の場合、無駄な送電シミュレーション計算をインテリジェントに休止してサーバー負荷を抑えます。

### ☀️ 3. 発電機＆太陽光活性化装置のスカイライト計算バイパス
- 太陽光発電機やソーラー中性子活性化装置において、夜間やバッファ満杯時に負荷の高い直射日光判定（レイキャスト）を $O(1)$ で完全バイパスします。

### 🚀 4. 21億（2.14B）搬出制限突破＆マルチバースト自動搬出
- Forge 標準の `Integer.MAX_VALUE`（21.4 億）による 1 Tick あたりの搬出上限を突破するオプションを搭載。
- 1 Tick 内で数十億〜数兆超のエネルギー・液体・気体、および数万個のアイテムを一括搬出可能です（初期値: 安全な OFF）。

### 🪵 5. 専用非同期ログシステム (`logs/mekanism_optimizer.log`)
- 他 MOD の大量のログに埋もれることなく、Mekanism Optimizer 単独の最適化統計・稼働メトリクスを `.minecraft/logs/mekanism_optimizer.log` に非同期（Tick 負荷完全ゼロ）で自動記録します。

### 🌐 6. 完全日本語化コンフィグ＆ツールチップ対応
- ゲーム内設定 MOD（**Configured** 等）でマウスホバーした際、全項目の分かりやすい日本語説明ツールチップが表示されます。

---

## 💡 参考・リスペクトした MOD (Inspirations)

本 MOD のアーキテクチャおよび最適化設計は、Minecraft コミュニティを牽引する以下の素晴らしい最適化 MOD からインスピレーションを得て開発されました：

- **[ModernFix](https://www.curseforge.com/minecraft/mc-mods/modernfix)** (by embeddedt): 洗練されたバイトコード注入、遅延 Capability 解決、構造的メモリ最適化設計。
- **[FerriteCore](https://www.curseforge.com/minecraft/mc-mods/ferritecore)** (by malte0811): メモリアロケーション削減技術および高速データキャッシング構造。
- **[Lithium](https://www.curseforge.com/minecraft/mc-mods/lithium) / [Canary](https://www.curseforge.com/minecraft/mc-mods/canary)** (by CaffeineMC & AbdElAziz308): ワールド Tick 処理、ブロックエンティティのスロットリング、および $O(1)$ 空間追跡パラダイム。
- **[Mekanism](https://www.curseforge.com/minecraft/mc-mods/mekanism)** (by aidancbrady & the Mekanism Team): Minecraft 史上最も素晴らしい工業体験を創り上げてくださった本家開発チームの皆様に心より感謝申し上げます。

---

## 📦 動作環境・互換性

- **Minecraft**: `1.20.1`
- **Mod Loader**: `Forge 47.3.0+` (`47.4.21` / `47.4.22` 推奨)
- **Java**: `17+`
- **必須前提 MOD**: [Mekanism](https://www.curseforge.com/minecraft/mc-mods/mekanism) (`10.4.x`)
- **アドオン互換性**: *Astral Mekanism (AME)*, *Mekanism Extras*, *Mekanism Generators*, *Mekanism Tools*, および主要倉庫 MOD（*AE2*, *Refined Storage*）と 100% 互換。
- **クライアント / サーバー**: クライアント・サーバー双方、またはサーバー単体への導入でも動作します。

---

## 📜 ライセンス

Mekanism Optimizer は **MIT License** のもとでオープンソースとして公開されています。  
ソースコード ＆ バグ報告: [GitHub Repository](https://github.com/sabu8190/Mekanism-Optimizer)
