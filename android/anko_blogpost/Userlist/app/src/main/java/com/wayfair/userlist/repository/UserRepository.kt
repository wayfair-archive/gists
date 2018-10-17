package com.wayfair.userlist.repository

import com.wayfair.networking.ApiService
import com.wayfair.userlist.data.UsersListDataModel
import io.reactivex.Observable
import javax.inject.Inject

class UserRepository @Inject constructor(var apiService: ApiService) : UserRepositoryContract {
    override fun getUsers(sinceId: Int): Observable<UsersListDataModel> {
        return apiService.getUsersList(sinceId)
            .map { users ->
                UsersListDataModel(
                    users.asSequence().map { userNM ->
                        UsersListDataModel.UserDataModel(
                            userNM.login,
                            userNM.id,
                            userNM.node_id,
                            userNM.avatar_url,
                            userNM.url,
                            userNM.html_url,
                            userNM.type,
                            userNM.site_admin
                        )
                    }.toMutableList()
                )
            }
    }
}