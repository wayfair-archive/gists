package com.wayfair.userlistanko.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.wayfair.userlistanko.interactor.UserInteractor
import com.wayfair.userlistanko.interactor.UserInteractorContract
import com.wayfair.userlistanko.repository.UserRepository
import com.wayfair.userlistanko.repository.UserRepositoryContract
import dagger.Binds
import dagger.MapKey
import dagger.Module
import dagger.multibindings.IntoMap
import kotlin.reflect.KClass

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