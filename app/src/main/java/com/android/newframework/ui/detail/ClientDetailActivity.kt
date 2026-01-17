package com.android.newframework.ui.detail

import android.view.LayoutInflater
import androidx.lifecycle.lifecycleScope
import com.android.newframework.databinding.ClientDetailActivityBinding
import com.android.newframework.netty.client.NettyClientManager
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

                    }
                    else -> {}
                }

            }
        }
    }

    companion object {
        fun action(context: android.content.Context) {
            context.startActivity(android.content.Intent(context, ClientDetailActivity::class.java))
        }
    }
}