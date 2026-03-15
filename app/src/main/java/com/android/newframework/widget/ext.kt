package com.android.newframework.widget

import android.widget.TextView
import com.android.newframework.databinding.MainItemLayoutBinding


/**
 * 设置 TextView 的选中状态并刷新 drawable state。
 *
 * @param selected 是否选中
 */
fun TextView.setSelectedState(selected: Boolean) {
    isSelected = selected
    refreshDrawableState()
}

fun MainItemLayoutBinding.setInfo(title: String, desc: String) {
    titleTv.text = title
    descTv.text = desc
}
fun MainItemLayoutBinding.setSelectedState(selected: Boolean) {
    root.isSelected = selected
    titleTv.isSelected = selected
    descTv.isSelected = selected
    root.refreshDrawableState()
    titleTv.refreshDrawableState()
    descTv.refreshDrawableState()
}