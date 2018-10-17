package com.wayfair.userlist.repository

import com.wayfair.userlist.data.UsersListDataModel
import io.reactivex.Observable

interface UserRepositoryContract {
    fun getUsers(sinceId: Int): Observable<UsersListDataModel>
}