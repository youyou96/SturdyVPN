package com.four.yy.vpn.activity

import android.os.Bundle
import android.view.View
import com.four.yy.vpn.R
import com.four.yy.vpn.base.BaseActivity
import com.four.yy.vpn.databinding.ActivityResultBinding
import com.four.yy.vpn.entity.AdBean
import com.four.yy.vpn.entity.CountryBean
import com.four.yy.vpn.manager.AdManage
import com.four.yy.vpn.utils.Constant
import com.four.yy.vpn.utils.EntityUtils
import com.four.yy.vpn.utils.SPUtils
import com.google.gson.Gson

class ResultActivity : BaseActivity<ActivityResultBinding>() {
    private var isStop: Boolean = false
    private var text = ""
    private var adManage = AdManage()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.idBack.setOnClickListener {
            finish()
        }
        isStop = intent.getBooleanExtra("isStop", false)
        text = intent.getStringExtra("text").toString()
        val countryJson = SPUtils.get().getString(Constant.connectedCountryBean, "")
        if (countryJson != null && countryJson.isNotEmpty()) {
            val countryBean = Gson().fromJson(countryJson, CountryBean::class.java)
            if (countryBean != null) {
                val country = EntityUtils().countryBeanToCountry(countryBean)
                binding.homeCountryTv.text = country.name
                if (country.src == 0) {
                    binding.homeCountryLogo.visibility = View.INVISIBLE
                } else {
                    country.src?.let { binding.homeCountryLogo.setImageResource(it) }
                }
            }
        }
        if (isStop) {
            binding.connectionStatus.text = "Disconnected succeeded"
            binding.homeService.setBackgroundResource(R.drawable.disconnect_background)
        } else {
            binding.connectionStatus.text = "Connected succeeded"
            binding.homeService.setBackgroundResource(R.drawable.connected_backgroud)
        }
        loadNativeAd()
    }

    private fun loadNativeAd() {
        val adBean = Constant.AdMap[Constant.adNative_r]
        var time: Long = 0
        if (adBean != null) {
            time = System.currentTimeMillis() - adBean.saveTime
        }
        if (adBean?.ad == null || time > 50 * 60 * 1000) {
            adManage.loadAd(Constant.adNative_r, this, object : AdManage.OnLoadAdCompleteListener {
                override fun onLoadAdComplete(ad: AdBean?) {
                    if (ad?.ad != null) {
                       showNativeAd(ad)
                    }
                }

                override fun isMax() {
                    binding.adFrameLayout.setBackgroundResource(R.mipmap.result_ad_background)
                }
            })
        } else {
          showNativeAd(adBean)
        }
    }
    fun showNativeAd(ad: AdBean){
        adManage.showAd(
            this@ResultActivity,
            Constant.adNative_r,
            ad,
            binding.adFrameLayout,
            object :
                AdManage.OnShowAdCompleteListener {
                override fun onShowAdComplete() {
                }

                override fun isMax() {
                    binding.adFrameLayout.setBackgroundResource(R.mipmap.result_ad_background)
                }

            })
    }
}