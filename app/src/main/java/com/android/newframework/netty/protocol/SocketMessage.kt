package com.android.newframework.netty.protocol

data class SocketMessage(
    val type: MessageType,
    val action: Action? = null,
    val requestId: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val payload: Payload? = null
)