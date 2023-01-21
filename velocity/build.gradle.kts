dependencies {
    compileOnly(libs.velocity.api)
    annotationProcessor(libs.velocity.api)

    compileOnly(libs.luckperms)

    implementation(libs.cloud.velocity)
    implementation(libs.cloud.annotations)
    implementation(libs.configurate.hocon)
}

tasks {
    build {
        dependsOn(named("shadowJar"))
    }

    compileJava {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }

    shadowJar {
        archiveClassifier.set(null as String?)
        archiveFileName.set(project.name + ".jar")
        destinationDirectory.set(rootProject.tasks.shadowJar.get().destinationDirectory.get())
    }
}
