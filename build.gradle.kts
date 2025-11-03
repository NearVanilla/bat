import com.github.jengelman.gradle.plugins.shadow.ShadowPlugin
import net.kyori.indra.IndraPlugin

plugins {
    id("net.kyori.indra") version "3.2.0"
    id("com.gradleup.shadow") version "9.0.0"
}

group = "com.nearvanilla.bat"
version = "1.1.1-SNAPSHOT"

subprojects {
    apply {
        plugin<ShadowPlugin>()
        plugin<IndraPlugin>()
    }

    repositories {
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://repo.sayandev.org/snapshots")
        mavenCentral()
        mavenLocal()
    }

    tasks {

        indra {
            gpl3OnlyLicense()

            javaVersions {
                target(21)
            }
        }

        processResources {
            expand("version" to rootProject.version)
        }

    }
}
