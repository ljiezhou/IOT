package com.android.newframework

import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.Executors

class WiFiMessageServer(
    private val port: Int,
    private val onMessage: (String) -> Unit
) {
    private var server: ServerSocket? = null
    private var client: Socket? = null
    private val executor = Executors.newCachedThreadPool()

    fun start() {
        executor.execute {
            server = ServerSocket(port)
            client = server!!.accept()

            listenMessage()
        }
    }

    private fun listenMessage() {
        executor.execute {
            val input = client!!.getInputStream()
            val buffer = ByteArray(1024)

            while (true) {
                val count = input.read(buffer)
                if (count == -1) break
                onMessage(String(buffer, 0, count))
            }
        }
    }

    fun sendToClient(msg: String) {
        executor.execute {
            client?.getOutputStream()?.apply {
                write(msg.toByteArray())
                flush()
            }
        }
    }
}