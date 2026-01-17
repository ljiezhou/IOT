package com.android.newframework.netty.server

import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import io.netty.handler.codec.LengthFieldBasedFrameDecoder
import io.netty.handler.codec.LengthFieldPrepender
import io.netty.handler.codec.LineBasedFrameDecoder
import io.netty.handler.codec.string.StringDecoder
import io.netty.handler.codec.string.StringEncoder
import io.netty.util.CharsetUtil

class ServerInitializer(
    private val server: NettyServer,
    private val callback: NettyServer.Callback
) : ChannelInitializer<SocketChannel>() {

    override fun initChannel(ch: SocketChannel) {
        ch.pipeline().apply {
            addLast(LineBasedFrameDecoder(1024))
            addLast(StringDecoder(CharsetUtil.UTF_8))
            addLast(StringEncoder(CharsetUtil.UTF_8))
            addLast(ServerHandler(server, callback))
        }
    }
}