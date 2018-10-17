package com.wayfair.userlistanko.injection.builder

import com.wayfair.userlistanko.ui.HomeActivity
import com.wayfair.userlistanko.ui.HomeModule
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityBuilder {

    @ContributesAndroidInjector(modules = [(HomeModule::class)])
    internal abstract fun bindHomeActivity(): HomeActivity

}