group = "io.skjaere"
version = "0.1.0" // x-release-please-version

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

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
    withSourcesJar()
}

tasks.withType<Test> {
    useJUnitPlatform()
    systemProperty(
        "jna.library.path",
        System.getenv("RAPIDYENC_LIB_PATH")
            ?: "/home/william/IdeaProjects/rapidyenc/build"
    )
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}
