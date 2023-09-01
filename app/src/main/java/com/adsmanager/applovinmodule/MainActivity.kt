package com.adsmanager.applovinmodule

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import com.adsmanager.applovin.ApplovinDiscoveryAds
import com.adsmanager.applovin.ApplovinMaxAds
import com.adsmanager.applovin.ApplovinOpenAds
import com.adsmanager.core.CallbackAds
import com.adsmanager.core.CallbackOpenAd
import com.adsmanager.core.SizeBanner
import com.adsmanager.core.SizeNative
import com.adsmanager.core.iadsmanager.IInitialize
import com.adsmanager.core.rewards.IRewards
import com.adsmanager.core.rewards.RewardsItem


private const val TAG = "TEST_APPLOVIN"

class MainActivity : AppCompatActivity() {

    private val bannerId = "6933903a50a9dc5a"
    private val interstitialId = "48acd44ceae68ec2"
    private val nativeId = "1bd465c12980924d"
    private val rewardsId = "c11378688d2adfd1"
    private val appOpenId = "fcd4981c18e62771"
    private val testDevices = listOf("4cf7dfd6-9019-462c-99a0-0d8087074a9a")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val applovinMaxAds = ApplovinDiscoveryAds()
        applovinMaxAds.initialize(this, null, object : IInitialize {
            override fun onInitializationComplete() {
                ApplovinOpenAds.getInstance(this@MainActivity).loadAd(this@MainActivity, appOpenId, object : CallbackAds() {
                    override fun onAdFailedToLoad(error: String?) {
                        super.onAdFailedToLoad(error)
                        Log.e("HALLO", "appOpen loadAd onAdFailedToLoad: $error")
                    }

                    override fun onAdLoaded() {
                        super.onAdLoaded()
                        Log.e("HALLO", "onAdLoaded loadAd appOpen")
                    }
                })
                applovinMaxAds.setTestDevices(this@MainActivity, testDevices)
                applovinMaxAds.loadInterstitial(this@MainActivity, interstitialId)
                applovinMaxAds.loadRewards(this@MainActivity, rewardsId)
                applovinMaxAds.loadGdpr(this@MainActivity, false)
            }
        })

        findViewById<Button>(R.id.btnShowBanner).setOnClickListener {
            val bannerView = findViewById<RelativeLayout>(R.id.bannerView)
            applovinMaxAds.showBanner(
                this,
                bannerView,
                SizeBanner.SMALL,
                bannerId,
                object : CallbackAds() {
                    override fun onAdFailedToLoad(error: String?) {
                        Log.e(TAG, error.toString())
                    }
                })
        }

        findViewById<Button>(R.id.btnShowInterstitial).setOnClickListener {
            applovinMaxAds.showInterstitial(this, interstitialId, object : CallbackAds() {
                override fun onAdFailedToLoad(error: String?) {
                    Log.e(TAG, error.toString())
                }
            })
        }

        findViewById<Button>(R.id.btnShowRewards).setOnClickListener {
            applovinMaxAds.showRewards(this, rewardsId, object : CallbackAds() {
                override fun onAdFailedToLoad(error: String?) {
                    Log.e(TAG, error.toString())
                }
            }, object : IRewards {
                override fun onUserEarnedReward(rewardsItem: RewardsItem?) {
                    Log.i(TAG, rewardsId)
                }
            })
        }

        findViewById<Button>(R.id.btnSmallNative).setOnClickListener {
            val nativeView = findViewById<RelativeLayout>(R.id.nativeView)
            applovinMaxAds.showNativeAds(
                this,
                nativeView,
                SizeNative.SMALL,
                nativeId,
                object : CallbackAds() {
                    override fun onAdFailedToLoad(error: String?) {
                        Log.e(TAG, error.toString())
                    }
                })
        }

        findViewById<Button>(R.id.btnSmallNativeRectangle).setOnClickListener {
            val nativeView = findViewById<RelativeLayout>(R.id.nativeView)
            applovinMaxAds.showNativeAds(
                this,
                nativeView,
                SizeNative.SMALL_RECTANGLE,
                nativeId,
                object : CallbackAds() {
                    override fun onAdFailedToLoad(error: String?) {
                        Log.e(TAG, error.toString())
                    }
                })
        }

        findViewById<Button>(R.id.btnShowMediumNative).setOnClickListener {
            val nativeView = findViewById<RelativeLayout>(R.id.nativeView)
            applovinMaxAds.showNativeAds(
                this,
                nativeView,
                SizeNative.MEDIUM,
                nativeId,
                object : CallbackAds() {
                    override fun onAdFailedToLoad(error: String?) {
                        Log.e(TAG, error.toString())
                    }
                })
        }

        findViewById<Button>(R.id.btnShowOpenApp).setOnClickListener {
            ApplovinOpenAds.getInstance(this@MainActivity).showAdIfAvailable(appOpenId, object : CallbackOpenAd() {
                override fun onAdFailedToLoad(error: String?) {
                    super.onAdFailedToLoad(error)
                    Log.e("HALLO", "appOpen showAdIfAvailable onAdFailedToLoad: $error")
                }

                override fun onAdLoaded() {
                    super.onAdLoaded()
                    Log.e("HALLO", "appOpen showAdIfAvailable onAdLoaded")
                }

                override fun onShowAdComplete() {
                    super.onShowAdComplete()
                    Log.e("HALLO", "appOpen showAdIfAvailable onShowAdComplete")
                }

            })
        }

    }


}