# Space Mob Spawning System Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Implement space-adapted mob immunity, conditional modded-mob replacement, and rebalanced spawn weights across all 31 planets.

**Architecture:** Three systems: (1) `FinalizeSpawnEvent` tags naturally-spawned mobs with `SpaceAdapted` persistent data; `OxygenDamageHandler` checks this tag. (2) `PlanetMaker` generates conditional biome modifier pairs (vanilla-when-mod-absent / modded-when-mod-present). (3) All spawns move exclusively to biome modifiers; biome JSON spawner sections are emptied.

**Tech Stack:** NeoForge 1.21.1, Java 21, Gradle with custom `makePlanets` task.

**Design Doc:** `docs/plans/2026-02-12-space-mob-spawning-design.md`

---

### Task 1: Add SpaceAdapted Check to OxygenDamageHandler

**Files:**
- Modify: `src/main/java/com/hecookin/adastramekanized/common/events/OxygenDamageHandler.java`

**Step 1: Add SpaceAdapted persistent data check**

Insert after line 55 (after the mod namespace checks, before the player check), add:

```java
        // Check if entity was tagged as space-adapted during natural spawning
        if (living.getPersistentData().getBoolean("SpaceAdapted")) {
            return; // Naturally spawned on this planet - immune to oxygen damage
        }
```

**Step 2: Add new mod namespaces to exempt list**

Add these checks inside the `if (entityId != null)` block (after the existing `born_in_chaos` check at line 53):

```java
            if (entityId.contains("mobs_of_mythology")) {
                return; // Mythology mobs are exempt - mythical beings
            }
            if (entityId.contains("rottencreatures")) {
                return; // Rotten Creatures are exempt - undead adapted to harsh environments
            }
            if (entityId.contains("shineals_prehistoric_expansion")) {
                return; // Prehistoric creatures are exempt - ancient adaptations
            }
```

Note: `luminousworld` and `undead_revamp2` are already exempt (lines 49 and 34).

**Step 3: Verify compilation**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL

**Step 4: Commit**

```bash
git add src/main/java/com/hecookin/adastramekanized/common/events/OxygenDamageHandler.java
git commit -m "feat: add SpaceAdapted tag check and new mod exemptions to OxygenDamageHandler"
```

---

### Task 2: Add SpaceAdapted Tagging via FinalizeSpawnEvent

**Files:**
- Modify: `src/main/java/com/hecookin/adastramekanized/common/events/PlanetMobSpawnHandler.java`

This handler already exists and uses `EntityJoinLevelEvent` for equipment. We add a second event handler using `FinalizeSpawnEvent` to tag naturally-spawned mobs on non-breathable planets.

**Step 1: Add imports**

Add these imports to `PlanetMobSpawnHandler.java` (after existing imports):

```java
import net.minecraft.world.entity.MobSpawnType;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import net.neoforged.bus.api.EventPriority;
import com.hecookin.adastramekanized.common.atmosphere.OxygenManager;
```

**Step 2: Add FinalizeSpawnEvent handler**

Add this method to the class (before the existing `onMobSpawn` method, around line 55):

```java
    /**
     * Tags naturally-spawned mobs on non-breathable planets with SpaceAdapted flag.
     * This makes them immune to oxygen damage (checked by OxygenDamageHandler).
     * Mobs from spawn eggs, commands, or spawners do NOT get this tag.
     */
    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void onFinalizeSpawn(FinalizeSpawnEvent event) {
        Mob mob = event.getEntity();
        Level level = event.getLevel().getLevel();

        // Only process in our planetary dimensions
        if (!level.dimension().location().getNamespace().equals(AdAstraMekanized.MOD_ID)) {
            return;
        }

        // Only tag natural spawns, not manual spawns (spawn eggs, commands, spawners)
        if (isManualSpawn(event.getSpawnType())) {
            return;
        }

        // Check if this planet has a non-breathable atmosphere
        if (!OxygenManager.getInstance().isDimensionBreathable(level.dimension())) {
            mob.getPersistentData().putBoolean("SpaceAdapted", true);
        }
    }

    /**
     * Check if a spawn type is manual (player-initiated).
     * Mirrors ModdedMobSpawnController.isManualSpawn() logic.
     */
    private static boolean isManualSpawn(MobSpawnType spawnType) {
        return spawnType == MobSpawnType.SPAWN_EGG ||
               spawnType == MobSpawnType.SPAWNER ||
               spawnType == MobSpawnType.TRIAL_SPAWNER ||
               spawnType == MobSpawnType.COMMAND ||
               spawnType == MobSpawnType.DISPENSER ||
               spawnType == MobSpawnType.BUCKET ||
               spawnType == MobSpawnType.BREEDING ||
               spawnType == MobSpawnType.MOB_SUMMONED ||
               spawnType == MobSpawnType.CONVERSION;
    }
```

**Step 3: Verify OxygenManager has `isDimensionBreathable()` method**

Check `src/main/java/com/hecookin/adastramekanized/common/atmosphere/OxygenManager.java` for a method that determines if a dimension is breathable. If it doesn't exist, add one:

```java
    /**
     * Check if a dimension has a breathable atmosphere.
     * Used by PlanetMobSpawnHandler to tag space-adapted mobs.
     */
    public boolean isDimensionBreathable(ResourceKey<Level> dimension) {
        // Non-mod dimensions (overworld, nether, end) are considered breathable
        if (!dimension.location().getNamespace().equals(AdAstraMekanized.MOD_ID)) {
            return true;
        }
        // Check planet registry for atmosphere breathability
        var builder = PlanetGenerationRunner.getAllPlanetBuilders().get(dimension.location().getPath());
        if (builder != null) {
            return builder.isBreathable();
        }
        return true; // Default to breathable for unknown dimensions
    }
```

If `PlanetBuilder.isBreathable()` doesn't exist, check for `hasAtmosphere` field access or add a getter. The key logic is: planets with `hasAtmosphere(false)` or those where `isBreathable` is false should return false.

**Step 4: Verify compilation**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL

**Step 5: Commit**

