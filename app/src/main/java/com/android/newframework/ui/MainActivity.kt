package com.android.newframework.ui

import android.view.LayoutInflater
import androidx.core.view.isVisible
import com.android.newframework.AppState
import com.android.newframework.R
import com.android.newframework.databinding.ActivityMainBinding
import com.android.newframework.ui.client.connect.ClientActivity
import com.android.newframework.ui.client.info.InfoFragment
import com.android.newframework.ui.host.HostActivity
import com.android.newframework.widget.setInfo
import com.android.newframework.widget.setSelectedState
import com.blankj.utilcode.util.FragmentUtils
import com.blankj.utilcode.util.ToastUtils
import com.iot.base.BaseActivity

class MainActivity : BaseActivity<ActivityMainBinding>() {
    override fun inflateBinding(inflater: LayoutInflater): ActivityMainBinding {
        return ActivityMainBinding.inflate(inflater)
    }

    override fun initView() {
        super.initView()
        binding.hostTv.setSelectedState(false)
        binding.hostTv.setInfo("主机", "创建连接")
        binding.clientTv.setSelectedState(false)
        binding.clientTv.setInfo("从机", "加入设备")

        binding.hostTv.root.setOnClickListener {
            // 点击主机 -> 主机选中，从机取消
            binding.hostTv.setSelectedState(true)
            binding.clientTv.setSelectedState(false)
            AppState.isHost = true
        }
        binding.clientTv.root.setOnClickListener {
            // 点击从机 -> 从机选中，主机取消
            binding.clientTv.setSelectedState(true)
            binding.hostTv.setSelectedState(false)
            AppState.isHost = false
        }
        binding.confirmTv.setOnClickListener {
            if (AppState.isHost == null) {
                ToastUtils.showShort("请选择主机或从机")
                return@setOnClickListener
            }
            if (AppState.isHost!!) {
                HostActivity.action(this)
            } else {
                ClientActivity.action(this)
            }
        }

//        FragmentUtils.add(supportFragmentManager, InfoFragment(), R.id.container)
//        binding.container.isVisible = true
    }
}