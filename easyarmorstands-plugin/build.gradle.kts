plugins {
    id("easyarmorstands.base")
    alias(libs.plugins.shadow)
}

configurations {
    register("addon")
}

dependencies {
    compileOnly(libs.bukkit)
    api(project(":easyarmorstands-api"))
    api(libs.adventure.platform.bukkit)
    api(libs.adventure.text.minimessage)
    api(libs.adventure.text.serializer.gson)
    api(libs.adventure.text.serializer.legacy)
    api(libs.bstats)
    api(libs.cloud.annotations)
    api(libs.cloud.minecraft.extras)
    api(libs.cloud.paper)
    api(libs.joml)
    "addon"(project(":easyarmorstands-display"))
    "addon"(project(":easyarmorstands-headdatabase"))
    "addon"(project(":easyarmorstands-traincarts"))
}

tasks {
    assemble {
        dependsOn(shadowJar)
    }

    processResources {
        inputs.property("version", version)
        filesMatching("*.yml") {
            expand("version" to version)
        }
    }

    shadowJar {
        val prefix = "me.m56738.easyarmorstands.lib"
        relocate("org.joml", "$prefix.joml")
        relocate("net.kyori", "$prefix.kyori")
        relocate("cloud.commandframework", "$prefix.cloud")
        relocate("io.leangen.geantyref", "$prefix.geantyref")
        relocate("me.lucko.commodore", "$prefix.commodore")
        relocate("org.bstats", "$prefix.bstats")
        dependencies {
            exclude(dependency("com.google.code.gson:gson"))
        }
        configurations += project.configurations.getByName("addon")
        mergeServiceFiles()
    }
}

java {
    disableAutoTargetJvm()
}

fun registerSourceSet(name: String) {
    val sourceSet = sourceSets.register(name) {
        compileClasspath += sourceSets.main.get().output
        runtimeClasspath += sourceSets.main.get().output
    }

    configurations.named("${name}Implementation") {
        extendsFrom(configurations.implementation.get())
    }

    tasks {
        shadowJar {
            from(sourceSet.map { it.output })
            archiveBaseName.set("EasyArmorStands")
            archiveClassifier.set("")
            destinationDirectory.set(layout.buildDirectory)
        }
    }
}

fun registerVersion(name: String, api: String) {
    registerSourceSet(name)
    dependencies {
        "${name}CompileOnly"(api) {
            exclude("net.kyori", "adventure-api")
        }
    }
}

fun registerAddon(name: String, api: Any = libs.bukkit) {
    registerSourceSet(name)
    dependencies {
        "${name}CompileOnly"(api)
    }
}

registerVersion("v1_8", "org.bukkit:bukkit:1.8.8-R0.1-SNAPSHOT")
registerVersion("v1_8_spigot", "org.spigotmc:spigot-api:1.8.8-R0.1-SNAPSHOT")
registerVersion("v1_9", "org.bukkit:bukkit:1.9-R0.1-SNAPSHOT")
registerVersion("v1_11", "org.bukkit:bukkit:1.11-R0.1-SNAPSHOT")
registerVersion("v1_12", "org.bukkit:bukkit:1.12-R0.1-SNAPSHOT")
registerVersion("v1_12_paper", "com.destroystokyo.paper:paper-api:1.12.2-R0.1-SNAPSHOT")
registerVersion("v1_13", "org.bukkit:bukkit:1.13-R0.1-SNAPSHOT")
registerVersion("v1_13_2", "org.bukkit:bukkit:1.13.2-R0.1-SNAPSHOT")
registerVersion("v1_14", "org.bukkit:bukkit:1.14-R0.1-SNAPSHOT")
registerVersion("v1_16", "org.spigotmc:spigot-api:1.16.5-R0.1-SNAPSHOT")
registerVersion("v1_16_paper", "com.destroystokyo.paper:paper-api:1.16.5-R0.1-SNAPSHOT")
registerVersion("v1_18", "org.spigotmc:spigot-api:1.18-R0.1-SNAPSHOT")
registerVersion("v1_18_paper", "io.papermc.paper:paper-api:1.18-R0.1-SNAPSHOT")
registerVersion("v1_19_4", "org.spigotmc:spigot-api:1.19.4-R0.1-SNAPSHOT")

registerAddon("worldguard_v6")
registerAddon("worldguard_v7", "org.bukkit:bukkit:1.13-R0.1-SNAPSHOT")

dependencies {
    "worldguard_v6CompileOnly"(libs.worldguard.v6)
    "worldguard_v7CompileOnly"(libs.worldguard.v7)
}
