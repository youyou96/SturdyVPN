package com.four.yy.vpn.manager

import android.content.Context
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import com.four.yy.vpn.databinding.AdViewHomeBinding
import com.four.yy.vpn.databinding.AdViewResultBinding
import com.four.yy.vpn.entity.AdBean
import com.four.yy.vpn.entity.AdResourceBean
import com.four.yy.vpn.entity.AdTimeBean
import com.four.yy.vpn.utils.Constant
import com.four.yy.vpn.utils.DateUtil
import com.four.yy.vpn.utils.EntityUtils
import com.four.yy.vpn.utils.SPUtils
import com.google.android.gms.ads.*
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.gson.Gson

private const val LOG_TAG = "AdManage"

class AdManage {
    fun loadAd(
        adType: String,
        context: Context,
        onLoadAdCompleteListener: OnLoadAdCompleteListener? = null
    ) {
        if (isMax()) {
            onLoadAdCompleteListener?.isMax()
            return
        }
        var adResourceBeanJson = SPUtils.get().getString(Constant.adResourceBean, "")
        if (adResourceBeanJson == null || adResourceBeanJson.isEmpty()) {
            adResourceBeanJson = EntityUtils().obtainNativeJsonData(context, "ad.json").toString()
            SPUtils.get().putString(Constant.adResourceBean, adResourceBeanJson)
        }
        val adResourceBean =
            Gson().fromJson(adResourceBeanJson, AdResourceBean::class.java) ?: return
        if (Constant.AdMapStatus[adType] == true) {
            Log.e(LOG_TAG, "ad location ${adType}  is  loading")
            return
        }
        when (adType) {
            Constant.adOpen -> {
                val adBeanList = adResourceBean.serpac_o_open
                adBeanList.sortByDescending { it.serpac_pri }
                loadAd(adType, 0, adBeanList, context, onLoadAdCompleteListener)
            }
            Constant.adInterstitial_r -> {
                val adBeanList = adResourceBean.serpac_i_2R
                adBeanList.sortByDescending { it.serpac_pri }
                loadAd(adType, 0, adBeanList, context, onLoadAdCompleteListener)
            }
            Constant.adInterstitial_h -> {
                val adBeanList = adResourceBean.serpac_i_2H
                adBeanList.sortByDescending { it.serpac_pri }
                loadAd(adType, 0, adBeanList, context, onLoadAdCompleteListener)

            }
            Constant.adNative_h -> {
                val adBeanList = adResourceBean.serpac_n_home
                adBeanList.sortByDescending { it.serpac_pri }
                loadAd(adType, 0, adBeanList, context, onLoadAdCompleteListener)
            }
            Constant.adNative_r -> {
                val adBeanList = adResourceBean.serpac_n_result
                adBeanList.sortByDescending { it.serpac_pri }
                loadAd(adType, 0, adBeanList, context, onLoadAdCompleteListener)
            }
        }
    }

