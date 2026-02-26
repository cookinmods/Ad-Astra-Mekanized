# Planet Spawn Control System

Ad Astra Mekanized controls mob spawning across all 31 planets using a tiered difficulty system. Three optional creature mods are supported as the **recommended experience** -- when installed, they replace vanilla fallback mobs with thematically appropriate creatures on designated planets.

## Recommended Optional Dependencies

| Mod | CurseForge | Role |
|-----|-----------|------|
| **Born in Chaos V1** | [Link](https://www.curseforge.com/minecraft/mc-mods/born-in-chaos) | Horror mobs for moderate and active-tier planets |
| **Kobolds** | [Link](https://www.curseforge.com/minecraft/mc-mods/kobolds) | Cave-dwelling kobold tribes for sparse and underground planets |
| **Ribbits** | [Link](https://www.curseforge.com/minecraft/mc-mods/ribbits) | Peaceful frog villagers on habitable earth-like planets |

All three mods are optional. Without them, planets use vanilla Minecraft mobs as fallbacks.

## Planet Tiers

Every planet is assigned a difficulty tier that determines its total mob spawn density:

| Tier | Total Spawn Weight | Description |
|------|-------------------|-------------|
| **Barren** | 0-6 | Near-empty, desolate worlds. Minimal encounters. |
| **Sparse** | 10-18 | Light encounters. Cave-dwellers and stragglers. |
| **Moderate** | 16-35 | Standard hostile populations with themed variety. |
| **Active** | 35-60 | Dangerous, combat-heavy worlds. |
| **Habitable** | 19-35 | Earth-like planets with peaceful creatures and light threats. |

## Spawn Rules

These rules apply to all planets:
- No phantoms, creepers, bees, or farm animals in space
- Blaze and wither skeleton never appear on the same planet
- Skeleton and zombie never appear on the same planet
- Ghasts appear at very low weight (1-2) across many planets
- All kobold planets spawn all 7 kobold variants
- Ribbits appear on 5 habitable planets
- Spawn eggs, spawners, and commands work everywhere regardless of restrictions

---

## Complete Planet Spawn Table

### Barren Tier

#### Earth Orbit
No mob spawning (void dimension).

#### Eris
| Mob | Weight | Group Size | Source |
|-----|--------|------------|--------|
| Endermite | 3 | 1 | Vanilla |

#### Pluto
| Mob | Weight | Group Size | Source |
|-----|--------|------------|--------|
| Endermite | 3 | 1 | Vanilla |
| Ghast | 1 | 1 | Vanilla |

#### Mercury
| Mob | Weight | Group Size | Source |
|-----|--------|------------|--------|
| Shulker | 3 | 1 | Vanilla |
| Endermite | 3 | 1-2 | Vanilla |

---

### Sparse Tier

#### Moon
| Mob | Weight | Group Size | Source | Condition |
|-----|--------|------------|--------|-----------|
| Kobold | 5 | 1-2 | Kobolds | Kobolds installed |
| Kobold Warrior | 3 | 1 | Kobolds | Kobolds installed |
| Kobold Enchanter | 2 | 1 | Kobolds | Kobolds installed |
| Kobold Engineer | 3 | 1 | Kobolds | Kobolds installed |
| Kobold Zombie | 2 | 1 | Kobolds | Kobolds installed |
| Kobold Skeleton | 2 | 1 | Kobolds | Kobolds installed |
| Witherbold | 1 | 1 | Kobolds | Kobolds installed |
| *Silverfish* | *10* | *2-4* | *Vanilla fallback* | *Kobolds NOT installed* |
| *Endermite* | *5* | *1-2* | *Vanilla fallback* | *Kobolds NOT installed* |

#### Callisto
| Mob | Weight | Group Size | Source | Condition |
|-----|--------|------------|--------|-----------|
| Kobold | 4 | 1-2 | Kobolds | Kobolds installed |
| Kobold Warrior | 3 | 1 | Kobolds | Kobolds installed |
| Kobold Enchanter | 2 | 1 | Kobolds | Kobolds installed |
| Kobold Engineer | 3 | 1 | Kobolds | Kobolds installed |
| Kobold Zombie | 2 | 1 | Kobolds | Kobolds installed |
| Kobold Skeleton | 2 | 1 | Kobolds | Kobolds installed |
| Witherbold | 1 | 1 | Kobolds | Kobolds installed |
| Ghast | 1 | 1 | Vanilla | Always |
| *Silverfish* | *8* | *2-3* | *Vanilla fallback* | *Kobolds NOT installed* |
| *Endermite* | *5* | *1-2* | *Vanilla fallback* | *Kobolds NOT installed* |
| *Ghast* | *1* | *1* | *Vanilla fallback* | *Kobolds NOT installed* |

#### Ceres
| Mob | Weight | Group Size | Source | Condition |
|-----|--------|------------|--------|-----------|
| Kobold | 5 | 1-2 | Kobolds | Kobolds installed |
| Kobold Warrior | 3 | 1 | Kobolds | Kobolds installed |
| Kobold Enchanter | 2 | 1 | Kobolds | Kobolds installed |
| Kobold Engineer | 3 | 1 | Kobolds | Kobolds installed |
| Kobold Zombie | 2 | 1 | Kobolds | Kobolds installed |
| Kobold Skeleton | 2 | 1 | Kobolds | Kobolds installed |
| Witherbold | 1 | 1 | Kobolds | Kobolds installed |
| *Silverfish* | *10* | *2-4* | *Vanilla fallback* | *Kobolds NOT installed* |
| *Endermite* | *5* | *1-2* | *Vanilla fallback* | *Kobolds NOT installed* |

#### Enceladus
| Mob | Weight | Group Size | Source |
|-----|--------|------------|--------|
| Stray | 10 | 1-2 | Vanilla |
| Ghast | 1 | 1 | Vanilla |

#### Triton
| Mob | Weight | Group Size | Source |
|-----|--------|------------|--------|
| Stray | 8 | 1-2 | Vanilla |
| Endermite | 5 | 1-2 | Vanilla |
| Ghast | 1 | 1 | Vanilla |

#### Ganymede
| Mob | Weight | Group Size | Source |
|-----|--------|------------|--------|
| Stray | 10 | 1-2 | Vanilla |
| Enderman | 3 | 1 | Vanilla |
| Ghast | 1 | 1 | Vanilla |

#### Titan
| Mob | Weight | Group Size | Source |
|-----|--------|------------|--------|
| Slime | 10 | 1-3 | Vanilla |
| Witch | 6 | 1 | Vanilla |

---

### Moderate Tier

#### Mars
| Mob | Weight | Group Size | Source | Condition |
|-----|--------|------------|--------|-----------|
| Enderman | 4 | 1 | Vanilla | Always |
| Ghast | 1 | 1 | Vanilla | Always |
| Decrepit Skeleton | 8 | 1-2 | Born in Chaos | BiC installed |
| Bone Imp | 5 | 1 | Born in Chaos | BiC installed |
| *Husk* | *12* | *1-2* | *Vanilla fallback* | *BiC NOT installed* |

#### Venus
| Mob | Weight | Group Size | Source | Condition |
|-----|--------|------------|--------|-----------|
| Witch | 6 | 1 | Vanilla | Always |
| Slime | 6 | 1-2 | Vanilla | Always |
| Decaying Zombie | 8 | 1-2 | Born in Chaos | BiC installed |
| Skeleton Thrasher | 5 | 1 | Born in Chaos | BiC installed |
| *Husk* | *10* | *1-2* | *Vanilla fallback* | *BiC NOT installed* |

#### Europa
| Mob | Weight | Group Size | Source | Condition |
|-----|--------|------------|--------|-----------|
| Guardian | 6 | 1-2 | Vanilla | Always |
| Corpse Fish | 8 | 2-3 | Born in Chaos | BiC installed |
| Zombie Fisherman | 5 | 1 | Born in Chaos | BiC installed |
| *Drowned* | *12* | *1-2* | *Vanilla fallback* | *BiC NOT installed* |

#### Io
| Mob | Weight | Group Size | Source |
|-----|--------|------------|--------|
| Magma Cube | 12 | 1-3 | Vanilla |
| Zombified Piglin | 8 | 1-2 | Vanilla |
| Ghast | 2 | 1 | Vanilla |

#### Frigidum
| Mob | Weight | Group Size | Source | Condition |
|-----|--------|------------|--------|-----------|
| Enderman | 3 | 1 | Vanilla | Always |
| Ghast | 1 | 1 | Vanilla | Always |
| Dread Hound | 6 | 1-2 | Born in Chaos | BiC installed |
| Baby Spider | 4 | 1-2 | Born in Chaos | BiC installed |
| *Stray* | *12* | *1-2* | *Vanilla fallback* | *BiC NOT installed* |

#### Arenos
| Mob | Weight | Group Size | Source | Condition |
|-----|--------|------------|--------|-----------|
| Enderman | 4 | 1 | Vanilla | Always |
| Ghast | 1 | 1 | Vanilla | Always |
| Bonescaller | 5 | 1 | Born in Chaos | BiC installed |
| Husk | 10 | 1-2 | Born in Chaos* | BiC installed |
| *Husk* | *15* | *1-3* | *Vanilla fallback* | *BiC NOT installed* |

*Arenos keeps Husks even with BiC, but at reduced weight alongside Bonescallers.

#### Glacio
| Mob | Weight | Group Size | Source | Condition |
|-----|--------|------------|--------|-----------|
| Enderman | 3 | 1 | Vanilla | Always |
| Kobold | 4 | 1-2 | Kobolds | Kobolds installed |
| Kobold Warrior | 3 | 1 | Kobolds | Kobolds installed |
| Kobold Enchanter | 2 | 1 | Kobolds | Kobolds installed |
| Kobold Engineer | 3 | 1 | Kobolds | Kobolds installed |
| Kobold Zombie | 2 | 1 | Kobolds | Kobolds installed |
| Kobold Skeleton | 2 | 1 | Kobolds | Kobolds installed |
| Witherbold | 1 | 1 | Kobolds | Kobolds installed |
| *Stray* | *10* | *1-2* | *Vanilla fallback* | *Kobolds NOT installed* |

#### Luxoria
| Mob | Weight | Group Size | Source | Condition |
|-----|--------|------------|--------|-----------|
| Enderman | 5 | 1 | Vanilla | Always |
| Shulker | 3 | 1 | Vanilla | Always |
| Restless Spirit | 5 | 1 | Born in Chaos | BiC installed |
| Seared Spirit | 4 | 1 | Born in Chaos | BiC installed |
| Firelight | 3 | 1 | Born in Chaos | BiC installed |

#### Proxima B
| Mob | Weight | Group Size | Source | Condition |
|-----|--------|------------|--------|-----------|
| Enderman | 4 | 1 | Vanilla | Always |
| Decaying Zombie | 6 | 1-2 | Born in Chaos | BiC installed |
| Bonescaller | 4 | 1 | Born in Chaos | BiC installed |
| *Husk* | *12* | *1-2* | *Vanilla fallback* | *BiC NOT installed* |

#### Gliese 667c
| Mob | Weight | Group Size | Source | Condition |
|-----|--------|------------|--------|-----------|
| Enderman | 5 | 1 | Vanilla | Always |
| Shulker | 3 | 1 | Vanilla | Always |
| Supreme Bonescaller | 3 | 1 | Born in Chaos | BiC installed |
| Lifestealer | 3 | 1 | Born in Chaos | BiC installed |
| Krampus | 2 | 1 | Born in Chaos | BiC installed |
| Missioner | 3 | 1 | Born in Chaos | BiC installed |

---

### Active Tier

#### Vulcan
| Mob | Weight | Group Size | Source | Condition |
|-----|--------|------------|--------|-----------|
| Wither Skeleton | 12 | 1-2 | Vanilla | Always |
| Magma Cube | 10 | 1-3 | Vanilla | Always |
| Ghast | 2 | 1 | Vanilla | Always |
| Door Knight | 5 | 1 | Born in Chaos | BiC installed |
| Skeleton Thrasher | 6 | 1 | Born in Chaos | BiC installed |

#### Pyrios
| Mob | Weight | Group Size | Source | Condition |
|-----|--------|------------|--------|-----------|
| Blaze | 10 | 1-2 | Vanilla | Always |
| Magma Cube | 12 | 1-3 | Vanilla | Always |
| Ghast | 2 | 1 | Vanilla | Always |
| Seared Spirit | 8 | 1 | Born in Chaos | BiC installed |
| Barrel Zombie | 6 | 1 | Born in Chaos | BiC installed |

#### Bellator
| Mob | Weight | Group Size | Source | Condition |
|-----|--------|------------|--------|-----------|
| Husk | 12 | 1-2 | Vanilla | Always |
| Enderman | 5 | 1 | Vanilla | Always |
| Piglin | 4 | 1-2 | Vanilla | Always |
| Ghast | 1 | 1 | Vanilla | Always |
| Pumpkin Dunce | 4 | 1 | Born in Chaos | BiC installed |
| Dunce Pumpkin | 4 | 1 | Born in Chaos | BiC installed |
| Pumpkin Spirit | 3 | 1 | Born in Chaos | BiC installed |

#### Primordium
| Mob | Weight | Group Size | Source | Condition |
|-----|--------|------------|--------|-----------|
| Spider | 10 | 1-2 | Vanilla | Always |
| Cave Spider | 8 | 1-2 | Vanilla | Always |
| Enderman | 4 | 1 | Vanilla | Always |
| Shulker | 3 | 1 | Vanilla | Always |
| Baby Spider | 5 | 1-2 | Born in Chaos | BiC installed |
| Mother Spider | 3 | 1 | Born in Chaos | BiC installed |
| Dread Hound | 4 | 1 | Born in Chaos | BiC installed |

#### Profundus
| Mob | Weight | Group Size | Source | Condition |
|-----|--------|------------|--------|-----------|
| Silverfish | 4 | 1-2 | Vanilla | Always |
| Kobold | 10 | 2-4 | Kobolds | Kobolds installed |
| Kobold Warrior | 8 | 1-2 | Kobolds | Kobolds installed |
| Kobold Enchanter | 5 | 1 | Kobolds | Kobolds installed |
| Kobold Engineer | 8 | 1-2 | Kobolds | Kobolds installed |
| Kobold Zombie | 6 | 2-3 | Kobolds | Kobolds installed |
| Kobold Skeleton | 6 | 2-3 | Kobolds | Kobolds installed |
| Witherbold | 3 | 1 | Kobolds | Kobolds installed |
| Born in Chaos Spirits | 20 | varies | Born in Chaos | BiC installed |
| *Cave Spider* | *6* | *1-2* | *Vanilla fallback* | *Kobolds NOT installed* |

---

### Habitable Tier

#### Terra Nova
| Mob | Weight | Group Size | Source | Condition |
|-----|--------|------------|--------|-----------|
| Goat | 4 | 1-2 | Vanilla | Always |
| Llama | 3 | 1-2 | Vanilla | Always |
| Enderman | 3 | 1 | Vanilla | Always |
| Spider | 4 | 1 | Vanilla | Always |
| Ribbits | 15 | varies | Ribbits | Ribbits installed |

#### Paludis
| Mob | Weight | Group Size | Source | Condition |
|-----|--------|------------|--------|-----------|
| Frog | 6 | 1-3 | Vanilla | Always |
| Drowned | 5 | 1-2 | Vanilla | Always |
| Slime | 6 | 1-2 | Vanilla | Always |
| Ribbits | 12 | varies | Ribbits | Ribbits installed |
| Corpse Fish | 6 | 1-2 | Born in Chaos | BiC installed |

#### Kepler-22b
| Mob | Weight | Group Size | Source | Condition |
|-----|--------|------------|--------|-----------|
| Tropical Fish | 4 | 2-4 | Vanilla | Always |
| Drowned | 5 | 1-2 | Vanilla | Always |
| Ribbits | 10 | varies | Ribbits | Ribbits installed |
| Corpse Fish | 5 | 1-2 | Born in Chaos | BiC installed |

#### Kepler-442b
| Mob | Weight | Group Size | Source | Condition |
|-----|--------|------------|--------|-----------|
| Goat | 3 | 1-2 | Vanilla | Always |
| Llama | 3 | 1-2 | Vanilla | Always |
| Spider | 5 | 1 | Vanilla | Always |
| Ribbits | 10 | varies | Ribbits | Ribbits installed |
| Baby Spider | 4 | 1-2 | Born in Chaos | BiC installed |

#### TRAPPIST-1e
| Mob | Weight | Group Size | Source | Condition |
|-----|--------|------------|--------|-----------|
| Frog | 4 | 1-2 | Vanilla | Always |
| Goat | 3 | 1-2 | Vanilla | Always |
| Enderman | 2 | 1 | Vanilla | Always |
| Ribbits | 10 | varies | Ribbits | Ribbits installed |

---

## Config Options

Config file: `config/adastramekanized-common.toml`

Under the `[Creature Mod Spawn Control]` section:

| Option | Default | Effect |
|--------|---------|--------|
| `restrictBornInChaosSpawns` | `true` | **ON**: BiC mobs only spawn on designated planets above. **OFF**: BiC mobs spawn everywhere as if the spawn control doesn't exist. |
| `restrictKoboldsSpawns` | `true` | **ON**: Kobolds only spawn on designated planets above. **OFF**: Kobolds spawn everywhere normally. |
| `restrictRibbitsSpawns` | `true` | **ON**: Ribbits only spawn on designated planets above. **OFF**: Ribbits spawn everywhere normally. |

When a restriction is **ON** (default), the mod's mobs:
- Spawn only on their assigned planets (entity-level whitelist)
- Spawn freely in vanilla dimensions (Overworld, Nether, End)
- Can always be summoned via spawn eggs, spawners, and commands on any planet

When a restriction is **OFF**, the mod behaves as if Ad Astra Mekanized's spawn control doesn't exist for that mod.

## How It Works (Technical)

The spawn control system uses two NeoForge event handlers at `HIGHEST` priority:

1. **FinalizeSpawnEvent** -- Intercepts mob spawns as they finalize. Checks if the mob belongs to a controlled mod namespace (`born_in_chaos_v1`, `kobolds`, `ribbits`). Manual spawn types (eggs, spawners, commands) are always allowed. Natural spawns are checked against an entity-level whitelist for planet dimensions.

2. **EntityJoinLevelEvent** -- Fallback handler that catches entities bypassing FinalizeSpawnEvent. Uses the same whitelist checks.

For planet dimensions, the whitelist operates at **entity level** -- only the specific creature IDs configured in PlanetGenerationRunner are allowed to spawn naturally. This prevents a mod's own biome modifiers from leaking unintended creatures onto planets.

For vanilla dimensions (Overworld, Nether, End), the whitelist operates at **namespace level** -- all entities from whitelisted mods can spawn freely.
