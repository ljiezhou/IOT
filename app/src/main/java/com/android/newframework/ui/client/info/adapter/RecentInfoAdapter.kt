package com.android.newframework.ui.client.info.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.android.newframework.databinding.ClientInfoRecentItemLayoutBinding
import com.android.newframework.databinding.ClientInfoTopItemLayoutBinding
import com.chad.library.adapter4.BaseQuickAdapter
import com.iot.CommonInfoItem

class RecentInfoAdapter : BaseQuickAdapter<CommonInfoItem, RecentInfoAdapter.VH>() {
    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): VH {
        return VH(parent)
    }

    override fun onBindViewHolder(holder: VH, position: Int, item: CommonInfoItem?) {
        if (item == null) return
        holder.binding.apply {
            nameTv.text = item.title
            descTv.text = item.description
        }
    }

    class VH(
        val parent: ViewGroup,
        val binding: ClientInfoRecentItemLayoutBinding = ClientInfoRecentItemLayoutBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root)

}