package com.android.newframework.ui

import android.view.LayoutInflater
import com.android.newframework.AppState
import com.android.newframework.databinding.ActivityMainBinding
import com.blankj.utilcode.util.ToastUtils
import com.iot.base.BaseActivity

class MainActivity : BaseActivity<ActivityMainBinding>() {
    override fun inflateBinding(inflater: LayoutInflater): ActivityMainBinding {
        return ActivityMainBinding.inflate(inflater)
    }


    override fun initView() {
        super.initView()
        binding.hostTv.isSelected = false
        binding.clientTv.isSelected = false
        binding.hostTv.setOnClickListener {
            // 点击主机 -> 主机选中，从机取消
            binding.hostTv.isSelected = true
            binding.clientTv.isSelected = false
            AppState.isHost = true
        }
        binding.clientTv.setOnClickListener {
            // 点击从机 -> 从机选中，主机取消
            binding.clientTv.isSelected = true
            binding.hostTv.isSelected = false
            AppState.isHost = false
        }
        binding.confirmTv.setOnClickListener {
            if (AppState.isHost == null) {
                ToastUtils.showShort("请选择主机或从机")
                return@setOnClickListener
            }
            if (AppState.isHost!!) {
                HostActivity.action(this)
            }
        }
    }
}