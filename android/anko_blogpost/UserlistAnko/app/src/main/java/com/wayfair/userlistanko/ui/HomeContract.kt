package com.wayfair.userlistanko.ui

import androidx.lifecycle.MutableLiveData
import com.wayfair.userlistanko.data.UsersListDataModel

interface HomeContract {
    interface View {

    }

    interface HomeViewModel {
        fun getUsers(): MutableLiveData<UsersListDataModel>

        fun fetchNewUsers()
    }
}