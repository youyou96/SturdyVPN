package com.four.yy.vpn.entity

import com.google.gson.annotations.SerializedName

data class IpEntity(
    var ip: String,
    @SerializedName("country_code")
    var country: String
)