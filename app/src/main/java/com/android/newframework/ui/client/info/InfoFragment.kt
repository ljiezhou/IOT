package com.android.newframework.ui.client.info

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.android.newframework.databinding.ClientInfoFragmentBinding
import com.android.newframework.ui.client.info.adapter.QuickActionAdapter
import com.android.newframework.ui.client.info.adapter.RecentInfoAdapter
import com.android.newframework.ui.client.info.adapter.TodayInfoAdapter
import com.android.newframework.ui.client.info.adapter.TopInfoAdapter
import com.blankj.utilcode.util.StringUtils
import com.chad.library.adapter4.BaseQuickAdapter
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

        recentVisitsAdapter.animationEnable = true
        binding.recentVisitsItemRv.adapter = recentVisitsAdapter

        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.bindingAdapterPosition
                recentVisitsAdapter.removeAt(position)
            }
        })
        itemTouchHelper.attachToRecyclerView(binding.recentVisitsItemRv)
        recentVisitsAdapter.setItemAnimation(BaseQuickAdapter.AnimationType.SlideInRight)

        todayAppointmentsAdapter.animationEnable = true
        binding.todayAppointmentsItemRv.adapter = todayAppointmentsAdapter
        todayAppointmentsAdapter.setItemAnimation(BaseQuickAdapter.AnimationType.SlideInRight)
        val itemTouchHelper2 = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.bindingAdapterPosition
                todayAppointmentsAdapter.removeAt(position)
            }
        })
        itemTouchHelper2.attachToRecyclerView(binding.todayAppointmentsItemRv)
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

    override fun initListener() {
        super.initListener()
        quickActionAdapter.setOnItemClickListener { adapter, view, i ->
            val item = quickActionAdapter.getItem(i)
            when (item.title) {
                "新增患者" -> {
                    recentVisitsAdapter.add(0, viewModel.getSampleRecentVisitsSets())
                    binding.recentVisitsItemRv.scrollToPosition(0)
                }

                "预约挂号" -> {
                    todayAppointmentsAdapter.add(0, viewModel.getSampleTodayAppointmentsSets())
                    binding.todayAppointmentsItemRv.scrollToPosition(0)
                }
            }
        }
    }

    fun updateText(title: String) {
        when (title) {
            StringUtils.getString(com.android.newframework.R.string.host_detail_btn_refresh) -> {
                viewModel.updateRecentVisitsInfo()
                viewModel.updateTodayAppointmentsInfo()
            }

            StringUtils.getString(com.android.newframework.R.string.host_detail_btn_add_patient) -> {
                todayAppointmentsAdapter.add(0, viewModel.getSampleTodayAppointmentsSets())
            }

            StringUtils.getString(com.android.newframework.R.string.host_detail_btn_remove_patient) -> {
                todayAppointmentsAdapter.removeAt(todayAppointmentsAdapter.items.size - 1)
            }

            StringUtils.getString(com.android.newframework.R.string.host_detail_btn_add_recent) -> {
                recentVisitsAdapter.add(0, viewModel.getSampleRecentVisitsSets())
            }

            StringUtils.getString(com.android.newframework.R.string.host_detail_btn_remove_recent) -> {
                recentVisitsAdapter.removeAt(recentVisitsAdapter.items.size - 1)
            }
        }

    }

}