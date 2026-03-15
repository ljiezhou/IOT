package com.android.newframework.ui.client.connect

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import androidx.lifecycle.lifecycleScope
import com.android.newframework.databinding.ClientActivityBinding
import com.android.newframework.netty.client.NettyClientManager
import com.android.newframework.ui.client.detail.ClientDetailActivity
import com.iot.base.BaseActivity
import kotlinx.coroutines.launch

class ClientActivity : BaseActivity<ClientActivityBinding>() {
    override fun inflateBinding(inflater: LayoutInflater): ClientActivityBinding {
        return ClientActivityBinding.inflate(inflater)
    }

    override fun initView() {
        super.initView()

        lifecycleScope.launch {
            NettyClientManager.state.collect { state ->
                when (state) {
                    is NettyClientManager.State.Connected -> {
                        ClientDetailActivity.Companion.action(this@ClientActivity)
                    }

                    else -> {

                    }
                }
            }
        }
    }

    override fun initListener() {
        super.initListener()
        binding.connectTv.setOnClickListener {
            NettyClientManager.connect(binding.ipEt.text.toString().trim(), 8888)
        }
        binding.backTv.setOnClickListener {
            finish()
        }
    }


    companion object {
        fun action(context: Context) {
            context.startActivity(Intent(context, ClientActivity::class.java))
        }
    }
}