    private fun loadAd(
        adType: String,
        startIndex: Int = 0,
        adBeanList: MutableList<AdBean>,
        context: Context,
        onLoadAdCompleteListener: OnLoadAdCompleteListener? = null
    ) {
        Constant.AdMapStatus[adType] = true

        if (startIndex == adBeanList.size) {
            Constant.AdMapStatus[adType] = false
            return
        }
        val adBean = adBeanList[startIndex]
        Log.e(
            LOG_TAG,
            "加载广告  优先级： ${adBean?.serpac_pri}   广告ID： ${adBean?.serpac_id} 广告location: ${adType}"
        )
        when (adBean.serpac_type) {
            Constant.openAdType -> {
                loadOpenAd(
                    adType,
                    adBeanList[startIndex],
                    context,
                    object : OnLoadAdCompleteListener {
                        override fun onLoadAdComplete(ad: AdBean?) {
                            if (ad?.ad == null) {
                                loadAd(
                                    adType,
                                    startIndex + 1,
                                    adBeanList,
                                    context,
                                    onLoadAdCompleteListener
                                )
                            } else {
                                Constant.AdMapStatus[adType] = false
                                onLoadAdCompleteListener?.onLoadAdComplete(ad)
                            }
                        }

                        override fun isMax() {
                            onLoadAdCompleteListener?.isMax()
                        }

                    })
            }
            Constant.nativeAdType -> {
                loadNativeAd(
                    adType,
                    adBeanList[startIndex],
                    context,
                    object : OnLoadAdCompleteListener {
                        override fun onLoadAdComplete(ad: AdBean?) {
                            if (ad?.ad == null) {
                                loadAd(
                                    adType,
                                    startIndex + 1,
                                    adBeanList,
                                    context,
                                    onLoadAdCompleteListener
                                )
                            } else {
                                Constant.AdMapStatus[adType] = false
                                onLoadAdCompleteListener?.onLoadAdComplete(ad)

                            }
                        }

                        override fun isMax() {
                            onLoadAdCompleteListener?.isMax()
                        }

                    })
            }
            Constant.interAdType -> {
                loadInterstitialAd(
                    adType,
                    adBeanList[startIndex],
                    context,
                    object : OnLoadAdCompleteListener {
                        override fun onLoadAdComplete(ad: AdBean?) {
                            if (ad?.ad == null) {
                                loadAd(
                                    adType,
                                    startIndex + 1,
                                    adBeanList,
                                    context,
                                    onLoadAdCompleteListener
                                )
                            } else {
                                Constant.AdMapStatus[adType] = false
                                onLoadAdCompleteListener?.onLoadAdComplete(ad)
                            }
                        }

                        override fun isMax() {
                            onLoadAdCompleteListener?.isMax()
                        }

                    })
            }
        }
    }


