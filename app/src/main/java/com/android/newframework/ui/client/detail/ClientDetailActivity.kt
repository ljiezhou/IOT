package com.android.newframework.ui.client.detail

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintSet
import androidx.lifecycle.lifecycleScope
import com.android.newframework.R
import com.android.newframework.databinding.ClientDetailActivityBinding
import com.android.newframework.netty.client.NettyClientManager
import com.android.newframework.netty.protocol.Action
import com.android.newframework.netty.protocol.MessageType
import com.android.newframework.netty.protocol.SocketMessage
import com.android.newframework.netty.protocol.TextMessagePayload
import com.android.newframework.ui.client.info.InfoFragment
import com.blankj.utilcode.util.FragmentUtils
import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.StringUtils
import com.iot.base.BaseActivity
import kotlinx.coroutines.launch

class ClientDetailActivity : BaseActivity<ClientDetailActivityBinding>() {
    override fun inflateBinding(inflater: LayoutInflater): ClientDetailActivityBinding {
        return ClientDetailActivityBinding.inflate(inflater)
    }

    override fun initView() {
        super.initView()

    }

    private var infoFragment = InfoFragment()

    override fun initObserver() {
        super.initObserver()

        lifecycleScope.launch {
            NettyClientManager.state.collect { state ->
                when (state) {
                    is NettyClientManager.State.Message -> {
                        val socketMessage = GsonUtils.fromJson(state.msg, SocketMessage::class.java)
                        handleSocketMessage(socketMessage)
                    }

                    else -> {}
                }

            }
        }

//        FragmentUtils.add(supportFragmentManager, infoFragment, R.id.center_container)
    }

    private fun handleSocketMessage(message: SocketMessage) {
        when (message.type) {
            MessageType.EVENT -> {
                when (message.action) {
                    Action.ANIMATION_START -> {
//                        binding.deviceStateTv.text = "Start"
//                        binding.loadingView.startWaiting()
                        FragmentUtils.add(supportFragmentManager, infoFragment, R.id.center_container)
                    }

                    Action.ANIMATION_STOP -> {
                        binding.deviceStateTv.text = "Stop"
                        binding.loadingView.stop()
                    }

                    Action.ANIMATION_PAUSE -> {
                        binding.deviceStateTv.text = "Pause"
                    }

                    Action.TEXT_MESSAGE -> {
                        try {
                            // 把 payload 先转成 JSON，再反序列化为目标类型，避免 LinkedTreeMap 强转异常
                            val payloadJson = GsonUtils.toJson(message.payload)
                            val payload = GsonUtils.fromJson(payloadJson, TextMessagePayload::class.java)

                            if (payload != null) {
                                if (StringUtils.getString(com.android.newframework.R.string.host_detail_btn_start) == payload.title) {
                                    FragmentUtils.add(supportFragmentManager, infoFragment, R.id.center_container)
                                    return
                                }
                                infoFragment.updateText(payload.title)
                                when (payload.title) {
                                    StringUtils.getString(R.string.host_detail_btn_full_screen_exit) -> {
                                        restoreCenterDefault()
                                    }

                                    StringUtils.getString(R.string.host_detail_btn_refresh) -> {
                                        setCenterFullScreen()
                                    }
                                }
                            } else {
                                // 兜底：如果 payload 本身就是目标类型，安全处理
                                if (message.payload is TextMessagePayload) {
                                    val p = message.payload as TextMessagePayload
                                    infoFragment.updateText(p.title)
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    else -> {

                    }
                }
            }

            else -> {}
        }
    }


    private fun setCenterFullScreen() {
        val constraintLayout = binding.root

        // 隐藏左侧
        binding.leftInfoLl.visibility = View.GONE

        val set = ConstraintSet()
        set.clone(constraintLayout)

        // 让 center_container 贴满父布局
        set.clear(R.id.center_container, ConstraintSet.START)
        set.connect(
            R.id.center_container,
            ConstraintSet.START,
            ConstraintSet.PARENT_ID,
            ConstraintSet.START
        )

        set.applyTo(constraintLayout)
    }


    private fun restoreCenterDefault() {
        val constraintLayout = binding.root

        // 显示左侧
        binding.leftInfoLl.visibility = View.VISIBLE

        val set = ConstraintSet()
        set.clone(constraintLayout)

        // 恢复原始约束（连接到左侧）
        set.clear(R.id.center_container, ConstraintSet.START)
        set.connect(
            R.id.center_container,
            ConstraintSet.START,
            R.id.left_info_ll,
            ConstraintSet.END
        )

        set.applyTo(constraintLayout)
    }

    companion object {
        fun action(context: Context) {
            context.startActivity(Intent(context, ClientDetailActivity::class.java))
        }
    }
}