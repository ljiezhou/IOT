package com.android.newframework

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.net.wifi.p2p.*
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.IOException
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.Executors

class WifiDirectManager(private val context: Context) : WifiP2pManager.ChannelListener {

    companion object {
        private const val TAG = "WifiDirectManager"
        private const val PORT = 8888
    }

    private val manager = context.getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
    private val channel = manager.initialize(context, context.mainLooper, this)

    private var receiver: BroadcastReceiver? = null
    private var intentFilter: IntentFilter = IntentFilter().apply {
        addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
        addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
        addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
        addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)
    }

    private val executor = Executors.newCachedThreadPool()
    private var peers = mutableListOf<WifiP2pDevice>()

    private var server: WiFiMessageServer? = null
    private var client: WiFiMessageClient? = null

    var events: WiFiEvents? = null
    var role: P2PConnectionRole = P2PConnectionRole.NONE
        private set

    // ----------- public API -----------
    fun register() {
        receiver = WiFiDirectReceiver(this)
        context.registerReceiver(receiver, intentFilter)
    }

    fun unregister() {
        try {
            receiver?.let { context.unregisterReceiver(it) }
        } catch (_: Exception) { }
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.NEARBY_WIFI_DEVICES])
    fun startDiscovery() {
        checkPermissions()

        manager.discoverPeers(channel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                events?.onStateChanged("Discovery started")
            }

            override fun onFailure(reason: Int) {
                events?.onStateChanged("Discovery failed: $reason")
            }
        })
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.NEARBY_WIFI_DEVICES])
    fun connectToDevice(device: WifiP2pDevice) {
        val config = WifiP2pConfig().apply {
            deviceAddress = device.deviceAddress
            groupOwnerIntent = 15   // 尽量让 Host 成为 GO
        }

        manager.connect(channel, config, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                events?.onStateChanged("Connecting to ${device.deviceName}")
            }

            override fun onFailure(reason: Int) {
                events?.onStateChanged("Connect failed: $reason")
            }
        })
    }

    fun send(msg: String) {
        when (role) {
            P2PConnectionRole.GROUP_OWNER -> server?.sendToClient(msg)
            P2PConnectionRole.CLIENT -> client?.sendToServer(msg)
            else -> Log.w(TAG, "Not connected. Cannot send.")
        }
    }

    // ----------- internal calls from broadcast receiver -----------

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.NEARBY_WIFI_DEVICES])
    fun requestPeers() {
        manager.requestPeers(channel) { list ->
            peers.clear()
            peers.addAll(list.deviceList)
            events?.onPeersAvailable(peers)
        }
    }

    fun onConnectionChanged(info: WifiP2pInfo) {
        if (!info.groupFormed) return

        if (info.isGroupOwner) {
            role = P2PConnectionRole.GROUP_OWNER
            events?.onRoleChanged(role)

            // 启动 Server
            server = WiFiMessageServer(PORT) { msg ->
                events?.onMessageReceived(msg)
            }.apply { start() }

        } else {
            role = P2PConnectionRole.CLIENT
            events?.onRoleChanged(role)

            val host = info.groupOwnerAddress.hostAddress
            client = WiFiMessageClient(host, PORT) { msg ->
                Log.d(TAG, "onConnectionChanged: $msg")
                events?.onMessageReceived(msg)
            }.apply { start() }
        }
    }

    override fun onChannelDisconnected() { }
    
    private fun checkPermissions() {
        val need = mutableListOf<String>()

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED)
            need.add(Manifest.permission.ACCESS_FINE_LOCATION)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.NEARBY_WIFI_DEVICES)
            != PackageManager.PERMISSION_GRANTED)
            need.add(Manifest.permission.NEARBY_WIFI_DEVICES)

        if (need.isNotEmpty() && context is android.app.Activity) {
            ActivityCompat.requestPermissions(context, need.toTypedArray(), 100)
        }
    }
}