```bash
git add src/main/java/com/hecookin/adastramekanized/common/events/PlanetMobSpawnHandler.java
git add src/main/java/com/hecookin/adastramekanized/common/atmosphere/OxygenManager.java
git commit -m "feat: tag naturally-spawned mobs with SpaceAdapted on non-breathable planets"
```

---

### Task 3: Add Replacement Spawns API to PlanetMaker

**Files:**
- Modify: `src/main/java/com/hecookin/adastramekanized/common/planets/PlanetMaker.java`

This is the largest task. We need to:
1. Add data structures for replacement spawn groups
2. Add `addModReplacementSpawns()` API method
3. Modify `generateBiomeModifier()` to output conditional files
4. Modify `createCustomBiome()` to empty spawner sections

**Step 1: Add replacement data structure**

Add new field in `PlanetBuilder` (near line 567 where other mob fields are):

```java
        // Mod replacement groups: modId -> (vanillaFallbacks, moddedReplacements)
        // When mod IS installed: moddedReplacements spawn. When NOT: vanillaFallbacks spawn.
        private java.util.Map<String, ModReplacementGroup> modReplacementGroups = new java.util.LinkedHashMap<>();
```

Add inner class (near `MobSpawnEntry` class around line 3992):

```java
    /**
     * A group of vanilla mobs and their modded replacements.
     * When the mod IS installed, only moddedReplacements spawn.
     * When the mod is NOT installed, only vanillaFallbacks spawn.
     */
    static class ModReplacementGroup {
        final String modId;
        final java.util.Map<String, java.util.List<MobSpawnEntry>> vanillaFallbacks = new java.util.LinkedHashMap<>();
        final java.util.Map<String, java.util.List<MobSpawnEntry>> moddedReplacements = new java.util.LinkedHashMap<>();

        ModReplacementGroup(String modId) {
            this.modId = modId;
        }

        void addVanillaFallback(String category, MobSpawnEntry entry) {
            vanillaFallbacks.computeIfAbsent(category, k -> new java.util.ArrayList<>()).add(entry);
        }

        void addModdedReplacement(String category, MobSpawnEntry entry) {
            moddedReplacements.computeIfAbsent(category, k -> new java.util.ArrayList<>()).add(entry);
        }
    }
```

**Step 2: Add `addModReplacementSpawns()` method**

Add after the existing `addMobSpawn()` method (around line 1502):

```java
    /**
     * Add mob spawns with conditional modded replacement.
     * When the specified mod IS installed, moddedSpawns are used instead of vanillaSpawns.
     * When the mod is NOT installed, vanillaSpawns are used as fallback.
     *
     * Each String[] is: {category, mobId, weight, minGroup, maxGroup}
     * Example: {"monster", "minecraft:silverfish", "30", "2", "4"}
     */
    public PlanetBuilder addModReplacementSpawns(String modId, String[][] vanillaSpawns, String[][] moddedSpawns) {
        ModReplacementGroup group = modReplacementGroups.computeIfAbsent(modId, ModReplacementGroup::new);

        for (String[] spawn : vanillaSpawns) {
            group.addVanillaFallback(spawn[0],
                new MobSpawnEntry(spawn[1], Integer.parseInt(spawn[2]),
                    Integer.parseInt(spawn[3]), Integer.parseInt(spawn[4])));
        }

        for (String[] spawn : moddedSpawns) {
            group.addModdedReplacement(spawn[0],
                new MobSpawnEntry(spawn[1], Integer.parseInt(spawn[2]),
                    Integer.parseInt(spawn[3]), Integer.parseInt(spawn[4])));
            // Track mod namespace for whitelist registration
            String namespace = spawn[1].contains(":") ? spawn[1].substring(0, spawn[1].indexOf(":")) : "minecraft";
            if (!namespace.equals("minecraft")) {
                usedModNamespaces.add(namespace);
            }
        }

        return this;
    }
```

**Step 3: Modify `generateBiomeModifier()` to handle replacement groups**

Replace the existing `generateBiomeModifier()` method (lines 7486-7554) with the updated version. The key changes:

1. Unconditional vanilla mobs from `mobSpawns` → `add_spawns.json` (no condition)
2. Vanilla fallbacks from `modReplacementGroups` → `add_spawns_vanilla_no_{modid}.json` with `neoforge:not` + `neoforge:mod_loaded` condition
3. Modded replacements from `modReplacementGroups` → `add_spawns_{modid}.json` with `neoforge:mod_loaded` condition
4. Existing modded mobs from regular `mobSpawns` → merged into `add_spawns_{modid}.json`

