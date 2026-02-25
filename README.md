# Ad Astra Mekanized

A Minecraft 1.21.1 NeoForge mod that brings space exploration integrated with Mekanism's infrastructure systems. Build rockets, explore 40+ planets and moons, manage oxygen distribution, and establish bases across the solar system and beyond.

## Overview

Ad Astra Mekanized is a complete space exploration mod built around Mekanism's chemical and energy systems. Produce oxygen with Electrolytic Separators, convert gas fuels to liquid with Rotary Condensentrators, and pipe chemicals directly into your machines and rockets. Four tiers of rockets unlock progressively distant destinations, from the Moon to deep-space exoplanets.

Each planet has unique gravity, atmosphere, temperature, terrain, ore deposits, and mob spawning. Airless worlds require space suits with portable oxygen tanks. Enclosed bases use Oxygen Distributors powered by Mekanism's oxygen chemical. Gravity Normalizers keep you grounded on low-gravity moons.

The mod supports optional integration with Create (mechanical crafting automation), Create Crafts & Additions (rolling machine recipes), Immersive Engineering (alternative fuels), and Born in Chaos (planet-specific horror mobs).

## Features

- **4-tier rocket system** with distinct fuel types and progressive destination unlocks
- **40+ explorable planets** spanning the solar system, exoplanets, and themed worlds
- **Oxygen system** with flood-fill distribution, portable gas tanks, and auto-refill space suits
- **3 space suit tiers** — Standard, Netherite (fireproof), and Jet Suit (nitrogen-powered flight)
- **Power systems** with wired and wireless FE transmission
- **NASA Workbench** for specialized rocket and equipment crafting
- **Oxygen Network Controller** for wireless management of up to 64 distributors
- **Custom sky rendering** per planet with unique celestial bodies, starfields, and atmospheres
- **Per-planet mob spawning** with support for 10+ modded mob mods
- **Gravity Normalizer** for adjusting gravity in enclosed areas
- **184 blocks** including planet stones, industrial plating, sliding doors, and alien wood
- **In-game guide book** (Patchouli) documenting all planets, machines, and systems

## Dependencies

### Required

| Mod | Version | Purpose |
|-----|---------|---------|
| NeoForge | 21.1.209+ | Mod loader |
| Mekanism | 10.7.8+ | Chemical systems, energy, oxygen production |
| Tectonic | 3.0.16+ | Planet terrain density functions |
| Lithostitched | 1.5.0+ | Required by Tectonic |
| ChemLib Mekanized | 1.0.0+ | Chemical library |

### Optional

| Mod | What it adds |
|-----|-------------|
| Create | 25 mechanical crafting recipes (rockets, engines, tanks, oxygen gear) + 5 pressing recipes (ingot-to-sheet) |
| Create Crafts & Additions | 3 rolling machine recipes (sheet-to-rod for iron, steel, etrium) |
| Immersive Engineering | Diesel and biodiesel as rocket fuel alternatives |
| Born in Chaos | Horror mob spawning on specific planets |
| JEI | Recipe viewer support for NASA Workbench recipes |

## Planets

### Inner Solar System

| Planet | Gravity | Atmosphere | Temp | Notable Resources |
|--------|---------|------------|------|-------------------|
| Moon | 17% | None | -173°C | Silver, Desh, Cheese, Etrium |
| Mars | 38% | Toxic | -65°C | Osmium, Ostrum, Desh, Etrium |
| Mercury | 38% | None | 167°C | Nickel, Ostrum, Etrium |
| Venus | 90% | Toxic | 464°C | Lead, Calorite, Etrium |
| Earth Orbit | 17% | None | -270°C | None (space station) |

### Jupiter System

| Planet | Gravity | Atmosphere | Temp | Notable Resources |
|--------|---------|------------|------|-------------------|
| Europa | 13% | None | -160°C | Lapis, Silver, Etrium |
| Io | 18% | Toxic | -130°C | Nickel, Diamond |
| Ganymede | 15% | None | -160°C | Osmium, Diamond |
| Callisto | 13% | None | -139°C | Lead, Nickel, Diamond |

### Saturn System

| Planet | Gravity | Atmosphere | Temp | Notable Resources |
|--------|---------|------------|------|-------------------|
| Titan | 14% | Toxic | -179°C | Osmium, Calorite, Etrium |
| Enceladus | 5% | None | -201°C | Silver, Diamond |

### Outer Solar System

| Planet | Gravity | Atmosphere | Temp | Notable Resources |
|--------|---------|------------|------|-------------------|
| Triton | 8% | None | -235°C | Ostrum, Diamond |
| Pluto | 6% | None | -229°C | Gold, Silver, Etrium |
| Eris | 8% | None | -231°C | Lead, Diamond |
| Ceres | 5% | None | -106°C | Osmium, Nickel, Etrium |

