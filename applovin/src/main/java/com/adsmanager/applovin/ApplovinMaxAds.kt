package com.adsmanager.applovin

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.util.Log
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.RelativeLayout
import com.applovin.mediation.*
import com.applovin.mediation.ads.MaxAdView
import com.applovin.mediation.ads.MaxInterstitialAd
import com.applovin.mediation.ads.MaxRewardedAd
import com.applovin.mediation.nativeAds.MaxNativeAdListener
import com.applovin.mediation.nativeAds.MaxNativeAdLoader
import com.applovin.mediation.nativeAds.MaxNativeAdView
import com.applovin.mediation.nativeAds.MaxNativeAdViewBinder
import com.applovin.sdk.AppLovinPrivacySettings
import com.applovin.sdk.AppLovinSdk
import com.applovin.sdk.AppLovinSdkConfiguration
import com.applovin.sdk.AppLovinSdkUtils
import java.util.concurrent.TimeUnit
import kotlin.math.pow
import com.adsmanager.core.*
import com.adsmanager.core.iadsmanager.*

private const val TAG = "ApplovinMaxAds"
class ApplovinMaxAds : IAds {

    override fun initialize(context: Context, appId: String?, iInitialize: IInitialize?) {
        AppLovinSdk.getInstance(context).mediationProvider = "max"
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
        val adView = when (sizeBanner) {
            SizeBanner.SMALL -> MaxAdView(adUnitId, activity)
            SizeBanner.MEDIUM -> MaxAdView(adUnitId, MaxAdFormat.MREC, activity)
        }

        adView.setListener(object : MaxAdViewAdListener {
            override fun onAdLoaded(ad: MaxAd?) {
                callbackAds?.onAdLoaded()
            }

            override fun onAdDisplayed(ad: MaxAd?) {

            }

            override fun onAdHidden(ad: MaxAd?) {

            }

            override fun onAdClicked(ad: MaxAd?) {

            }

            override fun onAdLoadFailed(adUnitId: String?, error: MaxError?) {
                callbackAds?.onAdFailedToLoad("adUnitId: $adUnitId, error: ${error?.message}")
            }

            override fun onAdDisplayFailed(ad: MaxAd?, error: MaxError?) {

            }

            override fun onAdExpanded(ad: MaxAd?) {

            }

            override fun onAdCollapsed(ad: MaxAd?) {

            }

        })

        when (sizeBanner) {
            SizeBanner.SMALL -> {
                val isTablet = AppLovinSdkUtils.isTablet(activity)
                val heightPx = AppLovinSdkUtils.dpToPx(activity, if (isTablet) 90 else 50)
                adView.layoutParams =
                    FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, heightPx)
                adView.setExtraParameter("adaptive_banner", "true")
            }
            else -> {
                // MREC width and height are 300 and 250 respectively, on phones and tablets
                val widthPx = AppLovinSdkUtils.dpToPx(activity, 300)
                val heightPx = AppLovinSdkUtils.dpToPx(activity, 250)
                adView.layoutParams = FrameLayout.LayoutParams(widthPx, heightPx)
            }
        }
        bannerView.removeAllViews()
        bannerView.addView(adView)
        // Load the ad
        adView.loadAd()
    }

    private lateinit var interstitialAd: MaxInterstitialAd
    private var retryAttempt = 0.0

    @Suppress("DEPRECATION")
    override fun loadInterstitial(activity: Activity, adUnitId: String) {
        interstitialAd = MaxInterstitialAd(adUnitId, activity)
        interstitialAd.setListener(object : MaxAdListener {
            // MAX Ad Listener
            override fun onAdLoaded(maxAd: MaxAd) {
                // Interstitial ad is ready to be shown. interstitialAd.isReady() will now return 'true'

                // Reset retry attempt
                retryAttempt = 0.0
                if(BuildConfig.DEBUG)
                    Log.e(TAG, "onAdLoaded")
            }

            override fun onAdLoadFailed(adUnitId: String?, error: MaxError?) {
                // Interstitial ad failed to load
                // AppLovin recommends that you retry with exponentially higher delays up to a maximum delay (in this case 64 seconds)
                if(BuildConfig.DEBUG)
                    Log.e(TAG, "adUnitId: $adUnitId, error: ${error?.message}")
                retryAttempt++
                val delayMillis =
                    TimeUnit.SECONDS.toMillis(2.0.pow(6.0.coerceAtMost(retryAttempt)).toLong())

                Handler().postDelayed({ interstitialAd.loadAd() }, delayMillis)
            }

            override fun onAdDisplayFailed(ad: MaxAd?, error: MaxError?) {
                // Interstitial ad failed to display. AppLovin recommends that you load the next ad.
                interstitialAd.loadAd()
            }

            override fun onAdDisplayed(maxAd: MaxAd) {}

            override fun onAdClicked(maxAd: MaxAd) {}

            override fun onAdHidden(maxAd: MaxAd) {
                // Interstitial ad is hidden. Pre-load the next ad
                interstitialAd.loadAd()
            }

        })

        // Load the first ad
        interstitialAd.loadAd()
    }

    override fun showInterstitial(activity: Activity, adUnitId: String, callbackAds: CallbackAds?) {
        if (interstitialAd.isReady) {
            callbackAds?.onAdLoaded()
            interstitialAd.showAd()
        } else {
            callbackAds?.onAdFailedToLoad("Interstitial not ready ")
            loadInterstitial(activity, adUnitId)
        }
    }

    private var nativeAdLoader: MaxNativeAdLoader? = null
    private var loadedNativeAd: MaxAd? = null

    override fun showNativeAds(
        activity: Activity,
        nativeView: RelativeLayout,
        sizeNative: SizeNative,
        adUnitId: String,
        callbackAds: CallbackAds?
    ) {
        val layoutNative: Int = when (sizeNative) {
            SizeNative.SMALL -> R.layout.max_small_native
            SizeNative.MEDIUM -> R.layout.max_big_native
        }
        val binder: MaxNativeAdViewBinder = MaxNativeAdViewBinder.Builder(layoutNative)
            .setTitleTextViewId(R.id.title_text_view)
            .setBodyTextViewId(R.id.body_text_view)
            .setAdvertiserTextViewId(R.id.advertiser_textView)
            .setIconImageViewId(R.id.icon_image_view)
            .setMediaContentViewGroupId(R.id.media_view_container)
            .setOptionsContentViewGroupId(R.id.ad_options_view)
            .setCallToActionButtonId(R.id.cta_button)
            .build()

        val maxNativeAdView = MaxNativeAdView(binder, activity)
        nativeAdLoader = MaxNativeAdLoader(adUnitId, activity)
        nativeAdLoader?.setNativeAdListener(object : MaxNativeAdListener() {
            override fun onNativeAdLoaded(nativeAdView: MaxNativeAdView?, nativeAd: MaxAd) {
                // Clean up any pre-existing native ad to prevent memory leaks.
                if (loadedNativeAd != null) {
                    nativeAdLoader?.destroy(loadedNativeAd)
                }

                // Save ad for cleanup.
                loadedNativeAd = nativeAd
                nativeView.removeAllViews()
                nativeView.addView(nativeAdView)
                callbackAds?.onAdLoaded()
            }

            override fun onNativeAdLoadFailed(adUnitId: String, error: MaxError) {
                // Native ad load failed.
                // AppLovin recommends retrying with exponentially higher delays up to a maximum delay.
                callbackAds?.onAdFailedToLoad("adUnitId: $adUnitId, error: ${error.message}")
            }

            override fun onNativeAdClicked(nativeAd: MaxAd) {}
        })
        nativeAdLoader?.loadAd(maxNativeAdView)
    }

    private lateinit var rewardedAd: MaxRewardedAd
    private var retryAttemptReward = 0.0

    override fun loadRewards(activity: Activity, adUnitId: String) {
        rewardedAd = MaxRewardedAd.getInstance(adUnitId, activity)
        rewardedAd.setListener(rewardListener(object : IRewards {
            override fun onUserEarnedReward(rewardsItem: RewardsItem?) {

            }
        }))
        rewardedAd.loadAd()
    }

    override fun showRewards(
        activity: Activity,
        adUnitId: String,
        callbackAds: CallbackAds?,
        iRewards: IRewards?
    ) {
        if (rewardedAd.isReady) {
            rewardedAd.setListener(rewardListener(object : IRewards {
                override fun onUserEarnedReward(rewardsItem: RewardsItem?) {
                    iRewards?.onUserEarnedReward(rewardsItem)
                }
            }))
            rewardedAd.showAd()
        } else {
            callbackAds?.onAdFailedToLoad("Rewards not ready")
            loadRewards(activity, adUnitId)
        }
    }

    @Suppress("DEPRECATION")
    private fun rewardListener(iRewards: IRewards?): MaxRewardedAdListener {
        return object : MaxRewardedAdListener {
            // MAX Ad Listener
            override fun onAdLoaded(maxAd: MaxAd) {
                // Rewarded ad is ready to be shown. rewardedAd.isReady() will now return 'true'

                // Reset retry attempt
                retryAttemptReward = 0.0
            }

            override fun onAdLoadFailed(adUnitId: String?, error: MaxError?) {
                // Rewarded ad failed to load
                // We recommend retrying with exponentially higher delays up to a maximum delay (in this case 64 seconds)

                retryAttemptReward++
                val delayMillis = TimeUnit.SECONDS.toMillis(
                    2.0.pow(
                        6.0.coerceAtMost(retryAttemptReward)
                    ).toLong()
                )

                Handler().postDelayed({ rewardedAd.loadAd() }, delayMillis)
            }

            override fun onAdDisplayFailed(ad: MaxAd?, error: MaxError?) {
                // Rewarded ad failed to display. We recommend loading the next ad
                rewardedAd.loadAd()
            }

            override fun onAdDisplayed(maxAd: MaxAd) {}

            override fun onAdClicked(maxAd: MaxAd) {}

            override fun onAdHidden(maxAd: MaxAd) {
                // rewarded ad is hidden. Pre-load the next ad
                rewardedAd.loadAd()
            }

            override fun onRewardedVideoStarted(maxAd: MaxAd) {}

            override fun onRewardedVideoCompleted(maxAd: MaxAd) {}

            override fun onUserRewarded(maxAd: MaxAd, maxReward: MaxReward) {
                // Rewarded ad was displayed and user should receive the reward
                iRewards?.onUserEarnedReward(RewardsItem(maxReward.amount, maxReward.label))
            }
        }
    }

}