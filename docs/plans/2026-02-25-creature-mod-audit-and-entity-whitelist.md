# Creature Mod Audit & Entity-Level Spawn Whitelist

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Audit all creature mod integrations, fix the spawn whitelist to be entity-level instead of namespace-level, and add missing optional dependency declarations to neoforge.mods.toml.

**Architecture:** The spawn controller currently whitelists entire mod namespaces per dimension, allowing creature mods' own biome modifiers to leak unwanted mobs onto planets. We change to entity-level whitelisting so only the specific mobs we configure in PlanetGenerationRunner can spawn. We also declare all 11 creature mods as optional dependencies in mods.toml for correct load ordering and release metadata.

**Tech Stack:** NeoForge 1.21.1, Java 21, NeoForge biome modifier system, FinalizeSpawnEvent/EntityJoinLevelEvent

---

## Audit Findings

### Current Creature Mod Integration Summary

| Mod | Mod ID | Planets | In mods.toml | Spawn Leak Risk |
|-----|--------|---------|--------------|-----------------|
| Born in Chaos | `born_in_chaos_v1` | 15 | Optional (declared) | HIGH - most planets |
| Kobolds | `kobolds` | 7 | **Missing** | HIGH - many planets |
| MCDoom | `doom` | 2 | **Missing** | Medium |
| Reptilian | `reptilian` | 2 | **Missing** | Low |
| Ribbits | `ribbits` | 1 | **Missing** | Low |
| Luminous World | `luminousworld` | 2 | **Missing** | Low |
| Mowzie's Mobs | `mowziesmobs` | 2 | **Missing** | Medium |
| Undead Revamped | `undead_revamp2` | 1 | **Missing** | Low |
| Rotten Creatures | `rottencreatures` | 1 | **Missing** | Low |
| Prehistoric Expansion | `shineals_prehistoric_expansion` | 1 | **Missing** | Low |
| Mobs of Mythology | `mobs_of_mythology` | 2 | **Missing** | Low |
| When Dungeons Arise | — | 0 | Not declared | N/A - no integration |

### The Spawn Leak Bug

**Root cause:** `PlanetMaker.addMobSpawn()` (line 1516) adds the mob's namespace to `usedModNamespaces`. During `registerModdedMobWhitelist()` (line 3945), the entire namespace is whitelisted for the dimension. The spawn controller (`ModdedMobSpawnController.onFinalizeSpawn()`) then allows ANY entity from that namespace, not just our configured ones.

**Example:** Mars configures 6 specific Born in Chaos mobs. But the namespace-level whitelist allows ALL Born in Chaos mobs (pumpkins, spirits, aquatics, bosses, etc.) to spawn via BiC's own biome modifiers.

**Fix:** Track specific entity IDs instead of namespaces. The spawn controller checks the full entity ID (e.g., `born_in_chaos_v1:decrepit_skeleton`) against the whitelist, not just the namespace (`born_in_chaos_v1`).

---

## Task 1: Add Entity-Level Tracking to PlanetBuilder

**Files:**
- Modify: `src/main/java/com/hecookin/adastramekanized/common/planets/PlanetMaker.java:570` (field declaration)
- Modify: `src/main/java/com/hecookin/adastramekanized/common/planets/PlanetMaker.java:1508-1521` (addMobSpawn)
- Modify: `src/main/java/com/hecookin/adastramekanized/common/planets/PlanetMaker.java:1531-1552` (addModReplacementSpawns)
- Modify: `src/main/java/com/hecookin/adastramekanized/common/planets/PlanetMaker.java:3935-3949` (registerModdedMobWhitelist)

**Step 1: Add `usedEntityTypes` field alongside `usedModNamespaces`**

At line 570, add a new field:

```java
private java.util.Set<String> usedModNamespaces = new java.util.HashSet<>();  // Track mod namespaces for spawn control
private java.util.Set<String> usedEntityIds = new java.util.HashSet<>();  // Track specific entity IDs for entity-level whitelist
```

**Step 2: Update `addMobSpawn()` to track entity IDs**

