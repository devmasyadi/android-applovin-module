package com.adsmanager.applovin

import android.app.Activity
import android.widget.RelativeLayout

interface IAds {

    fun initialize(
        activity: Activity,
        iInitialize: IInitialize,
        testDevices: List<String>? = listOf("")
    )

    fun loadGdpr(activity: Activity, childDirected: Boolean)

    fun showBanner(
        activity: Activity,
        bannerView: RelativeLayout,
        sizeBanner: SizeBanner,
        adUnitId: String,
        callbackAds: CallbackAds?
    )

    fun loadInterstitial(activity: Activity, adUnitId: String)
    fun showInterstitial(activity: Activity, adUnitId: String, callbackAds: CallbackAds?)
    fun showNativeAds(
        activity: Activity,
        nativeView: RelativeLayout,
        sizeNative: SizeNative,
        adUnitId: String,
        callbackAds: CallbackAds?
    )

    fun loadRewards(activity: Activity, adUnitId: String)
    fun showRewards(
        activity: Activity,
        adUnitId: String,
        callbackAds: CallbackAds?,
        iRewards: IRewards?
    )
}

interface IInitialize {
    fun onInitializationComplete()
}

data class RewardsItem(
    val amount: Int?,
    val type: String?,
)

interface IRewards {
    fun onUserEarnedReward(rewardsItem: RewardsItem?)
}

abstract class CallbackAds {
    open fun onAdLoaded() {}
    open fun onAdFailedToLoad(error: String? = "") {}
}

enum class SizeBanner {
    SMALL,
    MEDIUM
}

enum class SizeNative {
    SMALL,
    MEDIUM
}