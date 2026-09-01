# ⚡ Mekanism Optimizer

![CurseForge Banner](https://raw.githubusercontent.com/sabu8190/Mekanism-Optimizer/main/icon.png)

**Mekanism Optimizer** is a next-generation, high-performance optimization mod engineered specifically for **Mekanism** and its rich addon ecosystem (Astral Mekanism, Mekanism Extras, Mekanism Generators, etc.) on Minecraft 1.20.1 Forge.

It drastically reduces server tick duration, eliminates GC memory pressure, and removes bottlenecks caused by logistical transporters, universal cables, multiblock calculations, and high-frequency ticking environments.

---

## 🚀 Key Features

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

## 📜 License & Open Source

Mekanism Optimizer is open-source under the **MIT License**.  
Source code & Issue Tracker: [GitHub Repository](https://github.com/sabu8190/Mekanism-Optimizer)
