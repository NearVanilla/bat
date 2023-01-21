import com.github.jengelman.gradle.plugins.shadow.ShadowPlugin
import net.kyori.indra.IndraPlugin

plugins {
    id("net.kyori.indra") version "3.0.1"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "com.nearvanilla.bat"
version = "1.1.0-SNAPSHOT"

subprojects {
    apply {
        plugin<ShadowPlugin>()
        plugin<IndraPlugin>()
    }

    repositories {
        maven("https://papermc.io/repo/repository/maven-public/")
        mavenCentral()
        mavenLocal()
    }

    tasks {

        indra {
            gpl3OnlyLicense()

            javaVersions {
                target(17)
            }
        }

        processResources {
            expand("version" to rootProject.version)
        }

    }
}
