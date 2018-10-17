package com.wayfair.userlist.injection

import android.app.Application
import com.wayfair.userlist.MainApp
import com.wayfair.userlist.injection.builder.ActivityBuilder
import com.wayfair.userlist.injection.module.AppModule
import com.wayfair.userlist.injection.module.NetModule
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjectionModule

@Component(modules = [
    AndroidInjectionModule::class,
    AppModule::class,
    NetModule::class,
    ActivityBuilder::class
])
interface AppComponent {

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(application: Application): Builder

        fun build(): AppComponent
    }

    fun inject(app: MainApp)
}