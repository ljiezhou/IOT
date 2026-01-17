package com.android.newframework.ui.host

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.android.newframework.databinding.HostActivityBinding
import com.android.newframework.netty.server.NettyServerManager
import com.android.newframework.ui.detail.DetailActivity
import com.iot.base.BaseActivity
import kotlinx.coroutines.launch

class HostActivity : BaseActivity<HostActivityBinding>() {
    private val viewModel: HostViewModel by lazy { HostViewModel() }
    override fun inflateBinding(inflater: LayoutInflater): HostActivityBinding {
        return HostActivityBinding.inflate(inflater)
    }


    override fun initView() {
        super.initView()
        binding.loadingIv.startWaiting()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.serverState.collect { state ->
                    when (state) {
                        is NettyServerManager.State.Started -> {
//                            loadingView.startWaiting()
                            binding.ipTv.text = state.ip
                            binding.portTv.text = state.port.toString()
                        }

                        is NettyServerManager.State.Started ->{
                        }
                        is NettyServerManager.State.ClientConnected -> {
//                            loadingView.showConnected()
                            DetailActivity.action(this@HostActivity)
                        }

                        is NettyServerManager.State.ClientDisconnected -> {
//                            loadingView.startWaiting()
                        }

                        else -> {}
                    }
                }
            }
        }
        NettyServerManager.start(8080)
    }

    companion object {
        fun action(context: Context) {
            context.startActivity(Intent(context, HostActivity::class.java))
        }
    }
}