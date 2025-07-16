// Vị trí: build.gradle.kts (của dự án CMR-Server)
// SỬA LỖI: Sử dụng phiên bản cố định thay vì biến.

plugins {
    kotlin("jvm") version "1.9.23"
    id("io.ktor.plugin") version "2.3.11"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.23"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "com.example"
version = "0.0.1"

application {
    mainClass.set("com.example.ApplicationKt")
}

tasks.shadowJar {
    archiveBaseName.set("CMR-Server")
    archiveClassifier.set("all")
    manifest {
        attributes["Main-Class"] = "com.example.ApplicationKt"
    }
}

repositories {
    mavenCentral()
}

dependencies {
    val ktorVersion = "2.3.11"
    val logbackVersion = "1.4.14" // Sử dụng phiên bản ổn định gần đây

    // Các thư viện Ktor cơ bản
    implementation("io.ktor:ktor-server-core-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-netty-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-websockets-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:$ktorVersion")
    implementation("ch.qos.logback:logback-classic:$logbackVersion")

    // Thư viện kiểm thử
    testImplementation("io.ktor:ktor-server-tests-jvm:$ktorVersion")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:1.9.23")
}