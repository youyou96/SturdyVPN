package com.four.yy.vpn.entity

import com.google.gson.annotations.SerializedName


data class CountryBean(
    @SerializedName("serpa_ip")
    var ip: String = "51.161.131.222",
    @SerializedName("serpa_port")
    var port: Int = 4391,
    @SerializedName("serpa_pwd")
    var pwd: String = "t7I=X6YounKxhEaz",
    @SerializedName("serpa_account")
    var account: String = "chacha20-ietf-poly1305",
    @SerializedName("serpa_country")
    var country: String = "Austrilia",
    @SerializedName("serpa_city")
    var city: String = "Sydney"

)

