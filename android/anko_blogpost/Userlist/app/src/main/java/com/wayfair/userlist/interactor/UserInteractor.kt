package com.wayfair.userlist.interactor

import com.wayfair.userlist.data.UsersListDataModel
import com.wayfair.userlist.repository.UserRepositoryContract
import io.reactivex.Observable
import javax.inject.Inject

class UserInteractor @Inject constructor(var userRepository: UserRepositoryContract)
    : UserInteractorContract {
    override fun getUsersList(sinceId: Int): Observable<UsersListDataModel> {
        return userRepository.getUsers(sinceId)
    }

}