package com.android.newframework.netty.client

import io.netty.bootstrap.Bootstrap
import io.netty.channel.Channel
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioSocketChannel

class NettyClient(
    private val host: String,
    private val port: Int,
    private val callback: Callback
) {

    interface Callback {
        fun onConnected()
        fun onDisconnected(reason: String?)
        fun onMessage(msg: String)
        fun onError(t: Throwable)
    }

    private val group = NioEventLoopGroup()
    private var channel: Channel? = null

    fun connect() {
        Thread {
            try {
                val bootstrap = Bootstrap()
                    .group(group)
                    .channel(NioSocketChannel::class.java)
                    .handler(ClientInitializer(callback))

                val future = bootstrap.connect(host, port).sync()
                channel = future.channel()

                if (channel?.isActive == true) {
                    callback.onConnected()
                } else {
                    callback.onError(
                        IllegalStateException("Client connect failed")
                    )
                }

                channel?.closeFuture()?.sync()
            } catch (e: Exception) {
                e.printStackTrace()
                callback.onError(e)
            } finally {
                disconnect()
            }
        }.start()
    }

    fun send(msg: String) {
        channel?.writeAndFlush(msg)
    }

    fun disconnect() {
        try {
            channel?.close()
            group.shutdownGracefully()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}