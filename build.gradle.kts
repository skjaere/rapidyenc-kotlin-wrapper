group = "io.skjaere"
version = "0.2.0" // x-release-please-version

plugins {
    `java-library`
    `maven-publish`
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlinx.kover)
}

repositories {
    mavenCentral()
}

dependencies {
    api(libs.jna)
    testImplementation(libs.kotlin.test)
    testImplementation(libs.junit.jupiter)
}

kotlin {
    jvmToolchain(17)
}

java {
    withSourcesJar()
}

tasks.withType<Test> {
    useJUnitPlatform()
    // The tests load the bundled librapidyenc.so. RAPIDYENC_LIB_PATH points them at a folder holding another build of it.
    System.getenv("RAPIDYENC_LIB_PATH")?.let { systemProperty("jna.library.path", it) }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}
