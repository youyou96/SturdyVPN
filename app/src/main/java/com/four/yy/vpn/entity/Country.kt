package com.four.yy.vpn.entity

import com.four.yy.vpn.R


data class Country(
    var id: Int = 0,
    var name: String? = "Faster Server",
    var host: String = "example.shadowsocks.org",
    var remotePort: Int = 8388,
    var password: String = "u1rRWTssNv0p",
    var account: String = "chacha20-ietf-poly1305",
    var city:String = "",
    var src: Int? = R.mipmap.fast,
    var method: String = "aes-256-cfb",
    var isChoose: Boolean? = false,
    var route: String = "all",
    var remoteDns: String = "dns.google"
)