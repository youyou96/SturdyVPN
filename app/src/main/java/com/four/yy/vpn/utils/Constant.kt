package com.four.yy.vpn.utils

import com.four.yy.vpn.entity.AdBean


open class Constant {
    companion object {
        const val isConnectStatus = "isConnectStatus"
        const val iR = "iR"
        const val smart = "smart"
        const val service = "service"
        const val connectedCountryBean = "connectedCountryBean"
        const val chooseCountry = "chooseCountry"
        const val connectingCountryBean = "connectingCountryBean"
        const val mail = "1192390712@qq.com"
        const val PrivacyPolicy = "https://www.baidu.com"
        const val connectTime = "connectTime"
        const val isShowResultKey = "isShowResultKey"
        const val shareUrl = "https://play.google.com/store/apps/details?id="
        const val adResourceBean ="adResourceBean"
        const val adOpen = "serpac_o_open"
        const val adInterstitial_r = "serpac_i_2R"
        const val adInterstitial_h = "serpac_i_2H"
        const val adNative_h = "serpac_n_home"
        const val adNative_r = "serpac_n_result"
        const val openAdType ="open"
        const val nativeAdType = "native"
        const val interAdType = "inter"
        const val adTimeBean = "adTimeBean"


        var AdMap: MutableMap<String, AdBean> = mutableMapOf()
        var AdMapStatus: MutableMap<String, Boolean> = mutableMapOf()

        var isShowLead  = true
        var text = "00:00:00"
    }
}