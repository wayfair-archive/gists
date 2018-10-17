package com.wayfair.userlist.interactor

import com.wayfair.userlist.data.UsersListDataModel
import io.reactivex.Observable

interface UserInteractorContract {
    fun getUsersList(sinceId: Int): Observable<UsersListDataModel>
}