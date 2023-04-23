package com.four.yy.vpn.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.four.yy.vpn.adapter.ServerAdapter
import com.four.yy.vpn.base.BaseActivity
import com.four.yy.vpn.databinding.ActivityServerBinding
import com.four.yy.vpn.entity.Country
import com.four.yy.vpn.entity.CountryBean
import com.four.yy.vpn.manager.AdManage
import com.four.yy.vpn.utils.Constant
import com.four.yy.vpn.utils.EntityUtils
import com.four.yy.vpn.utils.SPUtils
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.greenrobot.eventbus.EventBus
import java.lang.reflect.Type

class ServiceActivity : BaseActivity<ActivityServerBinding>() {
    private var isConnection = false
    private var smartAdapter: ServerAdapter? = null
    private var serverAdapter: ServerAdapter? = null
    private val layoutManager by lazy { LinearLayoutManager(this, RecyclerView.VERTICAL, false) }
    private val serverLayoutManager by lazy {
        LinearLayoutManager(
            this,
            RecyclerView.VERTICAL,
            false
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isConnection = intent.getBooleanExtra("isConnection", false)
        smartAdapter = ServerAdapter()
        serverAdapter = ServerAdapter()
        binding.smartRv.layoutManager = layoutManager
        binding.smartRv.adapter = smartAdapter
        binding.allServerRv.layoutManager = serverLayoutManager
        binding.allServerRv.adapter = serverAdapter
        binding.idBack.setOnClickListener {
            showAd()
        }
        setServerList()
        smartAdapter?.setOnItemClickListener(object : ServerAdapter.OnItemClickListener {
            override fun onItemClick(view: View?, country: Country) {
                if (country.isChoose == true) {
                    finish()
                } else {
                    if (isConnection) {
                        val alertDialog = AlertDialog.Builder(this@ServiceActivity)
                            .setMessage("If you want to connect to another VPN, you need to disconnect the current connection first. Do you want to disconnect the current connection?")
                            .setPositiveButton("yes") { p0, p1 ->
                                EventBus.getDefault().post(country)
                                saveConnectingCountryBean(country)
                                finish()
                            }
                            .setNegativeButton("no", null)
                            .create()
                        alertDialog.show()
                    } else {
                        EventBus.getDefault().post(country)
                        saveConnectingCountryBean(country)
                        finish()
                    }
                }
            }
        })
        serverAdapter?.setOnItemClickListener(object : ServerAdapter.OnItemClickListener {
            override fun onItemClick(view: View?, country: Country) {
                if (country.isChoose == true) {
                    finish()
                } else {
                    if (isConnection) {
                        val alertDialog = AlertDialog.Builder(this@ServiceActivity)
                            .setMessage("If you want to connect to another VPN, you need to disconnect the current connection first. Do you want to disconnect the current connection?")
                            .setPositiveButton("yes") { p0, p1 ->
                                EventBus.getDefault().post(country)
                                saveConnectingCountryBean(country)
                                finish()
                            }
                            .setNegativeButton("no", null)
                            .create()
                        alertDialog.show()
                    } else {
                        EventBus.getDefault().post(country)
                        saveConnectingCountryBean(country)
                        finish()
                    }
                }
            }
        })
        loadAd()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setServerList() {
        val countryJson: String? = SPUtils.get().getString(Constant.service, "")
        val countrySmartList: MutableList<Country> = ArrayList()
        val countryServerList: MutableList<Country> = ArrayList()
        val smartJson: String? = SPUtils.get().getString(Constant.smart, "")

        if (countryJson != null) {
            if (countryJson.isNotEmpty()) {
                val type: Type = object : TypeToken<List<CountryBean?>?>() {}.type
                val countryBean: MutableList<CountryBean> =
                    Gson().fromJson(countryJson.toString(), type)
                if (countryBean.isNotEmpty()) {
                    countryBean.forEach {
                        val country: Country = EntityUtils().countryBeanToCountry(it)
                        countryServerList.add(country)
                    }
                }
            }
        }
        if (smartJson != null) {
            if (smartJson.isNotEmpty()) {
                val type: Type = object : TypeToken<List<String?>?>() {}.type
                val smartBean: MutableList<String> =
                    Gson().fromJson(smartJson, type)
                if (smartBean.isNotEmpty()) {

                    smartBean.forEach {
                        for (item in countryServerList) {
                            if (it == item.city) {
                                var country = Country()
                                country.name = "Faster Server"
                                country.host = item.host
                                country.city = item.city
                                country.src = item.src
                                country.method = item.method
                                country.password = item.password
                                country.remotePort = item.remotePort
                                countrySmartList.add(country)
                            }
                        }
                    }
                }
            }
        }

        val countryString = SPUtils.get().getString(Constant.chooseCountry, "")
        if (countryString != null && countryString.isNotEmpty()) {
            val country = Gson().fromJson(countryString, Country::class.java)
            if (country != null) {
                val profileName = country.name
                var isServer = false
                for (item in countryServerList) {
                    if (profileName?.contains(item.name!!) == true) {
                        item.isChoose = true
                        isServer = true
                    }
                }
                if (!isServer) {
                    val profileCity = country.city
                    for (item in countrySmartList) {
                        if (profileCity == item.city) {
                            item.isChoose = true
                        }
                    }
                }

            }
        }

        serverAdapter?.setList(countryServerList)
        serverAdapter?.notifyDataSetChanged()
        smartAdapter?.setList(countrySmartList)
        smartAdapter?.notifyDataSetChanged()
    }

    private fun saveConnectingCountryBean(event: Country) {
        val countryJson: String? = SPUtils.get().getString(Constant.service, "")
        if (countryJson != null) {
            if (countryJson.isNotEmpty()) {
                val type: Type = object : TypeToken<List<CountryBean?>?>() {}.type
                val countryBean: MutableList<CountryBean> =
                    Gson().fromJson(countryJson.toString(), type)
                if (countryBean.isNotEmpty()) {
                    if (event.name?.contains("Faster Server") == true) {
                        val countryData = CountryBean()
                        countryData.country = event.name!!
                        SPUtils.get()
                            .putString(Constant.connectingCountryBean, Gson().toJson(countryData))
                    } else {
                        countryBean.forEach {
                            if (event.name?.equals(it.country) == true) {
                                SPUtils.get()
                                    .putString(Constant.connectingCountryBean, Gson().toJson(it))
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onBackPressed() {
        showAd()
    }

    private var adManage = AdManage()
    private fun loadAd() {
        val adBean = Constant.AdMap[Constant.adInterstitial_r]
        if (adBean?.ad == null) {
            adManage.loadAd(
                Constant.adInterstitial_r,
                this
            )
        }
    }

    private fun showAd() {
        val adBean = Constant.AdMap[Constant.adInterstitial_r]
        var time: Long = 0
        if (adBean != null) {
            time = System.currentTimeMillis() - adBean.saveTime
        } else {
            finish()
            return
        }
        if (adBean.ad != null || time < 50 * 60 * 1000) {
            adManage.showAd(
                this@ServiceActivity,
                Constant.adInterstitial_r,
                adBean,
                null,
                object : AdManage.OnShowAdCompleteListener {
                    override fun onShowAdComplete() {
                        finish()
                    }

                    override fun isMax() {
                        finish()
                    }
                })

        } else {
            finish()
        }
    }
}