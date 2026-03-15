package com.android.newframework.ui.client.info.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.android.newframework.databinding.ClientInfoTopItemLayoutBinding
import com.chad.library.adapter4.BaseQuickAdapter
import com.iot.CommonInfoItem

class TopInfoAdapter : BaseQuickAdapter<CommonInfoItem, TopInfoAdapter.VH>() {
    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): VH {
        return VH(parent)
    }

    override fun onBindViewHolder(holder: VH, position: Int, item: CommonInfoItem?) {
        if (item == null) return
        holder.binding.apply {
            titleTv.text = item.title
            numTv.text = item.description
            item.iconRes?.let {
                typeIv.setImageResource(it)
            }
        }
    }

    class VH(
        val parent: ViewGroup,
        val binding: ClientInfoTopItemLayoutBinding = ClientInfoTopItemLayoutBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root)

}