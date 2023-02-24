package com.adsmanager.applovinmodule

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.adsmanager.applovin.ApplovinMaxAds
import com.adsmanager.applovin.ApplovinOpenAds
import com.adsmanager.core.iadsmanager.IInitialize

class MyApplication: Application(),  Application.ActivityLifecycleCallbacks, LifecycleObserver {



    override fun onCreate() {
        super.onCreate()

        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        ApplovinMaxAds().initialize(this, "", object : IInitialize {
            override fun onInitializationComplete() {

            }
        })
    }

    @Suppress("DEPRECATION")
    /** LifecycleObserver method that shows the app open ad when the app moves to foreground. */
    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onMoveToForeground() {
        // Show the ad (if available) when the app moves to foreground.
         ApplovinOpenAds.getInstance(this).showAdIfAvailable(
            "fcd4981c18e62771",
           null
        )
    }

    /** ActivityLifecycleCallback methods. */
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

    override fun onActivityStarted(activity: Activity) {

    }

    override fun onActivityResumed(activity: Activity) {}

    override fun onActivityPaused(activity: Activity) {}

    override fun onActivityStopped(activity: Activity) {}

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

    override fun onActivityDestroyed(activity: Activity) {}
}