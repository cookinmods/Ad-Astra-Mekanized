# Mob Spawning Overhaul Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Remove 8 unused creature mods, rebalance all 31 planets with a 5-tier spawn system, add config toggles for BiC/Kobolds/Ribbits spawn restriction.

**Architecture:** Strip all references to dropped mods (Mowzies, MCDoom, Reptilian, Luminous World, Undead Revamped, Rotten Creatures, Prehistoric Expansion, Mobs of Mythology) from Java code, build config, dependency metadata, and entity tags. Rewrite PlanetGenerationRunner spawn configs for all 31 planets using approved tier-based assignments with strict mob selection rules. Add 3 NeoForge config booleans that gate creature mod spawns to designated planets only.

**Tech Stack:** NeoForge 1.21.1, Java 21, Gradle 8.14.3+

---

## Design Decisions (User-Approved)

### 5-Tier Planet System
| Tier | Target Weight | Description |
|------|--------------|-------------|
| Barren | 0-5 | Near-empty, desolate worlds |
| Sparse | 10-25 | Light encounters, cave-dwellers |
| Moderate | 25-50 | Standard hostile/mixed populations |
| Active | 50-80 | Dangerous, combat-heavy worlds |
| Habitable | 30-50 | Earth-like with peaceful + hostile mix |

### Mob Selection Rules
- No phantoms in space
- No creepers in space
- No bees in space
- No farm animals in space (cows, pigs, sheep, chickens, horses)
- Never pair blaze + wither skeleton together
- Never pair skeleton + zombie together
- Spread ghasts at very low weight (1-2) across many planets
- Use underutilized mobs: shulkers, endermen, piglins, zombified piglins
- All kobold areas spawn ALL 7 kobold types
- Add goats and llamas sparingly on earth-like planets only
- Ribbits on 5 habitable planets instead of 1

### Kept Creature Mods (Optional Dependencies)
- **Born in Chaos** (`born_in_chaos_v1`) - Horror mobs for moderate/active planets
- **Kobolds** (`kobolds`) - Cave dwellers for sparse/moderate/active planets
- **Ribbits** (`ribbits`) - Frog villagers for habitable planets

### Removed Creature Mods
- Mowzie's Mobs (`mowziesmobs`)
- MCDoom (`doom`)
- Reptilian (`reptilian`)
- Luminous World (`luminousworld`)
- The Undead Revamped (`undead_revamp2`)
- Rotten Creatures (`rottencreatures`)
- Shineal's Prehistoric Expansion (`shineals_prehistoric_expansion`)
- Mobs of Mythology (`mobs_of_mythology`)

---

## Approved Planet Assignments (v2)

### Barren (0-5 total weight)
| Planet | Vanilla Mobs | Modded Mobs |
|--------|-------------|-------------|
| Earth Orbit | none (void) | none |
| Eris | endermite 3 | none |
| Pluto | endermite 3, ghast 1 | none |
| Mercury | shulker 3, endermite 3 | none |

### Sparse (10-25 total weight)
| Planet | Vanilla Mobs | Modded Mobs |
|--------|-------------|-------------|
| Moon | silverfish 10, endermite 5 (fallback) | kobolds: all 7 types (replace vanilla) |
| Callisto | silverfish 8, endermite 5, ghast 1 (fallback) | kobolds: all 7 types (replace vanilla) |
| Ceres | silverfish 10, endermite 5 (fallback) | kobolds: all 7 types (replace vanilla) |
| Enceladus | stray 10, ghast 1 | none |
| Triton | stray 8, endermite 5, ghast 1 | none |
| Ganymede | stray 10, enderman 3, ghast 1 | none |
| Titan | slime 10, witch 6 | none |

### Moderate (25-50 total weight)
| Planet | Vanilla Mobs | Modded Mobs |
|--------|-------------|-------------|
| Mars | husk 12, enderman 4, ghast 1 | BiC: decrepit_skeleton 8, bone_imp 5 |
| Venus | husk 10, witch 6, slime 6 | BiC: decaying_zombie 8, skeleton_thrasher 5 |
| Europa | drowned 12, guardian 6 | BiC: corpse_fish 8, zombie_fisherman 5 |
| Io | magma_cube 12, zombified_piglin 8, ghast 2 | none |
| Frigidum | stray 12, enderman 3, ghast 1 | BiC: dread_hound 6, baby_spider 4 |
| Arenos | husk 15, enderman 4, ghast 1 | BiC: bonescaller 5 |
| Glacio | stray 10, enderman 3 (fallback) | kobolds: all 7 types (replace vanilla) |
| Luxoria | enderman 5, shulker 3 | BiC: spirits (restless_spirit, seared_spirit, firelight) |
| Proxima B | husk 12, enderman 4 | BiC: decaying_zombie 6, bonescaller 4 |
| Gliese667c | enderman 5, shulker 3 | BiC: bosses (supreme_bonescaller, lifestealer, krampus) |

### Active (50-80 total weight)
| Planet | Vanilla Mobs | Modded Mobs |
|--------|-------------|-------------|
| Vulcan | wither_skeleton 12, magma_cube 10, ghast 2 | BiC: door_knight 5, skeleton_thrasher 6 |
| Pyrios | blaze 10, magma_cube 12, ghast 2 | BiC: seared_spirit 8, barrel_zombie 6 |
| Bellator | husk 12, enderman 5, piglin 4, ghast 1 | BiC: pumpkins (pumpkin_dunce, dunce_pumpkin, etc.) |
| Primordium | spider 10, cave_spider 8, enderman 4, shulker 3 | BiC: creatures (baby_spider, mother_spider, dread_hound, etc.) |
| Profundus | cave_spider 6, silverfish 4 (fallback) | kobolds: ALL 7 types heavy weights |

