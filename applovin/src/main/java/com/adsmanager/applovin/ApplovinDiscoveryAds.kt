package com.adsmanager.applovin

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.widget.RelativeLayout
import com.adsmanager.applovin.config.AppLovinCustomEventBanner
import com.adsmanager.applovin.config.AppLovinCustomEventInterstitial
import com.adsmanager.core.*
import com.adsmanager.core.iadsmanager.*
import com.applovin.adview.AppLovinAdView
import com.applovin.adview.AppLovinIncentivizedInterstitial
import com.applovin.adview.AppLovinInterstitialAd
import com.applovin.adview.AppLovinInterstitialAdDialog
import com.applovin.mediation.*
import com.applovin.sdk.*
import com.google.android.gms.ads.AdRequest
import java.util.concurrent.TimeUnit
import kotlin.math.pow


class ApplovinDiscoveryAds : IAds {

    override fun initialize(context: Context, appId: String?, iInitialize: IInitialize?) {
        AppLovinSdk.getInstance(context).initializeSdk {
            // AppLovin SDK is initialized, start loading ads
            iInitialize?.onInitializationComplete()
        }
    }

    override fun setTestDevices(activity: Activity, testDevices: List<String>) {
        AppLovinSdk.getInstance(activity).settings.testDeviceAdvertisingIds = testDevices
    }

    override fun loadGdpr(activity: Activity, childDirected: Boolean) {
        AppLovinSdk.getInstance(activity).initializeSdk { configuration: AppLovinSdkConfiguration ->
            when (configuration.consentDialogState) {
                AppLovinSdkConfiguration.ConsentDialogState.APPLIES -> {
                    // Show user consent dialog
                }
                AppLovinSdkConfiguration.ConsentDialogState.DOES_NOT_APPLY -> {
                    // No need to show consent dialog, proceed with initialization
                }
                else -> {
                    // Consent dialog state is unknown. Proceed with initialization, but check if the consent
                    // dialog should be shown on the next application initialization
                }
            }
        }
        AppLovinPrivacySettings.setHasUserConsent(true, activity)
        AppLovinPrivacySettings.setIsAgeRestrictedUser(childDirected, activity)
    }



    override fun showBanner(
        activity: Activity,
        bannerView: RelativeLayout,
        sizeBanner: SizeBanner,
        adUnitId: String,
        callbackAds: CallbackAds?
    ) {
        val builder: AdRequest.Builder = AdRequest.Builder()
        val bannerExtras = Bundle()
        bannerExtras.putString("zone_id", adUnitId)
        builder.addCustomEventExtrasBundle(AppLovinCustomEventBanner::class.java, bannerExtras)

        val isTablet2 = AppLovinSdkUtils.isTablet(activity)

        val adView = when (sizeBanner) {
            SizeBanner.SMALL -> {
                val adSize = if (isTablet2) AppLovinAdSize.LEADER else AppLovinAdSize.BANNER
                AppLovinAdView(adSize, activity)
            }
            SizeBanner.MEDIUM -> AppLovinAdView(AppLovinAdSize.MREC, activity)
        }
        adView.setAdLoadListener(object : AppLovinAdLoadListener {
            override fun adReceived(ad: AppLovinAd?) {
                callbackAds?.onAdLoaded()
            }

            override fun failedToReceiveAd(errorCode: Int) {
                callbackAds?.onAdFailedToLoad("applovin-d errorCode: $errorCode")
            }

        })

        bannerView.removeAllViews()
        bannerView.addView(adView)
        // Load the ad
        adView.loadNextAd()
    }

    private var interstitialAd: AppLovinInterstitialAdDialog? = null
    private var applovinAd: AppLovinAd? = null

