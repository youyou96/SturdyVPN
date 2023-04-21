package com.four.yy.vpn.entity

data class AdBean(
    var serpac_id: String,
    var serpac_source: String,
    var serpac_type: String,
    var serpac_pri: Int = 0,
    var saveTime: Long = 0,
    var ad: Any? = null
)