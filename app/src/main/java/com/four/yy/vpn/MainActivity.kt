package com.four.yy.vpn

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Bundle
import android.os.RemoteException
import android.os.SystemClock
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceDataStore
import com.four.yy.vpn.activity.PrivacyPolicyWebView
import com.four.yy.vpn.activity.ResultActivity
import com.four.yy.vpn.activity.ServiceActivity
import com.four.yy.vpn.base.BaseActivity
import com.four.yy.vpn.databinding.ActivityMainBinding
import com.four.yy.vpn.databinding.ConnectedViewBinding
import com.four.yy.vpn.databinding.DisconnectViewBinding
import com.four.yy.vpn.entity.AdBean
import com.four.yy.vpn.entity.Country
import com.four.yy.vpn.entity.CountryBean
import com.four.yy.vpn.entity.SmartBean
import com.four.yy.vpn.manager.AdManage
import com.four.yy.vpn.utils.*
import com.github.shadowsocks.Core
import com.github.shadowsocks.aidl.IShadowsocksService
import com.github.shadowsocks.aidl.ShadowsocksConnection
import com.github.shadowsocks.bg.BaseService
import com.github.shadowsocks.database.ProfileManager
import com.github.shadowsocks.preference.OnPreferenceDataStoreChangeListener
import com.github.shadowsocks.utils.Key
import com.github.shadowsocks.utils.StartService
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : BaseActivity<ActivityMainBinding>(), ShadowsocksConnection.Callback,
    OnPreferenceDataStoreChangeListener {
    private var state = BaseService.State.Idle
    private var adManage = AdManage()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
        initView()
        connection.connect(this, this)
        EventBus.getDefault().register(this)
        onCLickListener()
        loadNativeAd()
    }


    private fun onCLickListener() {
        binding.homeSettingSrc.setOnClickListener {
            if (binding.drawerLayout.isOpen) {
                binding.drawerLayout.close()
            } else {
                binding.drawerLayout.open()
            }
        }
        binding.homeService.setOnClickListener {
            if (binding.drawerLayout.isOpen) {
                return@setOnClickListener
            }
            //choose service
            val intent = Intent(this, ServiceActivity::class.java)
            intent.putExtra("isConnection", state.canStop)
            startActivity(intent)
        }
        binding.homeConnectStatusSrc.setOnClickListener {
            if (binding.drawerLayout.isOpen) {
                return@setOnClickListener
            }
            if (!ButtonUtils.isFastDoubleClick(R.id.home_connect_status_src)) {
                connectVpn()
            }
        }
        binding.settingLayout.contactUs.setOnClickListener {
            if (binding.drawerLayout.isOpen) {
                openSystemMail()
            }
        }

        binding.settingLayout.privacyPolicy.setOnClickListener {
            if (binding.drawerLayout.isOpen) {
                jumpActivity(PrivacyPolicyWebView::class.java)
            }
        }
        binding.settingLayout.shareTv.setOnClickListener {
            if (binding.drawerLayout.isOpen) {
                val intent = Intent()
                intent.action = Intent.ACTION_SEND
                intent.putExtra(Intent.EXTRA_TEXT, Constant.shareUrl)
                intent.type = "text/plain"
                startActivity(intent)
            }
        }
        binding.homeServerSrc.setOnClickListener {
            if (binding.drawerLayout.isOpen) {
                return@setOnClickListener
            }
            //choose service
            val intent = Intent(this, ServiceActivity::class.java)
            intent.putExtra("isConnection", state.canStop)
            startActivity(intent)
        }
    }

    private fun openSystemMail() {
        val uri: Uri = Uri.parse("mailto:" + Constant.mail)
        val packageInfos: List<ResolveInfo> =
            packageManager!!.queryIntentActivities(Intent(Intent.ACTION_SENDTO, uri), 0)
        val tempPkgNameList: MutableList<String> = ArrayList()
        val emailIntents: MutableList<Intent> = ArrayList()
        for (info in packageInfos) {
            val pkgName = info.activityInfo.packageName
            if (!tempPkgNameList.contains(pkgName)) {
                tempPkgNameList.add(pkgName)
                val intent: Intent? = packageManager!!.getLaunchIntentForPackage(pkgName)
                if (intent != null) {
                    emailIntents.add(intent)
                }
            }
        }
        if (emailIntents.isNotEmpty()) {
            val intent = Intent(Intent.ACTION_SENDTO, uri)
            startActivity(intent)
            val chooserIntent =
                Intent.createChooser(intent, "Please select mail application")
            if (chooserIntent != null) {
                startActivity(chooserIntent)
            } else {
                showDialogByActivity("Please set up a Mail account", "OK", true, null)
            }
        } else {
            showDialogByActivity("Please set up a Mail account", "OK", true, null)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun initView() {
        if (Constant.isShowLead) {
            val customizedDialog = CustomizedDialog(this, "images/main_lead.json", false, true)
            if (!customizedDialog.isShowing) {
                binding.homeConnectStatusSrc.visibility = View.INVISIBLE
                customizedDialog.show()
            }
            customizedDialog.setOnClick {
                Constant.isShowLead = false
                customizedDialog.dismiss()
                binding.homeConnectStatusSrc.visibility = View.VISIBLE
                if (!state.canStop) {
                    if (!ButtonUtils.isFastDoubleClick(R.id.animation_view)) {
                        connectVpn()
                    }
                }

            }
            customizedDialog.setOnCancelListener {
                Constant.isShowLead = false
                binding.homeConnectStatusSrc.visibility = View.VISIBLE
            }
        }
        val countryString = SPUtils.get().getString(Constant.chooseCountry, "")
        if (countryString != null && countryString.isNotEmpty()) {
            val country = Gson().fromJson(countryString, Country::class.java)
            if (country != null) {
                country.src?.let { it1 ->
                    binding.homeCountryLogo.setBackgroundResource(
                        it1
                    )
                }
                binding.homeCountryTv.text = country.name + "-" + country.city
            }
        }

    }

    private fun connectVpn() {
        if (InterNetUtil().isShowIR()) {
            showDialogByActivity(
                "Due to the policy reason , this service is not available in your country",
                "confirm", false
            ) { dialog, which -> finish() }

        } else {
            isHasNet()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun refreshUi() {
        var countryBeanJson = ""
        if (state.canStop) {
            if (SPUtils.get()
                    .getString(Constant.connectedCountryBean, "")?.isNotEmpty() == true
            ) {
                countryBeanJson = SPUtils.get()
                    .getString(Constant.connectedCountryBean, "").toString()
                Log.e("mainServiceChoose", "connected")
            }
        }
        if (countryBeanJson?.isEmpty() == true) {
            countryBeanJson = SPUtils.get()
                .getString(Constant.connectingCountryBean, "").toString()
            Log.e("mainServiceChoose", "connecting")

        }

        if (countryBeanJson != null) {
            val countryBean = Gson().fromJson(countryBeanJson, CountryBean::class.java)
            if (countryBean != null) {
                val country = EntityUtils().countryBeanToCountry(countryBean)
                country.src?.let { it1 ->
                    binding.homeCountryLogo.setBackgroundResource(
                        it1
                    )
                }
                binding.homeCountryTv.text = country?.name + "-" + country?.city
                Log.e("mainServiceChoose", country?.name + "-" + country?.city)
            }
        }

    }

    private fun toggle() = if (state.canStop) showConnect() else connect.launch(null)
    private fun isHasNet() {
        if (InterNetUtil().isNetConnection(this)) {
            toggle()
        } else {
            showDialogByActivity("Please check your network", "OK", true, null)
        }
    }

    private val connect = registerForActivityResult(StartService()) {
        if (!it) {
            showConnect()
        }
    }
    private var countryBean: CountryBean? = null
    private fun connectAnnotation() {
        ProfileManager.clear()
//        var countryBean: CountryBean? = null
        val countryBeanJson = if (state.canStop) SPUtils.get()
            .getString(Constant.connectedCountryBean, "") else SPUtils.get()
            .getString(Constant.connectingCountryBean, "")
        if (countryBeanJson != null) {
            if (countryBeanJson.isNotEmpty()) {
                countryBean = Gson().fromJson(countryBeanJson, CountryBean::class.java)
            }
        }
        if (countryBean == null) {
            runBlocking {
                val smartList = getFastSmart()
                if (smartList.isNotEmpty()) {
                    val fast = if (smartList.size >= 3) {
                        Random().nextInt(3)
                    } else {
                        Random().nextInt(smartList.size)
                    }
                    countryBean = smartList[fast].smart
                    countryBean?.country = "Faster Server"
                    val country = EntityUtils().countryBeanToCountry(countryBean!!)
                    country.src = R.mipmap.fast
                    val profile = EntityUtils().countryToProfile(country)
                    val profileNew = ProfileManager.createProfile(profile)
                    Core.switchProfile(profileNew.id)
                    SPUtils.get()
                        .putString(Constant.connectingCountryBean, Gson().toJson(countryBean))
                }
            }
        } else {
            val country = countryBean?.let { EntityUtils().countryBeanToCountry(it) }
            val profile = country?.let { EntityUtils().countryToProfile(it) }
            val profileNew = profile?.let { ProfileManager.createProfile(it) }
            profileNew?.id?.let { Core.switchProfile(it) }
            SPUtils.get().putString(Constant.connectingCountryBean, Gson().toJson(countryBean))
        }
    }

    private suspend fun getFastSmart(): MutableList<SmartBean> {
        val smartJson = SPUtils.get().getString(Constant.smart, "")
        val serviceJson = SPUtils.get().getString(Constant.service, "")
        var serviceList: MutableList<CountryBean> = mutableListOf()
        val smartBeanList: MutableList<SmartBean> = mutableListOf()
        if (serviceJson?.isNotEmpty() == true) {
            val serviceType: Type = object : TypeToken<List<CountryBean?>?>() {}.type
            serviceList = Gson().fromJson(serviceJson, serviceType)
        }
        if (smartJson?.isNotEmpty() == true) {
            val type: Type = object : TypeToken<List<String?>?>() {}.type
            val smartList: MutableList<String> = Gson().fromJson(smartJson, type)
            if (smartList.isNotEmpty() && serviceList.isNotEmpty()) {
                for (item in smartList) {
                    for (service in serviceList) {
                        if (item == service.city) {
                            smartBeanList.add(
                                SmartBean(
                                    service,
                                    InterNetUtil().delayTest(service.ip, 1)
                                )
                            )
                        }
                    }

                }
            }
        }
        return smartBeanList
    }

    override fun stateChanged(state: BaseService.State, profileName: String?, msg: String?) {
        changeConnectionStatus(state)
    }

    override fun onServiceConnected(service: IShadowsocksService) {
        changeConnectionStatus(
            try {
                BaseService.State.values()[service.state]
            } catch (_: RemoteException) {
                BaseService.State.Idle
            }
        )
    }

    private val connection = ShadowsocksConnection(true)
    override fun onBinderDied() {
        connection.disconnect(this)
        connection.connect(this, this)
    }

    override fun onPreferenceDataStoreChanged(store: PreferenceDataStore, key: String) {
        when (key) {
            Key.serviceMode -> {
                connection.disconnect(this)
                connection.connect(this, this)
            }
        }
    }

    private fun changeConnectionStatus(status: BaseService.State) {
        this.state = status

        when (status) {
            BaseService.State.Idle -> {
                SPUtils.get().putBoolean(Constant.isConnectStatus, false)
                val disconnectView = DisconnectViewBinding.inflate(layoutInflater).root
                binding.homeConnectStatusSrc.removeAllViews()
                binding.homeConnectStatusSrc.addView(disconnectView)
                binding.theConnectionTimeTv.stop()
                binding.theConnectionTimeTv.text = "00:00:00"
                Toast.makeText(this, "please try again", Toast.LENGTH_LONG).show()
            }
            BaseService.State.Connected -> {
                SPUtils.get().putBoolean(Constant.isConnectStatus, true)
                if (countryBean != null) {
                    SPUtils.get()
                        .putString(Constant.connectedCountryBean, Gson().toJson(countryBean))
                    SPUtils.get().putString(
                        Constant.chooseCountry,
                        Gson().toJson(EntityUtils().countryBeanToCountry(countryBean!!))
                    )
                }
                val connectedView = ConnectedViewBinding.inflate(layoutInflater).root
                binding.homeConnectStatusSrc.removeAllViews()
                binding.homeConnectStatusSrc.addView(connectedView)
                binding.theConnectionTimeTv.setOnChronometerTickListener {
                    val time = SystemClock.elapsedRealtime() - it.base
                    val date = Date(time)
                    val sdf = SimpleDateFormat("HH:mm:ss", Locale.US)
                    sdf.timeZone = TimeZone.getTimeZone("UTC")
                    binding.theConnectionTimeTv.text = sdf.format(date)
                }
                val connectTime = SPUtils.get().getLong(Constant.connectTime, 0)
                if (connectTime > 0) {
                    binding.theConnectionTimeTv.base = connectTime
                } else {
                    binding.theConnectionTimeTv.base = SystemClock.elapsedRealtime()
                }
                if (SystemClock.elapsedRealtime() - (binding.theConnectionTimeTv.base) < 20 && SPUtils.get()
                        .getBoolean(Constant.isShowResultKey, false)
                ) {
                    lifecycleScope.launch(Dispatchers.Main.immediate) {
                        delay(300L)
                        if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                            var country: Country? = null
                            if (countryBean != null) {
                                country = EntityUtils().countryBeanToCountry(countryBean!!)
                            }
                            val srcInt = if (country != null) country!!.src else R.mipmap.fast
                            val intent = Intent(this@MainActivity, ResultActivity::class.java)
                            intent.putExtra("base", binding.theConnectionTimeTv.base)
                            intent.putExtra("srcInt", srcInt)
                            startActivity(intent)
                            SPUtils.get().putBoolean(Constant.isShowResultKey, false)
                            refreshUi()
                        }
                    }
                }
                binding.theConnectionTimeTv.start()
            }
            BaseService.State.Stopped -> {
                val disconnectView = DisconnectViewBinding.inflate(layoutInflater).root
                binding.homeConnectStatusSrc.removeAllViews()
                binding.homeConnectStatusSrc.addView(disconnectView)
                binding.theConnectionTimeTv.stop()
                binding.theConnectionTimeTv.text = "00:00:00"
                SPUtils.get().putLong(Constant.connectTime, 0L)
                if (SPUtils.get()
                        .getBoolean(Constant.isShowResultKey, false) && Constant.text != "00:00:00"
                ) {
                    SPUtils.get().putBoolean(Constant.isConnectStatus, false)
                    var country: Country? = null
                    if (countryBean != null) {
                        country = EntityUtils().countryBeanToCountry(countryBean!!)
                    }
                    val srcInt = if (country != null) country!!.src else R.mipmap.fast
                    lifecycleScope.launch(Dispatchers.Main.immediate) {
                        delay(300L)
                        if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                            val intent = Intent(this@MainActivity, ResultActivity::class.java)
                            intent.putExtra("text", Constant.text)
                            intent.putExtra("isStop", true)
                            intent.putExtra("srcInt", srcInt)
                            startActivity(intent)
                            SPUtils.get().putBoolean(Constant.isShowResultKey, false)
                            refreshUi()
                            val countryBeanJson =
                                SPUtils.get().getString(Constant.connectingCountryBean, "")
                            if (countryBeanJson != null && countryBeanJson.isNotEmpty()) {
                                val countryBeanConnecting =
                                    Gson().fromJson(countryBeanJson, CountryBean::class.java)
                                if (countryBeanConnecting != null) {
                                    SPUtils.get().putString(
                                        Constant.chooseCountry,
                                        Gson().toJson(
                                            EntityUtils().countryBeanToCountry(
                                                countryBeanConnecting
                                            )
                                        )
                                    )
                                }
                            }
                        }
                    }

                }
            }
            else -> {
                SPUtils.get().putBoolean(Constant.isConnectStatus, false)
                val disconnectView = DisconnectViewBinding.inflate(layoutInflater).root
                binding.homeConnectStatusSrc.removeAllViews()
                binding.homeConnectStatusSrc.addView(disconnectView)
                binding.theConnectionTimeTv.base = SystemClock.elapsedRealtime()
                binding.theConnectionTimeTv.stop()
                binding.theConnectionTimeTv.text = "00:00:00"
                SPUtils.get().putLong(Constant.connectTime, 0L)
            }
        }
    }

    private var connectionJob: Job? = null
    private var time: Int = 0
    private var interAdIsShow = false

    private var customizedDialog: CustomizedDialog? = null
    private fun showConnect() {
        time = 0
        if (state.canStop) {
            Constant.text = binding.theConnectionTimeTv.text as String
        }
        SPUtils.get().putBoolean(Constant.isShowResultKey, true)
        val isCancel = state.canStop
        customizedDialog =
            CustomizedDialog(this@MainActivity, "images/data.json", isCancel, isCancel)
        connectionJob = lifecycleScope.launch {
            flow {
                (0 until 10).forEach {
                    delay(1000)
                    time += 1
                    emit(it)
                }
            }.onStart {
                //start
                customizedDialog?.show()
                connectAnnotation()
            }.onCompletion {
                //finish
                if (customizedDialog?.isShowing == true && !interAdIsShow) {
                    customizedDialog?.dismiss()
                    if (state.canStop) {
                        Core.stopService()
                    } else {
                        Core.startService()
                    }
                }
            }.collect {
                //process
                if (time == 1) {
                    loadInterAd(customizedDialog!!)
                }
                if (customizedDialog?.isShowing == false) {
                    connectionJob?.cancel()
                    return@collect
                }
            }
        }

    }
    private fun loadInterAd(customizedDialog: CustomizedDialog) {
        interAdIsShow = false
        val adBean = Constant.AdMap[Constant.adInterstitial_h]
        var time: Long = 0
        if (adBean != null) {
            time = System.currentTimeMillis() - adBean.saveTime
        }
        if (adBean?.ad == null || time > 50 * 60 * 1000) {
            adManage.loadAd(
                Constant.adInterstitial_h,
                this,
                object : AdManage.OnLoadAdCompleteListener {
                    override fun onLoadAdComplete(ad: AdBean?) {
                        if (ad?.ad != null && customizedDialog.isShowing) {
                            interAdIsShow = true
                            adManage.showAd(
                                this@MainActivity,
                                Constant.adInterstitial_h,
                                ad,
                                null,
                                object : AdManage.OnShowAdCompleteListener {
                                    override fun onShowAdComplete() {
                                        AdManage().loadAd(
                                            Constant.adInterstitial_h,
                                            this@MainActivity
                                        )
                                        if (customizedDialog.isShowing) {
                                            customizedDialog.dismiss()
                                        }
                                        if (state.canStop) {
                                            Core.stopService()
                                        } else {
                                            Core.startService()
                                        }
                                    }

                                    override fun isMax() {
                                        if (customizedDialog.isShowing) {
                                            customizedDialog.dismiss()
                                        }
                                        if (state.canStop) {
                                            Core.stopService()
                                        } else {
                                            Core.startService()
                                        }
                                    }

                                })
                        }
                    }

                    override fun isMax() {
                        if (customizedDialog.isShowing) {
                            customizedDialog.dismiss()
                        }
                        if (state.canStop) {
                            Core.stopService()
                        } else {
                            Core.startService()
                        }
                    }

                })
        } else {
            if (customizedDialog.isShowing) {
                interAdIsShow = true
                adManage.showAd(
                    this@MainActivity,
                    Constant.adInterstitial_h,
                    adBean,
                    null,
                    object : AdManage.OnShowAdCompleteListener {
                        override fun onShowAdComplete() {
                            AdManage().loadAd(Constant.adInterstitial_h, this@MainActivity)
                            if (customizedDialog.isShowing) {
                                customizedDialog.dismiss()
                            }
                            if (state.canStop) {
                                Core.stopService()
                            } else {
                                Core.startService()
                            }
                        }

                        override fun isMax() {
                            if (customizedDialog.isShowing) {
                                customizedDialog.dismiss()
                            }
                            if (state.canStop) {
                                Core.stopService()
                            } else {
                                Core.startService()
                            }
                        }

                    })
            }

        }

        val adBeanNativeR = Constant.AdMap[Constant.adNative_r]
        if (adBeanNativeR?.ad == null) {
            AdManage().loadAd(Constant.adNative_r, this)
        }
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: Country?) {
        connectVpn()
    }

    override fun onDestroy() {
        super.onDestroy()
        SPUtils.get().putLong(Constant.connectTime, binding.theConnectionTimeTv.base)
        SPUtils.get().putBoolean(Constant.isShowResultKey, false)
        EventBus.getDefault().unregister(this)
        countryBean = null
    }

    private fun loadNativeAd() {
        val adBean = Constant.AdMap[Constant.adNative_h]
        var time: Long = 0
        if (adBean != null) {
            time = System.currentTimeMillis() - adBean.saveTime
        }

        if (adBean?.ad == null || time > 50 * 60 * 1000) {
            adManage.loadAd(Constant.adNative_h, this, object : AdManage.OnLoadAdCompleteListener {
                override fun onLoadAdComplete(ad: AdBean?) {
                    if (ad?.ad != null) {
                        showNativeAd(ad)
                    }
                }

                override fun isMax() {

                }
            })
        } else {
            showNativeAd(adBean)
        }
    }

    fun showNativeAd(ad: AdBean) {
        adManage.showAd(
            this@MainActivity,
            Constant.adNative_h,
            ad,
            binding.adFrameLayout,
            object : AdManage.OnShowAdCompleteListener {
                override fun onShowAdComplete() {
                }

                override fun isMax() {
                }

            })
    }



    override fun onPause() {
        super.onPause()
        if (customizedDialog?.isShowing == true && state.canStop) {
            customizedDialog?.dismiss()
        }
    }

}