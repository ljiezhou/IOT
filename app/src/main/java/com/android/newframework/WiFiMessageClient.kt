package com.android.newframework

import java.net.InetSocketAddress
import java.net.Socket
import java.util.concurrent.Executors

class WiFiMessageClient(
    private val host: String,
    private val port: Int,
    private val onMessage: (String) -> Unit
) {
    private var socket: Socket? = null
    private val executor = Executors.newCachedThreadPool()

    fun start() {
        executor.execute {
            socket = Socket()
            socket!!.connect(InetSocketAddress(host, port), 5000)
            listenMessage()
        }
    }

    private fun listenMessage() {
        executor.execute {
            val input = socket!!.getInputStream()
            val buffer = ByteArray(1024)

            while (true) {
                val count = input.read(buffer)
                if (count == -1) break
                onMessage(String(buffer, 0, count))
            }
        }
    }

    fun sendToServer(msg: String) {
        executor.execute {
            socket?.getOutputStream()?.apply {
                write(msg.toByteArray())
                flush()
            }
        }
    }
}