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
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.Executors

class HostActivityOld : AppCompatActivity(), WifiP2pManager.ChannelListener {

    private val TAG = "HostActivity"
    private val PORT = 8888

    private lateinit var manager: WifiP2pManager
    private lateinit var channel: WifiP2pManager.Channel
    private lateinit var receiver: BroadcastReceiver

    private lateinit var btnStartService: Button
    private lateinit var tvStatus: TextView
    private lateinit var tvConnectionStatus: TextView
    private lateinit var lvDevices: ListView
    private lateinit var etMessage: EditText
    private lateinit var btnSend: Button

    private val peers = ArrayList<WifiP2pDevice>()
    private lateinit var deviceAdapter: ArrayAdapter<String>
    private val deviceNameList = ArrayList<String>()

    private var serverSocket: ServerSocket? = null
    private var clientSocket: Socket? = null
    private var isWifiP2pEnabled = false

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

        if (info.groupFormed && info.isGroupOwner) {
            tvConnectionStatus.text = "I am GO, starting server"
            startServerSocket()
        } else if (info.groupFormed) {
            tvConnectionStatus.text = "I am CLIENT, cannot start server."
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_host_old)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.host_layout)) { v, insets ->
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

        btnStartService.setOnClickListener {
            startDiscovery()
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
        btnStartService = findViewById(R.id.btn_start_service)
        tvStatus = findViewById(R.id.tv_status)
        tvConnectionStatus = findViewById(R.id.tv_connection_status)
        lvDevices = findViewById(R.id.lv_devices)
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

    private fun startDiscovery() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            tvStatus.text = "Searching for peers..."
            btnStartService.isEnabled = false

            manager.discoverPeers(channel, object : WifiP2pManager.ActionListener {
                override fun onSuccess() {
                    tvStatus.text = "Discovery started"
                    btnStartService.isEnabled = true
                }

                override fun onFailure(reason: Int) {
                    tvStatus.text = "Discovery failed: " + getFailureReason(reason)
                    btnStartService.isEnabled = true
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
    // Kotlin
    private @Volatile var isServerRunning = false

    private fun startServerSocket() {
        if (isServerRunning) {
            Log.d(TAG, "Server already running")
            return
        }

        Log.d(TAG, "Starting server socket on port $PORT")

        // Close any existing server socket safely
        try {
            serverSocket?.close()
        } catch (e: IOException) {
            Log.e(TAG, "Error closing existing server socket", e)
        }

        isServerRunning = true

        executorService.execute {
            try {
                serverSocket = ServerSocket(PORT)
                Log.d(TAG, "Server socket created successfully")

                runOnUiThread {
                    tvConnectionStatus.text = "Socket server started on port $PORT"
                    btnStartService.isEnabled = false
                }

                while (isServerRunning && serverSocket?.isClosed == false && !Thread.currentThread().isInterrupted) {
                    Log.d(TAG, "Waiting for client connection...")
                    try {
                        val socket = serverSocket?.accept() ?: continue
                        clientSocket = socket
                        Log.d(TAG, "Client connected: ${clientSocket?.inetAddress}")

                        runOnUiThread {
                            tvConnectionStatus.text = "Client connected: ${clientSocket?.inetAddress}"
                        }

                        val clientHandler = ClientHandler(clientSocket)
                        executorService.execute(clientHandler)
                    } catch (e: java.net.SocketException) {
                        // Socket closed when shutting down — treat as normal termination of accept loop
                        Log.d(TAG, "Accept interrupted or server socket closed: ${e.message}")
                        break
                    } catch (e: IOException) {
                        Log.e(TAG, "Error accepting client connection", e)
                        break
                    }
                }
            } catch (e: IOException) {
                Log.e(TAG, "Server socket error", e)
                runOnUiThread {
                    tvConnectionStatus.text = "Server socket error: ${e.message}"
                }
            } finally {
                isServerRunning = false
                try {
                    serverSocket?.close()
                } catch (e: IOException) {
                    Log.e(TAG, "Error closing server socket in finally", e)
                }
                runOnUiThread {
                    btnStartService.isEnabled = true
                }
            }
        }
    }

    private fun sendMessage(message: String) {
        Log.d(TAG, "Attempting to send message. Client socket: ${clientSocket != null}, Connected: ${clientSocket?.isConnected}, Closed: ${clientSocket?.isClosed}")

        val currentSocket = clientSocket
        if (currentSocket != null && currentSocket.isConnected && !currentSocket.isClosed) {
            executorService.execute {
                try {
                    Log.d(TAG, "Getting output stream...")
                    val outputStream: OutputStream = currentSocket.getOutputStream()

                    Log.d(TAG, "Writing message to output stream...")
                    outputStream.write(message.toByteArray())
                    outputStream.flush() // Make sure to flush the stream

                    Log.d(TAG, "Message sent successfully")
                    runOnUiThread {
                        Toast.makeText(
                            this@HostActivityOld,
                            "Message sent",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: IOException) {
                    Log.e(TAG, "Failed to send message", e)
                    e.printStackTrace()
                    runOnUiThread {
                        Toast.makeText(
                            this@HostActivityOld,
                            "Failed to send message: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    // If there's an error, the socket might be bad - close it
                    try {
                        currentSocket.close()
                    } catch (e2: IOException) {
                        Log.e(TAG, "Error closing socket after send failure", e2)
                    }
                    clientSocket = null
                }
            }
        } else {
            Log.w(TAG, "Cannot send message - no client connected or socket is closed")
            Toast.makeText(
                this,
                "No client connected. Please wait for a client to connect.",
                Toast.LENGTH_LONG
            ).show()

            // If we're the group owner but don't have a client socket, something might be wrong
            // with the server socket. Restart it.
            if (clientSocket == null) {
                Log.d(TAG, "Client socket is null, attempting to restart server socket")
                startServerSocket()
            }
        }
    }

    private inner class ClientHandler(private val handledSocket: Socket?) : Runnable {
        override fun run() {
            Log.d(TAG, "ClientHandler started for socket: $handledSocket")
            try {
                val inputStream: InputStream = handledSocket?.getInputStream() ?: run {
                    Log.e(TAG, "Failed to get input stream from socket")
                    return
                }
                val buffer = ByteArray(1024)

                while (!Thread.currentThread().isInterrupted && handledSocket?.isConnected == true && !handledSocket.isClosed) {
                    Log.d(TAG, "Waiting to read from client...")
                    try {
                        val bytesRead = inputStream.read(buffer)

                        if (bytesRead == -1) {
                            Log.d(TAG, "End of stream reached")
                            break // End of stream
                        }

                        val message = String(buffer, 0, bytesRead)
                        Log.d(TAG, "Received message from client: $message")

                        runOnUiThread {
                            // In a real app, you'd want to display this message in a chat UI
                            Toast.makeText(
                                this@HostActivityOld,
                                "Received: $message",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } catch (e: IOException) {
                        if (!handledSocket.isClosed) {
                            Log.e(TAG, "Error reading from client", e)
                        }
                        break
                    }
                }
            } catch (e: IOException) {
                Log.e(TAG, "ClientHandler encountered an error", e)
                e.printStackTrace()
            } finally {
                Log.d(TAG, "ClientHandler finishing, closing socket...")
                try {
                    // Only close if it's not the main client socket
                    // or if it is, set clientSocket to null
                    if (handledSocket == clientSocket) {
                        clientSocket = null
                    }
                    handledSocket?.close()
                } catch (e: IOException) {
                    Log.e(TAG, "Error closing client socket in handler", e)
                    e.printStackTrace()
                }
            }
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
            clientSocket?.close()
            serverSocket?.close()
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
                            this@HostActivityOld,
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