### Habitable Exoplanets

| Planet | Gravity | Atmosphere | Temp | Notable Resources |
|--------|---------|------------|------|-------------------|
| Glacio | 80% | Breathable | -50°C | Silver, Calorite, Etrium |
| Kepler-22b | 120% | Breathable | 22°C | Lapis, Osmium, Etrium, Diamond |
| Kepler-442b | 100% | Breathable | 15°C | Emerald, Osmium |
| Proxima B | 110% | Breathable | -40°C | Redstone, Lead |
| Trappist-1e | 90% | Breathable | 12°C | Lapis, Osmium, Diamond |
| Gliese 667c | 140% | Breathable | 10°C | Emerald, Osmium, Diamond |
| Terra Nova | 95% | Breathable | 18°C | Osmium, Emerald |
| Primordium | 100% | Breathable | 30°C | Lead, Emerald |
| Paludis | 90% | Breathable | 28°C | Gold, Lead |
| Arenos | 100% | Breathable | 55°C | Lead, Diamond |
| Bellator | 100% | Breathable | 35°C | Gold, Diamond |
| Frigidum | 110% | Breathable | -80°C | Silver, Diamond |

### Hostile Worlds

| Planet | Gravity | Atmosphere | Temp | Notable Resources |
|--------|---------|------------|------|-------------------|
| Vulcan | 120% | Toxic | 200°C | Nickel, Diamond |
| Pyrios | 100% | Toxic | 800°C | Nickel, Calorite |
| Luxoria | 80% | Toxic | 32°C | Emerald, Lapis, Diamond |
| Profundus | 85% | Toxic | 20°C | Osmium, Emerald |

### Themed Worlds

| Planet | Gravity | Atmosphere | Theme |
|--------|---------|------------|-------|
| Cretaceous | 100% | Breathable | Prehistoric jungle with dinosaur-era terrain |
| Decay | 100% | Breathable | Post-apocalyptic wasteland |
| Necropolis | 100% | Breathable | Dark undead realm with soul sand valleys |
| Olympus | 100% | Breathable | Mythic mountain realm |
| Scaleland | 90% | Breathable | Reptilian desert and savanna |
| Glowworld | 90% | Breathable | Bioluminescent mushroom caverns |
| Ribbits Swamp | 100% | Breathable | Frog villager swamplands |

## Rocket Tiers

| Tier | Fuel | Accessible Destinations |
|------|------|------------------------|
| 1 | Liquid Ethanol | Moon, Earth Orbit |
| 2 | Liquid Propane | + Mars, Mercury |
| 3 | Liquid Ethylene | + Venus, Glacio |
| 4 | Liquid Methane | + All remaining planets |

All fuels are produced as gases by Mekanism and must be converted to liquids using a Rotary Condensentrator set to Condensating mode. Rockets have 8 cargo slots for transporting resources between planets. Automated fueling is supported by connecting Mekanism mechanical pipes to the bottom of a Launch Pad.

## Machines & Equipment

### Functional Blocks

- **Oxygen Distributor** — Flood-fills enclosed spaces with breathable oxygen. Requires Mekanism oxygen chemical + FE power. Ring-based expansion up to 4100 blocks.
- **Gravity Normalizer** — Adjusts gravity in an area on low-gravity worlds. Requires argon gas + FE power.
- **NASA Workbench** — Specialized crafting station for rockets and advanced space equipment. Recipes also available through Create Mechanical Crafters when Create is installed.
- **Launch Pad** — Place rockets for launch. Supports automated fueling via Mekanism pipes connected to the bottom face.
- **Wireless Power Relay** — Channel-based wireless FE transmission between paired relays. Configure matching channels in the GUI for point-to-point or multi-receiver setups.
- **Redstone Toggle Relay** — Remote redstone signal control, bindable to an Oxygen Network Controller.
- **Oxygen Network Monitor** — Displays oxygen network status for connected distributors.

### Equipment

- **Space Suits** — Three tiers of portable life support:
  - **Standard** — Basic oxygen and environmental protection
  - **Netherite** — Enhanced protection, fireproof
  - **Jet Suit** — Built-in jet propulsion using nitrogen gas for flight
- **Gas Tank** — Portable oxygen storage (10,000 mB). Automatically refills equipped space suits.
- **Large Gas Tank** — Extended oxygen storage (50,000 mB) for long missions.
- **Oxygen Gear** — Core life support component used in suit and distributor crafting.
- **Oxygen Network Controller** — Handheld item that wirelessly links and manages up to 64 Oxygen Distributors and Gravity Normalizers.
- **Astronomer Journal** — In-game Patchouli guide book documenting all planets, machines, and systems.

