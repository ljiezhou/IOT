package com.android.newframework.ui.host

import com.android.newframework.netty.server.NettyServerManager
import com.iot.base.BaseViewModel

class HostViewModel: BaseViewModel() {

    // 直接暴露 StateFlow
    val serverState = NettyServerManager.state
    fun init(){
//        viewModelScope.launch {
//            repeatOnLifecycle(Lifecycle.State.STARTED) {
//                NettyServerManager.state.collect { state ->
//                    when (state) {
//                        is NettyServerManager.State.Started -> {
////                            loadingView.startWaiting()
//                        }
//                        is NettyServerManager.State.ClientConnected -> {
////                            loadingView.showConnected()
//                        }
//                        is NettyServerManager.State.ClientDisconnected -> {
////                            loadingView.startWaiting()
//                        }
//                    }
//                }
//            }
//        }
    }
}