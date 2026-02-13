# Space Mob Spawning System Design

**Date**: 2026-02-12
**Status**: Approved

## Goals

1. Naturally-spawned mobs on airless/toxic planets are immune to oxygen damage ("space-adapted")
2. Player-placed mobs (spawn eggs, commands, spawners) are NOT immune
3. Modded mobs fully replace vanilla mobs when the mod is installed; vanilla mobs serve as fallback
4. Every planet (except earth_orbit) has at least some mob presence, distributed across 4 density tiers
5. Spawn weights are standardized and balanced (no more w:1000 silverfish swarms)
6. All available mod integrations are activated (existing 6 + newly activated helpers)
7. Biome JSON spawners sections are emptied; all spawns come from biome modifiers only

## Design Decisions

| Decision | Choice | Rationale |
|----------|--------|-----------|
| Space-adapted detection | `FinalizeSpawnEvent` + `MobSpawnType` | Already implemented pattern in `ModdedMobSpawnController` |
| Immunity mechanism | Persistent data tag `SpaceAdapted` | Survives world saves, simple boolean check |
| Visual indicator | None | Players learn naturally; keeps mob visuals clean |
| Modded replacement | Full replacement (not alongside) | Thematic consistency when mod is present |
| Spawn source | Biome modifiers only | Eliminates duplication, cleaner architecture |
| Lifeless tier | earth_orbit only | All other planets have at least sparse mobs |

---

## 1. Oxygen Immunity System

### How It Works

When a mob spawns naturally on a planet with non-breathable atmosphere:
1. `FinalizeSpawnEvent` fires with `MobSpawnType` info
2. Handler checks: is this a non-breathable planet dimension?
3. Handler checks: is `getSpawnType()` a natural spawn type? (not spawn egg, command, spawner, etc.)
4. If both true: `entity.getPersistentData().putBoolean("SpaceAdapted", true)`
5. `OxygenDamageHandler` checks this tag and skips damage

### Spawn Type Classification

**Natural (gets SpaceAdapted tag)**:
- `NATURAL`, `CHUNK_GENERATION`, `EVENT`, `PATROL`, `JOCKEY`, `REINFORCEMENT`, `TRIGGERED`

**Manual (does NOT get tag)**:
- `SPAWN_EGG`, `COMMAND`, `SPAWNER`, `TRIAL_SPAWNER`, `DISPENSER`, `BUCKET`, `BREEDING`, `MOB_SUMMONED`, `CONVERSION`

### Oxygen Damage Check Flow

```
Is entity from exempt mod namespace? -> Skip damage (existing)
Is entity tagged SpaceAdapted?       -> Skip damage (NEW)
Is entity tagged lives_without_oxygen? -> Skip damage (existing)
Is entity tagged can_survive_in_space? -> Skip damage (existing)
Otherwise -> Apply oxygen damage
```

---

## 2. Modded Mob Replacement Architecture

### Conditional Biome Modifier Generation

For planets with modded replacements, `PlanetMaker` generates two biome modifier files:

**`{planet}/add_spawns_vanilla.json`**:
```json
{
  "type": "neoforge:add_spawns",
  "neoforge:conditions": [{ "type": "neoforge:not", "value": { "type": "neoforge:mod_loaded", "modid": "kobolds" } }],
  "biomes": "#adastramekanized:{planet}_biomes",
  "spawners": [ /* vanilla mobs */ ]
}
```

**`{planet}/add_spawns_kobolds.json`**:
```json
{
  "type": "neoforge:add_spawns",
  "neoforge:conditions": [{ "type": "neoforge:mod_loaded", "modid": "kobolds" }],
  "biomes": "#adastramekanized:{planet}_biomes",
  "spawners": [ /* kobold mobs */ ]
}
```

### PlanetMaker API Addition

```java
.addModReplacementSpawns("kobolds",
    new String[][] {
        {"monster", "minecraft:silverfish", "30", "2", "4"},
        {"monster", "minecraft:endermite", "10", "1", "2"}
    },
    new String[][] {
        {"monster", "kobolds:kobold", "30", "2", "4"},
        {"monster", "kobolds:warrior", "15", "1", "2"},
        {"monster", "kobolds:engineer", "10", "1", "2"}
    }
)
```

For planets with multiple replacement mods (e.g., Venus: born_in_chaos + reptilian), each mod gets its own conditional file pair.

---

## 3. Planet Density Tiers

### Tier Definitions

