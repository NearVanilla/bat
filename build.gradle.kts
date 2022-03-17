import com.github.jengelman.gradle.plugins.shadow.ShadowPlugin
import net.kyori.indra.IndraPlugin

plugins {
    id("net.kyori.indra") version "2.0.5"
    id("com.github.johnrengelman.shadow") version "7.0.0"
}

group = "com.nearvanilla.bat"
version = "1.0.2-SNAPSHOT"

subprojects {
    apply {
        plugin<ShadowPlugin>()
        plugin<IndraPlugin>()
    }

    repositories {
        maven("https://nexus.velocitypowered.com/repository/maven-public/")
        mavenCentral()
        mavenLocal()
    }

    tasks {

        indra {
            gpl3OnlyLicense()

            javaVersions {
                target(16)
            }
        }

        processResources {
            expand("version" to rootProject.version)
        }

    }
}
