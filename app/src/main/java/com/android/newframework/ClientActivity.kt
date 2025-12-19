package com.android.newframework

import android.Manifest
import android.net.wifi.p2p.WifiP2pDevice
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import com.android.newframework.databinding.ActivityClientBinding

class ClientActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "ClientActivity"
    }

    private lateinit var wifiManager: WifiDirectManager
    private lateinit var binding: ActivityClientBinding

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.NEARBY_WIFI_DEVICES])
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClientBinding.inflate(layoutInflater)
        setContentView(binding.root)

        wifiManager = WifiDirectManager(this)

        // 注册广播
        wifiManager.register()

        wifiManager.events = object : WiFiEvents {

            override fun onStateChanged(state: String) {
                binding.tvStatus.text = state
                Log.d(TAG, "onStateChanged: ")
            }

            override fun onPeersAvailable(list: List<WifiP2pDevice>) {
                binding.tvPeers.text = "发现主机 ${list.size}"

                if (list.isNotEmpty()) {
                    val host = list.first()
                    wifiManager.connectToDevice(host)
                }
            }

            override fun onRoleChanged(role: P2PConnectionRole) {
                binding.tvRole.text = "角色: $role"
            }

            override fun onConnectedToServer() {
                binding.tvStatus.text = "已连接到主机"
            }

            override fun onMessageReceived(msg: String) {
                runOnUiThread {
                    binding.tvMessages.append("Host: $msg\n")
                }
            }
        }

        // 搜索主机
        wifiManager.startDiscovery()

        binding.btnSend.setOnClickListener {
            val msg = binding.etMessage.text.toString()
            wifiManager.send(msg)
            binding.tvMessages.append("Client: $msg\n")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        wifiManager.unregister()
    }
}