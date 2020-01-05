import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

apply(plugin = "com.github.johnrengelman.shadow")

plugins {
    val kotlinVersion = "1.3.50"
    // Apply the Kotlin JVM plugin to add support for Kotlin on the JVM.
    id("org.jetbrains.kotlin.jvm").version(kotlinVersion)

    application
}

buildscript {
  repositories {
    maven {
      url = uri("https://plugins.gradle.org/m2/")
    }
  }
  dependencies {
    classpath("com.github.jengelman.gradle.plugins:shadow:5.1.0")
  }
}

repositories {
    mavenCentral()
}

val vertxVersion = "3.8.3"
val kmongoVersion = "3.11.1"

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("io.vertx:vertx-core:${vertxVersion}")
    implementation("io.vertx:vertx-web:${vertxVersion}")
    implementation("io.vertx:vertx-lang-kotlin:${vertxVersion}")
    implementation("io.vertx:vertx-lang-kotlin-coroutines:${vertxVersion}")
    implementation("io.vertx:vertx-web-client:${vertxVersion}")
    implementation("org.litote.kmongo:kmongo-async:${kmongoVersion}")
    implementation("org.litote.kmongo:kmongo-coroutine:${kmongoVersion}")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.9.9")
    implementation("commons-codec:commons-codec:1.13")
    implementation("org.slf4j:slf4j-api:1.7.29")
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:2.12.1")

    implementation(kotlin("stdlib-jdk8"))

    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
    testImplementation("org.junit.jupiter:junit-jupiter:5.5.2")
}

tasks.test {
    useJUnitPlatform()
}

application {
    // Define the main class for the application.
    mainClassName = "nerd.cave.AppKt"
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "11"
    }
}

task("stage"){
    dependsOn("build", "clean")
}
tasks["build"].mustRunAfter(tasks["clean"])