| Tier | Mob Types | Hostile Weight Range | Group Size | Description |
|------|-----------|---------------------|------------|-------------|
| **Lifeless** | 0 | n/a | n/a | No mobs at all |
| **Sparse** | 1-2 | 5-15 | 1-2 | Rare encounters, lonely atmosphere |
| **Moderate** | 3-5 | 15-40 | 1-3 | Regular encounters, populated |
| **Rich** | 5-8+ | 20-60 | 2-4 | Full ecosystem, diverse threats |

### Spawn Weight Standards

| Role | Weight | Group | Example |
|------|--------|-------|---------|
| Dominant hostile | 30-40 | 1-3 | Main planet threat |
| Secondary hostile | 15-25 | 1-2 | Supporting threat |
| Rare hostile | 5-10 | 1-1 | Elite/dangerous mob |
| Passive creature | 15-30 | 2-4 | Ambient wildlife |
| Rare creature | 5-10 | 1-2 | Unusual fauna |

---

## 4. Per-Planet Mob Assignments

### Lifeless Tier

| Planet | Mobs | Notes |
|--------|------|-------|
| **earth_orbit** | None | Void world, `disableMobGeneration=true` |

### Sparse Tier

| Planet | Vanilla Fallback | Modded Replacement | Mod |
|--------|-----------------|-------------------|-----|
| **enceladus** | stray (w:10, 1-1), phantom (w:5, 1-1) | luminous mobs (w:8) | luminousworld |
| **triton** | phantom (w:10, 1-1), stray (w:5, 1-1) | luminous mobs (w:8) | luminousworld |
| **pluto** | phantom (w:10, 1-1), stray (w:5, 1-1) | mythology mobs (w:8) | mobs_of_mythology |
| **eris** | endermite (w:8, 1-1), phantom (w:5, 1-1) | mythology mobs (w:5) | mobs_of_mythology |

### Moderate Tier

| Planet | Vanilla Fallback | Modded Replacement | Mod |
|--------|-----------------|-------------------|-----|
| **moon** | silverfish (w:30, 2-4), endermite (w:15, 1-2) | kobold (w:30, 2-4), warrior (w:15, 1-2) | kobolds |
| **mercury** | silverfish (w:30, 2-4), endermite (w:10, 1-2) | kobold (w:30, 2-4), warrior (w:15, 1-2), engineer (w:10, 1-2) | kobolds |
| **europa** | drowned (w:25, 1-2), glow_squid (w:15, 1-3) | aquatic undead (w:25) | born_in_chaos |
| **ganymede** | skeleton (w:25, 1-2), silverfish (w:15, 1-2) | undead revamp (w:20) | undead_revamp2 |
| **callisto** | endermite (w:20, 1-2), silverfish (w:10, 1-2) | kobold (w:20, 2-4), warrior (w:10, 1-2) | kobolds |
| **titan** | slime (w:25, 2-4), witch (w:15, 1-1), axolotl (w:20, 2-4) | prehistoric creatures (w:15) | prehistoric_expansion |
| **ceres** | silverfish (w:25, 2-4), endermite (w:10, 1-2) | kobold (w:25, 2-4), warrior (w:12, 1-2), engineer (w:12, 1-2) | kobolds |

### Rich Tier -- Solar System

| Planet | Vanilla Fallback | Modded Replacement | Mod |
|--------|-----------------|-------------------|-----|
| **mars** | husk (w:30, 1-3), enderman (w:10, 1-1), skeleton (w:15, 1-2) | undead roster (w:30 total) | born_in_chaos |
| **venus** | husk (w:25, 1-2), witch (w:15, 1-1), slime (w:20, 2-3) | undead (w:25) + gecko (w:15) | born_in_chaos + reptilian |
| **io** | magma_cube (w:30, 1-3), ghast (w:10, 1-1), blaze (w:15, 1-2) | doom fodder (w:40 total) | doom |
| **pyrios** | ghast (w:15, 1-1), strider (w:20, 1-2), magma_cube (w:25, 1-3) | rotten creatures (w:20) | rottencreatures |

### Rich Tier -- Habitable Exoplanets

