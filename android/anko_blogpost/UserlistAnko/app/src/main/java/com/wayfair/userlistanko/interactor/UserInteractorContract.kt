package com.wayfair.userlistanko.interactor

import com.wayfair.userlistanko.data.UsersListDataModel
import io.reactivex.Observable

interface UserInteractorContract {
    fun getUsersList(sinceId: Int): Observable<UsersListDataModel>
}