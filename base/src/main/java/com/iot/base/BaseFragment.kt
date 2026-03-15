package com.iot.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding

abstract class BaseFragment<VB : ViewBinding> : Fragment() {
    // 具体的 binding 实例，在 super.onCreate 调用后可用。
    private var _binding: VB? = null
    protected val binding get() = _binding!!

    /**
     * 子类必须提供用于 inflate 的方法。
     * 实现通常调用 SomethingBinding.inflate(inflater)。
     */
    protected abstract fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): VB


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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = inflateBinding(layoutInflater, container)
        return binding.root
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 提供给子类重写的钩子方法。
        initView()
        initObserver()
        initData()
        initListener()
    }

}