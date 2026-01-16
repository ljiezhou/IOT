package com.android.newframework.netty.client

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter

class ClientHandler(
    private val callback: NettyClient.Callback
) : ChannelInboundHandlerAdapter() {

    override fun channelActive(ctx: ChannelHandlerContext) {
        // 可在这里发握手消息
        // ctx.writeAndFlush("hello")
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        callback.onDisconnected("Connection closed")
    }

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        if (msg is String) {
            callback.onMessage(msg)
        }
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        callback.onError(cause)
        ctx.close()
    }
}