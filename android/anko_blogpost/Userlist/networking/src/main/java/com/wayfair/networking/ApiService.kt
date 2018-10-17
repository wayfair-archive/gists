package com.wayfair.networking

import com.wayfair.networking.networkmodel.UserNM
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("/users")
    fun getUsersList(@Query("since") sinceId: Int): Observable<MutableList<UserNM>>
}