At line 1508-1521, add entity ID tracking:

```java
public PlanetBuilder addMobSpawn(String category, String mobId, int weight, int minGroup, int maxGroup) {
    mobSpawns.computeIfAbsent(category, k -> new java.util.ArrayList<>())
            .add(new MobSpawnEntry(mobId, weight, minGroup, maxGroup));

    // Track mod namespace for spawn control (if not minecraft)
    if (mobId.contains(":")) {
        String namespace = mobId.substring(0, mobId.indexOf(":"));
        if (!namespace.equals("minecraft")) {
            usedModNamespaces.add(namespace);
            usedEntityIds.add(mobId);  // Track specific entity ID
        }
    }

    return this;
}
```

**Step 3: Update `addModReplacementSpawns()` to track entity IDs**

At line 1540-1548, add entity ID tracking:

```java
for (String[] spawn : moddedSpawns) {
    group.addModdedReplacement(spawn[0],
        new MobSpawnEntry(spawn[1], Integer.parseInt(spawn[2]),
            Integer.parseInt(spawn[3]), Integer.parseInt(spawn[4])));
    // Track mod namespace for whitelist registration
    String namespace = spawn[1].contains(":") ? spawn[1].substring(0, spawn[1].indexOf(":")) : "minecraft";
    if (!namespace.equals("minecraft")) {
        usedModNamespaces.add(namespace);
        usedEntityIds.add(spawn[1]);  // Track specific entity ID
    }
}
```

**Step 4: Update `registerModdedMobWhitelist()` to pass entity IDs**

At line 3935-3949, change to register entity IDs:

```java
private void registerModdedMobWhitelist() {
    if (usedEntityIds.isEmpty()) {
        return;
    }

    net.minecraft.resources.ResourceLocation dimensionId =
        net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(
            AdAstraMekanized.MOD_ID, this.name);

    // Register each used mod namespace for this dimension (for controlled mod detection)
    for (String modNamespace : usedModNamespaces) {
        com.hecookin.adastramekanized.common.events.ModdedMobSpawnController
            .registerPlanetMobWhitelist(dimensionId, modNamespace);
    }

    // Register specific entity IDs for entity-level filtering
    for (String entityId : usedEntityIds) {
        com.hecookin.adastramekanized.common.events.ModdedMobSpawnController
            .registerEntityWhitelist(dimensionId, entityId);
    }
}
```

**Step 5: Commit**

```bash
git add src/main/java/com/hecookin/adastramekanized/common/planets/PlanetMaker.java
git commit -m "feat: track entity-level IDs in PlanetBuilder for spawn whitelist"
```

---

## Task 2: Add Entity-Level Whitelist Data Structure

**Files:**
- Modify: `src/main/java/com/hecookin/adastramekanized/common/events/ModdedMobWhitelistData.java`

**Step 1: Add entity whitelist map**

Add a new field after the existing maps (after line 18):

```java
// Map: Dimension ID -> Set of allowed entity IDs (entity-level whitelist)
private final Map<ResourceLocation, Set<ResourceLocation>> entityWhitelist = new HashMap<>();
```

**Step 2: Add entity whitelist methods**

Add after `whitelistModForBiome()` (after line 73):

```java
/**
 * Whitelist a specific entity type for a dimension.
 */
public void whitelistEntityForDimension(ResourceLocation dimensionId, ResourceLocation entityId) {
    entityWhitelist.computeIfAbsent(dimensionId, k -> new HashSet<>()).add(entityId);
}

/**
 * Check if a specific entity is allowed in a dimension.
 * Returns true if the entity is in the entity whitelist for this dimension.
 * Returns false if the dimension has an entity whitelist but this entity isn't in it.
 * Falls back to namespace-level check if no entity whitelist exists for the dimension.
 */
public boolean isEntityAllowed(ResourceLocation dimensionId, ResourceLocation entityId, String modNamespace) {
    // If entity-level whitelist exists for this dimension, use it
    if (entityWhitelist.containsKey(dimensionId)) {
        return entityWhitelist.get(dimensionId).contains(entityId);
    }

    // Fall back to namespace-level (for vanilla dimensions where we whitelist whole namespaces)
    return isModAllowed(dimensionId, null, modNamespace);
}
```

