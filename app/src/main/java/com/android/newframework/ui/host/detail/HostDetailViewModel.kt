package com.android.newframework.ui.host.detail

import androidx.lifecycle.MutableLiveData
import com.android.newframework.R
import com.blankj.utilcode.util.StringUtils
import com.iot.CommonInfoItem
import com.iot.base.BaseViewModel

class HostDetailViewModel : BaseViewModel() {
    val btnItems = MutableLiveData<List<CommonInfoItem>>()

    fun loadBtnItems() {
        val items = listOf(
            CommonInfoItem(title = StringUtils.getString(R.string.host_detail_btn_start)),
            CommonInfoItem(title = StringUtils.getString(R.string.host_detail_btn_refresh)),
            CommonInfoItem(title = StringUtils.getString(R.string.host_detail_btn_add_patient)),
            CommonInfoItem(title = StringUtils.getString(R.string.host_detail_btn_remove_patient)),
            CommonInfoItem(title = StringUtils.getString(R.string.host_detail_btn_add_recent)),
            CommonInfoItem(title = StringUtils.getString(R.string.host_detail_btn_remove_recent)),
        )
        btnItems.value = items
    }

    init {
        loadBtnItems()
    }
}