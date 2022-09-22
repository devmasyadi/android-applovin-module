package com.adsmanager.applovinmodule

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import com.adsmanager.applovin.ApplovinMaxAds
import com.adsmanager.core.CallbackAds
import com.adsmanager.core.IRewards
import com.adsmanager.core.RewardsItem
import com.adsmanager.core.iadsmanager.IInitialize
import com.adsmanager.core.iadsmanager.SizeBanner
import com.adsmanager.core.iadsmanager.SizeNative


private const val TAG = "TEST_APPLOVIN"

class MainActivity : AppCompatActivity() {

    private val bannerId = "6933903a50a9dc5a"
    private val interstitialId = "7263a762d1a5366b"
    private val nativeId = "1bd465c12980924d"
    private val rewardsId = "c11378688d2adfd1"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val applovinMaxAds = ApplovinMaxAds()
        applovinMaxAds.initialize(this, null, object : IInitialize {
            override fun onInitializationComplete() {
                applovinMaxAds.loadInterstitial(this@MainActivity, interstitialId)
                applovinMaxAds.loadRewards(this@MainActivity, rewardsId)
                applovinMaxAds.loadGdpr(this@MainActivity, true)
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

    }


}