```java
    private static void generateBiomeModifier(PlanetBuilder planet) throws IOException {
        new File(RESOURCES_PATH + "neoforge/biome_modifier/" + planet.name).mkdirs();
        String biomeTag = "#adastramekanized:" + planet.name + "_biomes";

        // === 1. Unconditional vanilla spawns (from addMobSpawn with minecraft: prefix) ===
        JsonArray unconditionalVanilla = new JsonArray();
        for (var categoryEntry : planet.mobSpawns.entrySet()) {
            for (MobSpawnEntry mob : categoryEntry.getValue()) {
                String modid = mob.mobId.contains(":") ? mob.mobId.split(":")[0] : "minecraft";
                if (modid.equals("minecraft")) {
                    JsonObject spawner = new JsonObject();
                    spawner.addProperty("type", mob.mobId);
                    spawner.addProperty("weight", mob.weight);
                    spawner.addProperty("minCount", mob.minCount);
                    spawner.addProperty("maxCount", mob.maxCount);
                    unconditionalVanilla.add(spawner);
                }
            }
        }

        if (unconditionalVanilla.size() > 0) {
            JsonObject vanillaModifier = new JsonObject();
            vanillaModifier.addProperty("type", "neoforge:add_spawns");
            vanillaModifier.addProperty("biomes", biomeTag);
            vanillaModifier.add("spawners", unconditionalVanilla);
            writeJsonFile(RESOURCES_PATH + "neoforge/biome_modifier/" + planet.name + "/add_spawns.json", vanillaModifier);
        }

        // === 2. Modded mobs from regular addMobSpawn (existing behavior) ===
        java.util.Map<String, JsonArray> moddedFromRegular = new java.util.LinkedHashMap<>();
        for (var categoryEntry : planet.mobSpawns.entrySet()) {
            for (MobSpawnEntry mob : categoryEntry.getValue()) {
                String modid = mob.mobId.contains(":") ? mob.mobId.split(":")[0] : "minecraft";
                if (!modid.equals("minecraft")) {
                    JsonObject spawner = new JsonObject();
                    spawner.addProperty("type", mob.mobId);
                    spawner.addProperty("weight", mob.weight);
                    spawner.addProperty("minCount", mob.minCount);
                    spawner.addProperty("maxCount", mob.maxCount);
                    moddedFromRegular.computeIfAbsent(modid, k -> new JsonArray()).add(spawner);
                }
            }
        }

        // === 3. Process replacement groups ===
        for (var groupEntry : planet.modReplacementGroups.entrySet()) {
            String modId = groupEntry.getKey();
            ModReplacementGroup group = groupEntry.getValue();

            // 3a. Vanilla fallbacks: spawn ONLY when mod is NOT installed
            JsonArray vanillaFallbacks = new JsonArray();
            for (var catEntry : group.vanillaFallbacks.entrySet()) {
                for (MobSpawnEntry mob : catEntry.getValue()) {
                    JsonObject spawner = new JsonObject();
                    spawner.addProperty("type", mob.mobId);
                    spawner.addProperty("weight", mob.weight);
                    spawner.addProperty("minCount", mob.minCount);
                    spawner.addProperty("maxCount", mob.maxCount);
                    vanillaFallbacks.add(spawner);
                }
            }

            if (vanillaFallbacks.size() > 0) {
                JsonObject vanillaCondModifier = new JsonObject();
                // Add NOT mod_loaded condition
                JsonArray conditions = new JsonArray();
                JsonObject notCondition = new JsonObject();
                notCondition.addProperty("type", "neoforge:not");
                JsonObject modLoadedCondition = new JsonObject();
                modLoadedCondition.addProperty("type", "neoforge:mod_loaded");
                modLoadedCondition.addProperty("modid", modId);
                notCondition.add("value", modLoadedCondition);
                conditions.add(notCondition);
                vanillaCondModifier.add("neoforge:conditions", conditions);
                vanillaCondModifier.addProperty("type", "neoforge:add_spawns");
                vanillaCondModifier.addProperty("biomes", biomeTag);
                vanillaCondModifier.add("spawners", vanillaFallbacks);
                writeJsonFile(RESOURCES_PATH + "neoforge/biome_modifier/" + planet.name + "/add_spawns_vanilla_no_" + modId + ".json", vanillaCondModifier);
            }

            // 3b. Modded replacements: spawn ONLY when mod IS installed
            // Merge with any regular modded spawns for the same namespace
            for (var catEntry : group.moddedReplacements.entrySet()) {
                for (MobSpawnEntry mob : catEntry.getValue()) {
                    String namespace = mob.mobId.contains(":") ? mob.mobId.split(":")[0] : modId;
                    JsonObject spawner = new JsonObject();
                    spawner.addProperty("type", mob.mobId);
                    spawner.addProperty("weight", mob.weight);
                    spawner.addProperty("minCount", mob.minCount);
                    spawner.addProperty("maxCount", mob.maxCount);
                    moddedFromRegular.computeIfAbsent(namespace, k -> new JsonArray()).add(spawner);
                }
            }
        }

        // === 4. Write all modded spawn files with mod_loaded conditions ===
        for (var moddedEntry : moddedFromRegular.entrySet()) {
            String modId = moddedEntry.getKey();
            JsonArray spawners = moddedEntry.getValue();

            JsonObject moddedModifier = new JsonObject();
            JsonArray conditions = new JsonArray();
            JsonObject modLoadedCondition = new JsonObject();
            modLoadedCondition.addProperty("type", "neoforge:mod_loaded");
            modLoadedCondition.addProperty("modid", modId);
            conditions.add(modLoadedCondition);
            moddedModifier.add("neoforge:conditions", conditions);
            moddedModifier.addProperty("type", "neoforge:add_spawns");
            moddedModifier.addProperty("biomes", biomeTag);
            moddedModifier.add("spawners", spawners);
            writeJsonFile(RESOURCES_PATH + "neoforge/biome_modifier/" + planet.name + "/add_spawns_" + modId + ".json", moddedModifier);
        }
    }
```

**Step 4: Modify `createCustomBiome()` to empty spawner sections**

In `createCustomBiome()` (around lines 7575-7601), replace the spawners generation loop to always output empty arrays:

Find (approximately lines 7575-7601):
```java
        // Mob spawners - only add VANILLA mobs directly to biome JSON
        // Modded mobs are handled by biome_modifier with NeoForge conditions (see generateBiomeModifier)
        JsonObject spawners = new JsonObject();
        for (String category : new String[]{"monster", "creature", "ambient", "water_creature", "water_ambient", "misc"}) {
            JsonArray categorySpawns = new JsonArray();
            if (planet.mobSpawns.containsKey(category)) {
                for (PlanetBuilder.MobSpawnEntry mob : planet.mobSpawns.get(category)) {
                    // ... adds vanilla mobs ...
                }
            }
            spawners.add(category, categorySpawns);
        }
```

Replace with:
```java
        // Mob spawners - ALL spawns now handled exclusively by biome_modifier files
        // Biome JSON spawner sections are intentionally empty to prevent duplication
        JsonObject spawners = new JsonObject();
        for (String category : new String[]{"monster", "creature", "ambient", "water_creature", "water_ambient", "misc"}) {
            spawners.add(category, new JsonArray());
        }
```

**Step 5: Verify compilation**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL

**Step 6: Commit**

```bash
git add src/main/java/com/hecookin/adastramekanized/common/planets/PlanetMaker.java
git commit -m "feat: add addModReplacementSpawns API and conditional biome modifier generation"
```

---

