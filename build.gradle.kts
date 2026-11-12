group = "io.skjaere"
version = "0.4.0" // x-release-please-version

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
            pom {
                licenses {
                    license {
                        name = "PolyForm Noncommercial License 1.0.0"
                        url = "https://polyformproject.org/licenses/noncommercial/1.0.0"
                    }
                }
            }
        }
    }
}

// The jars carry the licence and the third-party notices in a folder of their own under META-INF, so they
// don't clash with another library's in an app that packs several.
tasks.withType<Jar>().configureEach {
    into("META-INF/${project.name}") {
        from("LICENSE.md", "THIRD_PARTY_NOTICES.md")
        into("LICENSES") { from("LICENSES") }
    }
}
