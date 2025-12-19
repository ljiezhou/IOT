package com.android.newframework

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pDeviceList
import android.net.wifi.p2p.WifiP2pInfo
import android.net.wifi.p2p.WifiP2pManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.Socket
import java.util.concurrent.Executors

class ClientActivityOld : AppCompatActivity(), WifiP2pManager.ChannelListener {

    private val TAG = "ClientActivity"
    private val PORT = 8888

    private lateinit var manager: WifiP2pManager
    private lateinit var channel: WifiP2pManager.Channel
    private lateinit var receiver: BroadcastReceiver

    private lateinit var btnDiscoverPeers: Button
    private lateinit var tvStatus: TextView
    private lateinit var tvConnectionStatus: TextView
    private lateinit var lvDevices: ListView
    private lateinit var tvMessageDisplay: TextView
    private lateinit var etMessage: EditText
    private lateinit var btnSend: Button

    private val peers = ArrayList<WifiP2pDevice>()
    private lateinit var deviceAdapter: ArrayAdapter<String>
    private val deviceNameList = ArrayList<String>()

    private var socket: Socket? = null
    private var serverAddress: String? = null
    private var isWifiP2pEnabled = false
    private var messageLog = StringBuilder()

    private val executorService = Executors.newFixedThreadPool(2)

    private val peerListListener = WifiP2pManager.PeerListListener { peerList: WifiP2pDeviceList ->
        val refreshedPeers = peerList.deviceList

        if (refreshedPeers != peers) {
            peers.clear()
            peers.addAll(refreshedPeers)

            deviceNameList.clear()
            peers.forEach { device ->
                deviceNameList.add(device.deviceName + " - " + device.deviceAddress)
            }

            deviceAdapter.notifyDataSetChanged()
        }

        if (peers.isEmpty()) {
            Log.d(TAG, "No devices found")
            return@PeerListListener
        }
    }

    private val connectionListener = WifiP2pManager.ConnectionInfoListener { info: WifiP2pInfo ->
        Log.d(TAG, "Connection info: groupFormed=${info.groupFormed}, isGroupOwner=${info.isGroupOwner}, groupOwnerAddress=${info.groupOwnerAddress}")

        if (info.groupFormed) {
            val hostAddress = info.groupOwnerAddress.hostAddress
            tvConnectionStatus.text = "Connected to host: $hostAddress"

            serverAddress = hostAddress
            connectToServer()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_client_old)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.client_layout)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initializeViews()

        deviceAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, deviceNameList)
        lvDevices.adapter = deviceAdapter

        manager = getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
        channel = manager.initialize(this, mainLooper, this)

        receiver = WiFiDirectBroadcastReceiver()

        checkPermissions()

        btnDiscoverPeers.setOnClickListener {
            discoverPeers()
        }

        lvDevices.setOnItemClickListener { _, _, position, _ ->
            val device = peers[position]
            connectToPeer(device)
        }

        btnSend.setOnClickListener {
            val message = etMessage.text.toString()
            if (message.isNotEmpty()) {
                sendMessage(message)
                etMessage.text.clear()
            } else {
                Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun initializeViews() {
        btnDiscoverPeers = findViewById(R.id.btn_discover_peers)
        tvStatus = findViewById(R.id.tv_status)
        tvConnectionStatus = findViewById(R.id.tv_connection_status)
        lvDevices = findViewById(R.id.lv_devices)
        tvMessageDisplay = findViewById(R.id.tv_message_display)
        etMessage = findViewById(R.id.et_message)
        btnSend = findViewById(R.id.btn_send)
    }

    private fun checkPermissions() {
        val permissions = mutableListOf<String>()

        // Location permission is required for Wi-Fi P2P
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        // Android 13+ needs NEARBY_WIFI_DEVICES permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.NEARBY_WIFI_DEVICES)
            != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.NEARBY_WIFI_DEVICES)
        }

        if (permissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                permissions.toTypedArray(),
                PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun discoverPeers() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            tvStatus.text = "Searching for peers..."
            btnDiscoverPeers.isEnabled = false

            manager.discoverPeers(channel, object : WifiP2pManager.ActionListener {
                override fun onSuccess() {
                    tvStatus.text = "Discovery started"
                    btnDiscoverPeers.isEnabled = true
                }

                override fun onFailure(reason: Int) {
                    tvStatus.text = "Discovery failed: " + getFailureReason(reason)
                    btnDiscoverPeers.isEnabled = true
                }
            })
        } else {
            Toast.makeText(
                this,
                "Location permission required for peer discovery",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun connectToPeer(device: WifiP2pDevice) {
        val config = WifiP2pConfig().apply {
            deviceAddress = device.deviceAddress
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            manager.connect(channel, config, object : WifiP2pManager.ActionListener {
                override fun onSuccess() {
                    tvConnectionStatus.text = "Connection initiated to: ${device.deviceName}"
                }

                override fun onFailure(reason: Int) {
                    tvConnectionStatus.text = "Connection failed: " + getFailureReason(reason)
                }
            })
        }
    }

    private fun connectToServer() {
        Log.d(TAG, "Connecting to server at address: $serverAddress")

        // Close any existing socket
        try {
            socket?.close()
        } catch (e: IOException) {
            Log.e(TAG, "Error closing existing socket", e)
        }

        serverAddress?.let { serverAddr ->
            executorService.execute {
                var retryCount = 0
                val maxRetries = 3

                while (retryCount < maxRetries) {
                    try {
                        Log.d(TAG, "Attempt ${retryCount + 1} to connect to $serverAddr:$PORT")

                        socket = Socket()
                        socket?.connect(InetSocketAddress(serverAddr, PORT), 5000)

                        Log.d(TAG, "Successfully connected to server at $serverAddr:$PORT")

                        runOnUiThread {
                            tvConnectionStatus.text = "Connected to server: $serverAddr"
                        }

                        // Start listening for incoming messages
                        startMessageListener()

                        // Success - break out of retry loop
                        break

                    } catch (e: IOException) {
                        retryCount++
                        Log.e(TAG, "Connection attempt $retryCount failed: ${e.message}")

                        if (retryCount >= maxRetries) {
                            Log.e(TAG, "Failed to connect after $maxRetries attempts", e)
                            e.printStackTrace()
                            runOnUiThread {
                                tvConnectionStatus.text = "Failed to connect: ${e.message}"
                            }
                        } else {
                            // Wait before retry
                            try {
                                Thread.sleep(1000)
                            } catch (ie: InterruptedException) {
                                Thread.currentThread().interrupt()
                                break
                            }
                        }
                    }
                }
            }
        } ?: run {
            Log.w(TAG, "No server address available")
            tvConnectionStatus.text = "No server address available"
        }
    }

    private fun startMessageListener() {
        Log.d(TAG, "Starting message listener")
        executorService.execute {
            try {
                val currentSocket = socket ?: run {
                    Log.e(TAG, "Socket is null, cannot listen for messages")
                    return@execute
                }

                val inputStream: InputStream = currentSocket.getInputStream()
                val buffer = ByteArray(1024)

                while (!Thread.currentThread().isInterrupted &&
                       currentSocket.isConnected &&
                       !currentSocket.isClosed) {

                    Log.d(TAG, "Waiting for message from server...")
                    try {
                        val bytesRead = inputStream.read(buffer)

                        if (bytesRead == -1) {
                            Log.d(TAG, "End of stream reached")
                            break // End of stream
                        }

                        val message = String(buffer, 0, bytesRead)
                        Log.d(TAG, "Received message from server: $message")

                        messageLog.append("Server: $message\n")

                        runOnUiThread {
                            tvMessageDisplay.text = messageLog.toString()
                        }
                    } catch (e: IOException) {
                        if (!currentSocket.isClosed) {
                            Log.e(TAG, "Error reading from server", e)
                        }
                        break
                    }
                }
            } catch (e: IOException) {
                Log.e(TAG, "MessageListener encountered an error", e)
                e.printStackTrace()
                runOnUiThread {
                    tvConnectionStatus.text = "Connection lost: ${e.message}"
                }
            } finally {
                if (socket?.isClosed == false) {
                    try {
                        socket?.close()
                    } catch (e: IOException) {
                        Log.e(TAG, "Error closing socket in message listener", e)
                    }
                }
            }
        }
    }

    private fun sendMessage(message: String) {
        Log.d(TAG, "Attempting to send message. Socket: ${socket != null}, Connected: ${socket?.isConnected}, Closed: ${socket?.isClosed}")

        val currentSocket = socket
        if (currentSocket != null && currentSocket.isConnected && !currentSocket.isClosed) {
            executorService.execute {
                try {
                    Log.d(TAG, "Getting output stream...")
                    val outputStream: OutputStream = currentSocket.getOutputStream()

                    Log.d(TAG, "Writing message to output stream...")
                    outputStream.write(message.toByteArray())
                    outputStream.flush()

                    Log.d(TAG, "Message sent successfully")
                    messageLog.append("Me: $message\n")
                    runOnUiThread {
                        tvMessageDisplay.text = messageLog.toString()
                        Toast.makeText(
                            this@ClientActivityOld,
                            "Message sent",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: IOException) {
                    Log.e(TAG, "Failed to send message", e)
                    e.printStackTrace()
                    runOnUiThread {
                        Toast.makeText(
                            this@ClientActivityOld,
                            "Failed to send message: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    // If there's an error, the socket might be bad - close it
                    try {
                        currentSocket.close()
                        socket = null
                        runOnUiThread {
                            tvConnectionStatus.text = "Connection lost, trying to reconnect..."
                        }
                        // Try to reconnect
                        connectToServer()
                    } catch (e2: IOException) {
                        Log.e(TAG, "Error closing socket after send failure", e2)
                    }
                }
            }
        } else {
            Log.w(TAG, "Cannot send message - not connected to server or socket is closed")
            Toast.makeText(
                this,
                "Not connected to a server. Trying to reconnect...",
                Toast.LENGTH_LONG
            ).show()

            // Try to reconnect
            connectToServer()
        }
    }

    private fun getFailureReason(reason: Int): String {
        return when (reason) {
            WifiP2pManager.P2P_UNSUPPORTED -> "P2P Unsupported"
            WifiP2pManager.BUSY -> "Service Busy"
            WifiP2pManager.ERROR -> "Internal Error"
            else -> "Unknown Error"
        }
    }

    override fun onResume() {
        super.onResume()
        val intentFilter = IntentFilter().apply {
            addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)
        }
        registerReceiver(receiver, intentFilter)
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(receiver)
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            socket?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        executorService.shutdown()
    }

    override fun onChannelDisconnected() {
        // Try to reinitialize channel
        channel = manager.initialize(this, mainLooper, this)
    }

    inner class WiFiDirectBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION -> {
                    val state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1)
                    isWifiP2pEnabled = state == WifiP2pManager.WIFI_P2P_STATE_ENABLED
                    tvStatus.text = "P2P State: " + if (isWifiP2pEnabled) "Enabled" else "Disabled"
                }
                WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> {
                    if (ActivityCompat.checkSelfPermission(
                            this@ClientActivityOld,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        manager.requestPeers(channel, peerListListener)
                    }
                }
                WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> {
                    manager.requestConnectionInfo(channel, connectionListener)
                }
                WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION -> {
                    // Not handling device changes in this example
                }
            }
        }
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 100
    }
}