### Task 4: Rewrite PlanetGenerationRunner Mob Configs

**Files:**
- Modify: `src/main/java/com/hecookin/adastramekanized/common/planets/PlanetGenerationRunner.java`

This task rewrites mob configurations for all 31 planets using the new tier system and `addModReplacementSpawns()`. The changes are in `configurePlanets()` (lines 346-1881).

**IMPORTANT**: Only mob-related calls change. All terrain, surface, atmosphere, biome, ore, and sky configuration stays the same. For each planet, locate the mob-related lines (typically `clearAllMobSpawns()`, `addMobSpawn()`, `addXxxMobs()`, `allowPeacefulMobs()`, `allowHostileMobs()`) and replace them.

**Step 1: Remove `applyMoonMobPreset()` method and other mob preset helpers**

Delete the `applyMoonMobPreset()` method (lines 265-287), `applyMarsMobPreset()` (lines 292-297), and `applyTestPlanetMobPreset()` (lines 302-310). These are replaced by inline configuration with the new API.

**Step 2: Rewrite each planet's mob configuration**

For each planet, replace the mob-related calls. Here is the complete mob configuration for every planet:

#### Lifeless Tier

**earth_orbit** (line ~409): No changes needed. Already has `disableMobGeneration(true)`.

#### Sparse Tier

**enceladus** (~line 863): Replace mob calls with:
```java
            .clearAllMobSpawns()
            .addModReplacementSpawns("luminousworld",
                new String[][] {
                    {"monster", "minecraft:stray", "10", "1", "1"},
                    {"monster", "minecraft:phantom", "5", "1", "1"}
                },
                new String[][] {
                    {"monster", "luminousworld:luminous_creeper", "8", "1", "1"},
                    {"monster", "luminousworld:luminous_skeleton", "6", "1", "1"}
                }
            )
            .allowPeacefulMobs(false)
```

**triton** (~line 905): Replace mob calls with:
```java
            .clearAllMobSpawns()
            .addModReplacementSpawns("luminousworld",
                new String[][] {
                    {"monster", "minecraft:phantom", "10", "1", "1"},
                    {"monster", "minecraft:stray", "5", "1", "1"}
                },
                new String[][] {
                    {"monster", "luminousworld:luminous_phantom", "8", "1", "1"},
                    {"monster", "luminousworld:luminous_skeleton", "6", "1", "1"}
                }
            )
            .allowPeacefulMobs(false)
```

**pluto** (~line 993): Replace mob calls with:
```java
            .clearAllMobSpawns()
            .addModReplacementSpawns("mobs_of_mythology",
                new String[][] {
                    {"monster", "minecraft:phantom", "10", "1", "1"},
                    {"monster", "minecraft:stray", "5", "1", "1"}
                },
                new String[][] {
                    {"monster", "mobs_of_mythology:yeti", "8", "1", "1"}
                }
            )
            .allowPeacefulMobs(false)
```

**eris** (~line 1034): Replace mob calls with:
```java
            .clearAllMobSpawns()
            .addModReplacementSpawns("mobs_of_mythology",
                new String[][] {
                    {"monster", "minecraft:endermite", "8", "1", "1"},
                    {"monster", "minecraft:phantom", "5", "1", "1"}
                },
                new String[][] {
                    {"monster", "mobs_of_mythology:shade", "5", "1", "1"}
                }
            )
            .allowPeacefulMobs(false)
```

#### Moderate Tier

**moon** (~line 350): Replace `applyMoonMobPreset(planet)` and surrounding mob calls with:
```java
            .clearAllMobSpawns()
            .addModReplacementSpawns("kobolds",
                new String[][] {
                    {"monster", "minecraft:silverfish", "30", "2", "4"},
                    {"monster", "minecraft:endermite", "15", "1", "2"}
                },
                new String[][] {
                    {"monster", "kobolds:kobold", "30", "2", "4"},
                    {"monster", "kobolds:kobold_warrior", "15", "1", "2"}
                }
            )
            .allowPeacefulMobs(false)
```

**mercury** (~line 580): Replace mob calls with:
```java
            .clearAllMobSpawns()
            .addModReplacementSpawns("kobolds",
                new String[][] {
                    {"monster", "minecraft:silverfish", "30", "2", "4"},
                    {"monster", "minecraft:endermite", "10", "1", "2"}
                },
                new String[][] {
                    {"monster", "kobolds:kobold", "30", "2", "4"},
                    {"monster", "kobolds:kobold_warrior", "15", "1", "2"},
                    {"monster", "kobolds:kobold_engineer", "10", "1", "2"}
                }
            )
            .allowPeacefulMobs(false)
```

**europa** (~line 630): Replace mob calls with:
```java
            .clearAllMobSpawns()
            .addModReplacementSpawns("born_in_chaos_v1",
                new String[][] {
                    {"monster", "minecraft:drowned", "25", "1", "2"},
                    {"water_creature", "minecraft:glow_squid", "15", "1", "3"}
                },
                new String[][] {
                    {"monster", "born_in_chaos_v1:corpse_fish", "25", "2", "4"},
                    {"monster", "born_in_chaos_v1:glutton_fish", "15", "1", "2"},
                    {"monster", "born_in_chaos_v1:zombie_fisherman", "10", "1", "2"}
                }
            )
```

**ganymede** (~line 729): Replace mob calls with:
```java
            .clearAllMobSpawns()
            .addModReplacementSpawns("undead_revamp2",
                new String[][] {
                    {"monster", "minecraft:skeleton", "25", "1", "2"},
                    {"monster", "minecraft:silverfish", "15", "1", "2"}
                },
                new String[][] {
                    {"monster", "undead_revamp2:revenant", "20", "1", "2"},
                    {"monster", "undead_revamp2:skeletal_knight", "15", "1", "1"}
                }
            )
            .allowPeacefulMobs(false)
```

