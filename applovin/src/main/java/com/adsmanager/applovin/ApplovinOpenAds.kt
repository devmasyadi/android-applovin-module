package com.adsmanager.applovin

import android.content.Context
import com.adsmanager.core.CallbackAds
import com.adsmanager.core.CallbackOpenAd
import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxAdListener
import com.applovin.mediation.MaxError
import com.applovin.mediation.ads.MaxAppOpenAd
import com.applovin.sdk.AppLovinSdk


class ApplovinOpenAds(applicationContext: Context?) {
    private var appOpenAd: MaxAppOpenAd? = null
    private lateinit var context: Context
    private var callbackAds: CallbackAds? = null
    private var callbackOpenAd: CallbackOpenAd? = null

    init {
        if (applicationContext != null) {
            context = applicationContext
        }
    }

    companion object : SingletonHolder<ApplovinOpenAds, Context>(::ApplovinOpenAds)

    fun loadAd(context: Context, adUnitId: String, callbackAds: CallbackAds?) {
        appOpenAd = MaxAppOpenAd(adUnitId, context)
        this.callbackAds = callbackAds
        appOpenAd?.setListener(maxAdListener)
        appOpenAd?.loadAd()
    }

    fun showAdIfAvailable(
        adUnitId: String,
        callbackOpenAd: CallbackOpenAd?
    ) {
        this.callbackOpenAd = callbackOpenAd
        if (appOpenAd == null || !AppLovinSdk.getInstance(context).isInitialized) return

        if (appOpenAd?.isReady == true) {
            appOpenAd?.showAd(adUnitId)
        } else {
            callbackOpenAd?.onAdFailedToLoad("The app open ad is not ready yet.")
            appOpenAd?.loadAd()
        }
    }

    private val maxAdListener = object : MaxAdListener {
        override fun onAdLoaded(ad: MaxAd) {
            callbackAds?.onAdLoaded()
        }

        override fun onAdLoadFailed(adUnitId: String, error: MaxError) {
            callbackAds?.onAdFailedToLoad(error.message)
        }

        override fun onAdDisplayed(ad: MaxAd) {}
        override fun onAdClicked(ad: MaxAd) {}

        override fun onAdHidden(ad: MaxAd) {
            callbackOpenAd?.onShowAdComplete()
            appOpenAd?.loadAd()
        }

        override fun onAdDisplayFailed(ad: MaxAd, error: MaxError) {
            callbackAds?.onAdFailedToLoad(error.message)
            appOpenAd?.loadAd()
        }
    }


}