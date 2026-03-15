package com.android.newframework.ui.host.detail

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.android.newframework.AppState
import com.android.newframework.R
import com.android.newframework.databinding.DetailActivityBinding
import com.android.newframework.netty.callback.DataCallback
import com.android.newframework.netty.client.NettyClientManager
import com.android.newframework.netty.protocol.Action
import com.android.newframework.netty.protocol.MessageType
import com.android.newframework.netty.protocol.SocketMessage
import com.android.newframework.netty.protocol.TextMessagePayload
import com.android.newframework.netty.server.NettyServerManager
import com.blankj.utilcode.util.StringUtils
import com.iot.base.BaseActivity
import kotlinx.coroutines.launch

class HostDetailActivity : BaseActivity<DetailActivityBinding>() {
    private val mViewModel by lazy { ViewModelProvider(this)[HostDetailViewModel::class.java] }
    private val mAdapter = HostDetailAdapter()
    override fun inflateBinding(inflater: LayoutInflater): DetailActivityBinding {
        return DetailActivityBinding.inflate(inflater)
    }

    override fun initView() {
        super.initView()
        binding.cmdRv.adapter = mAdapter
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
        mAdapter.setOnItemClickListener { adapter, view, i ->
            val item = mAdapter.getItem(i) ?: return@setOnItemClickListener
            handleCommand(item.title)
        }
    }

    fun handleCommand(title: String) {
        val textMessagePayload = TextMessagePayload(title)
        NettyServerManager.broadcast(SocketMessage(type = MessageType.EVENT, action = Action.TEXT_MESSAGE, payload = textMessagePayload))
    }

    override fun initObserver() {
        super.initObserver()
        mViewModel.btnItems.observe(this) {
            mAdapter.submitList(it)
        }
    }

    override fun initData() {
        super.initData()
        NettyServerManager.registerCallback(dataCallback)

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

    private val dataCallback = object : DataCallback {
        override fun onDataSent(channelId: String?, text: String) {
            binding.logTv.append("Sent to ${channelId ?: "all"}: $text\n")
        }

        override fun onDataReceived(channelId: String, text: String) {
            binding.logTv.append("Received from $channelId: $text\n")
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        NettyServerManager.unregisterCallback(dataCallback)
    }

    companion object {
        fun action(context: Context) {
            context.startActivity(Intent(context, HostDetailActivity::class.java))
        }
    }
}