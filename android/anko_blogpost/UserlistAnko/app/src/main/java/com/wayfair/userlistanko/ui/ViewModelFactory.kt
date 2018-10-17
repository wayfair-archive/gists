package com.wayfair.userlistanko.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.wayfair.userlistanko.interactor.UserInteractorContract
import javax.inject.Inject


class ViewModelFactory @Inject constructor(private val userInteractor: UserInteractorContract) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {

            return HomeViewModel(userInteractor) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: " + modelClass.name)
    }
}