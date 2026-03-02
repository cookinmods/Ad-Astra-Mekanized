# Liquid Features & Vanilla Structure Whitelist Design

**Goal:** Fix inappropriate water/lava on dry planets, add vanilla structure whitelisting API

**Architecture:** Respect existing per-planet liquid config for biome features; biome tag injection for vanilla structure opt-in

## Part 1: Liquid Feature Fix

### Problem
- `minecraft:lake_lava_underground`, `lake_lava_surface`, `spring_water`, `spring_lava` added to ALL 51 planet biomes
- `aquifersEnabled` forced to `true` on line 4922 regardless of per-planet setting
- Result: water springs and lava lakes on airless moons and dry deserts

### Fix
- Step 1 (lakes): Only add lava lakes if `lavaLakeFrequency > 0` or default fluid is lava
- Step 8 (springs): Only add `spring_water` if `seaLevel > 0` or `aquifersEnabled`; only add `spring_lava` if lava is relevant
- Remove forced `aquifersEnabled = true` override; use per-planet value
- Keep amethyst geodes (geological, not liquid-dependent)

### Affected Planets (~16 dry worlds lose liquid features)
Moon, Mars, Mercury, Callisto, Ceres, Ganymede, Enceladus, Triton, Pluto, Eris, Frigidum, Arenos, Bellator, Profundus, Venus, Earth Orbit

## Part 2: Vanilla Structure Whitelisting API

### API
```java
registerPlanet("mercury")
    .enableVanillaStructure("mineshaft")
    .generate();
```

### Mapping
| Structure | Biome Tag |
|-----------|-----------|
| mineshaft | minecraft:has_structure/mineshaft |
| mineshaft_mesa | minecraft:has_structure/mineshaft_mesa |
| stronghold | minecraft:has_structure/stronghold |
| ruined_portal | minecraft:has_structure/ruined_portal |
| desert_pyramid | minecraft:has_structure/desert_pyramid |
| igloo | minecraft:has_structure/igloo |
| ancient_city | minecraft:has_structure/ancient_city |
| trail_ruins | minecraft:has_structure/trail_ruins |
| trial_chambers | minecraft:has_structure/trial_chambers |

### Planet Distribution
- Mineshafts: Mercury, Mars, Callisto, Ceres, Gliese667C
- Mineshaft mesa: Proxima B, Arenos
- Ruined portals: Mars, Glacio, Terra Nova, Bellator
- Strongholds: Profundus, Primordium
- Igloos: Frigidum, Enceladus, Triton, Pluto
- Desert pyramids: Arenos, Venus
- Ancient cities: Profundus
- Trail ruins: Primordium, Terra Nova
