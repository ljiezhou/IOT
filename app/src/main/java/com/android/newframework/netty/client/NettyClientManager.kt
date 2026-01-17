package com.android.newframework.netty.client

import com.blankj.utilcode.util.LogUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object NettyClientManager {

    sealed class State {
        object Idle : State()
        object Connecting : State()
        data class Connected(val host: String, val port: Int) : State()
        data class Disconnected(val reason: String? = null) : State()
        data class Message(val msg: String) : State()
        data class Error(val throwable: Throwable) : State()
    }

    private val _state = MutableStateFlow<State>(State.Idle)
    val state: StateFlow<State> = _state

    private var client: NettyClient? = null

    fun connect(host: String, port: Int) {
        if (client != null) return

        _state.value = State.Connecting

        client = NettyClient(
            host = host,
            port = port,
            callback = object : NettyClient.Callback {
                override fun onConnected() {
                    _state.value = State.Connected(host, port)
                    LogUtils.d("onConnected")
                }

                override fun onDisconnected(reason: String?) {
                    _state.value = State.Disconnected(reason)
                    client = null
                    LogUtils.d("onDisconnected")
                }

                override fun onMessage(msg: String) {
                    _state.value = State.Message(msg)
                    LogUtils.d("onMessage",msg)
                }

                override fun onError(t: Throwable) {
                    _state.value = State.Error(t)
                    LogUtils.d("onError")
                    t.printStackTrace()
                }
            }
        )

        client?.connect()
    }

    fun send(msg: String) {
        client?.send(msg)
    }

    fun disconnect() {
        client?.disconnect()
        client = null
        _state.value = State.Idle
    }
}