    private fun loadOpenAd(
        adType: String,
        adBean: AdBean,
        context: Context,
        onLoadAdCompleteListener: OnLoadAdCompleteListener
    ) {
        val request = AdRequest.Builder().build()
        AppOpenAd.load(
            context,
            adBean.serpac_id,
            request,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                /**
                 * Called when an app open ad has loaded.
                 *
                 * @param ad the loaded app open ad.
                 */
                override fun onAdLoaded(ad: AppOpenAd) {
                    Log.e(
                        LOG_TAG,
                        "请求广告成功 优先级： ${adBean?.serpac_pri}   广告ID： ${adBean?.serpac_id} 广告location: ${adType}"
                    )
                    adBean.saveTime = System.currentTimeMillis()
                    adBean.ad = ad
                    Constant.AdMap[adType] = adBean
                    onLoadAdCompleteListener.onLoadAdComplete(adBean)
                }

                /**
                 * Called when an app open ad has failed to load.
                 *
                 * @param loadAdError the error.
                 */
                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.e(
                        LOG_TAG,
                        "请求广告失败 优先级： ${adBean?.serpac_pri}   广告ID： ${adBean?.serpac_id}  广告location: ${adType} "
                    )
                    adBean.ad = null
                    onLoadAdCompleteListener.onLoadAdComplete(adBean)
                }
            })
    }

    private fun loadInterstitialAd(
        adType: String,
        adBean: AdBean,
        context: Context,
        onLoadAdCompleteListener: OnLoadAdCompleteListener
    ) {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            context,
            adBean.serpac_id,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    adBean.ad = null
                    Log.e(
                        LOG_TAG,
                        "广告加载失败 优先级：  ${adBean.serpac_pri}   广告ID： ${adBean.serpac_id} 广告位：${adType}"
                    )
                    onLoadAdCompleteListener.onLoadAdComplete(adBean)
                }

                override fun onAdLoaded(ad: InterstitialAd) {
                    adBean.ad = ad
                    adBean.saveTime = System.currentTimeMillis()
                    Constant.AdMap[adType] = adBean
                    Log.e(
                        LOG_TAG,
                        "广告加载成功 优先级： ${adBean.serpac_pri}   广告ID： ${adBean.serpac_id} 广告位：${adType}"
                    )
                    onLoadAdCompleteListener.onLoadAdComplete(adBean)
                }
            }
        )
    }

    private fun loadNativeAd(
        adType: String,
        adBean: AdBean,
        context: Context,
        onLoadAdCompleteListener: OnLoadAdCompleteListener
    ) {
        val builder = AdLoader.Builder(context, adBean.serpac_id)
        builder.forNativeAd { nativeAd ->
            // OnUnifiedNativeAdLoadedListener implementation.
            // If this callback occurs after the activity is destroyed, you must call
            // destroy and return or you may get a memory leak.
            Log.e(
                LOG_TAG,
                "广告加载成功  优先级： ${adBean.serpac_pri}   广告ID： ${adBean.serpac_id} 广告位：${adType}"
            )
            adBean.ad = nativeAd
            adBean.saveTime = System.currentTimeMillis()
            Constant.AdMap[adType] = adBean
            onLoadAdCompleteListener.onLoadAdComplete(adBean)

        }
        val location =
            if (adType == Constant.adNative_h) NativeAdOptions.ADCHOICES_TOP_RIGHT else NativeAdOptions.ADCHOICES_TOP_LEFT
        val adLoader =
            builder
                .withAdListener(
                    object : AdListener() {
                        override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                            adBean.ad = null
                            Log.e(
                                LOG_TAG,
                                "广告加载失败  优先级：${adBean.serpac_pri}   广告ID： ${adBean.serpac_id}  广告位：${adType}"
                            )
                            onLoadAdCompleteListener.onLoadAdComplete(adBean)
                        }

                        override fun onAdClicked() {
                            addClickTimeOrShowTime(0, 1)
                        }
                    }
                )
                .withNativeAdOptions(
                    NativeAdOptions.Builder()
                        .setAdChoicesPlacement(location).build()
                )
                .build()

        adLoader.loadAd(AdRequest.Builder().build())
    }

    fun showAd(
        activity: FragmentActivity,
        adType: String,
        adBean: AdBean,
        frameLayout: FrameLayout? = null,
        onShowAdCompleteListener: OnShowAdCompleteListener
    ) {
        if (isMax()) {
            onShowAdCompleteListener.isMax()
            return
        }
        when (adBean.serpac_type) {
            Constant.openAdType -> {
                showOpenAd(activity, adType, adBean, onShowAdCompleteListener)
            }
            Constant.interAdType -> {
                showInterAd(activity, adType, adBean, onShowAdCompleteListener)
            }
            Constant.nativeAdType -> {
                showNativeAd(activity, adType, adBean, frameLayout)
            }

        }
    }

    private fun showOpenAd(
        activity: FragmentActivity,
        adType: String,
        adBean: AdBean,
        onShowAdCompleteListener: OnShowAdCompleteListener
    ) {
        val appOpenAd = adBean.ad as AppOpenAd
        appOpenAd.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdClicked() {
                addClickTimeOrShowTime(0, 1)

            }

            /** Called when full screen content is dismissed. */
            override fun onAdDismissedFullScreenContent() {
                // Set the reference to null so isAdAvailable() returns false.
                Constant.AdMap[adType]?.ad = null
                Log.e(
                    LOG_TAG,
                    "广告取消成功  优先级：  ${adBean?.serpac_pri}   广告ID： ${adBean?.serpac_id} 广告位：${adType}"
                )
                onShowAdCompleteListener.onShowAdComplete()
            }

            /** Called when fullscreen content failed to show. */
            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Constant.AdMap[adType]?.ad = null
                Log.e(
                    LOG_TAG,
                    "广告展示失败  优先级： ${adBean?.serpac_pri}   广告ID： ${adBean?.serpac_id}  广告位：${adType}"
                )
                onShowAdCompleteListener.onShowAdComplete()
            }

            /** Called when fullscreen content is shown. */
            override fun onAdShowedFullScreenContent() {
                addClickTimeOrShowTime(1, 0)
                Constant.AdMap[adType]?.ad = null
                Log.e(
                    LOG_TAG,
                    "广告展示成功 开屏广告  优先级： ${adBean?.serpac_pri}   广告ID： ${adBean?.serpac_id}"
                )
            }
        }
        if (activity.lifecycle.currentState == Lifecycle.State.RESUMED) {
            appOpenAd.show(activity)
        }
    }

    private fun showInterAd(
        activity: FragmentActivity,
        adType: String,
        adBean: AdBean,
        onShowAdCompleteListener: OnShowAdCompleteListener
    ) {
        if (adBean.ad != null) {
            val interstitialAd = adBean.ad as InterstitialAd
            interstitialAd.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdClicked() {
                    addClickTimeOrShowTime(0, 1)

                }

                /** Called when full screen content is dismissed. */
                override fun onAdDismissedFullScreenContent() {
                    // Set the reference to null so isAdAvailable() returns false.
                    Constant.AdMap[adType]?.ad = null
                    Log.e(
                        LOG_TAG,
                        "广告消失 优先级：  ${adBean?.serpac_pri}   广告ID： ${adBean?.serpac_id} 广告位：${adType}"
                    )
                    onShowAdCompleteListener.onShowAdComplete()
                }

                /** Called when fullscreen content failed to show. */
                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Constant.AdMap[adType]?.ad = null
                    Log.e(
                        LOG_TAG,
                        "广告展示失败  ${adBean?.serpac_pri}   广告ID： ${adBean?.serpac_id}  广告位：${adBean}"
                    )
                    onShowAdCompleteListener.onShowAdComplete()
                }

                /** Called when fullscreen content is shown. */
                override fun onAdShowedFullScreenContent() {
                    addClickTimeOrShowTime(1, 0)
                    Constant.AdMap[adType]?.ad = null
                    Log.e(
                        LOG_TAG,
                        "广告展示成功 优先级： ${adBean?.serpac_pri}   广告ID： ${adBean?.serpac_id}  广告位：${adBean}"
                    )
                }
            }
            if (activity.lifecycle.currentState == Lifecycle.State.RESUMED) {
                interstitialAd.show(activity)
            }
        } else {
            onShowAdCompleteListener.onShowAdComplete()
        }
    }

    private fun showNativeAd(
        activity: FragmentActivity,
        adType: String,
        adBean: AdBean,
        frameLayout: FrameLayout? = null
    ) {
        val activityDestroyed = activity.isDestroyed
        val nativeAd = adBean.ad as NativeAd
        if (activityDestroyed || activity.isFinishing || activity.isChangingConfigurations) {
            nativeAd.destroy()
            return
        }


        val adViewResultBinding = AdViewResultBinding.inflate(activity.layoutInflater)
        val adViewHomeBinding = AdViewHomeBinding.inflate(activity.layoutInflater)
        populateNativeAdView(nativeAd, adViewResultBinding, adViewHomeBinding, adType)
        frameLayout?.removeAllViews()
        if (adType == Constant.adNative_h) {
            frameLayout?.addView(adViewHomeBinding.root)
        } else {
            frameLayout?.addView(adViewResultBinding.root)
        }
        Constant.AdMap[adType]?.ad = null
        AdManage().loadAd(adType, activity)
    }

    private fun populateNativeAdView(
        nativeAd: NativeAd,
        adViewResultBinding: AdViewResultBinding,
        adViewHomeBinding: AdViewHomeBinding,
        adType: String
    ) {
        Log.e(LOG_TAG, "show native ad ing")
        if (adType == Constant.adNative_r) {
            adViewResultBinding.run {
                val nativeAdView = adViewResultBinding.root
                nativeAdView.mediaView = adViewResultBinding.adMedia

                nativeAdView.headlineView = adViewResultBinding.adHeadline
                nativeAdView.bodyView = adViewResultBinding.adContent
                nativeAdView.callToActionView = adViewResultBinding.adCallToAction
                nativeAdView.iconView = adViewResultBinding.adAppIcon

                if (nativeAd.mediaContent?.mainImage != null) {
                    adViewResultBinding.adMedia.mediaContent = nativeAd.mediaContent
                } else {
                    Log.e(LOG_TAG, "nativeAd mediaContent mainImage ==null")
                }
                nativeAd.mediaContent?.let { adViewResultBinding.adMedia.mediaContent = it }
                if (nativeAd.callToAction == null) {
                    adViewResultBinding.adCallToAction.visibility = View.GONE
                } else {
                    adViewResultBinding.adCallToAction.visibility = View.VISIBLE
                }

                val adAppIcon = adViewResultBinding.adAppIcon

                val adContent = adViewResultBinding.adContent

                if (nativeAd.body == null) {
                    adContent.visibility = View.INVISIBLE
                } else {
                    adContent.visibility = View.VISIBLE
                    adContent.text = nativeAd.body
                }
                if (nativeAd.icon == null) {
                    Log.e(LOG_TAG, "nativeAd app icon ==null")
                    adAppIcon.visibility = View.GONE
                } else {
                    adAppIcon.setImageDrawable(nativeAd.icon?.drawable)
                    adAppIcon.visibility = View.VISIBLE
                }
                val adHeadline = adViewResultBinding.adHeadline
                adHeadline.text = nativeAd.headline

                // This method tells the Google Mobile Ads SDK that you have finished populating your
                // native ad view with this native ad.
                nativeAdView.setNativeAd(nativeAd)
                addClickTimeOrShowTime(1, 0)
                // Get the video controller for the ad. One will always be provided, even if the ad doesn't
                // have a video asset.
                val mediaContent = nativeAd.mediaContent
                val vc = mediaContent?.videoController

                // Updates the UI to say whether or not this ad has a video asset.
                if (vc != null && mediaContent.hasVideoContent()) {
                    // Create a new VideoLifecycleCallbacks object and pass it to the VideoController. The
                    // VideoController will call methods on this object when events occur in the video
                    // lifecycle.
                    vc.videoLifecycleCallbacks =
                        object : VideoController.VideoLifecycleCallbacks() {
                            override fun onVideoEnd() {
                                // Publishers should allow native ads to complete video playback before
                                // refreshing or replacing them with another ad in the same UI location.
//                        mainActivityBinding.refreshButton.isEnabled = true
//                        mainActivityBinding.videostatusText.text = "Video status: Video playback has ended."
                                super.onVideoEnd()
                            }
                        }
                } else {
//            mainActivityBinding.videostatusText.text = "Video status: Ad does not contain a video asset."
//            mainActivityBinding.refreshButton.isEnabled = true
                }
            }
        } else {
            adViewHomeBinding.run {
                val nativeAdView = adViewHomeBinding.root
                nativeAdView.mediaView = adViewHomeBinding.adMedia

                nativeAdView.headlineView = adViewHomeBinding.adHeadline
                nativeAdView.bodyView = adViewHomeBinding.adContent
                nativeAdView.callToActionView = adViewHomeBinding.adCallToAction
                nativeAdView.iconView = adViewHomeBinding.adAppIcon

                if (nativeAd.mediaContent?.mainImage != null) {
                    adViewHomeBinding.adMedia.mediaContent = nativeAd.mediaContent
                } else {
                    Log.e(LOG_TAG, "nativeAd mediaContent mainImage ==null")
                }
                nativeAd.mediaContent?.let { adViewHomeBinding.adMedia.mediaContent = it }
                if (nativeAd.callToAction == null) {
                    adViewHomeBinding.adCallToAction.visibility = View.GONE
                } else {
                    adViewHomeBinding.adCallToAction.visibility = View.VISIBLE
                }

                val adAppIcon = adViewHomeBinding.adAppIcon

                val adContent = adViewHomeBinding.adContent

                if (nativeAd.body == null) {
                    adContent.visibility = View.INVISIBLE
                } else {
                    adContent.visibility = View.VISIBLE
                    adContent.text = nativeAd.body
                }
                if (nativeAd.icon == null) {
                    Log.e(LOG_TAG, "nativeAd app icon ==null")
                    adAppIcon.visibility = View.GONE
                } else {
                    adAppIcon.setImageDrawable(nativeAd.icon?.drawable)
                    adAppIcon.visibility = View.VISIBLE
                }
                val adHeadline = adViewResultBinding.adHeadline
                adHeadline.text = nativeAd.headline

                // This method tells the Google Mobile Ads SDK that you have finished populating your
                // native ad view with this native ad.
                nativeAdView.setNativeAd(nativeAd)
                addClickTimeOrShowTime(1, 0)
                // Get the video controller for the ad. One will always be provided, even if the ad doesn't
                // have a video asset.
                val mediaContent = nativeAd.mediaContent
                val vc = mediaContent?.videoController

                // Updates the UI to say whether or not this ad has a video asset.
                if (vc != null && mediaContent.hasVideoContent()) {
                    // Create a new VideoLifecycleCallbacks object and pass it to the VideoController. The
                    // VideoController will call methods on this object when events occur in the video
                    // lifecycle.
                    vc.videoLifecycleCallbacks =
                        object : VideoController.VideoLifecycleCallbacks() {
                            override fun onVideoEnd() {
                                // Publishers should allow native ads to complete video playback before
                                // refreshing or replacing them with another ad in the same UI location.
//                        mainActivityBinding.refreshButton.isEnabled = true
//                        mainActivityBinding.videostatusText.text = "Video status: Video playback has ended."
                                super.onVideoEnd()
                            }
                        }
                } else {
//            mainActivityBinding.videostatusText.text = "Video status: Ad does not contain a video asset."
//            mainActivityBinding.refreshButton.isEnabled = true
                }
            }
        }


    }

    fun addClickTimeOrShowTime(showNum: Int, clickNum: Int) {
        val adTimeBeanJson = SPUtils.get().getString(Constant.adTimeBean, "")
        var adTimeBean: AdTimeBean? = null
        if (adTimeBeanJson != null && adTimeBeanJson.isNotEmpty()) {
            adTimeBean = Gson().fromJson(adTimeBeanJson, AdTimeBean::class.java)
        }
        if (adTimeBean == null) {
            adTimeBean = AdTimeBean()
        }
        if (DateUtil().getTime() == adTimeBean.timeLast) {
            adTimeBean.showTime = adTimeBean.showTime + showNum
            adTimeBean.clickTime = adTimeBean.clickTime + clickNum
        } else {
            adTimeBean.showTime = showNum
            adTimeBean.clickTime = clickNum
            adTimeBean.timeLast = DateUtil().getTime()
        }
        SPUtils.get().putString(Constant.adTimeBean, Gson().toJson(adTimeBean))
    }

    private fun isMax(): Boolean {
        val adTimeBeanJson = SPUtils.get().getString(Constant.adTimeBean, "")
        if (adTimeBeanJson != null && adTimeBeanJson.isNotEmpty()) {
            val adTimeBean = Gson().fromJson(adTimeBeanJson, AdTimeBean::class.java) ?: return false
            if (adTimeBean.timeLast != DateUtil().getTime()) return false
            val showTime: Int = adTimeBean.showTime
            val clickTime: Int = adTimeBean.clickTime
            val adResourceBeanJson = SPUtils.get().getString(Constant.adResourceBean, "")
            if (adResourceBeanJson != null && adResourceBeanJson.isNotEmpty()) {
                val adResourceBean =
                    Gson().fromJson(adResourceBeanJson, AdResourceBean::class.java) ?: return false
                if (showTime >= adResourceBean.serpac_sm) {
                    Log.e(
                        LOG_TAG,
                        "广告展示次数超出范围"
                    )
                    return true
                }
                if (clickTime >= adResourceBean.serpac_cm) {
                    Log.e(
                        LOG_TAG,
                        "广告点击次数超出范围"
                    )
                    return true
                }
            }

        }
        return false
    }

    interface OnLoadAdCompleteListener {
        fun onLoadAdComplete(ad: AdBean?)
        fun isMax()
    }

    interface OnShowAdCompleteListener {
        fun onShowAdComplete()
        fun isMax()
    }

    fun isOutTime(): Boolean {
        val adTimeBeanJson = SPUtils.get().getString(Constant.adTimeBean, "")
        var adTimeBean: AdTimeBean? = null
        if (adTimeBeanJson != null && adTimeBeanJson.isNotEmpty()) {
            adTimeBean = Gson().fromJson(adTimeBeanJson, AdTimeBean::class.java)
        }
        if (adTimeBean == null) {
            adTimeBean = AdTimeBean()
        }
        if (DateUtil().getTime() != adTimeBean.timeLast) {
            return true
        }
        return false
    }
}