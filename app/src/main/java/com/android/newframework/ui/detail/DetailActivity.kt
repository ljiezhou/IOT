package com.android.newframework.ui.detail

import android.view.LayoutInflater
import androidx.lifecycle.lifecycleScope
import com.android.newframework.AppState
import com.android.newframework.databinding.DetailActivityBinding
import com.android.newframework.netty.callback.DataCallback
import com.android.newframework.netty.client.NettyClientManager
import com.android.newframework.netty.protocol.Action
import com.android.newframework.netty.protocol.MessageType
import com.android.newframework.netty.protocol.SocketMessage
import com.android.newframework.netty.server.NettyServerManager
import com.iot.base.BaseActivity
import kotlinx.coroutines.launch

class DetailActivity : BaseActivity<DetailActivityBinding>() {
    override fun inflateBinding(inflater: LayoutInflater): DetailActivityBinding {
        return DetailActivityBinding.inflate(inflater)
    }

    override fun initView() {
        super.initView()
        if (AppState.isHost == true) {
            lifecycleScope.launch {
                NettyServerManager.state.collect { state ->
                    when (state) {
                        is NettyServerManager.State.Message -> {

                        }

                        else -> {

                        }
                    }

                }
            }
        } else {
            lifecycleScope.launch {
                NettyClientManager.state.collect { state ->
                    when (state) {


                        is NettyClientManager.State.Message -> {

                        }

                        else -> {

                        }
                    }
                }
            }
        }
    }

    override fun initListener() {
        super.initListener()
        binding.btnStart.setOnClickListener {
            NettyServerManager.broadcast(SocketMessage(type = MessageType.EVENT, action = Action.ANIMATION_START))
        }
        binding.btnStop.setOnClickListener {
            NettyServerManager.broadcast(SocketMessage(type = MessageType.EVENT, action = Action.ANIMATION_STOP))
        }
        binding.btnPause.setOnClickListener {
            NettyServerManager.broadcast(SocketMessage(type = MessageType.EVENT, action = Action.ANIMATION_PAUSE))
        }

    }

    private val dataCallback = object : DataCallback {
        override fun onDataSent(channelId: String?, text: String) {
            binding.logTv.append("Sent to ${channelId ?: "all"}: $text\n")
        }

        override fun onDataReceived(channelId: String, text: String) {
            binding.logTv.append("Received from $channelId: $text\n")
        }

    }

    override fun initData() {
        super.initData()
        NettyServerManager.registerCallback(dataCallback)
    }

    override fun onDestroy() {
        super.onDestroy()
        NettyServerManager.unregisterCallback(dataCallback)
    }

    companion object {
        fun action(context: android.content.Context) {
            context.startActivity(android.content.Intent(context, DetailActivity::class.java))
        }
    }
}