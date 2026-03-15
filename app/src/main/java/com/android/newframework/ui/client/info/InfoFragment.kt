package com.android.newframework.ui.client.info

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.android.newframework.databinding.ClientInfoFragmentBinding
import com.android.newframework.ui.client.info.adapter.QuickActionAdapter
import com.android.newframework.ui.client.info.adapter.RecentInfoAdapter
import com.android.newframework.ui.client.info.adapter.TodayInfoAdapter
import com.android.newframework.ui.client.info.adapter.TopInfoAdapter
import com.iot.GridSpacingDecoration
import com.iot.base.BaseFragment
import com.iot.base.R

class InfoFragment : BaseFragment<ClientInfoFragmentBinding>() {
    private val viewModel by lazy { ViewModelProvider(this)[InfoViewModel::class.java] }
    private val topInfoAdapter = TopInfoAdapter()
    private val quickActionAdapter = QuickActionAdapter()
    private val recentVisitsAdapter = RecentInfoAdapter()
    private val todayAppointmentsAdapter = TodayInfoAdapter()

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): ClientInfoFragmentBinding {
        return ClientInfoFragmentBinding.inflate(inflater, container, false)
    }

    override fun initView() {
        super.initView()
        binding.logoIv.setImageResource(com.android.newframework.R.drawable.baseline_medical_services_24)
        binding.topTotalInfoRv.addItemDecoration(GridSpacingDecoration(4, requireContext().resources.getDimension(R.dimen.dp_5).toInt(), false))
        binding.topTotalInfoRv.adapter = topInfoAdapter

        binding.quickActionsRv.addItemDecoration(GridSpacingDecoration(6, requireContext().resources.getDimension(R.dimen.dp_5).toInt(), false))
        binding.quickActionsRv.adapter = quickActionAdapter

        binding.recentVisitsItemRv.adapter = recentVisitsAdapter

        binding.todayAppointmentsItemRv.adapter = todayAppointmentsAdapter
    }

    override fun initObserver() {
        super.initObserver()
        viewModel.topInfo.observe(this) {
            topInfoAdapter.submitList(it)
        }
        viewModel.quickInfo.observe(this) {
            quickActionAdapter.submitList(it)
        }
        viewModel.recentVisitsInfo.observe(this) {
            recentVisitsAdapter.submitList(it)
        }
        viewModel.todayAppointmentsInfo.observe(this) {
            todayAppointmentsAdapter.submitList(it)
        }
    }

    override fun initData() {
        super.initData()
        viewModel.updateTopInfo()
    }

}