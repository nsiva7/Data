plugins {
    kotlin("jvm") version "1.9.10"
    application
}

group = "siva.nimmala"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
}

application {
    mainClass.set("MainKt")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(11)
}

// Custom task to run the HTML generator
tasks.register("generateHtml", JavaExec::class) {
    group = "application"
    description = "Generate index.html from local Images folder"
    dependsOn("classes")

    mainClass.set("MainKt")
    classpath = sourceSets.main.get().runtimeClasspath
}