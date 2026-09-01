# ⚡ Mekanism Optimizer (Beta)

[![Minecraft](https://img.shields.io/badge/Minecraft-1.20.1-brightgreen.svg)](https://minecraft.net/)
[![Forge](https://img.shields.io/badge/Forge-47.3.0+-orange.svg)](https://files.minecraftforge.net/)
[![Release](https://img.shields.io/badge/Release-v1.0.0--Beta-blue.svg)](https://github.com/sabu8190/Mekanism-Optimizer)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

**Mekanism Optimizer** は、大規模工業環境・超巨大ファクトリー・天体倍速加速環境における Mekanism（および AME / Mekanism Extras / Generators 等のアドオン群）の Tick スパイク、GC 負荷、送電・パイプ探索オーバーヘッドを抜本的に解消する次世代ハイパフォーマンス最適化 MOD です。

---

## 🚀 主な機能と特徴

### 1. ⚡ パイプ＆トランスポーター探索の劇的軽量化
- 毎 Tick の経路探索・受け入れ先 Capability 照会を $O(1)$ キャッシュ化し、アイテムパイプの Tick 負荷を最大 90% 削減。

### 2. ⚡ 送電ネットワーク＆発電機群の最適化
- 満充電時の無駄な送電シミュレーションを休止（適応型バックオフ）。
- 太陽光・風力発電機の夜間・満杯時における直射日光計算を $O(1)$ でバイパス。

### 3. 🚀 21億制限突破＆マルチバースト自動搬出 (Config で自由有効化)
- 21.4 億（`Integer.MAX_VALUE`）の搬出制限を突破し、1 Tick 内で 100 億〜数兆超の液体・気体・エネルギー、および数万個のアイテムを一括搬出可能（初期値: 安全なオフ）。

### 4. 🪵 専用非同期ログシステム (`logs/mekanism_optimizer.log`)
- 他 MOD のログに埋もれず、Mekanism Optimizer 単独の最適化統計・パフォーマンスメトリクスを非同期で専用ファイルに自動記録（Tick 負荷ゼロ）。

### 5. 🇯🇵 完全日本語コンフィグ＆ツールチップ対応
- ゲーム内設定画面（Configured 等）において、全項目の分かりやすい日本語説明とツールチップを完備。

---

## 📦 動作環境

- **Minecraft**: `1.20.1`
- **Forge**: `47.3.0+` (`47.4.21` / `47.4.22` 推奨)
- **Java**: `17+`
- **必須前提 MOD**: `Mekanism 10.4.x`
