import com.github.jengelman.gradle.plugins.shadow.ShadowPlugin
import net.kyori.indra.IndraPlugin

plugins {
    id("net.kyori.indra")
    id("com.github.johnrengelman.shadow")
}

group = "com.nearvanilla.bat"
version = "1.1.0-SNAPSHOT"

subprojects {
    apply {
        plugin<ShadowPlugin>()
        plugin<IndraPlugin>()
    }

    repositories {
        maven("https://repo.papermc.io/repository/maven-public/")
        mavenCentral()
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
