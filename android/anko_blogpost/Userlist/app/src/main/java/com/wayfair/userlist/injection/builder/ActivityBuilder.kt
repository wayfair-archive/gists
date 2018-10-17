package com.wayfair.userlist.injection.builder

import com.wayfair.userlist.ui.HomeActivity
import com.wayfair.userlist.ui.HomeModule
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityBuilder {

    @ContributesAndroidInjector(modules = [(HomeModule::class)])
    internal abstract fun bindHomeActivity(): HomeActivity

}