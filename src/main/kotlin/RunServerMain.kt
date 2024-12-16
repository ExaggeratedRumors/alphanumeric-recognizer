package com.ertools

import com.ertools.common.Utils
import com.ertools.server.ServerRoutine
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    val port = if(args.isEmpty()) Utils.SERVER_PORT else args[0].toInt()

    if(port < 0 || port > 65535) {
        println("ERROR: Incorrect input - port.")
        exitProcess(1)
    }

    ServerRoutine(port).start()
}