**callisto** (~line 770): Replace mob calls with:
```java
            .clearAllMobSpawns()
            .addModReplacementSpawns("kobolds",
                new String[][] {
                    {"monster", "minecraft:endermite", "20", "1", "2"},
                    {"monster", "minecraft:silverfish", "10", "1", "2"}
                },
                new String[][] {
                    {"monster", "kobolds:kobold", "20", "2", "4"},
                    {"monster", "kobolds:kobold_warrior", "10", "1", "2"}
                }
            )
            .allowPeacefulMobs(false)
```

**titan** (~line 816): Replace mob calls with:
```java
            .clearAllMobSpawns()
            .addModReplacementSpawns("shineals_prehistoric_expansion",
                new String[][] {
                    {"monster", "minecraft:slime", "25", "2", "4"},
                    {"monster", "minecraft:witch", "15", "1", "1"},
                    {"creature", "minecraft:axolotl", "20", "2", "4"}
                },
                new String[][] {
                    {"monster", "shineals_prehistoric_expansion:raptor", "20", "1", "2"},
                    {"creature", "shineals_prehistoric_expansion:ankylosaurus", "15", "1", "2"}
                }
            )
```

**ceres** (~line 949): Replace mob calls with:
```java
            .clearAllMobSpawns()
            .addModReplacementSpawns("kobolds",
                new String[][] {
                    {"monster", "minecraft:silverfish", "25", "2", "4"},
                    {"monster", "minecraft:endermite", "10", "1", "2"}
                },
                new String[][] {
                    {"monster", "kobolds:kobold", "25", "2", "4"},
                    {"monster", "kobolds:kobold_warrior", "12", "1", "2"},
                    {"monster", "kobolds:kobold_engineer", "12", "1", "2"}
                }
            )
            .allowPeacefulMobs(false)
```

#### Rich Tier -- Solar System

**mars** (~line 471): Replace mob calls with:
```java
            .clearAllMobSpawns()
            .addModReplacementSpawns("born_in_chaos_v1",
                new String[][] {
                    {"monster", "minecraft:husk", "30", "1", "3"},
                    {"monster", "minecraft:enderman", "10", "1", "1"},
                    {"monster", "minecraft:skeleton", "15", "1", "2"}
                },
                new String[][] {
                    {"monster", "born_in_chaos_v1:decrepit_skeleton", "12", "1", "2"},
                    {"monster", "born_in_chaos_v1:skeleton_demoman", "7", "1", "1"},
                    {"monster", "born_in_chaos_v1:decaying_zombie", "12", "1", "2"},
                    {"monster", "born_in_chaos_v1:barrel_zombie", "7", "1", "1"},
                    {"monster", "born_in_chaos_v1:bonescaller", "6", "1", "1"},
                    {"monster", "born_in_chaos_v1:bone_imp", "7", "1", "1"}
                }
            )
```

**venus** (~line 534): Replace mob calls with:
```java
            .clearAllMobSpawns()
            .addModReplacementSpawns("born_in_chaos_v1",
                new String[][] {
                    {"monster", "minecraft:husk", "25", "1", "2"},
                    {"monster", "minecraft:witch", "15", "1", "1"},
                    {"monster", "minecraft:slime", "20", "2", "3"}
                },
                new String[][] {
                    {"monster", "born_in_chaos_v1:decrepit_skeleton", "25", "1", "2"},
                    {"monster", "born_in_chaos_v1:decaying_zombie", "20", "1", "2"},
                    {"monster", "born_in_chaos_v1:door_knight", "5", "1", "1"},
                    {"monster", "born_in_chaos_v1:skeleton_thrasher", "8", "1", "1"}
                }
            )
            .addReptilianCreature("gecko", 15, 1, 1)
            .allowPeacefulMobs(false)
```

**io** (~line 679): Replace mob calls with:
```java
            .clearAllMobSpawns()
            .addModReplacementSpawns("doom",
                new String[][] {
                    {"monster", "minecraft:magma_cube", "30", "1", "3"},
                    {"monster", "minecraft:ghast", "10", "1", "1"},
                    {"monster", "minecraft:blaze", "15", "1", "2"}
                },
                new String[][] {
                    {"monster", "doom:imp", "40", "2", "4"},
                    {"monster", "doom:lost_soul", "30", "1", "3"},
                    {"monster", "doom:cacodemon", "15", "1", "1"},
                    {"monster", "doom:pinky", "10", "1", "2"}
                }
            )
            .allowPeacefulMobs(false)
```

**pyrios** (~line 1330): Replace mob calls with:
```java
            .clearAllMobSpawns()
            .addModReplacementSpawns("rottencreatures",
                new String[][] {
                    {"monster", "minecraft:ghast", "15", "1", "1"},
                    {"monster", "minecraft:magma_cube", "25", "1", "3"}
                },
                new String[][] {
                    {"monster", "rottencreatures:burned", "20", "1", "2"},
                    {"monster", "rottencreatures:frostbitten", "15", "1", "1"}
                }
            )
            .addMobSpawn("creature", "minecraft:strider", 20, 1, 2)
            .allowPeacefulMobs(false)
```

#### Rich Tier -- Habitable Exoplanets

**kepler22b** (~line 1076): Replace mob calls with:
```java
            .clearAllMobSpawns()
            .addMobSpawn("creature", "minecraft:dolphin", 8, 1, 2)
            .addMobSpawn("creature", "minecraft:turtle", 6, 1, 2)
            .addMobSpawn("water_ambient", "minecraft:cod", 10, 2, 3)
            .addMobSpawn("water_ambient", "minecraft:salmon", 8, 2, 3)
            .addMobSpawn("water_ambient", "minecraft:tropical_fish", 12, 2, 4)
            .addModReplacementSpawns("born_in_chaos_v1",
                new String[][] {
                    {"monster", "minecraft:drowned", "15", "1", "2"}
                },
                new String[][] {
                    {"monster", "born_in_chaos_v1:corpse_fish", "15", "2", "4"},
                    {"monster", "born_in_chaos_v1:glutton_fish", "10", "1", "2"},
                    {"monster", "born_in_chaos_v1:zombie_fisherman", "12", "1", "2"}
                }
            )
```