    override fun loadInterstitial(activity: Activity, adUnitId: String) {
        val builder = AdRequest.Builder()
        val interstitialExtras = Bundle()
        interstitialExtras.putString("zone_id", adUnitId)
        builder.addCustomEventExtrasBundle(
            AppLovinCustomEventInterstitial::class.java,
            interstitialExtras
        )
        AppLovinSdk.getInstance(activity).adService.loadNextAd(
            AppLovinAdSize.INTERSTITIAL,
            object : AppLovinAdLoadListener {
                override fun adReceived(ad: AppLovinAd) {
                    applovinAd = ad
                }

                override fun failedToReceiveAd(errorCode: Int) {
                    // Look at AppLovinErrorCodes.java for list of error codes.
                    applovinAd = null
                }
            })
        AppLovinSdk.getInstance(activity).adService.loadNextAd(
            AppLovinAdSize.INTERSTITIAL,
            object : AppLovinAdLoadListener {
                override fun adReceived(ad: AppLovinAd) {

                }
                override fun failedToReceiveAd(errorCode: Int) {

                }
            })
        interstitialAd = AppLovinInterstitialAd.create(AppLovinSdk.getInstance(activity), activity)
    }

    override fun showInterstitial(activity: Activity, adUnitId: String, callbackAds: CallbackAds?) {
        if (interstitialAd != null) {
            val listener: AppLovinAdDisplayListener = object : AppLovinAdDisplayListener {
                override fun adDisplayed(ad: AppLovinAd?) {
                    callbackAds?.onAdLoaded()
                }

                override fun adHidden(ad: AppLovinAd?) {
                    callbackAds?.onAdFailedToLoad("applovin-d: $ad")
                }

            }
            callbackAds?.onAdLoaded()
            interstitialAd?.setAdDisplayListener(listener)
            interstitialAd?.showAndRender(applovinAd)
            loadInterstitial(activity, adUnitId)
        }
        else {
            callbackAds?.onAdFailedToLoad("Interstitial not ready ")
            loadInterstitial(activity, adUnitId)
        }
    }

    override fun showNativeAds(
        activity: Activity,
        nativeView: RelativeLayout,
        sizeNative: SizeNative,
        adUnitId: String,
        callbackAds: CallbackAds?
    ) {
        when (sizeNative) {
            SizeNative.SMALL -> showBanner(activity, nativeView, SizeBanner.SMALL, adUnitId, callbackAds)
            SizeNative.MEDIUM -> showBanner(activity, nativeView, SizeBanner.MEDIUM, adUnitId, callbackAds)
        }
    }

    private var incentivizedInterstitial: AppLovinIncentivizedInterstitial? = null

    override fun loadRewards(activity: Activity, adUnitId: String) {
        incentivizedInterstitial =
            AppLovinIncentivizedInterstitial.create(
                adUnitId,
                AppLovinSdk.getInstance(activity)
            )
        incentivizedInterstitial?.preload(object :
            AppLovinAdLoadListener {
            override fun adReceived(appLovinAd: AppLovinAd) {
                // A rewarded video was successfully received.
            }

            override fun failedToReceiveAd(errorCode: Int) {
                // A rewarded video failed to load.
            }
        })
    }

    override fun showRewards(
        activity: Activity,
        adUnitId: String,
        callbackAds: CallbackAds?,
        iRewards: IRewards?
    ) {
        if (incentivizedInterstitial != null) {
            incentivizedInterstitial?.show(activity, object : AppLovinAdRewardListener {
                override fun userRewardVerified(
                    ad: AppLovinAd?,
                    response: MutableMap<String, String>?
                ) {
                    iRewards?.onUserEarnedReward(null);
                }

                override fun userOverQuota(ad: AppLovinAd?, response: MutableMap<String, String>?) {

                }

                override fun userRewardRejected(
                    ad: AppLovinAd?,
                    response: MutableMap<String, String>?
                ) {

                }

                override fun validationRequestFailed(ad: AppLovinAd?, errorCode: Int) {

                }
            }, null, object : AppLovinAdDisplayListener {
                override fun adDisplayed(ad: AppLovinAd?) {

                }

                override fun adHidden(ad: AppLovinAd?) {
                    incentivizedInterstitial?.preload(null)
                }

            })
            callbackAds?.onAdLoaded()
            loadRewards(activity, adUnitId)
        } else {
            callbackAds?.onAdFailedToLoad("Rewards not ready")
            loadRewards(activity, adUnitId)
        }
    }

}