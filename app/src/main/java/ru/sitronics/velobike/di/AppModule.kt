package ru.sitronics.velobike.di

import android.content.Context
import ru.sitronics.velobike.data.AppContextProvider
import ru.sitronics.velobike.data.AppContextProviderImp
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Singleton
    @Provides
    fun provideAppContextProvider(@ApplicationContext appContext: Context): AppContextProvider {
        return AppContextProviderImp(appContext)
    }
}