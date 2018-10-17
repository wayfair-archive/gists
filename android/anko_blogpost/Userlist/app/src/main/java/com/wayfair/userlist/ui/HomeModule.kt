package com.wayfair.userlist.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.wayfair.userlist.interactor.UserInteractor
import com.wayfair.userlist.interactor.UserInteractorContract
import com.wayfair.userlist.repository.UserRepository
import com.wayfair.userlist.repository.UserRepositoryContract
import dagger.Binds
import dagger.Module

@Module
abstract class HomeModule {

    @Binds
    abstract fun provideUserInteractor(userInteractor: UserInteractor): UserInteractorContract

    @Binds
    abstract fun provideUserRepository(userRepository: UserRepository): UserRepositoryContract

    @Binds
    abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory

    @Binds
    abstract fun provideHomeViewModel(viewModel: HomeViewModel): ViewModel
}