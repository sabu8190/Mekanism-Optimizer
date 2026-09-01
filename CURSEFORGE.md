# ⚡ Mekanism Optimizer

**Mekanism Optimizer** is a high-performance optimization mod for **Mekanism** and its addons (Astral Mekanism, Mekanism Extras, Mekanism Generators, etc.) on Minecraft 1.20.1 Forge.

It dramatically reduces Tick duration, eliminates GC pressure, and removes bottlenecks caused by logistical transporters, universal cables, multiblock calculations, and high-frequency ticking.

---

## 🚀 Key Features

- ⚡ **Logistical Transporter & Pipe Optimization**: Caches acceptor capabilities and pathfinding lookups in $O(1)$ time, cutting pipe tick lag by up to 90%.
- ⚡ **Energy Network & Cable Adaptive Backoff**: Skips redundant energy transfer simulations when target machines are fully charged.
- ☀️ **Generator Optimizations**: Bypasses nighttime skylight raycasting for Solar Generators and Solar Neutron Activators.
- 🚀 **Bypass 2.14B Auto-Eject Limit**: Optional config to bypass Forge's `Integer.MAX_VALUE` (2.14B) limit per tick using multi-burst ejection (Default: false).
- 🪵 **Dedicated Async Logger**: Outputs optimization metrics to `logs/mekanism_optimizer.log` with zero tick overhead.
- 🌐 **Full Localization**: Includes complete English and Japanese (`ja_jp`) in-game config tooltips.

---

## 📦 Requirements

- **Minecraft**: `1.20.1`
- **Mod Loader**: `Forge 47.3.0+` (`47.4.21`+ recommended)
- **Dependency**: [Mekanism](https://www.curseforge.com/minecraft/mc-mods/mekanism) (10.4.x)

---

## 🛠️ Configuration

Configure settings in-game via **Configured** or edit `.minecraft/config/mekanism_optimizer-common.toml`.
