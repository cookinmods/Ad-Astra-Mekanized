plugins {
    id("java")
    id("eclipse")
    id("idea")
    id("maven-publish")
    id("net.neoforged.moddev") version "2.0.88"
}

version = project.findProperty("mod_version") ?: "1.0.0"
group = project.findProperty("mod_group_id") ?: "com.hecookin.adastramekanized"

val minecraftVersion = project.findProperty("minecraft_version") ?: "1.21.1"
val neoVersion = project.findProperty("neo_version") ?: "21.1.209"

base {
    archivesName = project.findProperty("mod_id") as String? ?: "adastramekanized"
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
}

neoForge {
    // Enable NeoForge for this project
    enable {
        version = neoVersion
        enabledSourceSets = sourceSets
    }

    mods {
        register("adastramekanized") {
            sourceSet(sourceSets.main.get())
        }
    }

    runs {
        configureEach {
            gameDirectory = file("run")

            // Enhanced class redefinition for JetBrains Runtime
            if (javaToolchains.launcherFor(java.toolchain).map {
                it.metadata.vendor
            }.getOrElse("").contains("JetBrains")) {
                jvmArguments.addAll(
                    "-XX:+IgnoreUnrecognizedVMOptions",
                    "-XX:+AllowEnhancedClassRedefinition"
                )
            }
        }

        register("client") {
            client()
            systemProperty("neoforge.enabledGameTestNamespaces", "adastramekanized")
        }

        register("server") {
            server()
            systemProperty("neoforge.enabledGameTestNamespaces", "adastramekanized")
            programArguments.addAll("--nogui")
        }

        register("gameTestServer") {
            type = "gameTestServer"
            systemProperty("neoforge.enabledGameTestNamespaces", "adastramekanized")
        }

        register("data") {
            data()
            programArguments.addAll(
                "--mod", "adastramekanized",
                "--all",
                "--output", file("src/generated/resources/").absolutePath,
                "--existing", file("src/main/resources/").absolutePath
            )
        }
    }

    // Unit testing support
    unitTest {
        enable()
        testedMod = mods.named("adastramekanized")
    }
}

sourceSets {
    main {
        resources {
            srcDir("src/generated/resources")
            exclude(".cache")
        }
    }
}

repositories {
    // Local Maven repository for development dependencies
    mavenLocal()
    mavenCentral()

    // NeoForged Maven (includes Mekanism)
    maven {
        name = "NeoForged"
        url = uri("https://maven.neoforged.net/releases")
    }

    // ModMaven for various mods
    maven {
        name = "ModMaven"
        url = uri("https://modmaven.dev")
    }

    // CurseForge Maven for Create and other mods
    maven {
        name = "CurseMaven"
        url = uri("https://cursemaven.com")
        content {
            includeGroup("curse.maven")
        }
    }

    // BlameJared Maven for JEI and other mods
    maven {
        name = "BlameJared"
        url = uri("https://maven.blamejared.com")
    }

    // Local libs directory (fallback)
    flatDir {
        dirs("libs")
    }

    // Parchment mappings
    maven {
        name = "ParchmentMC"
        url = uri("https://maven.parchmentmc.org")
    }
}

dependencies {
    // Core dependencies
    implementation("net.neoforged:neoforge:${neoVersion}")

    // Local Maven dependencies for development
    // Mekanism core and modules
    compileOnly("dev.local:mekanism:10.7.14")
    runtimeOnly("dev.local:mekanism:10.7.14")

    compileOnly("dev.local:mekanism-generators:10.7.14")
    runtimeOnly("dev.local:mekanism-generators:10.7.14")

    // Immersive Engineering
    compileOnly("dev.local:immersive-engineering:12.4.3")
    runtimeOnly("dev.local:immersive-engineering:12.4.3")

    // Create mod from CurseForge - NeoForge 1.21.1 version
    compileOnly("curse.maven:create-328085:6365883") // Create NeoForge 1.21.1
    runtimeOnly("curse.maven:create-328085:6365883")

    // JEI for recipe integration (optional but recommended)
    compileOnly("mezz.jei:jei-1.21.1-neoforge-api:19.21.0.246")
    runtimeOnly("mezz.jei:jei-1.21.1-neoforge:19.21.0.246")

    // TECTONIC WORLDGEN SYSTEM (REQUIRED dependencies for planet generation)
    // Tectonic for NeoForge 1.21.1
    implementation("curse.maven:tectonic-686836:7128220")

    // Lithostitched (required by Tectonic) - using latest
    implementation("curse.maven:lithostitched-936015:7063201")

    // Testing dependencies
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.1")
}

tasks.test {
    useJUnitPlatform()
}

tasks.processResources {
    val replaceProperties = mapOf(
        "mod_version" to version.toString()
    )

    inputs.properties(replaceProperties)

    filesMatching("META-INF/neoforge.mods.toml") {
        expand(replaceProperties)
    }
}

publishing {
    publications {
        register<MavenPublication>("maven") {
            from(components["java"])
        }
    }

    repositories {
        // Add publishing repositories here
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.compilerArgs.addAll(listOf("-Xmaxerrs", "1000"))
}