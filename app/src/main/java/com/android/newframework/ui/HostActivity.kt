package com.android.newframework.ui

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import com.android.newframework.databinding.HostActivityBinding
import com.iot.base.BaseActivity

class HostActivity  : BaseActivity<HostActivityBinding>() {
    override fun inflateBinding(inflater: LayoutInflater): HostActivityBinding {
        return HostActivityBinding.inflate(inflater)
    }


    override fun initView() {
        super.initView()
        binding.loadingIv.startWaiting()
    }

    companion object{
        fun action(context: Context){
            context.startActivity(Intent(context, HostActivity::class.java))
        }
    }
}