### Materials

The mod adds five material tiers used in progression:

| Material | Source | Used For |
|----------|--------|----------|
| Steel | Mekanism processing | Tier 1 rockets, basic machines, industrial blocks |
| Desh | Moon ore | Tier 2 rockets, oxygen equipment |
| Ostrum | Mars ore | Tier 4 rockets, advanced machinery |
| Calorite | Venus ore | Tier 3 rockets, heat-resistant components |
| Etrium | Rare on multiple planets | Etrionic cores, capacitors, high-tier components |

Each material has ingots, nuggets, sheets, rods, and decorative block variants (plating, panels, pillars, sheetblocks).

### Decorative Blocks

- Planet stone sets (Moon, Mars, Venus, Mercury, Glacio) with stairs and slabs
- Industrial blocks: factory blocks, plating, panels, pillars, encased blocks
- Glowing pillar variants for all five metals
- Alien wood sets: Glacian logs/planks/leaves, Aeronos and Strophar mushroom blocks
- Sliding doors in five metal variants
- Steel doors and trapdoors, reinforced doors, airlocks

## Mod Integrations

### Mekanism (Required)

Mekanism is the backbone of all chemical and energy systems:

- **Oxygen production** — Electrolytic Separators split water into oxygen and hydrogen
- **Chemical piping** — Connect oxygen pipes directly to Oxygen Distributors
- **Fuel conversion** — Rotary Condensentrator converts gas fuels (ethanol, propane, ethylene, methane) to liquid form for rockets
- **Automated fueling** — Mechanical pipes connected to Launch Pad bottom face auto-fuel placed rockets
- **Space suit integration** — Suits and gas tanks store oxygen/nitrogen via Mekanism chemical capability
- **Jet suit flight** — Nitrogen gas powers jet suit propulsion
- **Energy compatibility** — All machines accept both NeoForge FE and Mekanism Joules

### Create (Optional)

When Create is installed, all major crafting recipes become available through Mechanical Crafters:

- **25 mechanical crafting recipes** — All four rocket tiers, all four engine tiers, all four tank tiers, oxygen gear, gas tanks, fans, engine frames, rocket components, oxygen distributor, gravity normalizer, wireless power relay, redstone toggle relay, etrionic core, and etrionic capacitor
- **5 pressing recipes** — Steel, desh, ostrum, calorite, and etrium ingot-to-sheet conversion
- Enables full automation of rocket production lines

### Create Crafts & Additions (Optional)

- **3 rolling machine recipes** — Iron, steel, and etrium sheet-to-rod conversion

### Immersive Engineering (Optional)

- Diesel and biodiesel recognized as valid rocket fuel alternatives
- Diesel: 256 RF/mB energy value
- Biodiesel: 192 RF/mB energy value

### Born in Chaos (Optional)

- Horror mobs (spirits, undead, Halloween creatures) spawn on specific planets based on atmosphere and biome conditions

### Modded Mob Spawning

The mod includes a per-planet mob spawning system that controls which modded mobs appear on each world. Supported mods include Mowzie's Mobs, Kobolds, Born in Chaos, MC Doom, Undead Revamped, Shineals Prehistoric Expansion, Luminous World, Reptilian, and Ribbits. Spawning is configured per-planet with biome-level granularity. Manual spawns (spawn eggs, spawners, commands) are always allowed on all planets.

## Commands

| Command | Description |
|---------|-------------|
| `/planet list` | List all available planets |
| `/planet teleport <name>` | Teleport to a planet |
| `/planet info <name>` | View detailed planet information |

## Configuration

Key options in the mod config:

| Option | Default | Description |
|--------|---------|-------------|
| `enableOxygenSystem` | true | Toggle oxygen mechanics |
| `oxygenDistributorRange` | 16 | Distributor range in blocks (1-64) |
| `oxygenConsumptionRate` | 1.0 | Oxygen consumption multiplier (0.1-10.0) |
| `rocketFuelConsumptionRate` | 100 | Fuel usage rate (1-1000) |
| `enableMekanismIntegration` | true | Toggle Mekanism integration |
| `enableCreateIntegration` | true | Toggle Create integration |
| `enableImmersiveEngineeringIntegration` | true | Toggle IE integration |

## Building from Source

Requires Java 21.

```bash
./gradlew build          # Build the mod
./gradlew runClient      # Launch development client
./gradlew runServer      # Launch development server
./gradlew runData        # Run data generation
./gradlew makePlanets    # Generate planet JSON files
```

## License

All Rights Reserved.