**kepler442b** (~line 1131): Replace mob calls with:
```java
            .clearAllMobSpawns()
            .addModReplacementSpawns("born_in_chaos_v1",
                new String[][] {
                    {"creature", "minecraft:cow", "8", "2", "3"},
                    {"creature", "minecraft:pig", "8", "2", "3"},
                    {"creature", "minecraft:sheep", "8", "2", "3"},
                    {"creature", "minecraft:chicken", "10", "2", "4"},
                    {"creature", "minecraft:rabbit", "6", "1", "2"},
                    {"creature", "minecraft:fox", "4", "1", "1"},
                    {"creature", "minecraft:wolf", "3", "1", "2"},
                    {"monster", "minecraft:zombie", "8", "1", "2"},
                    {"monster", "minecraft:skeleton", "8", "1", "2"},
                    {"monster", "minecraft:creeper", "6", "1", "1"}
                },
                new String[][] {
                    {"monster", "born_in_chaos_v1:baby_spider", "10", "1", "2"},
                    {"monster", "born_in_chaos_v1:mother_spider", "5", "1", "1"},
                    {"monster", "born_in_chaos_v1:dread_hound", "8", "1", "2"},
                    {"monster", "born_in_chaos_v1:phantom_creeper", "6", "1", "1"},
                    {"monster", "born_in_chaos_v1:swarmer", "8", "2", "3"},
                    {"creature", "minecraft:cow", "8", "2", "3"},
                    {"creature", "minecraft:pig", "8", "2", "3"},
                    {"creature", "minecraft:sheep", "8", "2", "3"},
                    {"creature", "minecraft:chicken", "10", "2", "4"}
                }
            )
```

**proxima_b** (~line 1184): Replace mob calls with:
```java
            .clearAllMobSpawns()
            .addModReplacementSpawns("born_in_chaos_v1",
                new String[][] {
                    {"monster", "minecraft:husk", "30", "2", "3"},
                    {"creature", "minecraft:rabbit", "18", "2", "3"}
                },
                new String[][] {
                    {"monster", "born_in_chaos_v1:decaying_zombie", "20", "1", "2"},
                    {"monster", "born_in_chaos_v1:bonescaller", "10", "1", "1"},
                    {"creature", "minecraft:rabbit", "18", "2", "3"}
                }
            )
```

**trappist1e** (~line 1227): Replace mob calls with:
```java
            .clearAllMobSpawns()
            .addModReplacementSpawns("born_in_chaos_v1",
                new String[][] {
                    {"creature", "minecraft:cow", "10", "2", "3"},
                    {"creature", "minecraft:pig", "10", "2", "3"},
                    {"creature", "minecraft:sheep", "10", "2", "3"},
                    {"creature", "minecraft:chicken", "12", "2", "4"},
                    {"creature", "minecraft:bee", "8", "1", "2"},
                    {"creature", "minecraft:frog", "6", "1", "2"},
                    {"monster", "minecraft:zombie", "6", "1", "1"},
                    {"monster", "minecraft:skeleton", "6", "1", "1"}
                },
                new String[][] {
                    {"monster", "born_in_chaos_v1:baby_spider", "8", "1", "2"},
                    {"monster", "born_in_chaos_v1:thornshell_crab", "6", "1", "1"},
                    {"creature", "minecraft:cow", "10", "2", "3"},
                    {"creature", "minecraft:pig", "10", "2", "3"},
                    {"creature", "minecraft:sheep", "10", "2", "3"},
                    {"creature", "minecraft:chicken", "12", "2", "4"},
                    {"creature", "minecraft:bee", "8", "1", "2"},
                    {"creature", "minecraft:frog", "6", "1", "2"}
                }
            )
```

**gliese667c** (~line 1277): Replace mob calls with:
```java
            .clearAllMobSpawns()
            .addMobSpawn("creature", "minecraft:goat", 12, 2, 3)
            .addMobSpawn("creature", "minecraft:llama", 8, 1, 2)
            .addMobSpawn("creature", "minecraft:sheep", 8, 1, 2)
            .addModReplacementSpawns("born_in_chaos_v1",
                new String[][] {
                    {"monster", "minecraft:skeleton", "10", "1", "2"}
                },
                new String[][] {
                    {"monster", "born_in_chaos_v1:supreme_bonescaller", "5", "1", "1"},
                    {"monster", "born_in_chaos_v1:lifestealer", "5", "1", "1"},
                    {"monster", "born_in_chaos_v1:krampus", "3", "1", "1"},
                    {"monster", "born_in_chaos_v1:missioner", "5", "1", "1"}
                }
            )
```

#### Rich Tier -- Alien Worlds

**frigidum** (~line 1374): Replace mob calls with:
```java
            .clearAllMobSpawns()
            .addModReplacementSpawns("born_in_chaos_v1",
                new String[][] {
                    {"monster", "minecraft:stray", "30", "2", "3"},
                    {"monster", "minecraft:skeleton", "20", "1", "2"},
                    {"creature", "minecraft:polar_bear", "15", "1", "2"}
                },
                new String[][] {
                    {"monster", "born_in_chaos_v1:baby_spider", "10", "1", "2"},
                    {"monster", "born_in_chaos_v1:mother_spider", "5", "1", "1"},
                    {"monster", "born_in_chaos_v1:dread_hound", "10", "1", "2"},
                    {"monster", "born_in_chaos_v1:dire_hound_leader", "3", "1", "1"},
                    {"monster", "born_in_chaos_v1:nightmare_stalker", "5", "1", "1"},
                    {"creature", "minecraft:polar_bear", "15", "1", "2"}
                }
            )
```

**arenos** (~line 1423): Replace mob calls with:
```java
            .clearAllMobSpawns()
            .addModReplacementSpawns("reptilian",
                new String[][] {
                    {"monster", "minecraft:husk", "40", "2", "4"},
                    {"creature", "minecraft:rabbit", "15", "2", "3"}
                },
                new String[][] {
                    {"creature", "reptilian:gecko", "12", "1", "2"},
                    {"creature", "reptilian:komodo_dragon", "10", "1", "2"},
                    {"creature", "minecraft:rabbit", "15", "2", "3"}
                }
            )
```

