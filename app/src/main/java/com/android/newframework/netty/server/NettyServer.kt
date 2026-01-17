package com.android.newframework.netty.server

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.Channel
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import java.net.InetSocketAddress
import java.util.concurrent.ConcurrentHashMap

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

    private var serverChannel: Channel? = null

    /** 已连接客户端 */
    private val channels = ConcurrentHashMap<String, Channel>()

    // =========================
    // 生命周期
    // =========================

    fun start() {
        Thread {
            try {
                val bootstrap = ServerBootstrap()
                    .group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel::class.java)
                    .childHandler(ServerInitializer(this, callback))

                val bindFuture = bootstrap.bind(port).sync()
                serverChannel = bindFuture.channel()

                if (serverChannel?.isActive == true) {
                    val address = serverChannel?.localAddress() as InetSocketAddress
                    val ip = address.address.hostAddress ?: "0.0.0.0"
                    callback.onStarted(ip, port)
                } else {
                    throw IllegalStateException("Netty server start failed")
                }

                // 阻塞直到关闭
                serverChannel?.closeFuture()?.sync()

            } catch (e: Exception) {
                callback.onError(e)
            } finally {
                stop()
            }
        }.start()
    }

    fun stop() {
        try {
            serverChannel?.close()
            channels.values.forEach { it.close() }
            channels.clear()
            bossGroup.shutdownGracefully()
            workerGroup.shutdownGracefully()
        } catch (_: Exception) {
        }
    }

    // =========================
    // Channel 管理（给 Handler 调）
    // =========================

    internal fun addChannel(channel: Channel) {
        val id = channel.id().asShortText()
        channels[id] = channel
        callback.onClientConnected(id)
    }

    internal fun removeChannel(channel: Channel) {
        val id = channel.id().asShortText()
        channels.remove(id)
        callback.onClientDisconnected(id)
    }

    // =========================
    // 对外通信 API
    // =========================

    /** 单播 */
    fun sendTo(clientId: String, msg: String) {
        channels[clientId]?.let {
            if (it.isActive) {
                it.writeAndFlush(msg + "\n")
            }
        }
    }

    /** 广播 */
    fun broadcast(msg: String) {
        channels.values.forEach {
            if (it.isActive) {
                it.writeAndFlush(msg + "\n")
            }
        }
    }

    fun getClientCount(): Int = channels.size

    fun getClientIds(): List<String> = channels.keys.toList()
}