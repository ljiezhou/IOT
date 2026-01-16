package com.android.newframework.ui.client

import android.view.LayoutInflater
import com.android.newframework.databinding.ClientActivityBinding
import com.android.newframework.netty.client.NettyClientManager
import com.iot.base.BaseActivity

class ClientActivity : BaseActivity<ClientActivityBinding>() {
    override fun inflateBinding(inflater: LayoutInflater): ClientActivityBinding {
        return ClientActivityBinding.inflate(inflater)
    }

    override fun initView() {
        super.initView()

        binding.connectTv.setOnClickListener {
            NettyClientManager.connect(binding.ipEt.text.toString().trim(), 8080)
        }
    }


    companion object {
        fun action(context: android.content.Context) {
            context.startActivity(android.content.Intent(context, ClientActivity::class.java))
        }
    }
}