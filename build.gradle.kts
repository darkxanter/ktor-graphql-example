@file:Suppress("PropertyName", "VariableNaming")

val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project
val graphql_kotlin_version: String by project
val graphql_kotlin_plugin_version: String by project
val exposed_version: String by project

plugins {
    application
    kotlin("jvm") version "1.6.21"
    id("org.jetbrains.dokka") version "1.6.21"
    id("com.expediagroup.graphql") version "5.3.2"
}

group = "dev.xanter"
version = "0.1.0"

application {
    mainClass.set("dev.xanter.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
}

graphql {
    schema {
        packages = listOf("dev.xanter")
    }
}

dependencies {
    implementation("io.ktor:ktor-server-core-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-auth-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-sessions-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-content-negotiation:$ktor_version")
    implementation("io.ktor:ktor-server-compression-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-call-logging-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-websockets-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-netty-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-html-builder:$ktor_version")
    implementation("io.ktor:ktor-serialization-jackson:$ktor_version")

    implementation("ch.qos.logback:logback-classic:$logback_version")

    implementation("org.jetbrains.exposed:exposed-core:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-dao:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposed_version")

    implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlin_version")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.6.3")

    runtimeOnly("org.xerial:sqlite-jdbc:3.36.0.3")

    implementation("io.github.darkxanter.graphql", "graphql-kotlin-ktor-plugin", graphql_kotlin_plugin_version)

    implementation("com.expediagroup:graphql-kotlin-server:$graphql_kotlin_version")
    implementation("com.expediagroup:graphql-kotlin-schema-generator:$graphql_kotlin_version")
    implementation("com.expediagroup:graphql-kotlin-hooks-provider:$graphql_kotlin_version")
//    graphqlSDL("com.expediagroup:graphql-kotlin-federated-hooks-provider:$graphql_kotlin_version")
//    implementation("com.expediagroup:graphql-kotlin-dataloader:$graphql_kotlin_version")


    testImplementation("io.ktor:ktor-server-tests-jvm:$ktor_version")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
}

tasks {
    graphqlGenerateSDL {
        schemaFile.set(File(projectDir, "schema.graphql"))
    }
}
