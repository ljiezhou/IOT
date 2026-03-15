package com.android.newframework.ui.client.info.adapter

import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.android.newframework.databinding.ClientQuickActionItemLayoutBinding
import com.chad.library.adapter4.BaseQuickAdapter
import com.iot.CommonInfoItem

class QuickActionAdapter : BaseQuickAdapter<CommonInfoItem, QuickActionAdapter.VH>() {
    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): VH {
        return VH(parent)
    }

    override fun onBindViewHolder(holder: VH, position: Int, item: CommonInfoItem?) {
        if (item == null) return
        holder.binding.apply {
            titleTv.text = item.title
            item.iconRes?.let { iv.setImageResource(it) }
        }
    }

    class VH(
        val parent: ViewGroup,
        val binding: ClientQuickActionItemLayoutBinding = ClientQuickActionItemLayoutBinding.inflate(
            android.view.LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root)
}