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
        sourceCompatibility = "21"
        targetCompatibility = "21"

        // Suppress annotation processing warnings
        options.compilerArgs.add("-Xlint:-processing")
    }

    shadowJar {
        enableAutoRelocation = true
        relocationPrefix = "${rootProject.property("group")}.${rootProject.property("name").toString().lowercase()}.lib"
        minimize()
        archiveClassifier.set("")
        // Exclude duplicate meta files to fix remap errors
        exclude("META-INF/LICENSE")
        exclude("META-INF/NOTICE")
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }
}
