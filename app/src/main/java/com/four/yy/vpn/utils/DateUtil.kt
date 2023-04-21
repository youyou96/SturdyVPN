package com.four.yy.vpn.utils

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.Date

class DateUtil {
    @SuppressLint("SimpleDateFormat")
    fun getTime(): String {
        val simpleDateFormat = SimpleDateFormat("yyyy年MM月dd日")
        val curDate = Date(System.currentTimeMillis())
        return simpleDateFormat.format(curDate)
    }
}