| Planet | Vanilla Fallback | Modded Replacement | Mod |
|--------|-----------------|-------------------|-----|
| **kepler22b** | dolphin, turtle, cod, salmon, tropical_fish, drowned | aquatic undead | born_in_chaos |
| **kepler442b** | cow, pig, sheep, chicken, rabbit, fox, wolf, zombie, skeleton, creeper | creatures + undead | born_in_chaos |
| **proxima_b** | husk (w:30, 2-3), rabbit (w:18, 2-3) | undead (w:30) | born_in_chaos |
| **trappist1e** | cow, pig, sheep, chicken, bee, frog, zombie, skeleton | creatures | born_in_chaos |
| **gliese667c** | goat, llama, sheep, skeleton | bosses (w:10) | born_in_chaos |

### Rich Tier -- Alien Worlds

| Planet | Vanilla Fallback | Modded Replacement | Mod |
|--------|-----------------|-------------------|-----|
| **frigidum** | stray, skeleton, polar_bear | creatures (w:18) | born_in_chaos |
| **arenos** | husk (w:40, 2-4), rabbit (w:15, 2-3) | gecko + komodo_dragon | reptilian |
| **paludis** | slime, witch, drowned, frog, axolotl | ribbit (w:50, 2-5) + aquatic undead | ribbits + born_in_chaos |
| **luxoria** | glow_squid, axolotl, parrot, ocelot, phantom | spirits (w:18) | born_in_chaos |
| **glacio** | stray, skeleton, polar_bear | kobolds (w:8) | kobolds |
| **vulcan** | magma_cube (w:30, 1-3), ghast (w:10, 1-1), blaze (w:15, 1-2) | doom roster | doom |
| **terra_nova** | full Overworld roster | creatures + clowns | born_in_chaos |
| **primordium** | parrot, ocelot | foliaath, naga, wroughtnaut | mowziesmobs |
| **bellator** | husk, llama | umvuthana roster | mowziesmobs |
| **profundus** | silverfish | kobold full roster (hostile + friendly) | kobolds |

### Newly Activated Mod Integrations

| Planet | New Mod | Specific Mobs | Rationale |
|--------|---------|---------------|-----------|
| **titan** | prehistoric_expansion | prehistoric creatures | Alien swamp feels primordial |
| **ganymede** | undead_revamp2 | upgraded undead | Lonely plains need upgraded threats |
| **enceladus** | luminousworld | luminous/glowing mobs | Icy glow in perpetual darkness |
| **triton** | luminousworld | luminous/glowing mobs | Deep space glow theme |
| **pyrios** | rottencreatures | decaying/rotten mobs | Decay in volcanic heat |
| **pluto** | mobs_of_mythology | mythological guardians | Remote edge of solar system |
| **eris** | mobs_of_mythology | mythological guardians | Farthest, most mythical |

---

## 5. Implementation Plan

### File Modifications

#### 1. `PlanetMaker.java`
- Add `addModReplacementSpawns(modId, vanillaSpawns, moddedSpawns)` method
- Modify biome JSON generation to output empty `spawners` sections
- Generate conditional biome modifier files with `neoforge:not`/`neoforge:mod_loaded` conditions
- Add oxygen immunity helpers for newly activated mods (luminousworld, undead_revamp2, etc.)

#### 2. `PlanetGenerationRunner.java`
- Rewrite all 31 planet mob configurations using:
  - Standardized tier-based weights
  - `addModReplacementSpawns()` for conditional replacements
  - Newly activated mod helpers
- Remove all duplicate `addMobSpawn()` calls

#### 3. `OxygenDamageHandler.java`
- Add `SpaceAdapted` persistent data check after existing namespace check:
  ```java
  if (entity.getPersistentData().getBoolean("SpaceAdapted")) return;
  ```
- Add newly activated mod namespaces to exempt list (luminousworld, undead_revamp2, mobs_of_mythology, rottencreatures, shineals_prehistoric_expansion)

#### 4. `PlanetMobSpawnHandler.java`
- Add `FinalizeSpawnEvent` listener that tags naturally-spawned mobs with `SpaceAdapted = true` on non-breathable planets
- Use existing `isManualSpawn()` pattern from `ModdedMobSpawnController`

### Generated Output Changes
- All biome JSONs: `spawners` sections become empty `{}`
- New biome modifier files: `add_spawns_vanilla.json` with `neoforge:not` conditions
- Existing modded biome modifiers: kept as-is (already use `mod_loaded`)
- New modded biome modifiers for newly activated integrations

### Testing
- Run `./gradlew makePlanets` to regenerate all planet JSON
- Run `./gradlew build` to verify compilation
- Test in-game: verify mobs spawn on Moon, verify spawn egg mobs take oxygen damage, verify modded replacement works
