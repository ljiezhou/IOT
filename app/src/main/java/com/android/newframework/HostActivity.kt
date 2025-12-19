package com.android.newframework

import android.net.wifi.p2p.WifiP2pDevice
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.android.newframework.databinding.ActivityHostBinding

class HostActivity : AppCompatActivity() {

    private lateinit var wifiManager: WifiDirectManager
    private lateinit var binding: ActivityHostBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        wifiManager = WifiDirectManager(this)

        // 注册 Wi-Fi Direct 广播
        wifiManager.register()

        wifiManager.events = object : WiFiEvents {

            override fun onStateChanged(state: String) {
                binding.tvStatus.text = state
            }

            override fun onPeersAvailable(list: List<WifiP2pDevice>) {
                binding.tvPeers.text = "找到设备: ${list.size}"

                // 选择第一个设备连接
                if (list.isNotEmpty()) {
                    val dev = list.first()
                    wifiManager.connectToDevice(dev)
                }
            }

            override fun onRoleChanged(role: P2PConnectionRole) {
                binding.tvRole.text = "角色: $role"
            }

            override fun onClientConnected() {
                binding.tvStatus.text = "从机已连接"
            }

            override fun onMessageReceived(msg: String) {
                runOnUiThread {
                    binding.tvMessages.append("Client: $msg\n")
                }
            }
        }

        // 开始搜索设备
        wifiManager.startDiscovery()

        // 发送按钮
        binding.btnSend.setOnClickListener {
            val msg = binding.etMessage.text.toString()
            wifiManager.send(msg)
            binding.tvMessages.append("Host: $msg\n")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        wifiManager.unregister()
    }
}