**Step 3: Update `getDebugInfo()` to include entity whitelist**

Add at the end of `getDebugInfo()`:

```java
sb.append("Entity Whitelists:\n");
for (Map.Entry<ResourceLocation, Set<ResourceLocation>> entry : entityWhitelist.entrySet()) {
    sb.append("  ").append(entry.getKey()).append(": ").append(entry.getValue().size()).append(" entities\n");
}
```

**Step 4: Update `clear()` to include entity whitelist**

```java
public void clear() {
    dimensionWhitelist.clear();
    biomeWhitelist.clear();
    entityWhitelist.clear();
}
```

**Step 5: Commit**

```bash
git add src/main/java/com/hecookin/adastramekanized/common/events/ModdedMobWhitelistData.java
git commit -m "feat: add entity-level whitelist data structure"
```

---

## Task 3: Update Spawn Controller to Use Entity-Level Whitelist

**Files:**
- Modify: `src/main/java/com/hecookin/adastramekanized/common/events/ModdedMobSpawnController.java`

**Step 1: Add `registerEntityWhitelist()` method**

Add after `registerBiomeMobWhitelist()` (after line 212):

```java
/**
 * Register a specific entity type as allowed in a dimension.
 * Called by PlanetMaker during planet registration.
 */
public static void registerEntityWhitelist(ResourceLocation dimensionId, String entityIdStr) {
    ResourceLocation entityId = ResourceLocation.parse(entityIdStr);
    whitelist.whitelistEntityForDimension(dimensionId, entityId);
    AdAstraMekanized.LOGGER.debug("Whitelisted entity '{}' for dimension '{}'", entityId, dimensionId);
}
```

**Step 2: Update `onFinalizeSpawn()` to check entity-level whitelist**

Replace the namespace-level check at lines 90-103 with entity-level check:

```java
// For natural spawns, check whitelist
Level level = event.getLevel().getLevel();
ResourceKey<Level> dimension = level.dimension();
BlockPos pos = mob.blockPosition();

// Get biome at spawn location
Holder<Biome> biomeHolder = level.getBiome(pos);
ResourceLocation biomeId = getBiomeId(biomeHolder);

// Check if this specific entity is whitelisted for this dimension
// Uses entity-level whitelist for planet dimensions, falls back to namespace for vanilla dimensions
boolean allowed = whitelist.isEntityAllowed(dimension.location(), entityId, modNamespace);

if (!allowed) {
    // Cancel natural spawn - not whitelisted for this dimension
    event.setSpawnCancelled(true);
    AdAstraMekanized.LOGGER.info("[SpawnControl] BLOCKED {} spawn ({}) in {} biome {} - entity not whitelisted",
        entityId, spawnType, dimension.location(), biomeId);
} else {
    // Track this entity as allowed so EntityJoinLevelEvent doesn't block it
    allowedEntityIds.add(mob.getId());
    AdAstraMekanized.LOGGER.debug("Allowed {} natural spawn ({}) in dimension {} biome {} (entity whitelisted)",
        entityId, spawnType, dimension.location(), biomeId);
}
```

**Step 3: Update `onEntityJoinLevel()` backup handler similarly**

Replace the namespace-level check at lines 155-163:

```java
// Check if this specific entity is whitelisted
boolean allowed = whitelist.isEntityAllowed(dimension.location(), entityId, modNamespace);

if (!allowed) {
    // Cancel entity join - not whitelisted for this dimension
    event.setCanceled(true);
    AdAstraMekanized.LOGGER.info("[SpawnControl] BLOCKED {} join in {} biome {} - entity not whitelisted (EntityJoinLevel fallback)",
        entityId, dimension.location(), biomeId);
}
```

**Step 4: Commit**

```bash
git add src/main/java/com/hecookin/adastramekanized/common/events/ModdedMobSpawnController.java
git commit -m "feat: use entity-level whitelist in spawn controller"
```

