package com.wayfair.userlistanko.injection.module

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.wayfair.networking.ApiService
import com.wayfair.userlistanko.BuildConfig
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

@Module
class NetModule {

    @Provides
    internal fun provideGson(): Gson {
        return GsonBuilder().create()
    }

    @Provides
    internal fun provideApiSevice(gson: Gson): ApiService {
        val interceptor: HttpLoggingInterceptor = HttpLoggingInterceptor()
        if (BuildConfig.DEBUG) {
            interceptor.level = HttpLoggingInterceptor.Level.BASIC
        }
        val client: OkHttpClient = OkHttpClient.Builder()
                .readTimeout(60 * 5, TimeUnit.SECONDS)
                .connectTimeout(60 * 5, TimeUnit.SECONDS)
                .addInterceptor(interceptor).build()
        return Retrofit.Builder()
                .baseUrl(BuildConfig.API_BASE_URL)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .callFactory(client)
                .build().create(ApiService::class.java)

    }

}