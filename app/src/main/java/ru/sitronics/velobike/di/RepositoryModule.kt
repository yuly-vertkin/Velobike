package ru.sitronics.velobike.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.sitronics.velobike.data.repositories.auth.AuthRepositoryImp
import ru.sitronics.velobike.data.repositories.map.MapContentRepositoryImp
import ru.sitronics.velobike.data.repositories.profile.ProfileRepositoryImp
import ru.sitronics.velobike.data.repositories.rent.RentRepositoryImp
import ru.sitronics.velobike.domain.auth.AuthRepository
import ru.sitronics.velobike.domain.map.MapContentRepository
import ru.sitronics.velobike.domain.profile.ProfileRepository
import ru.sitronics.velobike.domain.rent.RentRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Singleton
    @Binds
    abstract fun bindAuthRepository(repositoryImpl: AuthRepositoryImp): AuthRepository

    @Singleton
    @Binds
    abstract fun bindMapContentRepository(repositoryImpl: MapContentRepositoryImp): MapContentRepository

    @Singleton
    @Binds
    abstract fun bindRentRepository(repositoryImpl: RentRepositoryImp): RentRepository

    @Singleton
    @Binds
    abstract fun bindProfileRepository(repositoryImpl: ProfileRepositoryImp): ProfileRepository
}