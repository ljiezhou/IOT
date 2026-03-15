package com.android.newframework.ui.client.info

import androidx.lifecycle.MutableLiveData
import com.android.newframework.R
import com.iot.CommonInfoItem
import com.iot.base.BaseViewModel

class InfoViewModel : BaseViewModel() {

    val topInfo = MutableLiveData<List<CommonInfoItem>>()
    val quickInfo = MutableLiveData<List<CommonInfoItem>>()
    val recentVisitsInfo = MutableLiveData<List<CommonInfoItem>>()
    val todayAppointmentsInfo = MutableLiveData<List<CommonInfoItem>>()


    init {
        updateTopInfo()
        updateQuickInfo()
        updateRecentVisitsInfo()
        updateTodayAppointmentsInfo()
    }

    fun updateTopInfo() {
        val info = arrayListOf<CommonInfoItem>()
        info.add(CommonInfoItem(title = "今日门诊", description = "128", iconRes = R.drawable.baseline_supervisor_account_24))
        info.add(CommonInfoItem(title = "住院患者", description = "45", iconRes = R.drawable.baseline_show_chart_24))
        info.add(CommonInfoItem(title = "待处理预约", description = "23", iconRes = R.drawable.baseline_calendar_month_24))
        info.add(CommonInfoItem(title = "紧急通知", description = "3", iconRes = R.drawable.sharp_error_outline_24))
        topInfo.value = info
    }

    fun updateQuickInfo() {
        val info = arrayListOf<CommonInfoItem>()
        info.add(CommonInfoItem(title = "新增患者", iconRes = R.drawable.baseline_person_add_alt_24))
        info.add(CommonInfoItem(title = "预约挂号", iconRes = R.drawable.baseline_calendar_month_242))
        info.add(CommonInfoItem(title = "开始诊疗", iconRes = R.drawable.start))
        info.add(CommonInfoItem(title = "病例查询", iconRes = R.drawable.baseline_document_scanner_24))
        info.add(CommonInfoItem(title = "检查报告", iconRes = R.drawable.baseline_document_scanner_242))
        info.add(CommonInfoItem(title = "系统设置", iconRes = R.drawable.baseline_settings_24))
        quickInfo.value = info
    }

    fun updateRecentVisitsInfo() {
        // 模拟数据
        val info = arrayListOf<CommonInfoItem>()
        info.add(CommonInfoItem(title = "张三", description = "2024-06-01 10:00"))
        info.add(CommonInfoItem(title = "李四", description = "2024-06-01 09:30"))
        info.add(CommonInfoItem(title = "王五", description = "2024-06-01 09:00"))
        recentVisitsInfo.value = info
    }

    fun updateTodayAppointmentsInfo() {
        // 模拟数据
        val info = arrayListOf<CommonInfoItem>()
        info.add(CommonInfoItem(title = "张三", description = "10:00 - 10:30"))
        info.add(CommonInfoItem(title = "李四", description = "10:30 - 11:00"))
        info.add(CommonInfoItem(title = "王五", description = "11:00 - 11:30"))
        todayAppointmentsInfo.value = info
    }
}