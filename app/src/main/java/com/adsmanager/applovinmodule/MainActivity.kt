package com.adsmanager.applovinmodule

import android.os.Bundle
import android.widget.Button
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import com.adsmanager.applovin.*

class MainActivity : AppCompatActivity() {

    private lateinit var applovinAds: ApplovinAds
    private val bannerId = "6933903a50a9dc5a"
    private val interstitialId = "7263a762d1a5366b"
    private val nativeId = "1bd465c12980924d"
    private val rewardsId = "c11378688d2adfd1"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        applovinAds = ApplovinAds()
        applovinAds.initialize(activity = this, object : IInitialize {
            override fun onInitializationComplete() {
                applovinAds.loadInterstitial(this@MainActivity, interstitialId)
                applovinAds.loadRewards(this@MainActivity, rewardsId)
                applovinAds.loadGdpr(this@MainActivity, true)
            }
        })

        findViewById<Button>(R.id.btnShowBanner).setOnClickListener {
            val bannerView = findViewById<RelativeLayout>(R.id.bannerView)
            applovinAds.showBanner(
                this,
                bannerView,
                SizeBanner.MEDIUM,
                bannerId,
                object : CallbackAds() {
                    override fun onAdFailedToLoad(error: String?) {

                    }
                })
        }

        findViewById<Button>(R.id.btnShowInterstitial).setOnClickListener {
            applovinAds.showInterstitial(this, interstitialId, object : CallbackAds() {
                override fun onAdFailedToLoad(error: String?) {

                }
            })
        }

        findViewById<Button>(R.id.btnShowRewards).setOnClickListener {
            applovinAds.showRewards(this, rewardsId, object : CallbackAds() {
                override fun onAdFailedToLoad(error: String?) {

                }
            }, object : IRewards {
                override fun onUserEarnedReward(rewardsItem: RewardsItem?) {
                }
            })
        }

        findViewById<Button>(R.id.btnSmallNative).setOnClickListener {
            val nativeView = findViewById<RelativeLayout>(R.id.nativeView)
            applovinAds.showNativeAds(
                this,
                nativeView,
                SizeNative.SMALL,
                nativeId,
                object : CallbackAds() {
                    override fun onAdFailedToLoad(error: String?) {

                    }
                })
        }

        findViewById<Button>(R.id.btnShowMediumNative).setOnClickListener {
            val nativeView = findViewById<RelativeLayout>(R.id.nativeView)
            applovinAds.showNativeAds(
                this,
                nativeView,
                SizeNative.MEDIUM,
                nativeId,
                object : CallbackAds() {
                    override fun onAdFailedToLoad(error: String?) {

                    }
                })
        }

    }


}