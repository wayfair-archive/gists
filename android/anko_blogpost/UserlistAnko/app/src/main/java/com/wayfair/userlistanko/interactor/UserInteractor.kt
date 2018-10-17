package com.wayfair.userlistanko.interactor

import com.wayfair.userlistanko.data.UsersListDataModel
import com.wayfair.userlistanko.repository.UserRepositoryContract
import io.reactivex.Observable
import javax.inject.Inject

class UserInteractor @Inject constructor(var userRepository: UserRepositoryContract)
    : UserInteractorContract {
    override fun getUsersList(sinceId: Int): Observable<UsersListDataModel> {
        return userRepository.getUsers(sinceId)
    }

}