### Habitable (30-50 total weight)
| Planet | Vanilla Mobs | Modded Mobs |
|--------|-------------|-------------|
| Terra Nova | goat 4, llama 3, enderman 3, spider 4 | ribbits 15 |
| Paludis | frog 6, drowned 5, slime 6 | ribbits 12, BiC: corpse_fish 6 |
| Kepler22b | tropical_fish 4, drowned 5 | ribbits 10, BiC: corpse_fish 5 |
| Kepler442b | goat 3, llama 3, spider 5 | ribbits 10, BiC: baby_spider 4 |
| Trappist1e | frog 4, goat 3, enderman 2 | ribbits 10 |

---

## Tasks

### Task 1: Remove dropped creature mods from build.gradle and neoforge.mods.toml

**Files:**
- Modify: `build.gradle:184-198`
- Modify: `src/main/resources/META-INF/neoforge.mods.toml:167-237`

**Step 1: Edit build.gradle**

Remove these lines from `build.gradle` (lines 184-198 area):

```groovy
// REMOVE ALL OF THESE:
implementation "curse.maven:MowiesMobs-250498:6700443"
implementation "curse.maven:theundeadRevamped-479710:6778715"
//implementation "curse.maven:reptilian-1169666:6816483"
implementation "blank:reptilian-1.1.0-neoforge-1.21.1"
//implementation "curse.maven:PrehistoricExpansion-909176:7011472"
implementation "blank:shineals_prehistoric_expansion-1.4.3-neoforge-1.21.1"
implementation "curse.maven:luminousoverworld-909107:6588233"
implementation "curse.maven:mcdoom-368765:5729612"
// implementation "curse.maven:mobsOfMythology-699989:6403508"
// implementation "curse.maven:rottenCreatures-371033:6677062"
```

Also remove these supporting libraries that are ONLY used by dropped mods:
```groovy
// REMOVE - only needed by Mowzies:
implementation "curse.maven:geckolib-388172:6659026"

// REMOVE - only needed by dropped mods:
implementation "curse.maven:smartbrainlib-661293:7055149"
implementation "curse.maven:azurelib-817423:7084472"
implementation "curse.maven:platform-997634:6677047"
```

**KEEP** these (used by Kobolds/Ribbits):
```groovy
implementation "curse.maven:kobolds-484967:7088059"
implementation "curse.maven:ribbits-622967:6988284"
implementation "curse.maven:yungsAPI-1015100:6715463"  // ribbits dependency
```

> **IMPORTANT:** Before removing geckolib/smartbrainlib/azurelib/platform, verify they are not also dependencies of kobolds, ribbits, or born_in_chaos. Check each mod's dependency chain. If any kept mod needs them, keep them.

**Step 2: Edit neoforge.mods.toml**

Remove these `[[dependencies.adastramekanized]]` blocks:
- Lines 167-173: MCDoom (`doom`)
- Lines 175-181: Reptilian (`reptilian`)
- Lines 191-197: Luminous World (`luminousworld`)
- Lines 199-205: Mowzie's Mobs (`mowziesmobs`)
- Lines 207-213: The Undead Revamped (`undead_revamp2`)
- Lines 215-221: Rotten Creatures (`rottencreatures`)
- Lines 223-229: Shineal's Prehistoric Expansion (`shineals_prehistoric_expansion`)
- Lines 231-237: Mobs of Mythology (`mobs_of_mythology`)

**KEEP** these dependency blocks:
- Born in Chaos (`born_in_chaos_v1`) - lines 151-157
- Kobolds (`kobolds`) - lines 159-165
- Ribbits (`ribbits`) - lines 183-189

