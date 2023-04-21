package com.four.yy.vpn.activity

import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import com.four.yy.vpn.MainActivity
import com.four.yy.vpn.base.BaseActivity
import com.four.yy.vpn.databinding.ActivityFlashBinding
import com.four.yy.vpn.entity.AdBean
import com.four.yy.vpn.manager.AdManage
import com.four.yy.vpn.utils.Constant
import com.four.yy.vpn.utils.EntityUtils
import com.four.yy.vpn.utils.InterNetUtil
import com.four.yy.vpn.utils.SPUtils

private const val COUNTER_TIME = 10L

class FlashActivity : BaseActivity<ActivityFlashBinding>() {
    private var isShowAd = false
    private var timer: CountDownTimer? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        countDownTimer()
        InterNetUtil().getIpByServer(this)
        setData()
    }

    private fun countDownTimer() {
        timer = object : CountDownTimer(COUNTER_TIME * 1000, 1000L) {
            override fun onTick(p0: Long) {
                val process = 100 - (p0 * 100 / COUNTER_TIME / 1000)
                binding.progressBar.setProgress(process.toInt())
                if (process >= 20) {
                    if (!isShowAd) {
                        showAd()
                    }
                }
            }

            override fun onFinish() {
                jumpActivityFinish(MainActivity::class.java)
            }

        }
        (timer as CountDownTimer).start()
    }

    override fun onStart() {
        super.onStart()
        loadAd()
    }

    override fun onRestart() {
        super.onRestart()

        if (timer != null) {
            timer?.cancel()
            countDownTimer()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
    }

    private fun setData() {
        if (SPUtils.get().getString(Constant.smart, "")?.isEmpty() == true) {
            val smartJson = EntityUtils().obtainNativeJsonData(this, "city.json")
            SPUtils.get().putString(Constant.smart, smartJson.toString())
        }
        if (SPUtils.get().getString(Constant.service, "")?.isEmpty() == true) {
            val serviceJson = EntityUtils().obtainNativeJsonData(this, "service.json")
            SPUtils.get().putString(Constant.service, serviceJson.toString())
        }
    }

    private fun showAd() {
        val adBean = Constant.AdMap[Constant.adOpen]
        val adManage = AdManage()
        var time: Long = 0
        if (adBean != null) {
            time = System.currentTimeMillis() - adBean.saveTime
        }
        if (adBean?.ad != null && time < 50 * 60 * 1000) {
            timer?.cancel()
            isShowAd = true
            adManage.showAd(
                this@FlashActivity,
                Constant.adOpen,
                adBean,
                null,
                object : AdManage.OnShowAdCompleteListener {
                    override fun onShowAdComplete() {
                        jumpActivityFinish(MainActivity::class.java)
                    }

                    override fun isMax() {
                        jumpActivityFinish(MainActivity::class.java)
                    }

                })
        } else {
            adManage.loadAd(Constant.adOpen, this, object : AdManage.OnLoadAdCompleteListener {
                override fun onLoadAdComplete(ad: AdBean?) {
                }

                override fun isMax() {
                    jumpActivityFinish(MainActivity::class.java)
                }

            })
        }
    }

    private fun loadAd() {
        isShowAd = false
        var adBean = Constant.AdMap[Constant.adOpen]
        val adManage = AdManage()
        if (adBean?.ad == null) {
            adManage.loadAd(Constant.adOpen, this, object : AdManage.OnLoadAdCompleteListener {
                override fun onLoadAdComplete(ad: AdBean?) {
                }

                override fun isMax() {
                }

            })
        }

    }

    override fun onBackPressed() {
    }
}