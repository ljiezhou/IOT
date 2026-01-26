package com.android.newframework.netty.server

import com.android.newframework.netty.protocol.SocketMessage
import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.NetworkUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object NettyServerManager {

    sealed class State {
        object Idle : State()
        object Starting : State()
        data class Started(val ip: String, val port: Int) : State()
        data class ClientConnected(val channelId: String) : State()
        data class ClientDisconnected(val channelId: String) : State()
        data class Message(val channelId: String, val text: String) : State()
        data class Error(val error: Throwable) : State()
    }

    private var server: NettyServer? = null

    private val _state = MutableStateFlow<State>(State.Idle)
    val state: StateFlow<State> = _state

    fun start(port: Int) {
        if (server != null) return

        _state.value = State.Starting

        server = NettyServer(port, object : NettyServer.Callback {

            override fun onStarted(ip: String, port: Int) {
//                _state.value = State.Started(ip, port)
                _state.value = State.Started(NetworkUtils.getIPAddress(true), port)
                LogUtils.d("onStarted", ip, port,NetworkUtils.getIPAddress(true))

            }

            override fun onClientConnected(id: String) {
                _state.value = State.ClientConnected(id)
                LogUtils.d("onClientConnected", id)
            }

            override fun onClientDisconnected(id: String) {
                _state.value = State.ClientDisconnected(id)
                LogUtils.d("onClientDisconnected", id)
            }

            override fun onMessage(id: String, msg: String) {
                _state.value = State.Message(id, msg)
                LogUtils.d("onMessage", id, msg)
            }

            override fun onError(t: Throwable) {
                _state.value = State.Error(t)
                LogUtils.d("onMessage", t.message)
                t.printStackTrace()
            }
        })

        server?.start()
    }

    fun stop() {
        server?.stop()
        server = null
        _state.value = State.Idle
    }



    fun sendToClient(channelId: String, text: String) {
        server?.sendTo(channelId, text)
    }

    fun broadcast(text: SocketMessage) {
        server?.broadcast(GsonUtils.toJson(text))
    }
}