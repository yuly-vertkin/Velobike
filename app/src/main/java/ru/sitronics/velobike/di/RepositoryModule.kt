package ru.sitronics.velobike.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.sitronics.velobike.data.repositories.auth.LoginRepositoryImp
import ru.sitronics.velobike.data.repositories.content.MapContentRepositoryImp
import ru.sitronics.velobike.domain.auth.LoginRepository
import ru.sitronics.velobike.domain.content.MapContentRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Singleton
    @Binds
    abstract fun bindLoginRepository(repositoryImpl: LoginRepositoryImp): LoginRepository

    @Singleton
    @Binds
    abstract fun bindMapContentRepository(repositoryImpl: MapContentRepositoryImp): MapContentRepository
}