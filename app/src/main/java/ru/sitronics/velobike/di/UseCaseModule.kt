 package ru.sitronics.velobike.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.sitronics.velobike.domain.map.MapContentUseCase
import ru.sitronics.velobike.domain.map.MapContentUseCaseImp
import ru.sitronics.velobike.domain.profile.ProfileUseCase
import ru.sitronics.velobike.domain.profile.ProfileUseCaseImp
import ru.sitronics.velobike.domain.rent.RentUseCase
import ru.sitronics.velobike.domain.rent.RentUseCaseImp
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class UseCaseModule {
    @Singleton
    @Binds
    abstract fun bindMapContentUseCase(useCaseImpl: MapContentUseCaseImp): MapContentUseCase

    @Singleton
    @Binds
    abstract fun bindRentUseCase(useCaseImpl: RentUseCaseImp): RentUseCase

    @Singleton
    @Binds
    abstract fun bindProfileUseCase(useCaseImpl: ProfileUseCaseImp): ProfileUseCase
}