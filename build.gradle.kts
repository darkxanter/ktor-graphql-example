@file:Suppress("PropertyName")

val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project
val graphql_kotlin_version: String by project

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
    implementation("io.ktor:ktor-server-compression-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-call-logging-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-websockets-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-netty-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-html-builder:$ktor_version")

    implementation("ch.qos.logback:logback-classic:$logback_version")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.6.1")
//    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.6.1")

//    implementation("io.projectreactor:reactor-core:3.4.17")
//    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions:1.1.6")

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
