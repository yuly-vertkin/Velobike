package ru.sitronics.velobike.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.sitronics.velobike.data.managers.AuthManagerImp
import ru.sitronics.velobike.domain.auth.AuthManager
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ManagerModule {
    @Singleton
    @Binds
    abstract fun bindAuthManager(managerImpl: AuthManagerImp): AuthManager
}