**Step 3: Verify build compiles**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL (may fail if Java code still references dropped mods - that's OK, we fix in Task 2)

**Step 4: Commit**

```bash
git add build.gradle src/main/resources/META-INF/neoforge.mods.toml
git commit -m "Remove 8 dropped creature mod dependencies from build config"
```

---

### Task 2: Remove dropped creature mod references from Java code

**Files:**
- Modify: `src/main/java/com/hecookin/adastramekanized/AdAstraMekanized.java:117-130`
- Modify: `src/main/java/com/hecookin/adastramekanized/common/events/OxygenDamageHandler.java:31-62`
- Modify: `src/main/java/com/hecookin/adastramekanized/common/events/ModdedMobWhitelistData.java:26-37`
- Delete or modify: Any `MowziesMobsIntegration.java` file if it exists

**Step 1: Clean AdAstraMekanized.java**

Remove line 117 (`MowziesMobsIntegration.init()`) and its import at line 20.

Remove these `allowIn*` calls (lines 124-130):
```java
// REMOVE:
ModdedMobSpawnController.allowInVanillaDimensions("undead_revamp2");
ModdedMobSpawnController.allowInVanillaDimensions("mowziesmobs");
ModdedMobSpawnController.allowInNether("doom");
ModdedMobSpawnController.allowInOverworld("rottencreatures");
ModdedMobSpawnController.allowInOverworld("reptilian");
```

**KEEP:**
```java
ModdedMobSpawnController.allowInVanillaDimensions("born_in_chaos_v1");
ModdedMobSpawnController.allowInVanillaDimensions("kobolds");
```

Add Ribbits to vanilla dimensions (currently missing):
```java
ModdedMobSpawnController.allowInVanillaDimensions("ribbits");
```

**Step 2: Clean OxygenDamageHandler.java**

Remove these namespace checks from `onEntityTick()` (lines 31-62):
```java
// REMOVE these blocks:
if (entityId.contains("mowziesmobs")) { return; }
if (entityId.contains("undead_revamp2")) { return; }
if (entityId.contains("doom")) { return; }
if (entityId.contains("reptilian")) { return; }
if (entityId.contains("lumination") || entityId.contains("luminousworld")) { return; }
if (entityId.contains("mobs_of_mythology")) { return; }
if (entityId.contains("rottencreatures")) { return; }
if (entityId.contains("shineals_prehistoric_expansion")) { return; }
```

**KEEP:**
```java
if (entityId.contains("ribbits")) { return; }
if (entityId.contains("kobolds")) { return; }
if (entityId.contains("born_in_chaos")) { return; }
```

**Step 3: Clean ModdedMobWhitelistData.java**

Remove these from the constructor's `controlledMods` set (lines 26-37):
```java
// REMOVE:
controlledMods.add("mowziesmobs");
controlledMods.add("doom");
controlledMods.add("mobs_of_mythology");
controlledMods.add("luminousworld");
controlledMods.add("undead_revamp2");
controlledMods.add("rottencreatures");
controlledMods.add("shineals_prehistoric_expansion");
controlledMods.add("reptilian");
```

**KEEP:**
```java
controlledMods.add("kobolds");
controlledMods.add("ribbits");
controlledMods.add("born_in_chaos_v1");
```

**Step 4: Delete MowziesMobsIntegration.java**

Find and delete the MowziesMobs integration class:
```bash
find src -name "MowziesMobsIntegration.java" -type f
```
Delete it if found.

**Step 5: Verify build**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL

**Step 6: Commit**

```bash
git add -A
git commit -m "Remove 8 dropped creature mod references from Java code"
```

---

### Task 3: Remove dropped creature mod references from entity tags

**Files:**
- Modify: `src/main/resources/data/adastramekanized/tags/entity_type/lives_without_oxygen.json`
- Modify: `src/main/resources/data/adastramekanized/tags/entity_types/can_survive_in_space.json`

**Step 1: Clean lives_without_oxygen.json**

Remove all entries for dropped mods. Keep vanilla entries, kobold entries, and ribbit entry.

Remove:
- Mowzies Mobs entries (lines 34-43): foliaath variants, umvuthana variants, grottol, bluff, lantern, naga, frostmaw
- Doom entries (lines 45-60): all 16 demon types
- Reptilian entries (lines 61-67): gecko, chameleon, alligator, caiman, crocodile, komodo_dragon, giant_tortoise
- Luminous World entries (lines 68-71): glowbug, luminescent_zombie, radiant_spider, shimmer_creeper

Keep:
- All vanilla undead, nether/end mobs, water mobs, constructs
- All kobold entries (8 variants)
- Ribbit entry

Add Born in Chaos entries (currently missing - these mobs need oxygen immunity since they spawn on airless planets):
```json
{"id": "born_in_chaos_v1:decrepit_skeleton", "required": false},
{"id": "born_in_chaos_v1:bone_imp", "required": false},
{"id": "born_in_chaos_v1:decaying_zombie", "required": false},
{"id": "born_in_chaos_v1:skeleton_thrasher", "required": false},
{"id": "born_in_chaos_v1:corpse_fish", "required": false},
{"id": "born_in_chaos_v1:zombie_fisherman", "required": false},
{"id": "born_in_chaos_v1:dread_hound", "required": false},
{"id": "born_in_chaos_v1:baby_spider", "required": false},
{"id": "born_in_chaos_v1:bonescaller", "required": false},
{"id": "born_in_chaos_v1:restless_spirit", "required": false},
{"id": "born_in_chaos_v1:seared_spirit", "required": false},
{"id": "born_in_chaos_v1:firelight", "required": false},
{"id": "born_in_chaos_v1:door_knight", "required": false},
{"id": "born_in_chaos_v1:barrel_zombie", "required": false},
{"id": "born_in_chaos_v1:supreme_bonescaller", "required": false},
{"id": "born_in_chaos_v1:lifestealer", "required": false},
{"id": "born_in_chaos_v1:krampus", "required": false},
{"id": "born_in_chaos_v1:missioner", "required": false},
{"id": "born_in_chaos_v1:pumpkin_dunce", "required": false},
{"id": "born_in_chaos_v1:dunce_pumpkin", "required": false},
{"id": "born_in_chaos_v1:pumpkin_spirit", "required": false},
{"id": "born_in_chaos_v1:mother_spider", "required": false}
```

> **NOTE:** Use `"required": false` for all BiC entries so the game doesn't crash if BiC isn't installed.

**Step 2: Clean can_survive_in_space.json**

Remove the entire `"optional"` array containing 11 Mowzies Mobs entries. Keep only:
```json
{
  "replace": false,
  "values": [
    "#adastramekanized:lives_without_oxygen"
  ]
}
```

**Step 3: Verify build**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL

**Step 4: Commit**

```bash
git add src/main/resources/data/adastramekanized/tags/
git commit -m "Clean entity tags: remove dropped mods, add BiC oxygen immunity"
```

---

### Task 4: Remove dropped creature mod helper methods from PlanetMaker.java

**Files:**
- Modify: `src/main/java/com/hecookin/adastramekanized/common/planets/PlanetMaker.java`

**Step 1: Delete these helper methods**

Remove these methods entirely (they reference dropped mod namespaces):

| Method | Lines | Mod |
|--------|-------|-----|
| `addMowziesMobsPreset(String)` | 1678-1729 | mowziesmobs |
| `addMowziesMob(String, int, int, int)` | 1738-1741 | mowziesmobs |
| `addMCDoomPreset(String)` | 2010-2053 | doom |
| `addMCDoomDemon(String, int, int, int)` | 2062-2065 | doom |
| `addMythologyMob(String, int, int, int)` | 2090-2092 | mobs_of_mythology |
| `addLuminousMob(String, int, int, int)` | 2102-2104 | luminousworld |
| `addUndeadRevampMob(String, int, int, int)` | 2114-2116 | undead_revamp2 |
| `addRottenCreature(String, int, int, int)` | 2126-2128 | rottencreatures |
| `addPrehistoricCreature(String, int, int, int)` | 2138-2140 | shineals_prehistoric_expansion |
| `addReptilianCreature(String, int, int, int)` | 2150-2152 | reptilian |

**KEEP these methods** (BiC, Kobolds, Ribbits):
- `addBornInChaosMob` (line 1830)
- `addBornInChaosSpirits` (line 1839)
- `addBornInChaosUndead` (line 1855)
- `addBornInChaosPumpkin` (line 1876)
- `addBornInChaosClowns` (line 1893)
- `addBornInChaosCreatures` (line 1903)
- `addBornInChaosAquatic` (line 1923)
- `addBornInChaosBosses` (line 1935)
- `addBornInChaosPreset` (line 1947)
- `addKoboldsMobs` (line 1763)
- `addHostileKobolds` (line 1775)
- `addRibbitsMobs` (line 1802)

**Step 2: Verify build**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL

**Step 3: Commit**

```bash
git add src/main/java/com/hecookin/adastramekanized/common/planets/PlanetMaker.java
git commit -m "Remove helper methods for 8 dropped creature mods from PlanetMaker"
```

---

### Task 5: Add config toggles for BiC/Kobolds/Ribbits spawn restriction

**Files:**
- Modify: `src/main/java/com/hecookin/adastramekanized/config/AdAstraMekanizedConfig.java`
- Modify: `src/main/java/com/hecookin/adastramekanized/common/events/ModdedMobSpawnController.java`

**Step 1: Add config fields to AdAstraMekanizedConfig.java**

Add 3 new boolean config fields inside the `CommonConfig` class. Place them after the existing mod integration settings (after line 78):

```java
// Creature Mod Spawn Control
public final ModConfigSpec.BooleanValue restrictBornInChaosSpawns;
public final ModConfigSpec.BooleanValue restrictKoboldsSpawns;
public final ModConfigSpec.BooleanValue restrictRibbitsSpawns;
```

In the `CommonConfig()` constructor, add a new section (after the Mod Integration Settings push/pop block):

```java
builder.push("Creature Mod Spawn Control");
builder.comment("Controls whether creature mod spawns are restricted to designated planets only.",
                "When ON (default): Mobs only spawn on planets they are assigned to.",
                "When OFF: Mobs spawn normally as if no spawn control exists (vanilla behavior).");

restrictBornInChaosSpawns = builder
    .comment("Restrict Born in Chaos mob spawns to designated planets only")
    .translation("adastramekanized.config.restrictBornInChaosSpawns")
    .define("restrictBornInChaosSpawns", true);

restrictKoboldsSpawns = builder
    .comment("Restrict Kobolds mob spawns to designated planets only")
    .translation("adastramekanized.config.restrictKoboldsSpawns")
    .define("restrictKoboldsSpawns", true);

restrictRibbitsSpawns = builder
    .comment("Restrict Ribbits mob spawns to designated planets only")
    .translation("adastramekanized.config.restrictRibbitsSpawns")
    .define("restrictRibbitsSpawns", true);

builder.pop();
```

Add static accessor methods:

```java
public static boolean isRestrictBornInChaosEnabled() {
    return COMMON.restrictBornInChaosSpawns.get();
}

public static boolean isRestrictKoboldsEnabled() {
    return COMMON.restrictKoboldsSpawns.get();
}

public static boolean isRestrictRibbitsEnabled() {
    return COMMON.restrictRibbitsSpawns.get();
}
```

**Step 2: Update ModdedMobSpawnController to respect config**

In `ModdedMobSpawnController.java`, modify the `isControlledMod` check path. Before checking `whitelist.isControlledMod(modNamespace)` in both `onFinalizeSpawn` and `onEntityJoinLevel`, add a config bypass:

```java
// In both event handlers, after getting modNamespace:
if (!whitelist.isControlledMod(modNamespace)) {
    return;
}

// NEW: Check if spawn restriction is disabled for this mod via config
if (!isSpawnRestrictionEnabled(modNamespace)) {
    return; // Config says don't restrict this mod, let it spawn freely
}
```

Add the helper method:

```java
/**
 * Check if spawn restriction is enabled for a specific mod namespace.
 * When disabled, the mod's spawns are not controlled (vanilla behavior).
 */
private static boolean isSpawnRestrictionEnabled(String modNamespace) {
    return switch (modNamespace) {
        case "born_in_chaos_v1" -> AdAstraMekanizedConfig.isRestrictBornInChaosEnabled();
        case "kobolds" -> AdAstraMekanizedConfig.isRestrictKoboldsEnabled();
        case "ribbits" -> AdAstraMekanizedConfig.isRestrictRibbitsEnabled();
        default -> true; // Unknown mods are always restricted
    };
}
```

**Step 3: Verify build**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL

**Step 4: Commit**

```bash
git add src/main/java/com/hecookin/adastramekanized/config/AdAstraMekanizedConfig.java
git add src/main/java/com/hecookin/adastramekanized/common/events/ModdedMobSpawnController.java
git commit -m "Add config toggles for BiC/Kobolds/Ribbits spawn restriction"
```

---

### Task 6: Rewrite all 31 planet spawn configs in PlanetGenerationRunner

**Files:**
- Modify: `src/main/java/com/hecookin/adastramekanized/common/planets/PlanetGenerationRunner.java`

This is the largest task. Replace ALL existing mob spawn code for every planet with the approved v2 assignments.

**Step 1: Rewrite Barren tier planets**

**Earth Orbit** (~line 370): Keep `.disableMobGeneration(true)` unchanged.

**Eris** (~line 1099): Replace entire spawn section with:
```java
.allowPeacefulMobs(false)
.addMobSpawn("monster", "minecraft:endermite", 3, 1, 1)
```

**Pluto** (~line 1050): Replace entire spawn section with:
```java
.allowPeacefulMobs(false)
.addMobSpawn("monster", "minecraft:endermite", 3, 1, 1)
.addMobSpawn("monster", "minecraft:ghast", 1, 1, 1)
```

**Mercury** (~line 561): Replace entire spawn section with:
```java
.allowPeacefulMobs(false)
.addMobSpawn("monster", "minecraft:shulker", 3, 1, 1)
.addMobSpawn("monster", "minecraft:endermite", 3, 1, 2)
```

**Step 2: Rewrite Sparse tier planets**

**Moon** (~line 300): Replace spawn section with:
```java
.allowPeacefulMobs(false)
.addModReplacementSpawns("kobolds",
    // Vanilla fallback spawns (when Kobolds not installed)
    new String[]{
        "monster/minecraft:silverfish/10/2/4",
        "monster/minecraft:endermite/5/1/2"
    },
    // Kobolds replacement spawns (when Kobolds installed)
    new String[]{
        "monster/kobolds:kobold/5/1/2",
        "monster/kobolds:kobold_warrior/3/1/1",
        "monster/kobolds:kobold_enchanter/2/1/1",
        "monster/kobolds:kobold_engineer/3/1/1",
        "monster/kobolds:kobold_zombie/2/1/1",
        "monster/kobolds:kobold_skeleton/2/1/1",
        "monster/kobolds:witherbold/1/1/1"
    })
```

**Callisto** (~line 784): Replace spawn section with:
```java
.allowPeacefulMobs(false)
.addModReplacementSpawns("kobolds",
    new String[]{
        "monster/minecraft:silverfish/8/2/3",
        "monster/minecraft:endermite/5/1/2",
        "monster/minecraft:ghast/1/1/1"
    },
    new String[]{
        "monster/kobolds:kobold/4/1/2",
        "monster/kobolds:kobold_warrior/3/1/1",
        "monster/kobolds:kobold_enchanter/2/1/1",
        "monster/kobolds:kobold_engineer/3/1/1",
        "monster/kobolds:kobold_zombie/2/1/1",
        "monster/kobolds:kobold_skeleton/2/1/1",
        "monster/kobolds:witherbold/1/1/1",
        "monster/minecraft:ghast/1/1/1"
    })
```

**Ceres** (~line 996): Replace spawn section with:
```java
.allowPeacefulMobs(false)
.addModReplacementSpawns("kobolds",
    new String[]{
        "monster/minecraft:silverfish/10/2/4",
        "monster/minecraft:endermite/5/1/2"
    },
    new String[]{
        "monster/kobolds:kobold/5/1/2",
        "monster/kobolds:kobold_warrior/3/1/1",
        "monster/kobolds:kobold_enchanter/2/1/1",
        "monster/kobolds:kobold_engineer/3/1/1",
        "monster/kobolds:kobold_zombie/2/1/1",
        "monster/kobolds:kobold_skeleton/2/1/1",
        "monster/kobolds:witherbold/1/1/1"
    })
```

**Enceladus** (~line 891): Replace spawn section with:
```java
.allowPeacefulMobs(false)
.addMobSpawn("monster", "minecraft:stray", 10, 1, 2)
.addMobSpawn("monster", "minecraft:ghast", 1, 1, 1)
```

**Triton** (~line 942): Replace spawn section with:
```java
.allowPeacefulMobs(false)
.addMobSpawn("monster", "minecraft:stray", 8, 1, 2)
.addMobSpawn("monster", "minecraft:endermite", 5, 1, 2)
.addMobSpawn("monster", "minecraft:ghast", 1, 1, 1)
```

**Ganymede** (~line 735): Replace spawn section with:
```java
.allowPeacefulMobs(false)
.addMobSpawn("monster", "minecraft:stray", 10, 1, 2)
.addMobSpawn("monster", "minecraft:enderman", 3, 1, 1)
.addMobSpawn("monster", "minecraft:ghast", 1, 1, 1)
```

**Titan** (~line 838): Replace spawn section with:
```java
.allowPeacefulMobs(false)
.addMobSpawn("monster", "minecraft:slime", 10, 1, 3)
.addMobSpawn("monster", "minecraft:witch", 6, 1, 1)
```

**Step 3: Rewrite Moderate tier planets**

**Mars** (~line 432): Replace spawn section with:
```java
.allowPeacefulMobs(false)
.addMobSpawn("monster", "minecraft:enderman", 4, 1, 1)
.addMobSpawn("monster", "minecraft:ghast", 1, 1, 1)
.addModReplacementSpawns("born_in_chaos_v1",
    new String[]{
        "monster/minecraft:husk/12/1/2"
    },
    new String[]{
        "monster/born_in_chaos_v1:decrepit_skeleton/8/1/2",
        "monster/born_in_chaos_v1:bone_imp/5/1/1"
    })
```

**Venus** (~line 506): Replace spawn section with:
```java
.allowPeacefulMobs(false)
.addMobSpawn("monster", "minecraft:witch", 6, 1, 1)
.addMobSpawn("monster", "minecraft:slime", 6, 1, 2)
.addModReplacementSpawns("born_in_chaos_v1",
    new String[]{
        "monster/minecraft:husk/10/1/2"
    },
    new String[]{
        "monster/born_in_chaos_v1:decaying_zombie/8/1/2",
        "monster/born_in_chaos_v1:skeleton_thrasher/5/1/1"
    })
```

**Europa** (~line 618): Replace spawn section with:
```java
.allowPeacefulMobs(false)
.addMobSpawn("monster", "minecraft:guardian", 6, 1, 2)
.addModReplacementSpawns("born_in_chaos_v1",
    new String[]{
        "monster/minecraft:drowned/12/1/2"
    },
    new String[]{
        "monster/born_in_chaos_v1:corpse_fish/8/2/3",
        "monster/born_in_chaos_v1:zombie_fisherman/5/1/1"
    })
```

**Io** (~line 675): Replace spawn section with:
```java
.allowPeacefulMobs(false)
.addMobSpawn("monster", "minecraft:magma_cube", 12, 1, 3)
.addMobSpawn("monster", "minecraft:zombified_piglin", 8, 1, 2)
.addMobSpawn("monster", "minecraft:ghast", 2, 1, 1)
```

**Frigidum** (~line 1499): Replace spawn section with:
```java
.allowPeacefulMobs(false)
.addMobSpawn("monster", "minecraft:enderman", 3, 1, 1)
.addMobSpawn("monster", "minecraft:ghast", 1, 1, 1)
.addModReplacementSpawns("born_in_chaos_v1",
    new String[]{
        "monster/minecraft:stray/12/1/2"
    },
    new String[]{
        "monster/born_in_chaos_v1:dread_hound/6/1/2",
        "monster/born_in_chaos_v1:baby_spider/4/1/2"
    })
```

**Arenos** (~line 1558): Replace spawn section with:
```java
.allowPeacefulMobs(false)
.addMobSpawn("monster", "minecraft:enderman", 4, 1, 1)
.addMobSpawn("monster", "minecraft:ghast", 1, 1, 1)
.addModReplacementSpawns("born_in_chaos_v1",
    new String[]{
        "monster/minecraft:husk/15/1/3"
    },
    new String[]{
        "monster/born_in_chaos_v1:bonescaller/5/1/1",
        "monster/minecraft:husk/10/1/2"
    })
```

**Glacio** (~line 1720): Replace spawn section with:
```java
.allowPeacefulMobs(false)
.addMobSpawn("monster", "minecraft:enderman", 3, 1, 1)
.addModReplacementSpawns("kobolds",
    new String[]{
        "monster/minecraft:stray/10/1/2"
    },
    new String[]{
        "monster/kobolds:kobold/4/1/2",
        "monster/kobolds:kobold_warrior/3/1/1",
        "monster/kobolds:kobold_enchanter/2/1/1",
        "monster/kobolds:kobold_engineer/3/1/1",
        "monster/kobolds:kobold_zombie/2/1/1",
        "monster/kobolds:kobold_skeleton/2/1/1",
        "monster/kobolds:witherbold/1/1/1"
    })
```

**Luxoria** (~line 1662): Replace spawn section with:
```java
.allowPeacefulMobs(false)
.addMobSpawn("monster", "minecraft:enderman", 5, 1, 1)
.addMobSpawn("monster", "minecraft:shulker", 3, 1, 1)
.addModReplacementSpawns("born_in_chaos_v1",
    new String[]{},
    new String[]{
        "monster/born_in_chaos_v1:restless_spirit/5/1/1",
        "monster/born_in_chaos_v1:seared_spirit/4/1/1",
        "monster/born_in_chaos_v1:firelight/3/1/1"
    })
```

> **NOTE on Luxoria:** BiC spirits are additive (no vanilla mobs to replace). Verify `addModReplacementSpawns` supports empty vanilla array. If not, use individual `addBornInChaosMob()` calls instead.

**Proxima B** (~line 1275): Replace spawn section with:
```java
.allowPeacefulMobs(false)
.addMobSpawn("monster", "minecraft:enderman", 4, 1, 1)
.addModReplacementSpawns("born_in_chaos_v1",
    new String[]{
        "monster/minecraft:husk/12/1/2"
    },
    new String[]{
        "monster/born_in_chaos_v1:decaying_zombie/6/1/2",
        "monster/born_in_chaos_v1:bonescaller/4/1/1"
    })
```

**Gliese667c** (~line 1387): Replace spawn section with:
```java
.allowPeacefulMobs(false)
.addMobSpawn("monster", "minecraft:enderman", 5, 1, 1)
.addMobSpawn("monster", "minecraft:shulker", 3, 1, 1)
.addModReplacementSpawns("born_in_chaos_v1",
    new String[]{},
    new String[]{
        "monster/born_in_chaos_v1:supreme_bonescaller/3/1/1",
        "monster/born_in_chaos_v1:lifestealer/3/1/1",
        "monster/born_in_chaos_v1:krampus/2/1/1",
        "monster/born_in_chaos_v1:missioner/3/1/1"
    })
```

**Step 4: Rewrite Active tier planets**

**Vulcan** (~line 1776): Replace spawn section with:
```java
.allowPeacefulMobs(false)
.addMobSpawn("monster", "minecraft:wither_skeleton", 12, 1, 2)
.addMobSpawn("monster", "minecraft:magma_cube", 10, 1, 3)
.addMobSpawn("monster", "minecraft:ghast", 2, 1, 1)
.addModReplacementSpawns("born_in_chaos_v1",
    new String[]{},
    new String[]{
        "monster/born_in_chaos_v1:door_knight/5/1/1",
        "monster/born_in_chaos_v1:skeleton_thrasher/6/1/1"
    })
```

**Pyrios** (~line 1446): Replace spawn section with:
```java
.allowPeacefulMobs(false)
.addMobSpawn("monster", "minecraft:blaze", 10, 1, 2)
.addMobSpawn("monster", "minecraft:magma_cube", 12, 1, 3)
.addMobSpawn("monster", "minecraft:ghast", 2, 1, 1)
.addModReplacementSpawns("born_in_chaos_v1",
    new String[]{},
    new String[]{
        "monster/born_in_chaos_v1:seared_spirit/8/1/1",
        "monster/born_in_chaos_v1:barrel_zombie/6/1/1"
    })
```

**Bellator** (~line 1956): Replace spawn section with:
```java
.allowPeacefulMobs(false)
.addMobSpawn("monster", "minecraft:husk", 12, 1, 2)
.addMobSpawn("monster", "minecraft:enderman", 5, 1, 1)
.addMobSpawn("monster", "minecraft:piglin", 4, 1, 2)
.addMobSpawn("monster", "minecraft:ghast", 1, 1, 1)
.addModReplacementSpawns("born_in_chaos_v1",
    new String[]{},
    new String[]{
        "monster/born_in_chaos_v1:pumpkin_dunce/4/1/1",
        "monster/born_in_chaos_v1:dunce_pumpkin/4/1/1",
        "monster/born_in_chaos_v1:pumpkin_spirit/3/1/1"
    })
```

**Primordium** (~line 1898): Replace spawn section with:
```java
.allowPeacefulMobs(false)
.addMobSpawn("monster", "minecraft:spider", 10, 1, 2)
.addMobSpawn("monster", "minecraft:cave_spider", 8, 1, 2)
.addMobSpawn("monster", "minecraft:enderman", 4, 1, 1)
.addMobSpawn("monster", "minecraft:shulker", 3, 1, 1)
.addModReplacementSpawns("born_in_chaos_v1",
    new String[]{},
    new String[]{
        "monster/born_in_chaos_v1:baby_spider/5/1/2",
        "monster/born_in_chaos_v1:mother_spider/3/1/1",
        "monster/born_in_chaos_v1:dread_hound/4/1/1"
    })
```

**Profundus** (~line 2014): Replace spawn section with:
```java
.allowPeacefulMobs(false)
.addMobSpawn("monster", "minecraft:silverfish", 4, 1, 2)
.addModReplacementSpawns("kobolds",
    new String[]{
        "monster/minecraft:cave_spider/6/1/2"
    },
    new String[]{
        "monster/kobolds:kobold/10/2/4",
        "monster/kobolds:kobold_warrior/8/1/2",
        "monster/kobolds:kobold_enchanter/5/1/1",
        "monster/kobolds:kobold_engineer/8/1/2",
        "monster/kobolds:kobold_zombie/6/2/3",
        "monster/kobolds:kobold_skeleton/6/2/3",
        "monster/kobolds:witherbold/3/1/1"
    })
```

**Step 5: Rewrite Habitable tier planets**

**Terra Nova** (~line 1829): Replace spawn section with:
```java
.addMobSpawn("creature", "minecraft:goat", 4, 1, 2)
.addMobSpawn("creature", "minecraft:llama", 3, 1, 2)
.addMobSpawn("monster", "minecraft:enderman", 3, 1, 1)
.addMobSpawn("monster", "minecraft:spider", 4, 1, 1)
.addRibbitsMobs(15)
```

**Paludis** (~line 1611): Replace spawn section with:
```java
.addMobSpawn("creature", "minecraft:frog", 6, 1, 3)
.addMobSpawn("monster", "minecraft:drowned", 5, 1, 2)
.addMobSpawn("monster", "minecraft:slime", 6, 1, 2)
.addRibbitsMobs(12)
.addModReplacementSpawns("born_in_chaos_v1",
    new String[]{},
    new String[]{
        "monster/born_in_chaos_v1:corpse_fish/6/1/2"
    })
```

**Kepler22b** (~line 1149): Replace spawn section with:
```java
.addMobSpawn("water_ambient", "minecraft:tropical_fish", 4, 2, 4)
.addMobSpawn("monster", "minecraft:drowned", 5, 1, 2)
.addRibbitsMobs(10)
.addModReplacementSpawns("born_in_chaos_v1",
    new String[]{},
    new String[]{
        "monster/born_in_chaos_v1:corpse_fish/5/1/2"
    })
```

**Kepler442b** (~line 1209): Replace spawn section with:
```java
.addMobSpawn("creature", "minecraft:goat", 3, 1, 2)
.addMobSpawn("creature", "minecraft:llama", 3, 1, 2)
.addMobSpawn("monster", "minecraft:spider", 5, 1, 1)
.addRibbitsMobs(10)
.addModReplacementSpawns("born_in_chaos_v1",
    new String[]{},
    new String[]{
        "monster/born_in_chaos_v1:baby_spider/4/1/2"
    })
```

**Trappist1e** (~line 1325): Replace spawn section with:
```java
.addMobSpawn("creature", "minecraft:frog", 4, 1, 2)
.addMobSpawn("creature", "minecraft:goat", 3, 1, 2)
.addMobSpawn("monster", "minecraft:enderman", 2, 1, 1)
.addRibbitsMobs(10)
```

**Step 6: Verify build**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL

**Step 7: Commit**

```bash
git add src/main/java/com/hecookin/adastramekanized/common/planets/PlanetGenerationRunner.java
git commit -m "Rewrite all 31 planet spawn configs with 5-tier balanced system"
```

---

### Task 7: Regenerate planet JSON files and verify

**Step 1: Run planet generation**

```bash
./gradlew makePlanets
```

This regenerates all biome modifier JSONs from the updated PlanetGenerationRunner.

**Step 2: Verify generated biome modifier files**

Check that old mod biome modifiers are gone:
```bash
# Should find NO files referencing dropped mods:
grep -r "mowziesmobs\|doom\|reptilian\|luminousworld\|undead_revamp2\|rottencreatures\|shineals_prehistoric_expansion\|mobs_of_mythology" src/main/resources/data/adastramekanized/neoforge/biome_modifier/
```
Expected: No results

Check that new files reference only kept mods:
```bash
# Should only find born_in_chaos_v1, kobolds, ribbits references:
grep -r "born_in_chaos_v1\|kobolds\|ribbits" src/main/resources/data/adastramekanized/neoforge/biome_modifier/ | head -20
```

**Step 3: Delete any stale biome modifier files**

If makePlanets doesn't clean up old files, manually delete biome modifier JSONs that reference dropped mods:
```bash
find src/main/resources/data/adastramekanized/neoforge/biome_modifier/ -name "*doom*" -o -name "*mowziesmobs*" -o -name "*reptilian*" -o -name "*luminousworld*" -o -name "*undead_revamp*" -o -name "*rottencreatures*" -o -name "*prehistoric*" -o -name "*mythology*" | xargs rm -f
```

**Step 4: Final build verification**

Run: `./gradlew clean build`
Expected: BUILD SUCCESSFUL

**Step 5: Commit**

```bash
git add -A
git commit -m "Regenerate planet biome modifiers with new spawn configs"
```

---

### Task 8: Final verification and cleanup

**Step 1: Full codebase search for dropped mod references**

Search for any remaining references to dropped mods across the entire codebase:
```bash
grep -r "mowziesmobs\|MowziesMobs" src/ --include="*.java" --include="*.json"
grep -r "doom" src/ --include="*.java" --include="*.json"  # Be careful of false positives
grep -r "reptilian" src/ --include="*.java" --include="*.json"
grep -r "luminousworld\|lumination" src/ --include="*.java" --include="*.json"
grep -r "undead_revamp2" src/ --include="*.java" --include="*.json"
grep -r "rottencreatures" src/ --include="*.java" --include="*.json"
grep -r "shineals_prehistoric_expansion" src/ --include="*.java" --include="*.json"
grep -r "mobs_of_mythology" src/ --include="*.java" --include="*.json"
```

Fix any remaining references found.

**Step 2: Verify no phantoms, creepers, bees, or farm animals in planet configs**

```bash
grep -n "phantom\|creeper\|bee\|cow\|pig\|sheep\|chicken\|horse" src/main/java/com/hecookin/adastramekanized/common/planets/PlanetGenerationRunner.java
```

Expected: No matches (or only in comments/non-spawn contexts).

**Step 3: Verify skeleton+zombie and blaze+wither_skeleton never paired**

Manual review: Check that no planet has both `skeleton` and `zombie` spawns, and no planet has both `blaze` and `wither_skeleton` spawns.

**Step 4: Clean build and data generation**

```bash
./gradlew clean build
./gradlew runData
```

Expected: Both succeed without errors.

**Step 5: Commit any final fixes**

```bash
git add -A
git commit -m "Final cleanup: remove all remaining dropped creature mod references"
```

---

## Execution Notes

### Order of Operations
Tasks MUST be executed in order (1 through 8). Each depends on the previous.

### Key Risk: Empty Vanilla Array in addModReplacementSpawns
Several planets (Luxoria, Vulcan, Pyrios, Bellator, Primordium, Paludis, Kepler22b, Kepler442b) use `addModReplacementSpawns` with an empty vanilla array (BiC mobs are additive, not replacing vanilla mobs). **Verify** that `addModReplacementSpawns` handles empty vanilla arrays correctly. If it doesn't, use individual `addBornInChaosMob()` calls for those planets instead.

### Key Risk: Shared Library Dependencies
Before removing geckolib, smartbrainlib, azurelib, and platform from build.gradle, **verify** they are not required by Kobolds, Ribbits, or Born in Chaos. Check each mod's dependency chain.

### Config Behavior
- **restrictBornInChaosSpawns=true** (default): BiC mobs only spawn on designated planets
- **restrictBornInChaosSpawns=false**: BiC mobs spawn normally everywhere (vanilla behavior)
- Same for Kobolds and Ribbits configs