**paludis** (~line 1472): Replace mob calls with:
```java
            .clearAllMobSpawns()
            .addMobSpawn("monster", "minecraft:slime", 15, 1, 2)
            .addMobSpawn("monster", "minecraft:witch", 8, 1, 1)
            .addMobSpawn("monster", "minecraft:drowned", 10, 1, 2)
            .addMobSpawn("creature", "minecraft:frog", 10, 2, 3)
            .addMobSpawn("creature", "minecraft:axolotl", 6, 1, 2)
            .addRibbitsMobs(50)
            .addBornInChaosAquatic(18)
```

**luxoria** (~line 1528): Replace mob calls with:
```java
            .clearAllMobSpawns()
            .addMobSpawn("creature", "minecraft:glow_squid", 12, 1, 2)
            .addMobSpawn("creature", "minecraft:axolotl", 12, 1, 2)
            .addMobSpawn("creature", "minecraft:parrot", 10, 1, 2)
            .addMobSpawn("creature", "minecraft:ocelot", 6, 1, 1)
            .addModReplacementSpawns("born_in_chaos_v1",
                new String[][] {
                    {"monster", "minecraft:phantom", "6", "1", "1"}
                },
                new String[][] {
                    {"monster", "born_in_chaos_v1:restless_spirit", "18", "1", "2"},
                    {"monster", "born_in_chaos_v1:seared_spirit", "13", "1", "1"},
                    {"monster", "born_in_chaos_v1:firelight", "13", "1", "1"},
                    {"monster", "born_in_chaos_v1:pumpkin_spirit", "10", "1", "1"}
                }
            )
```

**glacio** (~line 1579): Replace mob calls with:
```java
            .clearAllMobSpawns()
            .addModReplacementSpawns("kobolds",
                new String[][] {
                    {"monster", "minecraft:stray", "30", "2", "3"},
                    {"monster", "minecraft:skeleton", "15", "1", "2"},
                    {"creature", "minecraft:polar_bear", "20", "1", "2"}
                },
                new String[][] {
                    {"monster", "kobolds:kobold", "20", "2", "4"},
                    {"monster", "kobolds:kobold_warrior", "10", "1", "2"},
                    {"monster", "kobolds:kobold_enchanter", "5", "1", "1"},
                    {"monster", "kobolds:kobold_engineer", "10", "1", "2"},
                    {"creature", "minecraft:polar_bear", "20", "1", "2"}
                }
            )
```

**vulcan** (~line 1626): Replace mob calls with:
```java
            .clearAllMobSpawns()
            .addModReplacementSpawns("doom",
                new String[][] {
                    {"monster", "minecraft:magma_cube", "30", "1", "3"},
                    {"monster", "minecraft:ghast", "10", "1", "1"},
                    {"monster", "minecraft:blaze", "15", "1", "2"}
                },
                new String[][] {
                    {"monster", "doom:imp", "35", "2", "4"},
                    {"monster", "doom:lost_soul", "25", "1", "3"},
                    {"monster", "doom:cacodemon", "12", "1", "1"},
                    {"monster", "doom:pinky", "8", "1", "2"}
                }
            )
            .allowPeacefulMobs(false)
```

**terra_nova** (~line 1669): Replace mob calls with:
```java
            .clearAllMobSpawns()
            .addModReplacementSpawns("born_in_chaos_v1",
                new String[][] {
                    {"creature", "minecraft:cow", "25", "2", "4"},
                    {"creature", "minecraft:pig", "25", "2", "4"},
                    {"creature", "minecraft:sheep", "25", "2", "4"},
                    {"creature", "minecraft:chicken", "30", "3", "5"},
                    {"creature", "minecraft:horse", "10", "2", "4"},
                    {"monster", "minecraft:zombie", "20", "2", "3"},
                    {"monster", "minecraft:skeleton", "20", "2", "3"},
                    {"monster", "minecraft:creeper", "12", "1", "1"}
                },
                new String[][] {
                    {"monster", "born_in_chaos_v1:baby_spider", "10", "1", "2"},
                    {"monster", "born_in_chaos_v1:mother_spider", "5", "1", "1"},
                    {"monster", "born_in_chaos_v1:dread_hound", "10", "1", "2"},
                    {"monster", "born_in_chaos_v1:phantom_creeper", "8", "1", "1"},
                    {"monster", "born_in_chaos_v1:swarmer", "8", "2", "3"},
                    {"monster", "born_in_chaos_v1:zombie_clown", "12", "1", "2"},
                    {"creature", "minecraft:cow", "25", "2", "4"},
                    {"creature", "minecraft:pig", "25", "2", "4"},
                    {"creature", "minecraft:sheep", "25", "2", "4"},
                    {"creature", "minecraft:chicken", "30", "3", "5"},
                    {"creature", "minecraft:horse", "10", "2", "4"}
                }
            )
```

**primordium** (~line 1726): Replace mob calls with:
```java
            .clearAllMobSpawns()
            .addMobSpawn("creature", "minecraft:parrot", 18, 2, 3)
            .addMobSpawn("creature", "minecraft:ocelot", 12, 1, 2)
            .addModReplacementSpawns("mowziesmobs",
                new String[][] {
                    {"monster", "minecraft:spider", "20", "1", "2"},
                    {"monster", "minecraft:skeleton", "10", "1", "1"}
                },
                new String[][] {
                    {"monster", "mowziesmobs:foliaath", "70", "1", "4"},
                    {"monster", "mowziesmobs:baby_foliaath", "40", "2", "3"},
                    {"monster", "mowziesmobs:ferrous_wroughtnaut", "2", "1", "1"},
                    {"monster", "mowziesmobs:naga", "5", "1", "1"}
                }
            )
            .addBornInChaosCreatures(22)
```

