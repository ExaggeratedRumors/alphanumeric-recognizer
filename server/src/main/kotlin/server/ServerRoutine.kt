package com.ertools.server

import com.ertools.common.Utils
import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress

class ServerRoutine(private val port: Int) {
    private lateinit var server: HttpServer
    private var isRunning: Boolean = false
    private val statusQueue = arrayListOf<ModelStatus>()

    fun start() {
        try {
            val server = HttpServer.create(InetSocketAddress(port), 0)
            server.createContext("/", ServerHandler(statusQueue))
            server.executor = null
            server.start()
            isRunning = true
            println("I: Listening port $port ...")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun shutdown() {
        if(!isRunning) return
        server.stop(Utils.SERVER_STOP_DELAY_SECONDS)
    }
}