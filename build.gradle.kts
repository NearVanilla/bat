import com.github.jengelman.gradle.plugins.shadow.ShadowPlugin
import net.kyori.indra.IndraPlugin

plugins {
    id("net.kyori.indra") version "4.1.0"
    id("com.gradleup.shadow") version "9.6.1"
}

group = "com.nearvanilla.bat"
version = "1.1.4-SNAPSHOT"

indra {
    javaVersions {
        target(25)
    }
}

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
                target(25)
            }
        }

        processResources {
            expand("version" to rootProject.version)
        }

    }
}
