package com.android.newframework.netty.server

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter

class ServerHandler(
    private val callback: NettyServer.Callback
) : ChannelInboundHandlerAdapter() {

    override fun channelActive(ctx: ChannelHandlerContext) {
        callback.onClientConnected(ctx.channel().id().asShortText())
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        callback.onClientDisconnected(ctx.channel().id().asShortText())
    }

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        if (msg is String) {
            callback.onMessage(
                ctx.channel().id().asShortText(),
                msg
            )
        }
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        callback.onError(cause)
        ctx.close()
    }
}