package com.wayfair.userlistanko.repository

import com.wayfair.userlistanko.data.UsersListDataModel
import io.reactivex.Observable

interface UserRepositoryContract {
    fun getUsers(sinceId: Int): Observable<UsersListDataModel>
}