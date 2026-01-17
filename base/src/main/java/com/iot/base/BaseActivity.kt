package com.iot.base

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewbinding.ViewBinding

/**
 * 一个简单的基类 Activity，使用 ViewBinding（不使用反射）。
 *
 * 子类必须实现 [inflateBinding] 来创建各自的 ViewBinding 实例。
 * 这样避免使用反射或 reified 泛型，保持初始化显式。
 *
 * 使用示例：
 *
 * class MainActivity : BaseActivity<ActivityMainBinding>() {
 *     override fun inflateBinding(inflater: LayoutInflater) = ActivityMainBinding.inflate(inflater)
 *
 *     override fun onCreate(savedInstanceState: Bundle?) {
 *         super.onCreate(savedInstanceState)
 *         // 使用 binding
 *         binding.textView.text = "Hello"
 *     }
 * }
 */
abstract class BaseActivity<VB : ViewBinding> : AppCompatActivity() {

    // 具体的 binding 实例，在 super.onCreate 调用后可用。
    protected lateinit var binding: VB
        private set

    /**
     * 子类必须提供用于 inflate 的方法。
     * 实现通常调用 SomethingBinding.inflate(inflater)。
     */
    protected abstract fun inflateBinding(inflater: LayoutInflater): VB

    /**
     * 是否启用 Edge-to-Edge（沉浸式显示）。默认启用。
     * 子类重写此方法即可关闭（返回 false）。
     */
    protected open fun enableEdgeToEdge(): Boolean = true

    /**
     * 指定应用 WindowInsets 的目标 view 的 id（如果需要）。
     * 返回 null 表示使用 android.R.id.content 作为默认容器。
     * 子类可重写并返回 R.id.main（或其它 id）来指定自定义视图。
     */
    protected open fun edgeToEdgeViewId(): Int? = null

    /**
     * Enable full screen
     *
     * @return
     */
    protected open fun enableFullScreen(): Boolean = true

    /**
     * 初始化视图（findViewById / 视图设置）。子类需要时可重写。
     * 在 [setContentView] 之后调用。
     */
    protected open fun initView() {}

    protected open fun initListener() {}



    /**
     * 初始化观察者（LiveData / Flow 收集器）。子类需要时可重写。
     * 默认：无操作。
     */
    protected open fun initObserver() {}

    /**
     * 初始化或加载数据（网络 / 数据库）。子类需要时可重写。
     * 在 [initView] 和 [initObserver] 之后调用。
     */
    protected open fun initData() {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = inflateBinding(layoutInflater)
        setContentView(binding.root)

        // 设置 Edge-to-Edge（如果启用）
        if (enableEdgeToEdge()) {
            setupEdgeToEdge()
        }

        if (enableFullScreen()) {
            fullScreen()
        }

        // 提供给子类重写的钩子方法。
        initView()
        initObserver()
        initData()
        initListener()
    }

    /**
     * 在指定的 view 上应用系统栏内边距（WindowInsets）。
     * 使用 WindowInsetsCompat 和 ViewCompat 来兼容性处理。
     */
    private fun setupEdgeToEdge() {
        // 不让系统为 decor view 预留系统窗口，以便我们手动处理内边距
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val target: View? = edgeToEdgeViewId()?.let { id ->
            // 通过 id 查找目标 view，若 id 在当前布局中不存在则返回 null
            findViewById(id)
        } ?: findViewById(android.R.id.content)

        target?.let { v ->
            ViewCompat.setOnApplyWindowInsetsListener(v) { vv, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                vv.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
            // 主动请求一次 inset 应用，确保首次布局时生效
            ViewCompat.requestApplyInsets(v)
        }
    }


    open fun fullScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            window.insetsController?.let { controller ->
                controller.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        }
    }
}