package com.wayfair.userlist.ui

import androidx.lifecycle.MutableLiveData
import com.wayfair.userlist.data.UsersListDataModel

interface HomeContract {
    interface View {

    }

    interface HomeViewModel {
        fun getUsers(): MutableLiveData<UsersListDataModel>

        fun fetchNewUsers()
    }
}