package com.wayfair.userlistanko.injection.module

import android.app.Application
import android.content.Context
import dagger.Binds
import dagger.Module

@Module
abstract class AppModule {
    @Binds
    internal abstract fun provideContext(application: Application): Context
}