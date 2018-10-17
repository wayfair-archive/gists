package com.wayfair.userlist

import android.app.Activity
import android.app.Application
import com.wayfair.userlist.injection.DaggerAppComponent
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import javax.inject.Inject

class MainApp : Application(), HasActivityInjector {
    @Inject
    lateinit var activityDispatchingAndroidInjector: DispatchingAndroidInjector<Activity>

    override fun onCreate() {
        super.onCreate()
        DaggerAppComponent.builder().application(this).build().inject(this)

    }


    override fun activityInjector(): DispatchingAndroidInjector<Activity> {
        return activityDispatchingAndroidInjector
    }
}