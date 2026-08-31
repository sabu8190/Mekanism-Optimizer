# ⚡ Mekanism Optimizer v2.0 (Closed Beta)

[![Minecraft](https://img.shields.io/badge/Minecraft-1.20.1-brightgreen.svg)](https://minecraft.net/)
[![Forge](https://img.shields.io/badge/Forge-47.3.0+-orange.svg)](https://files.minecraftforge.net/)
[![Release](https://img.shields.io/badge/Release-v2.0-blue.svg)](https://github.com/sabu8190/Mekanism-Optimizer)

**Mekanism Optimizer** は、大規模工業環境・超巨大ファクトリー・天体倍速加速環境における Mekanism（および AME / Mekanism Extras / Generators 等のアドオン群）の Tick スパイク、GC 負荷、送電・パイプ探索オーバーヘッドを抜本的に解消する次世代ハイパフォーマンス最適化 MOD です。

---

## 🚀 主な機能とアーキテクチャ (v2.0)

### 1. 🪵 専用ログファイル生成機能 (`logs/mekanism_optimizer.log`)
- 他の MOD のログに埋もれることなく、Mekanism Optimizer 専用のログファイル `logs/mekanism_optimizer.log` を非同期で自動生成。
- 毎分の最適化スキップ統計（パイプ・送電・バックオフ・放射線等）やバースト搬出の稼働状況をリアルタイムに記録。

### 2. 🛡️ 完全な GUI 互換性と安全性
- 温室（Greenhouse）を含め、全マシンの GUI が 100% 正常に動作。

### 3. 🌐 完全日本語化コンフィグ＆ツールチップ対応 (`ja_jp.json` / `en_us.json`)
- ゲーム内コンフィグ画面で全項目の分かりやすい日本語名・詳細ツールチップを表示。

### 4. 🚀 自動搬出 21億制限突破 ＆ Tick 単位搬出レート調整 (Config で自由有効化)
- 1 Tick 内で 100億〜数兆超の液体・気体・エネルギーを一括搬出可能。

### 5. ☀️ 太陽光・風力・全発電機群の劇的軽量化 (`TileEntityGeneratorMixin` 系列)
- 夜間スカイライト完全バイパスおよび満杯時アダプティブ・スリープ。

---

## 📦 動作環境

- **Minecraft**: 1.20.1
- **Forge**: 47.3.0+ (47.4.21 / 47.4.22 推奨)
- **Java**: 17+
- **前提 MOD**: Mekanism 10.4.x
