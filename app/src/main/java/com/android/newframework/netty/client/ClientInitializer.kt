package com.android.newframework.netty.client

import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import io.netty.handler.codec.LengthFieldBasedFrameDecoder
import io.netty.handler.codec.LengthFieldPrepender
import io.netty.handler.codec.string.StringDecoder
import io.netty.handler.codec.string.StringEncoder
import io.netty.util.CharsetUtil

class ClientInitializer(
    private val callback: NettyClient.Callback
) : ChannelInitializer<SocketChannel>() {

    override fun initChannel(ch: SocketChannel) {
        ch.pipeline().apply {
            addLast(
                LengthFieldBasedFrameDecoder(
                    1024 * 1024,
                    0, 4, 0, 4
                )
            )
            addLast(LengthFieldPrepender(4))
            addLast(StringDecoder(CharsetUtil.UTF_8))
            addLast(StringEncoder(CharsetUtil.UTF_8))
            addLast(ClientHandler(callback))
        }
    }
}