---

## Task 4: Preserve Vanilla Dimension Namespace-Level Behavior

**Important:** The `allowInVanillaDimensions()`, `allowInOverworld()`, `allowInNether()`, `allowInEnd()` methods must continue to work at namespace level. We don't want to enumerate every Born in Chaos entity for the Overworld — we just want them to spawn normally there.

**Files:**
- Verify: `src/main/java/com/hecookin/adastramekanized/common/events/ModdedMobWhitelistData.java`

The `isEntityAllowed()` method already handles this correctly:

```java
public boolean isEntityAllowed(ResourceLocation dimensionId, ResourceLocation entityId, String modNamespace) {
    // If entity-level whitelist exists for this dimension, use it (planet dimensions)
    if (entityWhitelist.containsKey(dimensionId)) {
        return entityWhitelist.get(dimensionId).contains(entityId);
    }
    // Fall back to namespace-level (vanilla dimensions from allowInOverworld/etc.)
    return isModAllowed(dimensionId, null, modNamespace);
}
```

**Verify:** `allowInVanillaDimensions("born_in_chaos_v1")` adds to `dimensionWhitelist` only, NOT to `entityWhitelist`. So vanilla dimensions keep namespace-level behavior. Planet dimensions get entity-level behavior because `registerEntityWhitelist()` populates `entityWhitelist`.

**Step 1: Verify `isModAllowed` fallback works for vanilla dimensions**

The existing `isModAllowed()` at line 83 checks `biomeWhitelist` first, then `dimensionWhitelist`. When called with `biomeId = null` from `isEntityAllowed`, the biome check will fail (no null key), and it falls through to `dimensionWhitelist`. This is correct.

**Step 2: Add a test comment to `isEntityAllowed` documenting this behavior**

```java
/**
 * Check if a specific entity is allowed in a dimension.
 *
 * For planet dimensions (which have entity-level entries via registerEntityWhitelist):
 *   Only allows specific entities we configured in PlanetGenerationRunner.
 *
 * For vanilla dimensions (Overworld/Nether/End, which only have namespace-level entries
 * via allowInVanillaDimensions/allowInOverworld/etc.):
 *   Falls back to namespace-level check, allowing all entities from that mod.
 */
```

**Step 3: Commit**

```bash
git add src/main/java/com/hecookin/adastramekanized/common/events/ModdedMobWhitelistData.java
git commit -m "docs: clarify entity vs namespace whitelist fallback behavior"
```

---

## Task 5: Add Missing Optional Dependencies to neoforge.mods.toml

**Files:**
- Modify: `src/main/resources/META-INF/neoforge.mods.toml:157` (after existing Born in Chaos entry)

**Step 1: Add 10 missing optional dependency declarations**

Append after line 157 (after the Born in Chaos entry):

