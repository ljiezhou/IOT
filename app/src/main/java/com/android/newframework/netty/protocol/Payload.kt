package com.android.newframework.netty.protocol

// Payloads.kt
sealed class Payload {

    data class AnimationStart(
        val animationId: String,
        val loop: Boolean,
        val speed: Float
    ) : Payload()

    data class AnimationStop(
        val animationId: String
    ) : Payload()

    data class TextMessage(
        val from: String,
        val text: String
    ) : Payload()

    data class PageSwitch(
        val page: String
    ) : Payload()
}