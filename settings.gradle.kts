rootProject.name = "bat"

include("velocity")
project(":velocity").name = "bat-velocity"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://repo.stellardrift.ca/repository/snapshots/")
    }
}

plugins {
    id("ca.stellardrift.polyglot-version-catalogs") version "5.0.0-SNAPSHOT"
}
