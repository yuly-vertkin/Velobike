package ru.sitronics.velobike.di

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import ru.sitronics.velobike.BuildConfig
import ru.sitronics.velobike.data.network.AuthInterceptor
import ru.sitronics.velobike.data.network.DebugOkHttpHelper
import ru.sitronics.velobike.data.network.AuthService
import ru.sitronics.velobike.data.network.MapContentService
import ru.sitronics.velobike.data.network.ProfileService
import ru.sitronics.velobike.data.network.RentService
import ru.sitronics.velobike.data.network.SecureInterceptor
import ru.sitronics.velobike.data.network.TestInterceptor
import ru.sitronics.velobike.domain.auth.AuthManager
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RetrofitModule {
    @Singleton
    @Provides
    fun provideRetrofit(gson: Gson, authManager: AuthManager): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(provideOkHttpClient(authManager))
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Singleton
    @Provides
    fun provideOkHttpClient(authManager: AuthManager): OkHttpClient {
        val clientBuilder = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(authManager))
            .addInterceptor(SecureInterceptor())

        if (BuildConfig.DEBUG) {
            clientBuilder.addInterceptor(TestInterceptor())
            clientBuilder.addInterceptor(DebugOkHttpHelper.getInterceptor())
        }
        return clientBuilder.build()
    }

    @Singleton
    @Provides
    fun provideGson(): Gson {
        return GsonBuilder().create()
    }

    @Singleton
    @Provides
    fun provideAuthService(retrofit: Retrofit): AuthService =
        retrofit.create(AuthService::class.java)

    @Singleton
    @Provides
    fun provideBikeService(retrofit: Retrofit): MapContentService =
        retrofit.create(MapContentService::class.java)

    @Singleton
    @Provides
    fun provideRentService(retrofit: Retrofit): RentService =
        retrofit.create(RentService::class.java)

    @Singleton
    @Provides
    fun provideProfileService(retrofit: Retrofit): ProfileService =
        retrofit.create(ProfileService::class.java)
}