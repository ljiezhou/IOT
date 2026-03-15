package com.iot

data class CommonInfoItem(
    val type: Int = 0,
    val title: String,
    val description: String = "",
    val iconRes: Int? = null,    // 本地 drawable 资源 id（可选）
    val iconUrl: String = ""  // 网络图片地址（可选）
)