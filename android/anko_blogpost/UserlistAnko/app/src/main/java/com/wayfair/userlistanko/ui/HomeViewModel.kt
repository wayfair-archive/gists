package com.wayfair.userlistanko.ui

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.wayfair.userlistanko.data.UsersListDataModel
import com.wayfair.userlistanko.interactor.UserInteractorContract
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class HomeViewModel @Inject constructor(val userInteractor: UserInteractorContract) : ViewModel(), HomeContract.HomeViewModel {

    private val TAG = HomeViewModel::class.java.simpleName

    private lateinit var usersLiveData: MutableLiveData<UsersListDataModel>
    private var sinceID = 0
    private val subscribeOn = Schedulers.io()

    override fun getUsers(): MutableLiveData<UsersListDataModel> {
        if (!::usersLiveData.isInitialized) {
            usersLiveData = MutableLiveData()
        }
        return usersLiveData
    }

    @SuppressLint("CheckResult")
    override fun fetchNewUsers(){
        userInteractor.getUsersList(sinceID)
                .subscribeOn(subscribeOn)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ usersDataModel ->
                    sinceID = usersDataModel.usersList.last().id
                    usersLiveData.value = usersDataModel
                }, { error ->
                    Log.e(TAG, "/users", error)
                })
    }
}