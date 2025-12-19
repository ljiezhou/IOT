package com.android.newframework

import android.net.wifi.p2p.WifiP2pDevice

interface WiFiEvents {
    fun onStateChanged(state: String) {}
    fun onPeersAvailable(list: List<WifiP2pDevice>) {}
    fun onRoleChanged(role: P2PConnectionRole) {}

    fun onClientConnected() {}
    fun onConnectedToServer() {}

    fun onMessageReceived(msg: String)
}