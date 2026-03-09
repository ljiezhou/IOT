package com.android.newframework.ui.detail

import android.view.LayoutInflater
import androidx.lifecycle.lifecycleScope
import com.android.newframework.databinding.ClientDetailActivityBinding
import com.android.newframework.netty.client.NettyClientManager
import com.android.newframework.netty.protocol.Action
import com.android.newframework.netty.protocol.MessageType
import com.android.newframework.netty.protocol.SocketMessage
import com.blankj.utilcode.util.GsonUtils
import com.iot.base.BaseActivity
import kotlinx.coroutines.launch

class ClientDetailActivity : BaseActivity<ClientDetailActivityBinding>() {
    override fun inflateBinding(inflater: LayoutInflater): ClientDetailActivityBinding {
        return ClientDetailActivityBinding.inflate(inflater)
    }

    override fun initView() {
        super.initView()

    }

    override fun initObserver() {
        super.initObserver()

        lifecycleScope.launch {
            NettyClientManager.state.collect { state ->
                when (state) {
                    is NettyClientManager.State.Message -> {
                        val socketMessage = GsonUtils.fromJson(state.msg, SocketMessage::class.java)
                        handleSocketMessage(socketMessage)
                    }

                    else -> {}
                }

            }
        }
    }

    private fun handleSocketMessage(message: SocketMessage) {
        when (message.type) {
            MessageType.EVENT -> {
                when (message.action) {
                    Action.ANIMATION_START -> {
                        binding.deviceStateTv.text = "Start"
                        binding.loadingView.startWaiting()
                    }

                    Action.ANIMATION_STOP -> {
                        binding.deviceStateTv.text = "Stop"
                        binding.loadingView.stop()
                    }

                    Action.ANIMATION_PAUSE -> {
                        binding.deviceStateTv.text = "Pause"
                    }

                    else -> {

                    }
                }
            }

            else -> {}
        }
    }

    companion object {
        fun action(context: android.content.Context) {
            context.startActivity(android.content.Intent(context, ClientDetailActivity::class.java))
        }
    }
}