```toml
# Kobolds - Underground cave dwellers (kobold dens, 7 kobold variants)
[[dependencies.adastramekanized]]
modId = "kobolds"
type = "optional"
versionRange = "[3.4.0,)"
ordering = "AFTER"
side = "BOTH"

# MCDoom - Demonic creatures from DOOM (volcanic/hell planets)
[[dependencies.adastramekanized]]
modId = "doom"
type = "optional"
versionRange = "[5.0.0,)"
ordering = "AFTER"
side = "BOTH"

# Reptilian - Lizard and reptile creatures (desert/tropical planets)
[[dependencies.adastramekanized]]
modId = "reptilian"
type = "optional"
versionRange = "[1.1.0,)"
ordering = "AFTER"
side = "BOTH"

# Ribbits - Frog villagers and swamp villages (Paludis)
[[dependencies.adastramekanized]]
modId = "ribbits"
type = "optional"
versionRange = "[1.2.0,)"
ordering = "AFTER"
side = "BOTH"

# Luminous World - Glowing magical creatures (icy moons)
[[dependencies.adastramekanized]]
modId = "luminousworld"
type = "optional"
versionRange = "[1.0.0,)"
ordering = "AFTER"
side = "BOTH"

# Mowzie's Mobs - Exotic jungle and tribal creatures (Primordium, Bellator)
[[dependencies.adastramekanized]]
modId = "mowziesmobs"
type = "optional"
versionRange = "[1.7.0,)"
ordering = "AFTER"
side = "BOTH"

# The Undead Revamped - Enhanced undead mobs (Ganymede)
[[dependencies.adastramekanized]]
modId = "undead_revamp2"
type = "optional"
versionRange = "[1.0.0,)"
ordering = "AFTER"
side = "BOTH"

# Rotten Creatures - Fire-themed undead (Pyrios)
[[dependencies.adastramekanized]]
modId = "rottencreatures"
type = "optional"
versionRange = "[1.0.0,)"
ordering = "AFTER"
side = "BOTH"

# Shineal's Prehistoric Expansion - Dinosaurs and prehistoric creatures (Titan)
[[dependencies.adastramekanized]]
modId = "shineals_prehistoric_expansion"
type = "optional"
versionRange = "[1.4.0,)"
ordering = "AFTER"
side = "BOTH"

# Mobs of Mythology - Mythological creatures (Pluto, Eris)
[[dependencies.adastramekanized]]
modId = "mobs_of_mythology"
type = "optional"
versionRange = "[1.0.0,)"
ordering = "AFTER"
side = "BOTH"
```

**NOTE:** Version ranges are approximate. The user should verify minimum compatible versions from the actual mod JARs before release. The mod IDs match what's in `ModdedMobWhitelistData.java`.

**Step 2: Commit**

```bash
git add src/main/resources/META-INF/neoforge.mods.toml
git commit -m "feat: declare all 11 creature mods as optional dependencies in mods.toml"
```

---

## Task 6: Build and Verify

**Step 1: Clean build**

```bash
./gradlew clean build
```

Expected: BUILD SUCCESSFUL. No compilation errors from the whitelist changes.

**Step 2: Run data generation**

```bash
./gradlew runData
```

Expected: Mod loads and initializes without errors. Check logs for:
- "Whitelisted entity '...' for dimension '...'" messages (new entity-level logs)
- No errors from spawn controller initialization

**Step 3: Manual in-game verification**

```bash
./gradlew runClient
```

Test checklist:
- [ ] Teleport to Mars (`/planet teleport mars`) — should ONLY see the 6 configured BiC mobs, NO pumpkins/spirits
- [ ] Teleport to Bellator (`/planet teleport bellator`) — should see pumpkin mobs (configured there)
- [ ] Teleport to Overworld — Born in Chaos mobs should spawn normally (namespace-level fallback)
- [ ] Use spawn egg for a non-whitelisted BiC mob on Mars — should work (manual spawns always allowed)
- [ ] Check logs for "[SpawnControl] BLOCKED" messages for leaked mobs being stopped

**Step 4: Commit verification results**

If all tests pass, no additional commit needed. If fixes are required, commit those fixes.

---

## Task 7 (Future - Out of Scope): Structure Blocking

**Not part of this plan.** Noted for future work:

- When Dungeons Arise and other structure-adding mods can place structures in planet dimensions
- Need a `ModdedStructureController` equivalent that blocks non-whitelisted structure generation
- Similar approach: whitelist specific structure sets per dimension, block everything else
- Requires investigation of NeoForge structure placement events/biome modifiers

---

## Summary of Changes

| File | Change |
|------|--------|
| `PlanetMaker.java` | Add `usedEntityIds` field, track entity IDs in `addMobSpawn()` and `addModReplacementSpawns()`, pass to controller |
| `ModdedMobWhitelistData.java` | Add `entityWhitelist` map, `whitelistEntityForDimension()`, `isEntityAllowed()` with namespace fallback |
| `ModdedMobSpawnController.java` | Add `registerEntityWhitelist()`, update both event handlers to use `isEntityAllowed()` |
| `neoforge.mods.toml` | Add 10 optional dependency declarations for creature mods |

Total estimated scope: ~80 lines of new code, ~20 lines modified, 10 dependency entries added.
