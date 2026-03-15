package com.android.newframework.ui.host.detail

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.android.newframework.databinding.HostDetailItemLayoutBinding
import com.chad.library.adapter4.BaseQuickAdapter
import com.iot.CommonInfoItem

class HostDetailAdapter : BaseQuickAdapter<CommonInfoItem, HostDetailAdapter.VH>() {
    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): VH {
        return VH(parent)
    }

    override fun onBindViewHolder(holder: VH, position: Int, item: CommonInfoItem?) {
        if (item == null) return
        holder.binding.apply {
            btnTv.text = item.title
        }
    }

    class VH(
        val parent: ViewGroup,
        val binding: HostDetailItemLayoutBinding = HostDetailItemLayoutBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root)

}