**bellator** (~line 1779): Replace mob calls with:
```java
            .clearAllMobSpawns()
            .addMobSpawn("creature", "minecraft:llama", 15, 2, 4)
            .addModReplacementSpawns("mowziesmobs",
                new String[][] {
                    {"monster", "minecraft:husk", "25", "1", "2"},
                    {"monster", "minecraft:skeleton", "15", "1", "1"}
                },
                new String[][] {
                    {"monster", "mowziesmobs:umvuthana_raptor", "50", "1", "1"},
                    {"monster", "mowziesmobs:umvuthana_crane", "30", "1", "1"},
                    {"monster", "mowziesmobs:umvuthana", "40", "1", "2"}
                }
            )
            .addBornInChaosPumpkin(18)
```

**profundus** (~line 1831): Replace mob calls with:
```java
            .clearAllMobSpawns()
            .addModReplacementSpawns("kobolds",
                new String[][] {
                    {"monster", "minecraft:silverfish", "30", "2", "4"},
                    {"monster", "minecraft:cave_spider", "15", "1", "2"}
                },
                new String[][] {
                    {"monster", "kobolds:kobold", "50", "2", "4"},
                    {"monster", "kobolds:kobold_warrior", "25", "1", "2"},
                    {"monster", "kobolds:kobold_enchanter", "16", "1", "1"},
                    {"monster", "kobolds:kobold_engineer", "25", "1", "2"},
                    {"monster", "kobolds:kobold_zombie", "15", "2", "4"},
                    {"monster", "kobolds:kobold_skeleton", "15", "2", "4"},
                    {"monster", "kobolds:witherbold", "5", "1", "2"}
                }
            )
            .addBornInChaosSpirits(20)
            .allowPeacefulMobs(false)
```

**Step 3: Verify structure and enablement calls**

Ensure that for each planet, the existing `enableXxxStructures()` calls remain intact. Only mob-related calls should change. Structure calls like `enableKoboldsStructures()`, `enableRibbitsStructures()`, `enableBornInChaosStructures()`, `enableDungeonsAriseStructures()`, `enableSevenSeasStructures()` should be preserved as they are.

**Step 4: Verify compilation**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL

**Step 5: Commit**

```bash
git add src/main/java/com/hecookin/adastramekanized/common/planets/PlanetGenerationRunner.java
git commit -m "feat: rewrite all planet mob configs with tier system and modded replacement"
```

---

### Task 5: Regenerate Planet Files and Verify

**Step 1: Clean old biome modifier files**

Before regenerating, delete existing biome modifier files to avoid stale leftovers:

```bash
find src/main/resources/data/adastramekanized/neoforge/biome_modifier/ -name "*.json" -delete
```

**Step 2: Regenerate planet files**

Run: `./gradlew makePlanets`
Expected: Generates new biome modifier files with:
- `add_spawns.json` (unconditional vanilla) -- only for planets with unconditional vanilla mobs
- `add_spawns_vanilla_no_{modid}.json` (conditional vanilla fallback) -- for planets with modded replacements
- `add_spawns_{modid}.json` (conditional modded) -- for planets with modded mobs

**Step 3: Verify generated files**

Spot-check a few planets:

1. **Moon** should have:
   - `moon/add_spawns_vanilla_no_kobolds.json` with NOT kobolds condition + silverfish/endermite
   - `moon/add_spawns_kobolds.json` with kobolds condition + kobold/warrior
   - NO `moon/add_spawns.json` (no unconditional vanilla)

2. **Paludis** should have:
   - `paludis/add_spawns.json` with unconditional slime/witch/drowned/frog/axolotl
   - `paludis/add_spawns_ribbits.json` with ribbits condition
   - `paludis/add_spawns_born_in_chaos_v1.json` with BIC condition

3. **Eris** should have:
   - `eris/add_spawns_vanilla_no_mobs_of_mythology.json` with NOT condition + endermite/phantom
   - `eris/add_spawns_mobs_of_mythology.json` with condition + shade

4. **Earth_orbit** should have NO biome modifier files (mob generation disabled)

**Step 4: Verify biome JSONs have empty spawners**

Check any biome JSON (e.g., `moon_highlands.json`) and confirm the `spawners` section has empty arrays for all categories.

**Step 5: Full build**

Run: `./gradlew clean build`
Expected: BUILD SUCCESSFUL

**Step 6: Commit generated files**

```bash
git add src/main/resources/data/adastramekanized/neoforge/biome_modifier/
git add src/main/resources/data/adastramekanized/worldgen/biome/
git commit -m "chore: regenerate planet files with new mob spawning system"
```

---

### Task 6: Research and Verify Modded Mob IDs

**IMPORTANT**: The mob IDs for newly activated mods (luminousworld, mobs_of_mythology, rottencreatures, shineals_prehistoric_expansion, undead_revamp2) used in Task 4 are **placeholder names** based on common naming conventions. Before final deployment, verify the actual mob registry names by:

1. Check each mod's source or documentation for exact entity type IDs
2. Look in `/home/keroppi/Development/Minecraft/_ReferenceMods/` for these mods
3. If mods aren't available locally, search their CurseForge/Modrinth pages for entity lists
4. Update the mob IDs in `PlanetGenerationRunner.java` if any placeholders are incorrect
5. Re-run `./gradlew makePlanets` and `./gradlew build` after corrections

**Known confirmed mob IDs** (from existing working code):
- `born_in_chaos_v1:*` -- all confirmed in existing biome modifiers
- `mowziesmobs:*` -- all confirmed in existing biome modifiers
- `doom:*` -- all confirmed in existing biome modifiers
- `kobolds:*` -- all confirmed in existing biome modifiers
- `ribbits:ribbit` -- confirmed in existing biome modifier
- `reptilian:gecko`, `reptilian:komodo_dragon` -- confirmed in existing biome modifiers

**Needs verification**:
- `luminousworld:luminous_creeper`, `luminousworld:luminous_skeleton`, `luminousworld:luminous_phantom`
- `mobs_of_mythology:yeti`, `mobs_of_mythology:shade`
- `rottencreatures:burned`, `rottencreatures:frostbitten`
- `shineals_prehistoric_expansion:raptor`, `shineals_prehistoric_expansion:ankylosaurus`
- `undead_revamp2:revenant`, `undead_revamp2:skeletal_knight`
