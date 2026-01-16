package com.android.newframework.netty.server

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.Channel
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import java.net.InetSocketAddress

class NettyServer(
    private val port: Int,
    private val callback: Callback
) {

    interface Callback {
        fun onStarted(ip: String, port: Int)
        fun onClientConnected(id: String)
        fun onClientDisconnected(id: String)
        fun onMessage(id: String, msg: String)
        fun onError(t: Throwable)
    }

    private val bossGroup = NioEventLoopGroup(1)
    private val workerGroup = NioEventLoopGroup()

    private var channel: Channel? = null

    fun start() {
        Thread {
            try {
                val bootstrap = ServerBootstrap()
                    .group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel::class.java)
                    .childHandler(ServerInitializer(callback))

                val future = bootstrap.bind(port).sync()
                channel = future.channel()

                if (channel?.isActive == true) {
                    val localAddress = channel?.localAddress() as? InetSocketAddress
                    val ip = localAddress?.address?.hostAddress ?: ""
                    callback.onStarted(ip, port)
                } else {
                    callback.onError(
                        IllegalStateException("Netty server start failed")
                    )
                    stop()
                    return@Thread
                }

                channel?.closeFuture()?.sync()

            } catch (e: Exception) {
                callback.onError(e)
            } finally {
                stop()
            }
        }.start()
    }

    fun stop() {
        try {
            channel?.close()
            bossGroup.shutdownGracefully()
            workerGroup.shutdownGracefully()
        } catch (_: Exception) {
        }
    }
}