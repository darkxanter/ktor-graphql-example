package dev.xanter

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import dev.xanter.plugins.*

fun main() {
    embeddedServer(Netty, port = 4000, host = "0.0.0.0") {
        configureRouting()
        configureSecurity()
        configureHTTP()
        configureMonitoring()
        configureSockets()
        configureGraphQL()
    }.start(wait = true)
}
