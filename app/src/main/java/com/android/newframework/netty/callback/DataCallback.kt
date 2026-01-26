package com.android.newframework.netty.callback

interface DataCallback {
    /**
     * Called when data is sent.
     * @param channelId null for broadcast, or the target channel id for single send
     * @param text the serialized text that was sent
     */
    fun onDataSent(channelId: String?, text: String)

    /**
     * Called when data is received from a client.
     * @param channelId the source channel id
     * @param text the text received
     */
    fun onDataReceived(channelId: String, text: String)
}
